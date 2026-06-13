package com.dx.rsgzgl.salary.service.impl;

import com.dx.rsgzgl.common.exception.BusinessException;
import com.dx.rsgzgl.salary.dto.SalaryFieldConfigAdminItem;
import com.dx.rsgzgl.salary.dto.SalaryFieldConfigAuditItem;
import com.dx.rsgzgl.salary.dto.SalaryFieldConfigIssue;
import com.dx.rsgzgl.salary.dto.SalaryFieldConfigItem;
import com.dx.rsgzgl.salary.dto.SalaryFieldConfigUpdateCommand;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Service
public class SalaryFieldConfigService {

    private static final Set<String> SUPPORTED_CATEGORIES = Set.of("00", "01", "10");

    private final JdbcTemplate jdbcTemplate;

    public SalaryFieldConfigService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    List<SalaryFieldConfig> loadConfigs() {
        return jdbcTemplate.query("""
                        SELECT
                            field_name,
                            field_cap,
                            field_caps,
                            category,
                            category6,
                            sfsy,
                            sfsy06,
                            sequence
                        FROM fldgz
                        WHERE field_type = 'N'
                          AND UPPER(TRIM(field_name)) <> 'HJ2'
                        ORDER BY sequence, field_name
                        """,
                (rs, rowNum) -> new SalaryFieldConfig(
                        normalizeFieldName(rs.getString("field_name")),
                        trim(rs.getString("field_cap")),
                        trim(rs.getString("field_caps")),
                        trim(rs.getString("category")),
                        trim(rs.getString("category6")),
                        trim(rs.getString("sfsy")),
                        trim(rs.getString("sfsy06")),
                        rs.getInt("sequence")
                )).stream().sorted(Comparator.comparingInt(SalaryFieldConfig::sequence)).toList();
    }

    public List<SalaryFieldConfigItem> effectiveConfigs(String category, int year, String dwsx) {
        String normalizedCategory = normalizeCategory(category);
        boolean useLegacy2006Config = year <= 2006;
        Map<String, Object> titleContext = Map.of("dwsx", trim(dwsx));
        return loadConfigs().stream()
                .filter(config -> config.appliesTo(normalizedCategory, useLegacy2006Config))
                .map(config -> new SalaryFieldConfigItem(
                        config.fieldName(),
                        SalaryTitlePolicy.title(config, titleContext),
                        config.effectiveCategory(useLegacy2006Config),
                        config.effectiveActiveFlag(useLegacy2006Config),
                        config.sequence()
                ))
                .toList();
    }

    public List<SalaryFieldConfigIssue> configIssues(int year) {
        boolean useLegacy2006Config = year <= 2006;
        List<SalaryFieldConfigIssue> issues = new ArrayList<>();
        for (SalaryFieldConfig config : loadConfigs()) {
            String effectiveCategory = config.effectiveCategory(useLegacy2006Config);
            if ("SDBT".equals(config.fieldName()) && !"01".equals(effectiveCategory)) {
                issues.add(new SalaryFieldConfigIssue(
                        config.fieldName(),
                        config.fieldCap(),
                        "WARN",
                        "\u5de5\u4f5c\u6027\u6d25\u8d34\u662f\u516c\u52a1\u5458\u4e13\u5c5e\u9879\u76ee\uff0ccategory/category6 \u5e94\u4e3a 01"
                ));
            }
            if ("DFBT2".equals(config.fieldName()) && !"00".equals(effectiveCategory)) {
                issues.add(new SalaryFieldConfigIssue(
                        config.fieldName(),
                        config.fieldCap(),
                        "WARN",
                        "\u751f\u6d3b\u6027\u8865\u8d34/\u57fa\u7840\u7ee9\u6548\u662f\u540c\u4e00\u9879\u7684\u4e0d\u540c\u6807\u9898\uff0ccategory/category6 \u5e94\u4fdd\u6301 00"
                ));
            }
        }
        return issues;
    }

    public SalaryFieldConfigAdminItem config(String itemCode) {
        return toAdminItem(findConfig(itemCode));
    }

