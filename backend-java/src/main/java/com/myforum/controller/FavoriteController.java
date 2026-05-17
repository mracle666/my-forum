package com.myforum.controller;

import com.myforum.common.Result;
import com.myforum.model.dto.FavoriteRequest;
import com.myforum.security.JwtClaims;
import com.myforum.service.FavoriteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;

    @PostMapping("/favorite")
    public Result<?> toggle(@AuthenticationPrincipal JwtClaims claims, @Valid @RequestBody FavoriteRequest req) {
        boolean favorited = favoriteService.toggle(claims.getUserId(), req.getPostId());
        return Result.success(Map.of("favorited", favorited));
    }

    @GetMapping("/favorites")
    public Result<?> list(@AuthenticationPrincipal JwtClaims claims,
                          @RequestParam(defaultValue = "1") Integer page,
                          @RequestParam(name = "page_size", defaultValue = "20") Integer pageSize) {
        return Result.success(favoriteService.list(claims.getUserId(), page, pageSize));
    }
}
