package com.myforum.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.myforum.mapper.*;
import com.myforum.model.entity.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FavoriteService {

    private final FavoriteMapper favoriteMapper;
    private final PostMapper postMapper;
    private final UserMapper userMapper;
    private final CategoryMapper categoryMapper;
    private final NotificationService notificationService;

    @Transactional
    public boolean toggle(Long userId, Long postId) {
        Favorite existing = favoriteMapper.selectOne(new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getUserId, userId).eq(Favorite::getPostId, postId));

        if (existing != null) {
            favoriteMapper.deleteById(existing.getId());
            return false;
        }

        Favorite fav = new Favorite();
        fav.setUserId(userId);
        fav.setPostId(postId);
        favoriteMapper.insert(fav);

        // 通知帖子作者
        Post post = postMapper.selectById(postId);
        if (post != null && !post.getUserId().equals(userId)) {
            User favoriter = userMapper.selectById(userId);
            notificationService.create(post.getUserId(), userId, Notification.TYPE_LIKE,
                    (favoriter != null ? favoriter.getUsername() : "用户") + " 收藏了你的帖子", postId);
        }

        return true;
    }

    public Map<String, Object> list(Long userId, int page, int pageSize) {
        Page<Favorite> favPage = new Page<>(page, pageSize);
        LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getUserId, userId)
                .orderByDesc(Favorite::getCreatedAt);
        Page<Favorite> result = favoriteMapper.selectPage(favPage, wrapper);

        List<Long> postIds = result.getRecords().stream().map(Favorite::getPostId).collect(Collectors.toList());
        Map<Long, Post> postMap = Collections.emptyMap();
        if (!postIds.isEmpty()) {
            List<Post> posts = postMapper.selectBatchIds(postIds);
            postMap = posts.stream().collect(Collectors.toMap(Post::getId, p -> p));
        }

        List<Map<String, Object>> items = new ArrayList<>();
        for (Favorite fav : result.getRecords()) {
            Post post = postMap.get(fav.getPostId());
            if (post == null || post.getStatus() == 0) continue;
            User u = userMapper.selectById(post.getUserId());
            Category cat = categoryMapper.selectById(post.getCategoryId());
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", post.getId());
            item.put("title", post.getTitle());
            item.put("content", truncate(post.getContent(), 200));
            item.put("view_count", post.getViewCount());
            item.put("user_id", post.getUserId());
            item.put("created_at", post.getCreatedAt());
            if (u != null) item.put("user", Map.of("id", u.getId(), "username", u.getUsername(), "avatar", u.getAvatar() != null ? u.getAvatar() : ""));
            if (cat != null) item.put("category", Map.of("id", cat.getId(), "name", cat.getName()));
            items.add(item);
        }

        return Map.of("list", items, "total", result.getTotal(), "page", page, "page_size", pageSize);
    }

    private String truncate(String content, int len) {
        if (content == null) return "";
        String p = content.replaceAll("<[^>]*>", "");
        return p.length() <= len ? p : p.substring(0, len) + "...";
    }
}
