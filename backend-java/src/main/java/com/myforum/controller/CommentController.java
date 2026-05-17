package com.myforum.controller;

import com.myforum.common.Result;
import com.myforum.model.dto.CommentRequest;
import com.myforum.security.JwtClaims;
import com.myforum.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @GetMapping("/comments")
    public Result<?> list(@RequestParam("post_id") Long postId, @AuthenticationPrincipal JwtClaims claims) {
        Long uid = claims != null ? claims.getUserId() : 0L;
        return Result.success(commentService.listByPost(postId, uid));
    }

    @PostMapping("/comment")
    public Result<?> create(@AuthenticationPrincipal JwtClaims claims, @Valid @RequestBody CommentRequest req) {
        return Result.success(commentService.create(claims.getUserId(), req));
    }

    @DeleteMapping("/comment/{id}")
    public Result<?> delete(@AuthenticationPrincipal JwtClaims claims, @PathVariable Long id) {
        commentService.delete(id, claims.getUserId(), claims.getRole() == 1);
        return Result.success(null);
    }
}
