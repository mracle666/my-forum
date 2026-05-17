package com.myforum.controller;

import com.myforum.common.Result;
import com.myforum.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    // ==================== 用户管理 ====================

    @GetMapping("/users")
    public Result<?> listUsers(@RequestParam(defaultValue = "1") Integer page,
                               @RequestParam(name = "page_size", defaultValue = "20") Integer pageSize) {
        return Result.success(adminService.listUsers(page, pageSize));
    }

    @PutMapping("/user/{id}/status")
    public Result<?> updateUserStatus(@PathVariable Long id, @RequestBody Map<String, Integer> req) {
        adminService.updateUserStatus(id, req.get("status"));
        return Result.success(null);
    }

    // ==================== 帖子管理 ====================

    @GetMapping("/posts")
    public Result<?> listPosts(@RequestParam(defaultValue = "1") Integer page,
                               @RequestParam(name = "page_size", defaultValue = "20") Integer pageSize,
                               @RequestParam(required = false) String keyword,
                               @RequestParam(required = false) Integer status) {
        return Result.success(adminService.listPosts(page, pageSize, keyword, status));
    }

    @DeleteMapping("/post/{id}")
    public Result<?> deletePost(@PathVariable Long id) {
        adminService.deletePost(id);
        return Result.success(null);
    }

    @PutMapping("/post/{id}/pin")
    public Result<?> togglePin(@PathVariable Long id) {
        adminService.togglePin(id);
        return Result.success(null);
    }

    // ==================== 板块管理 ====================

    @GetMapping("/categories")
    public Result<?> listCategories() {
        return Result.success(adminService.listCategories());
    }

    @PostMapping("/category")
    public Result<?> createCategory(@RequestBody Map<String, Object> req) {
        var cat = adminService.createCategory(
                (String) req.get("name"),
                (String) req.get("slug"),
                (String) req.get("description"),
                req.get("sort") != null ? ((Number) req.get("sort")).intValue() : 0);
        return Result.success(cat);
    }

    @PutMapping("/category/{id}")
    public Result<?> updateCategory(@PathVariable Integer id, @RequestBody Map<String, Object> req) {
        var cat = adminService.updateCategory(
                id,
                (String) req.get("name"),
                (String) req.get("slug"),
                (String) req.get("description"),
                req.get("sort") != null ? ((Number) req.get("sort")).intValue() : null);
        return Result.success(cat);
    }

    @DeleteMapping("/category/{id}")
    public Result<?> deleteCategory(@PathVariable Integer id) {
        adminService.deleteCategory(id);
        return Result.success(null);
    }
}
