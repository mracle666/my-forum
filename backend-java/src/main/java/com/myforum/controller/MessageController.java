package com.myforum.controller;

import com.myforum.common.Result;
import com.myforum.security.JwtClaims;
import com.myforum.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @PostMapping("/message")
    public Result<?> send(@AuthenticationPrincipal JwtClaims claims, @RequestBody Map<String, Object> req) {
        Long toId = ((Number) req.get("to_id")).longValue();
        String content = (String) req.get("content");
        return Result.success(messageService.send(claims.getUserId(), toId, content));
    }

    @GetMapping("/messages")
    public Result<?> conversation(@AuthenticationPrincipal JwtClaims claims,
                                  @RequestParam(name = "with_user_id") Long withUserId,
                                  @RequestParam(defaultValue = "1") Integer page,
                                  @RequestParam(name = "page_size", defaultValue = "50") Integer pageSize) {
        return Result.success(messageService.conversation(claims.getUserId(), withUserId, page, pageSize));
    }

    @GetMapping("/messages/conversations")
    public Result<?> conversations(@AuthenticationPrincipal JwtClaims claims) {
        return Result.success(messageService.conversations(claims.getUserId()));
    }

    @GetMapping("/messages/unread")
    public Result<?> unreadCount(@AuthenticationPrincipal JwtClaims claims) {
        return Result.success(Map.of("count", messageService.unreadCount(claims.getUserId())));
    }
}
