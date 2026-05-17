package com.myforum.controller;

import com.myforum.common.Result;
import com.myforum.oss.OssService;
import com.myforum.security.JwtClaims;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class UploadController {

    private final OssService ossService;

    @PostMapping("/upload/image")
    public Result<?> uploadImage(@AuthenticationPrincipal JwtClaims claims, @RequestParam("file") MultipartFile file) throws IOException {
        String url = ossService.upload(file, "images");
        return Result.success(Map.of("url", url));
    }
}
