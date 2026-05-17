package com.myforum.controller;

import com.myforum.common.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class HomeController {

    @GetMapping("/")
    public Result<?> home() {
        return Result.success(Map.of(
                "name", "MyForum API",
                "version", "1.0.0",
                "docs", "/api/v1"
        ));
    }
}
