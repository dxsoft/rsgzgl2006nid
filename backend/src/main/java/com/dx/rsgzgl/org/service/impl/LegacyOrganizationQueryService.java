package com.dx.rsgzgl.org.service.impl;

import com.dx.rsgzgl.org.dto.OrganizationNode;
import com.dx.rsgzgl.org.dto.OrganizationRecord;
import com.dx.rsgzgl.org.mapper.LegacyOrganizationMapper;
import com.dx.rsgzgl.org.service.OrganizationQueryService;
import com.dx.rsgzgl.system.service.OrganizationAccessService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class LegacyOrganizationQueryService implements OrganizationQueryService {

    private final LegacyOrganizationMapper legacyOrganizationMapper;
    private final OrganizationAccessService organizationAccessService;

    public LegacyOrganizationQueryService(
            LegacyOrganizationMapper legacyOrganizationMapper,
            OrganizationAccessService organizationAccessService
    ) {
        this.legacyOrganizationMapper = legacyOrganizationMapper;
        this.organizationAccessService = organizationAccessService;
    }

    @Override
    public List<OrganizationNode> tree() {
        Map<String, MutableOrganizationNode> nodeMap = new LinkedHashMap<>();
        for (OrganizationRecord record : legacyOrganizationMapper.findAll()) {
            nodeMap.put(record.orgCode(), new MutableOrganizationNode(record.orgCode(), record.orgName()));
        }

        List<MutableOrganizationNode> roots = new ArrayList<>();
        for (MutableOrganizationNode node : nodeMap.values()) {
            MutableOrganizationNode parent = findParent(node.orgCode, nodeMap);
            if (parent == null) {
                roots.add(node);
            } else {
                parent.children.add(node);
            }
        }

        if (organizationAccessService.hasFullAccess()) {
            return roots.stream().map(MutableOrganizationNode::toNode).toList();
        }
        List<String> allowedOrgCodes = organizationAccessService.allowedOrgCodes();
        return roots.stream()
                .map(root -> root.toAccessibleNode(allowedOrgCodes))
                .filter(node -> node != null)
                .toList();
    }

    private MutableOrganizationNode findParent(String orgCode, Map<String, MutableOrganizationNode> nodeMap) {
        for (int length = orgCode.length() - 1; length > 0; length--) {
            MutableOrganizationNode parent = nodeMap.get(orgCode.substring(0, length));
            if (parent != null) {
                return parent;
            }
        }
        return null;
    }

    private static final class MutableOrganizationNode {
        private final String orgCode;
        private final String orgName;
        private final List<MutableOrganizationNode> children = new ArrayList<>();

        private MutableOrganizationNode(String orgCode, String orgName) {
            this.orgCode = orgCode;
            this.orgName = orgName;
        }

        private OrganizationNode toNode() {
            return new OrganizationNode(orgCode, orgName, children.stream().map(MutableOrganizationNode::toNode).toList());
        }

        private OrganizationNode toAccessibleNode(List<String> allowedOrgCodes) {
            boolean selfOrDescendantAllowed = allowedOrgCodes.stream()
                    .anyMatch(code -> code.startsWith(orgCode) || orgCode.startsWith(code));
            if (!selfOrDescendantAllowed) {
                return null;
            }
            List<OrganizationNode> accessibleChildren = children.stream()
                    .map(child -> child.toAccessibleNode(allowedOrgCodes))
                    .filter(node -> node != null)
                    .toList();
            return new OrganizationNode(orgCode, orgName, accessibleChildren);
        }
    }
}
