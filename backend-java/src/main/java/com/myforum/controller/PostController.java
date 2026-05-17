package com.myforum.controller;

import com.myforum.common.Result;
import com.myforum.model.dto.PostRequest;
import com.myforum.security.JwtClaims;
import com.myforum.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @GetMapping("/posts")
    public Result<?> list(@RequestParam(defaultValue = "1") Integer page,
                          @RequestParam(name = "page_size", defaultValue = "20") Integer pageSize,
                          @RequestParam(name = "category_id", required = false) Integer categoryId,
                          @RequestParam(defaultValue = "latest") String sort,
                          @RequestParam(required = false) String keyword,
                          @RequestParam(name = "user_id", required = false) Long userId,
                          @AuthenticationPrincipal JwtClaims claims) {
        Long uid = claims != null ? claims.getUserId() : 0L;
        return Result.success(postService.list(page, pageSize, categoryId, sort, keyword, userId, uid));
    }

    @GetMapping("/post/{id}")
    public Result<?> getDetail(@PathVariable Long id, @AuthenticationPrincipal JwtClaims claims) {
        Long uid = claims != null ? claims.getUserId() : 0L;
        return Result.success(postService.getDetail(id, uid));
    }

    @GetMapping("/posts/search")
    public Result<?> search(@RequestParam String keyword,
                            @RequestParam(defaultValue = "1") Integer page,
                            @RequestParam(name = "page_size", defaultValue = "20") Integer pageSize,
                            @AuthenticationPrincipal JwtClaims claims) {
        Long uid = claims != null ? claims.getUserId() : 0L;
        return Result.success(postService.list(page, pageSize, null, "latest", keyword, null, uid));
    }

    @PostMapping("/post")
    public Result<?> create(@AuthenticationPrincipal JwtClaims claims, @Valid @RequestBody PostRequest req) {
        return Result.success(postService.create(claims.getUserId(), req));
    }

    @PutMapping("/post/{id}")
    public Result<?> update(@AuthenticationPrincipal JwtClaims claims, @PathVariable Long id, @RequestBody Map<String, String> req) {
        return Result.success(postService.update(claims.getUserId(), id, req.get("title"), req.get("content")));
    }

    @DeleteMapping("/post/{id}")
    public Result<?> delete(@AuthenticationPrincipal JwtClaims claims, @PathVariable Long id) {
        postService.delete(claims.getUserId(), id, claims.getRole() == 1);
        return Result.success(null);
    }
}
