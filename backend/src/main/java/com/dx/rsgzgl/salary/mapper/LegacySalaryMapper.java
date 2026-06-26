package com.dx.rsgzgl.salary.mapper;

import com.dx.rsgzgl.salary.dto.SalaryHistoryItem;
import com.dx.rsgzgl.salary.dto.SalaryHistoryLinkItem;
import com.dx.rsgzgl.salary.dto.SalaryExpectedEventCandidate;
import com.dx.rsgzgl.salary.dto.SalaryPeriodItem;
import com.dx.rsgzgl.salary.dto.SalaryRecordSummary;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Optional;

@Mapper
public interface LegacySalaryMapper {

    @Select("""
            SELECT
                CAST(TRIM(jsnf) AS UNSIGNED) AS year,
                CAST(TRIM(jsyf) AS UNSIGNED) AS month,
                COUNT(1) AS recordCount
            FROM hisbase
            WHERE dwbm LIKE CONCAT(#{orgCode}, '%')
              AND TRIM(jsnf) <> ''
              AND TRIM(jsyf) <> ''
            GROUP BY CAST(TRIM(jsnf) AS UNSIGNED), CAST(TRIM(jsyf) AS UNSIGNED)
            ORDER BY year DESC, month DESC
            LIMIT #{limit}
            """)
    List<SalaryPeriodItem> findPeriodsByOrg(@Param("orgCode") String orgCode, @Param("limit") int limit);

    @Select("""
            SELECT
                TRIM(id) AS id,
                CONCAT(TRIM(dwbm), '-', TRIM(grbm)) AS personCode,
                CAST(TRIM(jsnf) AS UNSIGNED) AS year,
                CAST(TRIM(jsyf) AS UNSIGNED) AS month,
                TRIM(jslb) AS changeType,
                hj2 AS totalAmount
            FROM hisbase
            WHERE dwbm = #{orgCode}
              AND grbm = #{personNo}
            ORDER BY jsnf, jsyf, hj2, bbz
            """)
    List<SalaryHistoryItem> findHistory(@Param("orgCode") String orgCode, @Param("personNo") String personNo);

    @Select("""
            SELECT
                TRIM(id) AS id,
                TRIM(sid) AS nextId,
                CONCAT(TRIM(dwbm), '-', TRIM(grbm)) AS personCode,
                CAST(TRIM(jsnf) AS UNSIGNED) AS year,
                CAST(TRIM(jsyf) AS UNSIGNED) AS month,
                TRIM(jslb) AS changeType,
                hj2 AS totalAmount
            FROM hisbase
            WHERE dwbm = #{orgCode}
              AND grbm = #{personNo}
            """)
    List<SalaryHistoryLinkItem> findHistoryLinks(@Param("orgCode") String orgCode, @Param("personNo") String personNo);

