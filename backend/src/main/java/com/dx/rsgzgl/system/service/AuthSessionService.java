package com.dx.rsgzgl.system.service;

import com.dx.rsgzgl.common.exception.BusinessException;
import com.dx.rsgzgl.system.dto.CurrentUserResponse;
import com.dx.rsgzgl.system.dto.LoginRequest;
import com.dx.rsgzgl.system.dto.PasswordChangeRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuthSessionService {

    public static final String SESSION_USERNAME = "CURRENT_USERNAME";

    private final JdbcTemplate jdbcTemplate;
    private final PasswordEncoder passwordEncoder;
    private final CurrentUserService currentUserService;
    private final SystemAuditService systemAuditService;

    public AuthSessionService(
            JdbcTemplate jdbcTemplate,
            PasswordEncoder passwordEncoder,
            CurrentUserService currentUserService,
            SystemAuditService systemAuditService
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.passwordEncoder = passwordEncoder;
        this.currentUserService = currentUserService;
        this.systemAuditService = systemAuditService;
    }

    public CurrentUserResponse login(LoginRequest request, HttpServletRequest servletRequest) {
        UserCredential credential = credential(request.username());
        if (credential == null || !"ACTIVE".equals(credential.status()) || !passwordMatches(request.password(), credential.passwordHash())) {
            throw new BusinessException("AUTH_FAILED", "用户名或密码错误");
        }
        HttpSession session = servletRequest.getSession(true);
        session.setAttribute(SESSION_USERNAME, credential.username());
        return currentUserService.currentUser();
    }

    public void logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
    }

    public void changePassword(PasswordChangeRequest request) {
        String username = currentUserService.currentUsername();
        if (username == null || username.isBlank()) {
            throw new BusinessException("UNAUTHENTICATED", "请先登录");
        }
        UserCredential credential = credential(username);
        if (credential == null || !passwordMatches(request.oldPassword(), credential.passwordHash())) {
            throw new BusinessException("AUTH_FAILED", "原密码错误");
        }
        String newPassword = normalizePassword(request.newPassword());
        jdbcTemplate.update("""
                UPDATE sys_user
                SET password_hash = ?
                WHERE username = ?
                """, passwordEncoder.encode(newPassword), username);
        systemAuditService.record("账户安全", "修改密码", "USER", username, "用户自行修改密码");
    }

    private UserCredential credential(String username) {
        List<UserCredential> users = jdbcTemplate.query("""
                SELECT username, password_hash, status
                FROM sys_user
                WHERE username = ?
                LIMIT 1
                """, (rs, rowNum) -> new UserCredential(
                rs.getString("username"),
                rs.getString("password_hash"),
                rs.getString("status")
        ), username);
        return users.isEmpty() ? null : users.getFirst();
    }

    public boolean passwordMatches(String rawPassword, String storedPassword) {
        if (storedPassword == null || storedPassword.isBlank()) {
            return false;
        }
        if (storedPassword.startsWith("{noop}")) {
            return storedPassword.substring("{noop}".length()).equals(rawPassword);
        }
        return passwordEncoder.matches(rawPassword, storedPassword);
    }

    public String encodePassword(String rawPassword) {
        return passwordEncoder.encode(normalizePassword(rawPassword));
    }

    private String normalizePassword(String password) {
        String value = password == null ? "" : password.trim();
        if (value.length() < 4 || value.length() > 64) {
            throw new BusinessException("VALIDATION_ERROR", "密码长度应为 4-64 位");
        }
        return value;
    }

    private record UserCredential(String username, String passwordHash, String status) {
    }
}