    public List<SalaryFieldConfigAdminItem> allConfigs() {
        ensureAuditTable();
        Map<String, AuditSummary> auditSummaries = auditSummaries();
        return loadConfigs().stream()
                .map(config -> toAdminItem(config, auditSummaries.getOrDefault(config.fieldName(), AuditSummary.empty())))
                .toList();
    }

    public List<SalaryFieldConfigAuditItem> configAudit(String itemCode) {
        ensureAuditTable();
        String normalizedCode = normalizeFieldName(itemCode);
        return jdbcTemplate.query("""
                        SELECT id, item_code, field_name, old_value, new_value, changed_by, changed_at
                        FROM salary_field_config_audit
                        WHERE item_code = ?
                        ORDER BY changed_at DESC, id DESC
                        LIMIT 50
                        """,
                (rs, rowNum) -> new SalaryFieldConfigAuditItem(
                        rs.getLong("id"),
                        rs.getString("item_code"),
                        rs.getString("field_name"),
                        rs.getString("old_value"),
                        rs.getString("new_value"),
                        rs.getString("changed_by"),
                        rs.getTimestamp("changed_at").toLocalDateTime()
                ),
                normalizedCode
        );
    }

    public SalaryFieldConfigAdminItem updateConfig(String itemCode, SalaryFieldConfigUpdateCommand command) {
        if (command == null) {
            throw new BusinessException("INVALID_SALARY_FIELD_CONFIG", "Salary field config update body is required.");
        }
        SalaryFieldConfig existing = findConfig(itemCode);
        SalaryFieldConfig next = new SalaryFieldConfig(
                existing.fieldName(),
                patch(command.fieldCap(), existing.fieldCap()),
                patch(command.fieldCaps(), existing.fieldCaps()),
                normalizeConfigCategory(patch(command.category(), existing.category()), "category"),
                normalizeConfigCategory(patch(command.category6(), existing.category6()), "category6"),
                patch(command.activeFlag(), existing.sfsy()),
                patch(command.activeFlag2006(), existing.sfsy06()),
                command.sequence() == null ? existing.sequence() : validateSequence(command.sequence())
        );
        int updated = jdbcTemplate.update("""
                        UPDATE fldgz
                        SET field_cap = ?,
                            field_caps = ?,
                            category = ?,
                            category6 = ?,
                            sfsy = ?,
                            sfsy06 = ?,
                            sequence = ?
                        WHERE UPPER(TRIM(field_name)) = ?
                          AND field_type = 'N'
                        """,
                next.fieldCap(),
                next.fieldCaps(),
                next.category(),
                next.category6(),
                next.sfsy(),
                next.sfsy06(),
                next.sequence(),
                next.fieldName()
        );
        if (updated != 1) {
            throw new BusinessException("SALARY_FIELD_CONFIG_UPDATE_FAILED", "Salary field config update failed: " + itemCode);
        }
        writeAudit(existing, next);
        return config(next.fieldName());
    }

    private SalaryFieldConfig findConfig(String itemCode) {
        String normalizedCode = normalizeFieldName(itemCode);
        if (!StringUtils.hasText(normalizedCode)) {
            throw new BusinessException("INVALID_SALARY_FIELD", "Salary field code is required.");
        }
        return loadConfigs().stream()
                .filter(config -> normalizedCode.equals(config.fieldName()))
                .findFirst()
                .orElseThrow(() -> new BusinessException("SALARY_FIELD_CONFIG_NOT_FOUND", "Salary field config not found: " + itemCode));
    }

    private SalaryFieldConfigAdminItem toAdminItem(SalaryFieldConfig config) {
        return toAdminItem(config, auditSummary(config.fieldName()));
    }

    private SalaryFieldConfigAdminItem toAdminItem(SalaryFieldConfig config, AuditSummary auditSummary) {
        return new SalaryFieldConfigAdminItem(
                config.fieldName(),
                config.fieldCap(),
                config.fieldCaps(),
                config.category(),
                config.category6(),
                config.sfsy(),
                config.sfsy06(),
                config.sequence(),
                auditSummary.count(),
                auditSummary.lastChangedAt()
        );
    }