    @Select("""
            SELECT *
            FROM (
                SELECT
                    'dryjbxx' AS source,
                    CAST(uid AS CHAR) AS sourceId,
                    CONCAT(TRIM(dwbm), '-', TRIM(grbm)) AS personCode,
                    2006 AS year,
                    7 AS month,
                    '2006套改' AS changeType,
                    '参加工作时间不晚于2006.07，按2006工资制度改革生成' AS note
                FROM dryjbxx
                WHERE dwbm = #{orgCode}
                  AND grbm = #{personNo}
                  AND TRIM(COALESCE(cjgzny, '')) <> ''
                  AND CAST(REPLACE(TRIM(cjgzny), '.', '') AS UNSIGNED) < 200607
                  AND NOT (
                      TRIM(COALESCE(zzny, '')) <> ''
                      AND CAST(REPLACE(TRIM(zzny), '.', '') AS UNSIGNED) = 200607
                  )

                UNION ALL

                SELECT
                    'dryzwbh' AS source,
                    CAST(id AS CHAR) AS sourceId,
                    CONCAT(TRIM(dwbm), '-', TRIM(grbm)) AS personCode,
                    CASE
                        WHEN LEFT(TRIM(zwbm), 2) = '03'
                             AND COALESCE(previousPrefix, '') <> '03' THEN '\u6cd5\u68c0\u5957\u6539'
                        WHEN LEFT(TRIM(zwbm), 2) IN ('23','24','25','26','27','28')
                             AND COALESCE(previousPrefix, '') IN ('23','24','25','26','27','28')
                             AND TRIM(COALESCE(xrzwbz, '')) = '1'
                            THEN YEAR(STR_TO_DATE(CONCAT(REPLACE(TRIM(srny), '.', ''), '01'), '%Y%m%d'))
                        ELSE YEAR(DATE_ADD(STR_TO_DATE(CONCAT(REPLACE(TRIM(srny), '.', ''), '01'), '%Y%m%d'), INTERVAL 1 MONTH))
                    END AS year,
                    CASE
                        WHEN LEFT(TRIM(zwbm), 2) IN ('23','24','25','26','27','28')
                             AND COALESCE(previousPrefix, '') IN ('23','24','25','26','27','28')
                             AND TRIM(COALESCE(xrzwbz, '')) = '1'
                            THEN MONTH(STR_TO_DATE(CONCAT(REPLACE(TRIM(srny), '.', ''), '01'), '%Y%m%d'))
                        ELSE MONTH(DATE_ADD(STR_TO_DATE(CONCAT(REPLACE(TRIM(srny), '.', ''), '01'), '%Y%m%d'), INTERVAL 1 MONTH))
                    END AS month,
                    CASE
                        WHEN LEFT(TRIM(zwbm), 2) IN ('23','24','25','26','27','28')
                             AND COALESCE(previousPrefix, '') NOT IN ('23','24','25','26','27','28') THEN '\u804c\u7ea7\u5957\u6539'
                        WHEN LEFT(TRIM(zwbm), 2) IN ('23','24','25','26','27','28')
                             AND COALESCE(previousPrefix, '') IN ('23','24','25','26','27','28')
                             AND TRIM(COALESCE(previousPostCode, '')) <> ''
                             AND TRIM(zwbm) <> TRIM(previousPostCode) THEN '\u804c\u7ea7\u664b\u5347'
                        WHEN LEFT(TRIM(zwbm), 2) IN ('21','22')
                             AND COALESCE(previousPrefix, '') NOT IN ('21','22') THEN '警员套改'
                        ELSE '职务变化'
                    END AS changeType,
                    CONCAT('任职信息 srny=', TRIM(srny), '，次月执行') AS note
                FROM (
                    SELECT z.*,
                           LAG(TRIM(zwbm)) OVER (PARTITION BY dwbm, grbm ORDER BY srny, id) AS previousPostCode,
                           LAG(LEFT(TRIM(zwbm), 2)) OVER (PARTITION BY dwbm, grbm ORDER BY srny, id) AS previousPrefix
                    FROM dryzwbh z
                    WHERE dwbm = #{orgCode}
                      AND grbm = #{personNo}
                ) posts
                WHERE posts.dwbm = #{orgCode}
                  AND posts.grbm = #{personNo}
                  AND previousPrefix IS NOT NULL
                  AND TRIM(COALESCE(zwbm, '')) <> ''
                  AND NOT (
                      LEFT(TRIM(zwbm), 2) IN ('23','24','25','26','27','28')
                      AND TRIM(COALESCE(xrzwbz, '')) = ''
                      AND EXISTS (
                          SELECT 1
                          FROM dryzwbh same_month
                          WHERE same_month.dwbm = posts.dwbm
                            AND same_month.grbm = posts.grbm
                            AND TRIM(COALESCE(same_month.srny, '')) = TRIM(COALESCE(posts.srny, ''))
                            AND same_month.id <> posts.id
                            AND TRIM(COALESCE(same_month.zwbm, '')) <> ''
                            AND LEFT(TRIM(same_month.zwbm), 2) NOT IN ('23','24','25','26','27','28')
                      )
                  )
                  AND TRIM(COALESCE(srny, '')) <> ''
                  AND CAST(REPLACE(TRIM(srny), '.', '') AS UNSIGNED) >= 200607
                  AND NOT EXISTS (
                      SELECT 1
                      FROM hjxx punishment
                      WHERE punishment.dwbm = posts.dwbm
                        AND punishment.grbm = posts.grbm
                        AND TRIM(COALESCE(punishment.hjsj, '')) = TRIM(COALESCE(posts.srny, ''))
                        AND (
                            HEX(CONVERT(CONCAT(
                                TRIM(COALESCE(punishment.hjmc, '')),
                                TRIM(COALESCE(punishment.jllx, '')),
                                TRIM(COALESCE(punishment.qtqk, ''))
                            ) USING utf8mb4)) LIKE '%E5A484E58886%'
                            OR HEX(CONVERT(CONCAT(
                                TRIM(COALESCE(punishment.hjmc, '')),
                                TRIM(COALESCE(punishment.jllx, '')),
                                TRIM(COALESCE(punishment.qtqk, ''))
                            ) USING utf8mb4)) LIKE '%E9998D%'
                        )
                  )

                UNION ALL

                SELECT
                    'jx' AS source,
                    CONCAT(CAST(id AS CHAR), ':rank') AS sourceId,
                    CONCAT(TRIM(dwbm), '-', TRIM(grbm)) AS personCode,
                    YEAR(DATE_ADD(STR_TO_DATE(CONCAT(REPLACE(TRIM(sysj), '.', ''), '01'), '%Y%m%d'), INTERVAL 1 MONTH)) AS year,
                    MONTH(DATE_ADD(STR_TO_DATE(CONCAT(REPLACE(TRIM(sysj), '.', ''), '01'), '%Y%m%d'), INTERVAL 1 MONTH)) AS month,
                    CASE
                        WHEN TRIM(jx) LIKE '%\u8b66%' THEN '\u8b66\u8854\u53d8\u5316'
                        WHEN TRIM(jx) LIKE '%\u6cd5%' THEN '\u6cd5\u5b98\u7b49\u7ea7'
                        WHEN TRIM(jx) LIKE '%\u68c0%' THEN '\u68c0\u5bdf\u7b49\u7ea7'
                        WHEN TRIM(jx) LIKE '%\u76d1%' THEN '\u76d1\u5bdf\u5b98\u7b49\u7ea7'
                        ELSE '\u8b66\u8854\u53d8\u5316'
                    END AS changeType,
                    CONCAT('\u8b66\u8854/\u6cd5\u68c0/\u76d1\u5bdf\u7b49\u7ea7\u4fe1\u606f sysj=', TRIM(sysj), '\uff0cjx=', TRIM(jx), '\uff0c\u6b21\u6708\u6267\u884c') AS note
                FROM (
                    SELECT r.*,
                           LAG(TRIM(jx)) OVER (PARTITION BY dwbm, grbm ORDER BY sysj, id) AS previousRank
                    FROM jx r
                    WHERE dwbm = #{orgCode}
                      AND grbm = #{personNo}
                ) rank_rows
                WHERE rank_rows.dwbm = #{orgCode}
                  AND rank_rows.grbm = #{personNo}
                  AND TRIM(COALESCE(sysj, '')) <> ''
                  AND CAST(REPLACE(TRIM(sysj), '.', '') AS UNSIGNED) >= 200607
                  AND TRIM(COALESCE(jx, '')) <> ''
                  AND (
                      TRIM(jx) LIKE '%\u8b66%'
                      OR TRIM(jx) LIKE '%\u6cd5%'
                      OR TRIM(jx) LIKE '%\u68c0%'
                      OR TRIM(jx) LIKE '%\u76d1%'
                  )
                  AND TRIM(COALESCE(previousRank, '')) <> ''
                  AND TRIM(jx) <> TRIM(previousRank)
                  AND EXISTS (
                      SELECT 1
                      FROM hisbase hb
                      WHERE hb.dwbm = rank_rows.dwbm
                        AND hb.grbm = rank_rows.grbm
                        AND TRIM(hb.jslb) IN (
                            '\u8b66\u8854\u53d8\u5316', '\u8b66\u8854\u6d25\u8d34',
                            '\u6cd5\u5b98\u7b49\u7ea7', '\u5ba1\u5224\u6d25\u8d34',
                            '\u68c0\u5bdf\u7b49\u7ea7', '\u68c0\u5bdf\u6d25\u8d34',
                            '\u76d1\u5bdf\u5b98\u7b49\u7ea7', '\u76d1\u5bdf\u7b49\u7ea7', '\u76d1\u5bdf\u6d25\u8d34'
                        )
                        AND ABS((CAST(TRIM(hb.jsnf) AS SIGNED) * 12 + CAST(TRIM(hb.jsyf) AS SIGNED)) -
                            (
                                YEAR(DATE_ADD(STR_TO_DATE(CONCAT(REPLACE(TRIM(rank_rows.sysj), '.', ''), '01'), '%Y%m%d'), INTERVAL 1 MONTH)) * 12
                                + MONTH(DATE_ADD(STR_TO_DATE(CONCAT(REPLACE(TRIM(rank_rows.sysj), '.', ''), '01'), '%Y%m%d'), INTERVAL 1 MONTH))
                            )) <= 1
                  )

                UNION ALL

                SELECT
                    'dxl' AS source,
                    CAST(id AS CHAR) AS sourceId,
                    CONCAT(TRIM(dwbm), '-', TRIM(grbm)) AS personCode,
                    YEAR(DATE_ADD(STR_TO_DATE(CONCAT(REPLACE(TRIM(bysj), '.', ''), '01'), '%Y%m%d'), INTERVAL 1 MONTH)) AS year,
                    MONTH(DATE_ADD(STR_TO_DATE(CONCAT(REPLACE(TRIM(bysj), '.', ''), '01'), '%Y%m%d'), INTERVAL 1 MONTH)) AS month,
                    '学历变化' AS changeType,
                    CONCAT('学历取得 bysj=', TRIM(bysj), '，学历类别=', TRIM(COALESCE(xllb, ''))) AS note
                FROM dxl
                WHERE dwbm = #{orgCode}
                  AND grbm = #{personNo}
                  AND TRIM(COALESCE(bysj, '')) <> ''
                  AND CAST(REPLACE(TRIM(bysj), '.', '') AS UNSIGNED) > 200607
                  AND TRIM(COALESCE(xllb, '')) NOT IN ('其他', '其它')

                  AND EXISTS (
                      SELECT 1
                      FROM hisbase hb
                      WHERE hb.dwbm = dxl.dwbm
                        AND hb.grbm = dxl.grbm
                        AND (CAST(TRIM(hb.jsnf) AS UNSIGNED) * 100 + CAST(TRIM(hb.jsyf) AS UNSIGNED))
                            < (
                                YEAR(DATE_ADD(STR_TO_DATE(CONCAT(REPLACE(TRIM(dxl.bysj), '.', ''), '01'), '%Y%m%d'), INTERVAL 1 MONTH)) * 100
                                + MONTH(DATE_ADD(STR_TO_DATE(CONCAT(REPLACE(TRIM(dxl.bysj), '.', ''), '01'), '%Y%m%d'), INTERVAL 1 MONTH))
                            )
                        AND TRIM(COALESCE(hb.zwbm2, '')) <> ''
                        AND TRIM(hb.zwbm2) NOT LIKE '%F%'
                        AND COALESCE(hb.hj2, 0) > 0
                      LIMIT 1
                  )

                UNION ALL

                SELECT
                    'hjxx' AS source,
                    CAST(id AS CHAR) AS sourceId,
                    CONCAT(TRIM(dwbm), '-', TRIM(grbm)) AS personCode,
                    YEAR(DATE_ADD(STR_TO_DATE(CONCAT(REPLACE(TRIM(hjsj), '.', ''), '01'), '%Y%m%d'), INTERVAL 1 MONTH)) AS year,
                    MONTH(DATE_ADD(STR_TO_DATE(CONCAT(REPLACE(TRIM(hjsj), '.', ''), '01'), '%Y%m%d'), INTERVAL 1 MONTH)) AS month,
                    CASE
                        WHEN HEX(CONVERT(CONCAT(
                            TRIM(COALESCE(hjmc, '')),
                            TRIM(COALESCE(jllx, '')),
                            TRIM(COALESCE(qtqk, ''))
                        ) USING utf8mb4)) LIKE '%E9998D%'
                          OR HEX(CONVERT(CONCAT(
                            TRIM(COALESCE(hjmc, '')),
                            TRIM(COALESCE(jllx, '')),
                            TRIM(COALESCE(qtqk, ''))
                        ) USING utf8mb4)) LIKE '%E5A484E58886%'
                            THEN '\u964d\u8d44\u5904\u5206'
                        ELSE '\u5956\u52b1\u664b\u5347'
                    END AS changeType,
                    CONCAT('奖惩信息 hjsj=', TRIM(hjsj), '，', TRIM(COALESCE(jllx, hjmc, ''))) AS note
                FROM hjxx
                WHERE dwbm = #{orgCode}
                  AND grbm = #{personNo}
                  AND TRIM(COALESCE(hjsj, '')) <> ''
                  AND CAST(REPLACE(TRIM(hjsj), '.', '') AS UNSIGNED) >= 200607
                  AND (
                      HEX(CONVERT(CONCAT(
                          TRIM(COALESCE(hjmc, '')),
                          TRIM(COALESCE(jllx, '')),
                          TRIM(COALESCE(qtqk, ''))
                      ) USING utf8mb4)) LIKE '%E9998D%'
                      OR HEX(CONVERT(CONCAT(
                          TRIM(COALESCE(hjmc, '')),
                          TRIM(COALESCE(jllx, '')),
                          TRIM(COALESCE(qtqk, ''))
                      ) USING utf8mb4)) LIKE '%E5A484E58886%'
                      OR HEX(CONVERT(CONCAT(
                          TRIM(COALESCE(hjmc, '')),
                          TRIM(COALESCE(jllx, '')),
                          TRIM(COALESCE(qtqk, ''))
                      ) USING utf8mb4)) LIKE '%E5A596E58AB1E6998BE58D87%'
                  )

                UNION ALL

                SELECT
                    'dndkh' AS source,
                    CAST(khnd AS CHAR) AS sourceId,
                    CONCAT(TRIM(k.dwbm), '-', TRIM(k.grbm)) AS personCode,
                    CAST(TRIM(k.khnd) AS UNSIGNED) + 1 AS year,
                    1 AS month,
                    '正常档次' AS changeType,
                    CONCAT('年度考核 ', TRIM(k.khnd), '=', TRIM(k.khjg), '，次年1月正常调整') AS note
                FROM dndkh k
                JOIN dryjbxx p ON p.dwbm = k.dwbm AND p.grbm = k.grbm
                WHERE k.dwbm = #{orgCode}
                  AND k.grbm = #{personNo}
                  AND TRIM(k.khnd) REGEXP '^[0-9]{4}$'
                  AND CAST(TRIM(k.khnd) AS UNSIGNED) >= 2006
                  AND TRIM(k.khjg) IN ('优秀', '称职', '合格')
                  AND (
                      LEFT(TRIM(COALESCE(p.zjbm, p.dwsx, '')), 2) IN ('07','08','09','10','11')
                      OR MOD(CAST(TRIM(k.khnd) AS UNSIGNED) - 2006 + 1, 2) = 0
                  )
            ) expected
            WHERE year >= 2006
              AND month BETWEEN 1 AND 12
            ORDER BY year, month,
                CASE changeType
                    WHEN '\u804c\u7ea7\u5957\u6539' THEN 16
                    WHEN '\u804c\u7ea7\u664b\u5347' THEN 17
                    WHEN '\u6cd5\u68c0\u5957\u6539' THEN 18
                    WHEN '2006套改' THEN 0
                    WHEN '职务变化' THEN 10
                    WHEN '警员套改' THEN 15
                    WHEN '学历变化' THEN 20
                    WHEN '正常级别' THEN 30
                    WHEN '正常档次' THEN 40
                    ELSE 99
                END,
                source,
                sourceId
            """)
    List<SalaryExpectedEventCandidate> findExpectedEventsFromBaseInfo(
            @Param("orgCode") String orgCode,
            @Param("personNo") String personNo
    );

