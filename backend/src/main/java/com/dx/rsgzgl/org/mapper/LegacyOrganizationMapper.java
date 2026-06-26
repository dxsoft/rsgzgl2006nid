package com.dx.rsgzgl.org.mapper;

import com.dx.rsgzgl.org.dto.OrganizationRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface LegacyOrganizationMapper {

    @Select("""
            SELECT
                TRIM(dwbm) AS orgCode,
                TRIM(dwmc) AS orgName
            FROM dwbm
            WHERE dwbm IS NOT NULL
              AND TRIM(dwbm) <> ''
            ORDER BY LENGTH(TRIM(dwbm)), TRIM(dwbm)
            """)
    List<OrganizationRecord> findAll();
}