    private Map<String, AuditSummary> auditSummaries() {
        return jdbcTemplate.query("""
                        SELECT item_code, COUNT(*) AS audit_count, MAX(changed_at) AS last_changed_at
                        FROM salary_field_config_audit
                        GROUP BY item_code
                        """,
                (rs, rowNum) -> Map.entry(
                        rs.getString("item_code"),
                        new AuditSummary(
                                rs.getLong("audit_count"),
                                rs.getTimestamp("last_changed_at") == null ? null : rs.getTimestamp("last_changed_at").toLocalDateTime().toString()
                        )
                )
        ).stream().collect(java.util.stream.Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private AuditSummary auditSummary(String itemCode) {
        ensureAuditTable();
        return jdbcTemplate.queryForObject("""
                        SELECT COUNT(*) AS audit_count, MAX(changed_at) AS last_changed_at
                        FROM salary_field_config_audit
                        WHERE item_code = ?
                        """,
                (rs, rowNum) -> new AuditSummary(
                        rs.getLong("audit_count"),
                        rs.getTimestamp("last_changed_at") == null ? null : rs.getTimestamp("last_changed_at").toLocalDateTime().toString()
                ),
                itemCode
        );
    }

    private void writeAudit(SalaryFieldConfig existing, SalaryFieldConfig next) {
        ensureAuditTable();
        auditField(next.fieldName(), "field_cap", existing.fieldCap(), next.fieldCap());
        auditField(next.fieldName(), "field_caps", existing.fieldCaps(), next.fieldCaps());
        auditField(next.fieldName(), "category", existing.category(), next.category());
        auditField(next.fieldName(), "category6", existing.category6(), next.category6());
        auditField(next.fieldName(), "sfsy", existing.sfsy(), next.sfsy());
        auditField(next.fieldName(), "sfsy06", existing.sfsy06(), next.sfsy06());
        auditField(next.fieldName(), "sequence", String.valueOf(existing.sequence()), String.valueOf(next.sequence()));
    }

    private void auditField(String itemCode, String fieldName, String oldValue, String newValue) {
        if (oldValue.equals(newValue)) {
            return;
        }
        jdbcTemplate.update("""
                        INSERT INTO salary_field_config_audit(item_code, field_name, old_value, new_value, changed_by)
                        VALUES (?, ?, ?, ?, ?)
                        """,
                itemCode,
                fieldName,
                oldValue,
                newValue,
                currentUser()
        );
    }

    private void ensureAuditTable() {
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS salary_field_config_audit (
                    id BIGINT PRIMARY KEY AUTO_INCREMENT,
                    item_code VARCHAR(64) NOT NULL,
                    field_name VARCHAR(64) NOT NULL,
                    old_value VARCHAR(512) NULL,
                    new_value VARCHAR(512) NULL,
                    changed_by VARCHAR(64) NULL,
                    changed_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    KEY idx_salary_field_config_audit_item (item_code),
                    KEY idx_salary_field_config_audit_changed_at (changed_at)
                )
                """);
    }

    private String currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !StringUtils.hasText(authentication.getName())) {
            return "system";
        }
        return authentication.getName();
    }

    private String normalizeCategory(String category) {
        String normalized = trim(category);
        return StringUtils.hasText(normalized) ? normalized : "01";
    }

    private String normalizeConfigCategory(String category, String fieldName) {
        String normalized = trim(category);
        if (!SUPPORTED_CATEGORIES.contains(normalized)) {
            throw new BusinessException("INVALID_SALARY_FIELD_CATEGORY", fieldName + " must be one of 00, 01, 10.");
        }
        return normalized;
    }

    private String normalizeFieldName(String value) {
        return trim(value).toUpperCase(Locale.ROOT);
    }

    private int validateSequence(int sequence) {
        if (sequence < 0 || sequence > 9999) {
            throw new BusinessException("INVALID_SALARY_FIELD_SEQUENCE", "sequence must be between 0 and 9999.");
        }
        return sequence;
    }

    private String patch(String incoming, String existing) {
        return incoming == null ? existing : trim(incoming);
    }

    private static String trim(Object value) {
        return value == null ? "" : value.toString().trim();
    }

    private record AuditSummary(long count, String lastChangedAt) {
        static AuditSummary empty() {
            return new AuditSummary(0, null);
        }
    }
}
