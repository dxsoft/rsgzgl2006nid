package com.dx.rsgzgl.salary.service.impl;

import com.dx.rsgzgl.common.exception.BusinessException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
class PersonCodeParser {

    PersonCodeParts parse(String personCode) {
        return parse(personCode, null);
    }

    PersonCodeParts parse(String personCode, String fallbackOrgCode) {
        if (!StringUtils.hasText(personCode)) {
            throw new BusinessException("INVALID_PERSON_CODE", "Person code is required.");
        }

        String normalized = personCode.trim();
        int separator = normalized.indexOf('-');
        if (separator > 0 && separator < normalized.length() - 1) {
            return new PersonCodeParts(normalized.substring(0, separator), normalized.substring(separator + 1));
        }

        if (normalized.length() > 5) {
            return new PersonCodeParts(normalized.substring(0, normalized.length() - 5), normalized.substring(normalized.length() - 5));
        }

        if (StringUtils.hasText(fallbackOrgCode)) {
            return new PersonCodeParts(fallbackOrgCode.trim(), normalized);
        }

        throw new BusinessException("INVALID_PERSON_CODE", "Use orgCode-personNo, for example 001-00055.");
    }
}
