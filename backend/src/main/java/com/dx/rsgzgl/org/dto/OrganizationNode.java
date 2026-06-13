package com.dx.rsgzgl.org.dto;

import java.util.List;

public record OrganizationNode(String orgCode, String orgName, List<OrganizationNode> children) {
}