    @Select("""
            SELECT
                TRIM(id) AS id,
                CONCAT(TRIM(dwbm), '-', TRIM(grbm)) AS personCode,
                CAST(TRIM(jsnf) AS UNSIGNED) AS year,
                CAST(TRIM(jsyf) AS UNSIGNED) AS month,
                TRIM(jslb) AS changeType,
                hj2 AS totalAmount
            FROM hisbase
            WHERE dwbm = #{orgCode}
              AND grbm = #{personNo}
              AND (CAST(TRIM(jsnf) AS UNSIGNED) * 100 + CAST(TRIM(jsyf) AS UNSIGNED)) <= #{yearMonth}
            ORDER BY jsnf DESC, jsyf DESC, hj2 DESC, bbz DESC
            LIMIT 1
            """)
    Optional<SalaryHistoryItem> findBaselineAtOrBefore(
            @Param("orgCode") String orgCode,
            @Param("personNo") String personNo,
            @Param("yearMonth") int yearMonth
    );

    @Select("""
            SELECT
                TRIM(id) AS id,
                CONCAT(TRIM(dwbm), '-', TRIM(grbm)) AS personCode,
                CAST(TRIM(jsnf) AS UNSIGNED) AS year,
                CAST(TRIM(jsyf) AS UNSIGNED) AS month,
                TRIM(jslb) AS changeType,
                hj2 AS totalAmount
            FROM hisbase
            WHERE dwbm = #{orgCode}
              AND grbm = #{personNo}
              AND (CAST(TRIM(jsnf) AS UNSIGNED) * 100 + CAST(TRIM(jsyf) AS UNSIGNED)) < #{yearMonth}
            ORDER BY jsnf DESC, jsyf DESC, hj2 DESC, bbz DESC
            LIMIT 1
            """)
    Optional<SalaryHistoryItem> findBaselineBefore(
            @Param("orgCode") String orgCode,
            @Param("personNo") String personNo,
            @Param("yearMonth") int yearMonth
    );

