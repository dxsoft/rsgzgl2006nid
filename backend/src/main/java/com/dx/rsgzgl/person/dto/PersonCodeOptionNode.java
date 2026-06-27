package com.dx.rsgzgl.person.dto;

import java.util.List;

public record PersonCodeOptionNode(
        String code,
        String rawCode,
        String name,
        boolean selectable,
        List<PersonCodeOptionNode> children
) {
}
