package com.dx.rsgzgl.system.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class UserPermissionService {

    private final JdbcTemplate jdbcTemplate;

    public UserPermissionService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public boolean hasMenu(String username, String menuCode) {
        Integer count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM sys_user u
                JOIN sys_user_role ur ON ur.username = u.username
                JOIN sys_role r ON r.code = ur.role_code
                JOIN sys_role_menu rm ON rm.role_code = r.code
                JOIN sys_menu m ON m.code = rm.menu_code
                WHERE u.username = ?
                  AND m.code = ?
                  AND u.status = 'ACTIVE'
                  AND r.status = 'ACTIVE'
                  AND m.status = 'ACTIVE'
                """, Integer.class, username, menuCode);
        return count != null && count > 0;
    }
}
