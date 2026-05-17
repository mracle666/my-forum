package com.myforum.controller;

import com.myforum.common.Result;
import com.myforum.model.dto.*;
import com.myforum.oss.OssService;
import com.myforum.security.JwtClaims;
import com.myforum.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final OssService ossService;

    @PostMapping("/register")
    public Result<?> register(@Valid @RequestBody RegisterRequest req) {
        var user = userService.register(req);
        return Result.success(Map.of("id", user.getId(), "username", user.getUsername(), "email", user.getEmail()));
    }

    @PostMapping("/login")
    public Result<?> login(@Valid @RequestBody LoginRequest req) {
        return Result.success(userService.login(req));
    }

    @GetMapping("/profile")
    public Result<?> getProfile(@AuthenticationPrincipal JwtClaims claims) {
        return Result.success(userService.getProfile(claims.getUserId()));
    }

    @PutMapping("/profile")
    public Result<?> updateProfile(@AuthenticationPrincipal JwtClaims claims, @RequestBody Map<String, Object> fields) {
        userService.updateProfile(claims.getUserId(), fields);
        return Result.success(null);
    }

    @PutMapping("/password")
    public Result<?> changePassword(@AuthenticationPrincipal JwtClaims claims, @Valid @RequestBody ChangePasswordRequest req) {
        userService.changePassword(claims.getUserId(), req);
        return Result.success(null);
    }

    @PostMapping("/avatar")
    public Result<?> uploadAvatar(@AuthenticationPrincipal JwtClaims claims, @RequestParam("avatar") MultipartFile file) throws IOException {
        String avatarUrl = ossService.upload(file, "avatars");
        userService.updateProfile(claims.getUserId(), Map.of("avatar", avatarUrl));
        return Result.success(Map.of("avatar", avatarUrl));
    }

    @GetMapping("/{id}")
    public Result<?> getPublicProfile(@PathVariable Long id) {
        return Result.success(userService.getPublicProfile(id));
    }
}
