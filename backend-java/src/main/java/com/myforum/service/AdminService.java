package com.myforum.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.myforum.common.BusinessException;
import com.myforum.mapper.*;
import com.myforum.model.entity.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserMapper userMapper;
    private final PostMapper postMapper;
    private final CategoryMapper categoryMapper;
    private final LikeMapper likeMapper;

    // ==================== 用户管理 ====================

    public Map<String, Object> listUsers(int page, int pageSize) {
        Page<User> pageObj = new Page<>(page, pageSize);
        Page<User> result = userMapper.selectPage(pageObj,
                new LambdaQueryWrapper<User>().orderByDesc(User::getCreatedAt));

        List<Map<String, Object>> items = result.getRecords().stream().map(u -> {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", u.getId());
            item.put("username", u.getUsername());
            item.put("email", u.getEmail());
            item.put("avatar", u.getAvatar());
            item.put("role", u.getRole());
            item.put("status", u.getStatus());
            item.put("created_at", u.getCreatedAt());
            return item;
        }).toList();

        return Map.of("list", items, "total", result.getTotal(), "page", page, "page_size", pageSize);
    }

    @Transactional
    public void updateUserStatus(Long userId, Integer status) {
        if (status != 0 && status != 1) throw new BusinessException("无效的状态值");
        User user = userMapper.selectById(userId);
        if (user != null) {
            user.setStatus(status);
            userMapper.updateById(user);
        }
    }

    // ==================== 帖子管理 ====================

    public Map<String, Object> listPosts(int page, int pageSize, String keyword, Integer status) {
        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<Post>()
                .orderByDesc(Post::getCreatedAt);
        if (status != null) {
            wrapper.eq(Post::getStatus, status);
        } else {
            wrapper.ne(Post::getStatus, 0); // exclude deleted
        }
        if (StringUtils.hasText(keyword)) {
            wrapper.like(Post::getTitle, keyword);
        }

        Page<Post> pageObj = new Page<>(page, pageSize);
        Page<Post> result = postMapper.selectPage(pageObj, wrapper);

        List<Map<String, Object>> items = new ArrayList<>();
        for (Post p : result.getRecords()) {
            User user = userMapper.selectById(p.getUserId());
            Category cat = categoryMapper.selectById(p.getCategoryId());
            long likeCount = likeMapper.countByTarget(Like.TARGET_POST, p.getId());

            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", p.getId());
            item.put("title", p.getTitle());
            item.put("user_id", p.getUserId());
            item.put("status", p.getStatus());
            item.put("view_count", p.getViewCount());
            item.put("like_count", likeCount);
            item.put("created_at", p.getCreatedAt());
            if (user != null) item.put("username", user.getUsername());
            if (cat != null) item.put("category_name", cat.getName());
            items.add(item);
        }

        return Map.of("list", items, "total", result.getTotal(), "page", page, "page_size", pageSize);
    }

    @Transactional
    public void deletePost(Long postId) {
        Post post = postMapper.selectById(postId);
        if (post != null) {
            post.setStatus(0);
            postMapper.updateById(post);
        }
    }

    @Transactional
    public void togglePin(Long postId) {
        Post post = postMapper.selectById(postId);
        if (post == null) throw new BusinessException("帖子不存在");
        post.setStatus(post.getStatus() == 2 ? 1 : 2);
        postMapper.updateById(post);
    }

    // ==================== 板块管理 ====================

    public List<Category> listCategories() {
        return categoryMapper.selectList(
                new LambdaQueryWrapper<Category>().orderByAsc(Category::getSort));
    }

    @Transactional
    public Category createCategory(String name, String slug, String description, Integer sort) {
        if (!StringUtils.hasText(name)) throw new BusinessException("板块名称不能为空");
        if (!StringUtils.hasText(slug)) throw new BusinessException("板块标识不能为空");

        Category cat = new Category();
        cat.setName(name);
        cat.setSlug(slug);
        cat.setDescription(description != null ? description : "");
        cat.setSort(sort != null ? sort : 0);
        categoryMapper.insert(cat);
        return cat;
    }

    @Transactional
    public Category updateCategory(Integer id, String name, String slug, String description, Integer sort) {
        Category cat = categoryMapper.selectById(id);
        if (cat == null) throw new BusinessException("板块不存在");

        if (StringUtils.hasText(name)) cat.setName(name);
        if (StringUtils.hasText(slug)) cat.setSlug(slug);
        if (description != null) cat.setDescription(description);
        if (sort != null) cat.setSort(sort);
        categoryMapper.updateById(cat);
        return cat;
    }

    @Transactional
    public void deleteCategory(Integer id) {
        // Check if any posts use this category
        long postCount = postMapper.selectCount(new LambdaQueryWrapper<Post>()
                .eq(Post::getCategoryId, id));
        if (postCount > 0) {
            throw new BusinessException("该板块下还有帖子，无法删除");
        }
        categoryMapper.deleteById(id);
    }
}
