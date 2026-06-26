package com.dx.rsgzgl.salary.service.impl;

import org.springframework.util.StringUtils;

record SalaryFieldConfig(
        String fieldName,
        String fieldCap,
        String fieldCaps,
        String category,
        String category6,
        String sfsy,
        String sfsy06,
        int sequence
) {
    boolean appliesTo(String salaryCategory, boolean useLegacy2006Config) {
        String itemCategory = effectiveCategory(useLegacy2006Config);
        return isActive(effectiveActiveFlag(useLegacy2006Config))
                && ("00".equals(itemCategory) || salaryCategory.equals(itemCategory));
    }

    String effectiveCategory(boolean useLegacy2006Config) {
        return useLegacy2006Config && StringUtils.hasText(category6) ? category6 : category;
    }

    String effectiveActiveFlag(boolean useLegacy2006Config) {
        return useLegacy2006Config ? sfsy06 : sfsy;
    }

    private boolean isActive(String flag) {
        String normalized = trim(flag);
        return StringUtils.hasText(normalized)
                && !"0".equals(normalized)
                && !"N".equalsIgnoreCase(normalized)
                && !"NO".equalsIgnoreCase(normalized)
                && !"FALSE".equalsIgnoreCase(normalized)
                && !"\u5426".equals(normalized);
    }

    private static String trim(Object value) {
        return value == null ? "" : value.toString().trim();
    }
}
