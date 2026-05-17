package com.myforum.controller;

import com.myforum.common.Result;
import com.myforum.security.JwtClaims;
import com.myforum.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/notifications")
    public Result<?> list(@AuthenticationPrincipal JwtClaims claims,
                          @RequestParam(defaultValue = "1") Integer page,
                          @RequestParam(name = "page_size", defaultValue = "20") Integer pageSize) {
        return Result.success(notificationService.list(claims.getUserId(), page, pageSize));
    }

    @PutMapping("/notification/{id}/read")
    public Result<?> markRead(@AuthenticationPrincipal JwtClaims claims, @PathVariable Long id) {
        notificationService.markRead(id, claims.getUserId());
        return Result.success(null);
    }

    @GetMapping("/notifications/unread")
    public Result<?> unreadCount(@AuthenticationPrincipal JwtClaims claims) {
        return Result.success(Map.of("count", notificationService.countUnread(claims.getUserId())));
    }

    @PutMapping("/notifications/read-all")
    public Result<?> markAllRead(@AuthenticationPrincipal JwtClaims claims) {
        notificationService.markAllRead(claims.getUserId());
        return Result.success(null);
    }
}
