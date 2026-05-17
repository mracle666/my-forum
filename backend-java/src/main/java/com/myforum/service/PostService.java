package com.myforum.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.myforum.common.BusinessException;
import com.myforum.common.Result;
import com.myforum.mapper.*;
import com.myforum.model.dto.PostRequest;
import com.myforum.model.entity.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostMapper postMapper;
    private final UserMapper userMapper;
    private final CategoryMapper categoryMapper;
    private final LikeMapper likeMapper;
    private final FavoriteMapper favoriteMapper;

    @Transactional
    public Post create(Long userId, PostRequest req) {
        Post post = new Post();
        post.setUserId(userId);
        post.setCategoryId(req.getCategoryId());
        post.setTitle(req.getTitle().trim());
        post.setContent(htmlEscape(req.getContent()));
        post.setContentRaw(req.getContent());
        post.setStatus(1);
        post.setViewCount(0L);
        postMapper.insert(post);
        enrichPost(post, userId);
        return post;
    }

    @Transactional
    public Post update(Long userId, Long postId, String title, String content) {
        Post post = postMapper.selectById(postId);
        if (post == null) throw new BusinessException(Result.CODE_NOT_FOUND, "帖子不存在");
        if (!post.getUserId().equals(userId)) throw new BusinessException(Result.CODE_FORBIDDEN, "无权编辑此帖子");

        if (StringUtils.hasText(title)) post.setTitle(title.trim());
        if (StringUtils.hasText(content)) {
            post.setContentRaw(content);
            post.setContent(htmlEscape(content));
        }
        postMapper.updateById(post);
        enrichPost(post, userId);
        return post;
    }

    @Transactional
    public void delete(Long userId, Long postId, boolean isAdmin) {
        Post post = postMapper.selectById(postId);
        if (post == null) throw new BusinessException(Result.CODE_NOT_FOUND, "帖子不存在");
        if (!post.getUserId().equals(userId) && !isAdmin) {
            throw new BusinessException(Result.CODE_FORBIDDEN, "无权删除此帖子");
        }
        post.setStatus(0);
        postMapper.updateById(post);
    }

    public Map<String, Object> getDetail(Long postId, Long currentUserId) {
        Post post = postMapper.selectById(postId);
        if (post == null || post.getStatus() == 0) {
            throw new BusinessException(Result.CODE_NOT_FOUND, "帖子不存在或已删除");
        }

        postMapper.incrementViewCount(postId);
        enrichPost(post, currentUserId);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", post.getId());
        result.put("title", post.getTitle());
        result.put("content", post.getContent());
        result.put("content_raw", post.getContentRaw());
        result.put("status", post.getStatus());
        result.put("view_count", (post.getViewCount() != null ? post.getViewCount() : 0) + 1);
        result.put("like_count", post.getLikeCount());
        result.put("category_id", post.getCategoryId());
        result.put("user_id", post.getUserId());
        result.put("created_at", post.getCreatedAt());
        result.put("updated_at", post.getUpdatedAt());

        if (post.getUser() != null) {
            result.put("user", Map.of("id", post.getUser().getId(), "username", post.getUser().getUsername(), "avatar", post.getUser().getAvatar() != null ? post.getUser().getAvatar() : ""));
        }
        if (post.getCategory() != null) {
            result.put("category", Map.of("id", post.getCategory().getId(), "name", post.getCategory().getName(), "slug", post.getCategory().getSlug()));
        }
        if (currentUserId > 0) {
            result.put("liked", post.getLiked());
            result.put("favorited", post.getFavorited());
        }
        return result;
    }

    public Map<String, Object> list(Integer page, Integer pageSize, Integer categoryId, String sort, String keyword, Long userId, Long currentUserId) {
        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<>();
        wrapper.ne(Post::getStatus, 0);
        if (categoryId != null && categoryId > 0) wrapper.eq(Post::getCategoryId, categoryId);
        if (userId != null && userId > 0) wrapper.eq(Post::getUserId, userId);
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(Post::getTitle, keyword).or().like(Post::getContent, keyword));
        }

        if ("hot".equals(sort)) {
            wrapper.orderByDesc(Post::getViewCount).orderByDesc(Post::getCreatedAt);
        } else {
            wrapper.orderByDesc(Post::getCreatedAt);
        }

        Page<Post> pageObj = new Page<>(page, pageSize);
        Page<Post> result = postMapper.selectPage(pageObj, wrapper);

        List<Post> posts = result.getRecords();
        enrichPosts(posts, currentUserId);

        List<Map<String, Object>> items = posts.stream().map(p -> {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", p.getId());
            item.put("title", p.getTitle());
            item.put("content", truncateContent(p.getContent(), 200));
            item.put("status", p.getStatus());
            item.put("view_count", p.getViewCount());
            item.put("category_id", p.getCategoryId());
            item.put("user_id", p.getUserId());
            item.put("created_at", p.getCreatedAt());
            item.put("updated_at", p.getUpdatedAt());
            item.put("like_count", p.getLikeCount() != null ? p.getLikeCount() : 0L);
            item.put("favorite_count", favoriteMapper.countByPost(p.getId()));
            item.put("liked", p.getLiked());
            item.put("favorited", p.getFavorited());
            if (p.getUser() != null) item.put("user", Map.of("id", p.getUser().getId(), "username", p.getUser().getUsername(), "avatar", p.getUser().getAvatar() != null ? p.getUser().getAvatar() : ""));
            if (p.getCategory() != null) item.put("category", Map.of("id", p.getCategory().getId(), "name", p.getCategory().getName(), "slug", p.getCategory().getSlug()));
            return item;
        }).collect(Collectors.toList());

        return Map.of("list", items, "total", result.getTotal(), "page", page, "page_size", pageSize);
    }

    public void pinPost(Long postId, Integer status, boolean isAdmin) {
        if (!isAdmin) throw new BusinessException(Result.CODE_FORBIDDEN, "无权限");
        if (status != 1 && status != 2) throw new BusinessException("无效的状态");
        Post post = postMapper.selectById(postId);
        if (post != null) {
            post.setStatus(status);
            postMapper.updateById(post);
        }
    }

    private void enrichPost(Post post, Long currentUserId) {
        if (post.getUserId() != null) {
            post.setUser(userMapper.selectById(post.getUserId()));
        }
        if (post.getCategoryId() != null) {
            post.setCategory(categoryMapper.selectById(post.getCategoryId()));
        }
        post.setLikeCount(likeMapper.countByTarget(Like.TARGET_POST, post.getId()));
        post.setFavorited(favoriteMapper.existsByUserAndPost(currentUserId != null ? currentUserId : 0L, post.getId()));
        if (currentUserId != null && currentUserId > 0) {
            post.setLiked(likeMapper.existsByUserAndTarget(currentUserId, Like.TARGET_POST, post.getId()));
            post.setFavorited(favoriteMapper.existsByUserAndPost(currentUserId, post.getId()));
        } else {
            post.setLiked(false);
            post.setFavorited(false);
        }
    }

    private void enrichPosts(List<Post> posts, Long currentUserId) {
        for (Post post : posts) {
            enrichPost(post, currentUserId);
        }
    }

    private String htmlEscape(String text) {
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&#39;");
    }

    private String truncateContent(String content, int maxLen) {
        if (content == null) return "";
        String plain = content.replaceAll("<[^>]*>", "");
        if (plain.length() <= maxLen) return plain;
        return plain.substring(0, maxLen) + "...";
    }
}
