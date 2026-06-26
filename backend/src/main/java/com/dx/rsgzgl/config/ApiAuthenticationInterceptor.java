package com.dx.rsgzgl.config;

import com.dx.rsgzgl.common.api.ApiResponse;
import com.dx.rsgzgl.system.service.AuthSessionService;
import com.dx.rsgzgl.system.service.UserPermissionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.nio.charset.StandardCharsets;

@Component
public class ApiAuthenticationInterceptor implements HandlerInterceptor {

    private final ObjectMapper objectMapper;
    private final UserPermissionService userPermissionService;

    public ApiAuthenticationInterceptor(ObjectMapper objectMapper, UserPermissionService userPermissionService) {
        this.objectMapper = objectMapper;
        this.userPermissionService = userPermissionService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (request.getRequestURI().startsWith("/api/")) {
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        }
        if (!requiresAuthentication(request)) {
            return true;
        }
        HttpSession session = request.getSession(false);
        if (session == null || !(session.getAttribute(AuthSessionService.SESSION_USERNAME) instanceof String username) || username.isBlank()) {
            writeError(response, HttpServletResponse.SC_UNAUTHORIZED, "UNAUTHENTICATED", "Please log in first");
            return false;
        }
        String requiredMenu = requiredMenu(request);
        if (requiredMenu != null && !userPermissionService.hasMenu(username, requiredMenu)) {
            writeError(response, HttpServletResponse.SC_FORBIDDEN, "FORBIDDEN", "No permission for this function");
            return false;
        }
        return true;
    }

    private boolean requiresAuthentication(HttpServletRequest request) {
        String path = request.getRequestURI();
        if (!path.startsWith("/api/")) {
            return false;
        }
        return !path.equals("/api/auth/login")
                && !path.equals("/api/auth/logout")
                && !path.equals("/api/health");
    }

    private String requiredMenu(HttpServletRequest request) {
        String path = request.getRequestURI();
        if (path.equals("/api/auth/me") || path.equals("/api/auth/change-password")) {
            return null;
        }
        if (path.equals("/api/system/menus")) {
            return null;
        }
        if (path.startsWith("/api/system/audits")) {
            return "SYSTEM_AUDIT";
        }
        if (path.equals("/api/system/menu-admin") || path.startsWith("/api/system/menus/")) {
            return "SYSTEM_MENU";
        }
        if (path.equals("/api/system/roles")
                || path.startsWith("/api/system/roles/")
                || path.equals("/api/system/role-templates")) {
            return "SYSTEM_ROLE";
        }
        if (path.equals("/api/system/users") || path.startsWith("/api/system/users/")) {
            return "SYSTEM_USER";
        }
        if (path.equals("/api/workbench/items.csv")) {
            return "SALARY_EXPORT";
        }
        if (path.startsWith("/api/workbench")) {
            return "WORKBENCH";
        }
        if (path.startsWith("/api/salary/field-config")) {
            return "SALARY_CONFIG";
        }
        if (path.equals("/api/salary/trial-calc")
                || path.equals("/api/salary/timeline-generated-batch")
                || path.startsWith("/api/salary/rule-trial/normal-grade")) {
            return path.endsWith(".csv") ? "SALARY_EXPORT" : "SALARY_TRIAL";
        }
        if (path.equals("/api/salary/reconcile") || path.equals("/api/salary/reconcile-batch")) {
            return "SALARY_RECONCILE";
        }
        if (path.endsWith(".csv")) {
            return "SALARY_EXPORT";
        }
        if (path.startsWith("/api/salary") || path.startsWith("/api/persons") || path.startsWith("/api/org")) {
            return "SALARY_PERSON";
        }
        return null;
    }

    private void writeError(HttpServletResponse response, int status, String code, String message) throws Exception {
        response.setStatus(status);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(), ApiResponse.fail(code, message));
    }
}
