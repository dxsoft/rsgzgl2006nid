package com.dx.rsgzgl.salary.service.impl;

import com.dx.rsgzgl.common.exception.BusinessException;
import com.dx.rsgzgl.salary.dto.SalaryCalculationDetail;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class SalaryDetailBuilder {

    private static final Set<String> DEDUCTION_FIELDS = Set.of("GRSDS", "ZFGJJ", "YLBXF", "YLF", "QTDK");
    private static final Set<String> CIVIL_POST_PREFIXES = Set.of("01", "02", "03", "05", "06");

    private final JdbcTemplate jdbcTemplate;
    private final SalaryFieldConfigService salaryFieldConfigService;

    public SalaryDetailBuilder(JdbcTemplate jdbcTemplate, SalaryFieldConfigService salaryFieldConfigService) {
        this.jdbcTemplate = jdbcTemplate;
        this.salaryFieldConfigService = salaryFieldConfigService;
    }

    public List<SalaryCalculationDetail> build(String historyId) {
        Map<String, Object> salaryRow = findSalaryRow(historyId);
        String category = salaryCategory(salaryRow);
        boolean useLegacy2006Config = parseInt(salaryRow.get("jsnf")) <= 2006;

        return salaryFieldConfigService.loadConfigs().stream()
                .filter(config -> config.appliesTo(category, useLegacy2006Config))
                .map(config -> toDetail(config, salaryRow))
                .filter(detail -> detail.amount().compareTo(BigDecimal.ZERO) != 0)
                .collect(Collectors.toList());
    }

    private Map<String, Object> findSalaryRow(String historyId) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
                SELECT
                    h.*,
                    p.ryfl AS person_ryfl,
                    p.gwfl AS person_gwfl,
                    p.dwsx AS person_dwsx,
                    o.dwbz AS org_dwbz
                FROM hisbase h
                LEFT JOIN dryjbxx p ON p.dwbm = h.dwbm AND p.grbm = h.grbm
                LEFT JOIN dwbm o ON o.dwbm = h.dwbm
                WHERE h.id = ?
                   OR h.id = LPAD(?, 36, ' ')
                """, historyId, historyId);
        if (rows.isEmpty()) {
            throw new BusinessException("SALARY_HISTORY_NOT_FOUND", "Salary history not found: " + historyId);
        }
        Map<String, Object> normalized = new LinkedHashMap<>();
        rows.get(0).forEach((key, value) -> {
            if (StringUtils.hasText(key)) {
                normalized.put(key.toLowerCase(Locale.ROOT), value);
            }
        });
        return normalized;
    }

    private SalaryCalculationDetail toDetail(SalaryFieldConfig config, Map<String, Object> salaryRow) {
        BigDecimal amount = numericValue(salaryRow.get(config.fieldName().toLowerCase(Locale.ROOT)));
        if (DEDUCTION_FIELDS.contains(config.fieldName())) {
            amount = amount.negate();
        }
        return new SalaryCalculationDetail(
                config.fieldName(),
                SalaryTitlePolicy.title(config, salaryRow),
                amount,
                "FLDGZ",
                ruleNote(config, salaryRow)
        );
    }

    private String ruleNote(SalaryFieldConfig config, Map<String, Object> salaryRow) {
        boolean specialTitle = SalaryTitlePolicy.usesSpecialTitle(salaryRow);
        return switch (config.fieldName()) {
            case "ZWGZSE2" -> ruleWithCodeAndGrade(
                    specialTitle ? "\u5c97\u4f4d\u4ee3\u7801" : "\u804c\u52a1\u4ee3\u7801",
                    salaryRow.get("zwbm2"),
                    salaryRow.get("zwgzdc2")
            );
            case "JBGZSE2" -> specialTitle
                    ? ruleWithCodeAndGrade("\u85aa\u7ea7", salaryRow.get("jbgzjb2"), salaryRow.get("djc2"))
                    : civilLevelSalaryRule(salaryRow);
            case "GWJT2" -> trim(salaryRow.get("gwjtlb"));
            default -> null;
        };
    }

    private String civilLevelSalaryRule(Map<String, Object> salaryRow) {
        String baseRule = ruleWithCodeAndGrade("\u7ea7\u522b", salaryRow.get("jbgzjb2"), salaryRow.get("djc2"));
        String suffix = "\uff0c\u7ea7\u522b\u5de5\u8d44\u7531\u7ea7\u522b\u548c\u6863\u6b21\u5171\u540c\u51b3\u5b9a";
        return StringUtils.hasText(baseRule) ? baseRule + suffix : suffix.substring(1);
    }

    private String salaryCategory(Map<String, Object> salaryRow) {
        String personCategory = trim(salaryRow.get("person_ryfl")) + " "
                + trim(salaryRow.get("ryfl")) + " "
                + trim(salaryRow.get("person_gwfl")) + " "
                + trim(salaryRow.get("gwfl"));
        if (containsAny(personCategory,
                "\u4e8b\u4e1a",
                "\u4e13\u4e1a\u6280\u672f",
                "\u7ba1\u7406\u5c97\u4f4d",
                "\u5de5\u52e4")) {
            return "10";
        }
        if (containsAny(personCategory,
                "\u516c\u52a1\u5458",
                "\u53c2\u7167",
                "\u8b66\u5458",
                "\u7efc\u5408\u7ba1\u7406",
                "\u6267\u6cd5\u52e4\u52a1")) {
            return "01";
        }

        String organizationType = trim(salaryRow.get("org_dwbz"));
        if (organizationType.contains("\u4e8b\u4e1a")) {
            return "10";
        }
        if (organizationType.contains("\u884c\u653f")) {
            return "01";
        }

        String zwbm2 = trim(salaryRow.get("zwbm2"));
        if (zwbm2.length() >= 2 && CIVIL_POST_PREFIXES.contains(zwbm2.substring(0, 2))) {
            return "01";
        }
        return "10";
    }

    private boolean containsAny(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private String ruleWithCodeAndGrade(String codeLabel, Object codeValue, Object gradeValue) {
        String code = trim(codeValue);
        String grade = trim(gradeValue);
        if (!StringUtils.hasText(code) && !StringUtils.hasText(grade)) {
            return null;
        }
        if (!StringUtils.hasText(grade)) {
            return codeLabel + "=" + code;
        }
        return codeLabel + "=" + code + ", \u6863\u6b21=" + grade;
    }

    private BigDecimal numericValue(Object value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        if (value instanceof BigDecimal decimal) {
            return decimal;
        }
        if (value instanceof Number number) {
            return BigDecimal.valueOf(number.doubleValue());
        }
        String text = value.toString().trim();
        return StringUtils.hasText(text) ? new BigDecimal(text) : BigDecimal.ZERO;
    }

    private int parseInt(Object value) {
        String text = trim(value);
        return StringUtils.hasText(text) ? Integer.parseInt(text) : 0;
    }

    private static String trim(Object value) {
        return value == null ? "" : value.toString().trim();
    }
}
