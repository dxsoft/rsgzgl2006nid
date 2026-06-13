package com.dx.rsgzgl.system.service;

import com.dx.rsgzgl.system.dto.CurrentUserResponse;
import com.dx.rsgzgl.common.exception.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;

@Service
public class CurrentUserService {

    private final JdbcTemplate jdbcTemplate;

    public CurrentUserService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public String currentUsername() {
        String sessionUsername = sessionUsername();
        if (sessionUsername != null) {
            return sessionUsername;
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null
                && authentication.isAuthenticated()
                && authentication.getName() != null
                && !authentication.getName().isBlank()
                && !"anonymousUser".equals(authentication.getName())) {
            return authentication.getName();
        }
        return null;
    }

    public CurrentUserResponse currentUser() {
        String username = currentUsername();
        if (username == null || username.isBlank()) {
            throw new BusinessException("UNAUTHENTICATED", "请先登录");
        }
        List<CurrentUserResponse> users = jdbcTemplate.query("""
                SELECT username, display_name
                FROM sys_user
                WHERE username = ?
                LIMIT 1
                """, (rs, rowNum) -> new CurrentUserResponse(
                rs.getString("username"),
                rs.getString("display_name"),
                "bootstrap-token"
        ), username);
        if (!users.isEmpty()) {
            return users.getFirst();
        }
        return new CurrentUserResponse(username, username, "bootstrap-token");
    }

    private String sessionUsername() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return null;
        }
        HttpServletRequest request = attributes.getRequest();
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        Object username = session.getAttribute(AuthSessionService.SESSION_USERNAME);
        if (username instanceof String value && !value.isBlank()) {
            return value;
        }
        return null;
    }
}
