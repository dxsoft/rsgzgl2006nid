package com.dx.rsgzgl.person.mapper;

import com.dx.rsgzgl.person.dto.PersonSummary;
import com.dx.rsgzgl.person.dto.PersonDetail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Optional;

@Mapper
public interface LegacyPersonMapper {

    @Select("""
            <script>
            SELECT
                CONCAT(TRIM(p.dwbm), '-', TRIM(p.grbm)) AS personCode,
                TRIM(p.xm) AS personName,
                TRIM(p.dwbm) AS orgCode,
                TRIM(COALESCE(o.dwmc, '')) AS orgName
            FROM dryjbxx p
            LEFT JOIN dwbm o ON o.dwbm = p.dwbm
            <where>
                <if test="orgCode != null and orgCode != ''">
                    AND p.dwbm LIKE CONCAT(#{orgCode}, '%')
                </if>
                <if test="fullAccess == false">
                    <choose>
                        <when test="allowedOrgCodes != null and allowedOrgCodes.size() > 0">
                            AND (
                            <foreach collection="allowedOrgCodes" item="allowedOrgCode" separator=" OR ">
                                p.dwbm LIKE CONCAT(#{allowedOrgCode}, '%')
                            </foreach>
                            )
                        </when>
                        <otherwise>
                            AND 1 = 0
                        </otherwise>
                    </choose>
                </if>
                <if test="keyword != null and keyword != ''">
                    AND (
                        p.xm LIKE CONCAT('%', #{keyword}, '%')
                        OR CONCAT(TRIM(p.dwbm), '-', TRIM(p.grbm)) LIKE CONCAT('%', #{keyword}, '%')
                        OR p.grbm LIKE CONCAT('%', #{keyword}, '%')
                        OR p.dwbm LIKE CONCAT('%', #{keyword}, '%')
                        OR o.dwmc LIKE CONCAT('%', #{keyword}, '%')
                    )
                </if>
            </where>
            ORDER BY p.dwbm, p.grbm
            LIMIT #{size} OFFSET #{offset}
            </script>
            """)
    List<PersonSummary> findPage(
            @Param("keyword") String keyword,
            @Param("orgCode") String orgCode,
            @Param("fullAccess") boolean fullAccess,
            @Param("allowedOrgCodes") List<String> allowedOrgCodes,
            @Param("offset") long offset,
            @Param("size") long size
    );

    @Select("""
            <script>
            SELECT COUNT(1)
            FROM dryjbxx p
            LEFT JOIN dwbm o ON o.dwbm = p.dwbm
            <where>
                <if test="orgCode != null and orgCode != ''">
                    AND p.dwbm LIKE CONCAT(#{orgCode}, '%')
                </if>
                <if test="fullAccess == false">
                    <choose>
                        <when test="allowedOrgCodes != null and allowedOrgCodes.size() > 0">
                            AND (
                            <foreach collection="allowedOrgCodes" item="allowedOrgCode" separator=" OR ">
                                p.dwbm LIKE CONCAT(#{allowedOrgCode}, '%')
                            </foreach>
                            )
                        </when>
                        <otherwise>
                            AND 1 = 0
                        </otherwise>
                    </choose>
                </if>
                <if test="keyword != null and keyword != ''">
                    AND (
                        p.xm LIKE CONCAT('%', #{keyword}, '%')
                        OR CONCAT(TRIM(p.dwbm), '-', TRIM(p.grbm)) LIKE CONCAT('%', #{keyword}, '%')
                        OR p.grbm LIKE CONCAT('%', #{keyword}, '%')
                        OR p.dwbm LIKE CONCAT('%', #{keyword}, '%')
                        OR o.dwmc LIKE CONCAT('%', #{keyword}, '%')
                    )
                </if>
            </where>
            </script>
            """)
    long count(
            @Param("keyword") String keyword,
            @Param("orgCode") String orgCode,
            @Param("fullAccess") boolean fullAccess,
            @Param("allowedOrgCodes") List<String> allowedOrgCodes
    );

    @Select("""
            SELECT
                CONCAT(TRIM(p.dwbm), '-', TRIM(p.grbm)) AS personCode,
                TRIM(p.xm) AS personName,
                TRIM(p.dwbm) AS orgCode,
                TRIM(COALESCE(o.dwmc, '')) AS orgName,
                TRIM(p.sfzh) AS idCard,
                TRIM(p.xb) AS gender,
                TRIM(p.csny) AS birthDate,
                TRIM(p.ryfl) AS personCategory,
                TRIM(p.dwsx) AS organizationType,
                TRIM(p.gwfl) AS postCategory,
                TRIM(p.cjgzny) AS workStartDate,
                TRIM(p.jrny) AS joinOrgDate,
                TRIM(p.xrzw) AS currentPost,
                TRIM(p.zwjb) AS postLevel,
                TRIM(p.srny) AS postStartDate,
                p.gznx AS workYears,
                TRIM(p.zgxl) AS education,
                TRIM(p.zzmm) AS politicalStatus,
                TRIM(p.mz) AS nation,
                TRIM(p.gryhzh) AS bankAccount
            FROM dryjbxx p
            LEFT JOIN dwbm o ON o.dwbm = p.dwbm
            WHERE CONCAT(TRIM(p.dwbm), '-', TRIM(p.grbm)) = #{personCode}
               OR TRIM(p.grbm) = #{personCode}
            ORDER BY p.dwbm, p.grbm
            LIMIT 1
            """)
    Optional<PersonDetail> findByPersonCode(@Param("personCode") String personCode);
}