    @Select("""
            SELECT
                TRIM(id) AS id,
                CONCAT(TRIM(dwbm), '-', TRIM(grbm)) AS personCode,
                CAST(TRIM(jsnf) AS UNSIGNED) AS year,
                CAST(TRIM(jsyf) AS UNSIGNED) AS month,
                TRIM(jslb) AS changeType,
                hj2 AS totalAmount
            FROM hisbase
            WHERE dwbm = #{orgCode}
              AND grbm = #{personNo}
              AND CAST(TRIM(jsnf) AS UNSIGNED) = #{year}
              AND CAST(TRIM(jsyf) AS UNSIGNED) = #{month}
            ORDER BY hj2 DESC, bbz DESC
            LIMIT 1
            """)
    Optional<SalaryHistoryItem> findRecordAtYearMonth(
            @Param("orgCode") String orgCode,
            @Param("personNo") String personNo,
            @Param("year") int year,
            @Param("month") int month
    );

    @Select("""
            SELECT
                TRIM(id) AS id,
                CONCAT(TRIM(dwbm), '-', TRIM(grbm)) AS personCode,
                CAST(TRIM(jsnf) AS UNSIGNED) AS year,
                CAST(TRIM(jsyf) AS UNSIGNED) AS month,
                TRIM(jslb) AS changeType,
                hj2 AS totalAmount
            FROM hisbase
            WHERE dwbm = #{orgCode}
              AND grbm = #{personNo}
              AND CAST(TRIM(jsnf) AS UNSIGNED) = #{year}
              AND CAST(TRIM(jsyf) AS UNSIGNED) = #{month}
              AND TRIM(jslb) = #{changeType}
            ORDER BY hj2 DESC, bbz DESC
            LIMIT 1
            """)
    Optional<SalaryHistoryItem> findRecordAtYearMonthByChangeType(
            @Param("orgCode") String orgCode,
            @Param("personNo") String personNo,
            @Param("year") int year,
            @Param("month") int month,
            @Param("changeType") String changeType
    );

    @Select("""
            SELECT
                CONCAT(TRIM(dwbm), '-', TRIM(grbm)) AS personCode,
                CAST(TRIM(jsnf) AS UNSIGNED) AS year,
                CAST(TRIM(jsyf) AS UNSIGNED) AS month,
                hj2 AS totalAmount
            FROM hisbase
            WHERE id = #{historyId}
               OR id = LPAD(#{historyId}, 36, ' ')
            LIMIT 1
            """)
    Optional<SalaryRecordSummary> findSummaryById(@Param("historyId") String historyId);

    @Select("""
            SELECT
                TRIM(id) AS id,
                CONCAT(TRIM(dwbm), '-', TRIM(grbm)) AS personCode,
                CAST(TRIM(jsnf) AS UNSIGNED) AS year,
                CAST(TRIM(jsyf) AS UNSIGNED) AS month,
                TRIM(jslb) AS changeType,
                hj2 AS totalAmount
            FROM hisbase
            WHERE id = #{historyId}
               OR id = LPAD(#{historyId}, 36, ' ')
            LIMIT 1
            """)
    Optional<SalaryHistoryItem> findHistoryItemById(@Param("historyId") String historyId);
}
