package com.dx.rsgzgl.person.service;

import com.dx.rsgzgl.person.dto.PersonCodeOptionNode;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class PersonCodeOptionService {

    private static final int MAX_FIELD_NAME_LENGTH = 16;

    private final JdbcTemplate jdbcTemplate;

    public PersonCodeOptionService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<PersonCodeOptionNode> tree(String fieldName) {
        String safeFieldName = normalizeFieldName(fieldName);
        if (!StringUtils.hasText(safeFieldName)) {
            return List.of();
        }
        String category = optionCategory(safeFieldName);
        if (!StringUtils.hasText(category)) {
            return List.of();
        }
        List<OptionRow> rows = optionRows(category);
        if (rows.isEmpty()) {
            return List.of();
        }
        return buildTree(category, rows);
    }

    private String optionCategory(String fieldName) {
        List<String> categories = jdbcTemplate.queryForList("""
                SELECT TRIM(dmlb)
                FROM fldjbxx
                WHERE UPPER(TRIM(field_name)) = UPPER(?)
                  AND TRIM(dmlb) <> ''
                ORDER BY sequence
                LIMIT 1
                """, String.class, fieldName);
        if (!categories.isEmpty()) {
            return categories.getFirst();
        }
        categories = jdbcTemplate.queryForList("""
                SELECT TRIM(dmlb)
                FROM fldprop
                WHERE UPPER(TRIM(field_name)) = UPPER(?)
                  AND TRIM(dmlb) <> ''
                LIMIT 1
                """, String.class, fieldName);
        return categories.isEmpty() ? "" : categories.getFirst();
    }

    private List<OptionRow> optionRows(String category) {
        String normalizedCategory = trim(category);
        if (normalizedCategory.isBlank()) {
            return List.of();
        }
        return jdbcTemplate.query("""
                SELECT DISTINCT TRIM(bm) AS raw_code,
                       TRIM(mc) AS name,
                       sfsy AS enabled
                FROM dmb
                WHERE LEFT(TRIM(bm), CHAR_LENGTH(?)) = ?
                  AND CHAR_LENGTH(TRIM(bm)) >= CHAR_LENGTH(?)
                  AND sfsy <> 0
                ORDER BY TRIM(bm)
                """, (rs, rowNum) -> new OptionRow(
                trim(rs.getString("raw_code")),
                trim(rs.getString("name")),
                rs.getInt("enabled") != 0
        ), normalizedCategory, normalizedCategory, normalizedCategory);
    }

    private List<PersonCodeOptionNode> buildTree(String category, List<OptionRow> rows) {
        Map<String, MutableNode> nodes = new LinkedHashMap<>();
        String rootCode = trim(category);
        nodes.put(rootCode, new MutableNode(rootCode, "", "", "", false));
        for (OptionRow row : rows) {
            if (!row.enabled() || row.rawCode().isBlank()) {
                continue;
            }
            String rawCode = row.rawCode();
            if (rawCode.equals(rootCode)) {
                nodes.get(rootCode).name = row.name();
                continue;
            }
            String businessCode = rawCode.startsWith(rootCode) && rawCode.length() > rootCode.length()
                    ? rawCode.substring(rootCode.length())
                    : rawCode;
            MutableNode node = nodes.computeIfAbsent(rawCode, key -> new MutableNode(rawCode, businessCode, rawCode, row.name(), true));
            node.name = row.name();
            node.code = businessCode;
            node.selectable = rawCode.length() > rootCode.length();
            attachToParent(rootCode, rawCode, nodes, node);
        }
        return nodes.get(rootCode).children.stream()
                .sorted(Comparator.comparing(MutableNode::rawCode))
                .map(MutableNode::toNode)
                .toList();
    }

    private void attachToParent(String rootCode, String rawCode, Map<String, MutableNode> nodes, MutableNode node) {
        String parentCode = parentCode(rootCode, rawCode, nodes);
        MutableNode parent = nodes.computeIfAbsent(parentCode, key -> new MutableNode(parentCode, "", parentCode, "", false));
        if (!parent.children.contains(node)) {
            parent.children.add(node);
        }
    }

    private String parentCode(String rootCode, String rawCode, Map<String, MutableNode> nodes) {
        if (rawCode.equals(rootCode) || rawCode.length() <= rootCode.length()) {
            return rootCode;
        }
        for (int length = rawCode.length() - 1; length >= rootCode.length(); length--) {
            String candidate = rawCode.substring(0, length);
            if (nodes.containsKey(candidate)) {
                return candidate;
            }
        }
        return rootCode;
    }

    private String normalizeFieldName(String fieldName) {
        String text = trim(fieldName);
        if (text.length() > MAX_FIELD_NAME_LENGTH) {
            text = text.substring(0, MAX_FIELD_NAME_LENGTH);
        }
        return text;
    }

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }

    private record OptionRow(String rawCode, String name, boolean enabled) {
    }

    private static final class MutableNode {
        private final String rawCode;
        private String code;
        private final String fallbackName;
        private String name;
        private boolean selectable;
        private final List<MutableNode> children = new ArrayList<>();

        private MutableNode(String rawCode, String code, String fallbackName, String name, boolean selectable) {
            this.rawCode = rawCode;
            this.code = code;
            this.fallbackName = fallbackName;
            this.name = name;
            this.selectable = selectable;
        }

        private String rawCode() {
            return rawCode;
        }

        private PersonCodeOptionNode toNode() {
            return new PersonCodeOptionNode(
                    code,
                    rawCode,
                    StringUtils.hasText(name) ? name : fallbackName,
                    selectable,
                    children.stream()
                            .sorted(Comparator.comparing(MutableNode::rawCode))
                            .map(MutableNode::toNode)
                            .toList()
            );
        }
    }
}
