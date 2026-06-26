package com.dx.rsgzgl.salary.service.impl;

import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.Set;

final class SalaryTitlePolicy {

    private static final Set<String> SPECIAL_POST_TYPES = Set.of("07", "08", "09", "10");

    private SalaryTitlePolicy() {
    }

    static String title(SalaryFieldConfig config, Map<String, Object> salaryRow) {
        if (usesSpecialTitle(salaryRow) && StringUtils.hasText(config.fieldCaps())) {
            return config.fieldCaps();
        }
        if (StringUtils.hasText(config.fieldCap())) {
            return config.fieldCap();
        }
        if (StringUtils.hasText(config.fieldCaps())) {
            return config.fieldCaps();
        }
        return config.fieldName();
    }

    static boolean usesSpecialTitle(Map<String, Object> salaryRow) {
        return SPECIAL_POST_TYPES.contains(firstText(salaryRow, "dwsx", "person_dwsx"));
    }

    private static String firstText(Map<String, Object> row, String... keys) {
        for (String key : keys) {
            String value = trim(row.get(key));
            if (StringUtils.hasText(value)) {
                return value;
            }
        }
        return "";
    }

    private static String trim(Object value) {
        return value == null ? "" : value.toString().trim();
    }
}
