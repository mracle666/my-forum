package com.myforum.controller;

import com.myforum.common.Result;
import com.myforum.model.dto.LikeRequest;
import com.myforum.security.JwtClaims;
import com.myforum.service.LikeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class LikeController {

    private final LikeService likeService;

    @PostMapping("/like")
    public Result<?> toggle(@AuthenticationPrincipal JwtClaims claims, @Valid @RequestBody LikeRequest req) {
        boolean liked = likeService.toggle(claims.getUserId(), req.getTargetType(), req.getTargetId());
        return Result.success(Map.of("liked", liked));
    }
}
