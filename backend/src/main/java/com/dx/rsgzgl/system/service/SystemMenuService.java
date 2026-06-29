package com.dx.rsgzgl.system.service;

import com.dx.rsgzgl.system.dto.MenuItemResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class SystemMenuService {

    private final JdbcTemplate jdbcTemplate;
    private final CurrentUserService currentUserService;

    public SystemMenuService(JdbcTemplate jdbcTemplate, CurrentUserService currentUserService) {
        this.jdbcTemplate = jdbcTemplate;
        this.currentUserService = currentUserService;
    }

    public List<MenuItemResponse> menusForCurrentUser() {
        if (currentUserService.currentUsername() == null) {
            return List.of();
        }
        List<MenuItemResponse> menus = menusFromDatabase();
        return menus == null ? defaultMenus() : menus;
    }

    private List<MenuItemResponse> menusFromDatabase() {
        List<MenuRow> rows;
        try {
            rows = jdbcTemplate.query("""
                    SELECT DISTINCT m.code,
                           COALESCE(m.parent_code, '') AS parent_code,
                           m.title,
                           COALESCE(m.icon, '') AS icon,
                           COALESCE(m.view_name, '') AS view_name,
                           m.sequence_no
                    FROM sys_menu m
                    JOIN sys_role_menu rm ON rm.menu_code = m.code
                    JOIN sys_role r ON r.code = rm.role_code
                    JOIN sys_user_role ur ON ur.role_code = rm.role_code
                    JOIN sys_user u ON u.username = ur.username
                    WHERE ur.username = ?
                      AND m.status = 'ACTIVE'
                      AND r.status = 'ACTIVE'
                      AND u.status = 'ACTIVE'
                    ORDER BY COALESCE(m.parent_code, ''), m.sequence_no, m.code
                    """, (rs, rowNum) -> new MenuRow(
                    rs.getString("code"),
                    rs.getString("parent_code"),
                    rs.getString("title"),
                    rs.getString("icon"),
                    rs.getString("view_name"),
                    rs.getInt("sequence_no")
            ), currentUserService.currentUsername());
        } catch (RuntimeException ignored) {
            return null;
        }
        Map<String, MutableMenu> byCode = new LinkedHashMap<>();
        for (MenuRow row : rows) {
            byCode.put(row.code(), new MutableMenu(row));
        }
        List<MutableMenu> roots = new ArrayList<>();
        for (MutableMenu menu : byCode.values()) {
            String parentCode = menu.row.parentCode();
            MutableMenu parent = byCode.get(parentCode);
            if (parent == null || parentCode.isBlank()) {
                roots.add(menu);
            } else {
                parent.children.add(menu);
            }
        }
        return roots.stream().map(MutableMenu::toResponse).toList();
    }

    private List<MenuItemResponse> defaultMenus() {
        return List.of(
                menu("WORKBENCH", "\u5de5\u4f5c\u53f0", "desktop", "workbench", 10),
                group("SALARY", "\u5de5\u8d44\u4e1a\u52a1", "salary", 20, List.of(
                        menu("SALARY_PERSON", "\u4eba\u5458\u5de5\u8d44", "people", "salary", 10),
                        menu("SALARY_TODO", "\u5f85\u529e\u5de5\u8d44\u53d8\u52a8", "todo", "workbench", 20),
                        menu("SALARY_DONE", "\u5df2\u529e\u5de5\u8d44\u53d8\u52a8", "done", "workbench", 30),
                        menu("SALARY_TRIAL", "\u5de5\u8d44\u8bd5\u7b97", "trial", "salary", 40),
                        menu("SALARY_RECONCILE", "\u5de5\u8d44\u5bf9\u8d26", "reconcile", "salary", 50),
                        menu("SALARY_EXPORT", "\u5de5\u8d44\u5bfc\u51fa", "export", "salary", 60),
                        menu("SALARY_REPORT", "\u62a5\u8868\u6253\u5370", "print", "workbench", 70),
                        menu("SALARY_HISTORY_WRITE", "\u5386\u53f2\u5199\u5165", "write", "workbench", 80),
                        menu("SALARY_HISTORY_ROLLBACK", "\u5386\u53f2\u5199\u5165\u64a4\u9500", "rollback", "workbench", 90)
                )),
                group("MIGRATION", "\u8fc1\u79fb\u6838\u9a8c", "migration", 25, List.of(
                        menu("SALARY_GOVERNANCE", "\u6570\u636e\u6cbb\u7406", "governance", "workbench", 10),
                        menu("SALARY_ACCEPTANCE", "\u8fc1\u79fb\u9a8c\u6536", "acceptance", "workbench", 20),
                        menu("SALARY_DELIVERY_ARCHIVE", "\u4ea4\u4ed8\u5f52\u6863", "archive", "workbench", 30)
                )),
                group("APPLICATION", "\u7533\u529e\u4e1a\u52a1", "application", 30, List.of(
                        menu("APPLICATION_TODO", "\u7533\u529e\u5f85\u529e", "todo", "workbench", 10),
                        menu("APPLICATION_DONE", "\u7533\u529e\u5df2\u529e", "done", "workbench", 20)
                )),
                group("SYSTEM", "\u7cfb\u7edf\u7ba1\u7406", "system", 90, List.of(
                        menu("SYSTEM_MENU", "\u83dc\u5355\u7ba1\u7406", "menu", "system", 10),
                        menu("SYSTEM_ROLE", "\u89d2\u8272\u6743\u9650", "role", "system", 20),
                        menu("SYSTEM_USER", "\u7528\u6237\u7ba1\u7406", "user", "system", 30),
                        menu("SYSTEM_AUDIT", "\u64cd\u4f5c\u5ba1\u8ba1", "audit", "system", 40),
                        menu("SALARY_CONFIG", "\u5de5\u8d44\u9879\u76ee\u914d\u7f6e", "config", "system", 50)
                ))
        );
    }

    private MenuItemResponse menu(String code, String title, String icon, String view, int sequence) {
        return new MenuItemResponse(code, title, icon, view, sequence, List.of());
    }

    private MenuItemResponse group(String code, String title, String icon, int sequence, List<MenuItemResponse> children) {
        return new MenuItemResponse(code, title, icon, "", sequence, children);
    }

    private record MenuRow(String code, String parentCode, String title, String icon, String viewName, int sequence) {
    }

    private static class MutableMenu {
        private final MenuRow row;
        private final List<MutableMenu> children = new ArrayList<>();

        private MutableMenu(MenuRow row) {
            this.row = row;
        }

        private MenuItemResponse toResponse() {
            return new MenuItemResponse(
                    row.code(),
                    row.title(),
                    row.icon(),
                    row.viewName(),
                    row.sequence(),
                    children.stream().map(MutableMenu::toResponse).toList()
            );
        }
    }
}
