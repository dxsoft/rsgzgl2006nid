package com.dx.rsgzgl.system.controller;

import com.dx.rsgzgl.common.api.ApiResponse;
import com.dx.rsgzgl.system.dto.CurrentUserResponse;
import com.dx.rsgzgl.system.dto.LoginRequest;
import com.dx.rsgzgl.system.dto.PasswordChangeRequest;
import com.dx.rsgzgl.system.service.AuthSessionService;
import com.dx.rsgzgl.system.service.CurrentUserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final CurrentUserService currentUserService;
    private final AuthSessionService authSessionService;

    public AuthController(CurrentUserService currentUserService, AuthSessionService authSessionService) {
        this.currentUserService = currentUserService;
        this.authSessionService = authSessionService;
    }

    @PostMapping("/login")
    public ApiResponse<CurrentUserResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest servletRequest
    ) {
        return ApiResponse.ok(authSessionService.login(request, servletRequest));
    }

    @GetMapping("/me")
    public ApiResponse<CurrentUserResponse> me() {
        return ApiResponse.ok(currentUserService.currentUser());
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(HttpServletRequest request) {
        authSessionService.logout(request);
        return ApiResponse.ok();
    }

    @PostMapping("/change-password")
    public ApiResponse<Void> changePassword(@Valid @RequestBody PasswordChangeRequest request) {
        authSessionService.changePassword(request);
        return ApiResponse.ok();
    }
}
