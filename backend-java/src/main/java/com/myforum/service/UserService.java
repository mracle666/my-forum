package com.myforum.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.myforum.common.BusinessException;
import com.myforum.common.PasswordUtils;
import com.myforum.common.Result;
import com.myforum.mapper.*;
import com.myforum.model.dto.*;
import com.myforum.model.entity.*;
import com.myforum.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final PostMapper postMapper;
    private final CommentMapper commentMapper;
    private final LikeMapper likeMapper;
    private final CategoryMapper categoryMapper;
    private final JwtTokenProvider jwtTokenProvider;

    public User register(RegisterRequest req) {
        if (userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, req.getUsername())) != null) {
            throw new BusinessException("用户名已存在");
        }
        if (userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getEmail, req.getEmail())) != null) {
            throw new BusinessException("邮箱已被注册");
        }

        User user = new User();
        user.setUsername(req.getUsername());
        user.setEmail(req.getEmail());
        user.setPassword(PasswordUtils.hash(req.getPassword()));
        user.setRole(0);
        user.setStatus(1);
        user.setAvatar("");
        user.setBio("");
        userMapper.insert(user);
        return user;
    }

    public LoginResult login(LoginRequest req) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, req.getUsername()));
        if (user == null || !PasswordUtils.check(req.getPassword(), user.getPassword())) {
            throw new BusinessException(Result.CODE_UNAUTHORIZED, "用户名或密码错误");
        }
        if (user.getStatus() == 0) {
            throw new BusinessException(Result.CODE_UNAUTHORIZED, "账号已被禁用");
        }

        String accessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getUsername(), user.getRole());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId(), user.getUsername(), user.getRole());
        user.setPassword(null);
        return new LoginResult(accessToken, refreshToken, user);
    }

    public String refreshToken(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new BusinessException(Result.CODE_UNAUTHORIZED, "refresh token 无效或已过期");
        }
        var claims = jwtTokenProvider.parseToken(refreshToken);
        return jwtTokenProvider.generateAccessToken(claims.getUserId(), claims.getUsername(), claims.getRole());
    }

    public User getProfile(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(Result.CODE_NOT_FOUND, "用户不存在");
        }
        user.setPassword(null);
        return user;
    }

    @Transactional
    public void updateProfile(Long userId, Map<String, Object> fields) {
        User user = userMapper.selectById(userId);
        if (user == null) throw new BusinessException(Result.CODE_NOT_FOUND, "用户不存在");

        if (fields.containsKey("bio")) user.setBio((String) fields.get("bio"));
        if (fields.containsKey("avatar")) user.setAvatar((String) fields.get("avatar"));
        userMapper.updateById(user);
    }

    public void changePassword(Long userId, ChangePasswordRequest req) {
        User user = userMapper.selectById(userId);
        if (user == null) throw new BusinessException(Result.CODE_NOT_FOUND, "用户不存在");
        if (!PasswordUtils.check(req.getOldPassword(), user.getPassword())) {
            throw new BusinessException("旧密码不正确");
        }
        user.setPassword(PasswordUtils.hash(req.getNewPassword()));
        userMapper.updateById(user);
    }

    public Map<String, Object> getPublicProfile(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) throw new BusinessException(Result.CODE_NOT_FOUND, "用户不存在");

        // Count posts and comments
        Long postCount = postMapper.selectCount(new LambdaQueryWrapper<Post>()
                .eq(Post::getUserId, userId)
                .eq(Post::getStatus, 1));
        Long commentCount = commentMapper.selectCount(new LambdaQueryWrapper<Comment>()
                .eq(Comment::getUserId, userId)
                .eq(Comment::getStatus, 1));

        // Count likes received on all posts
        List<Post> userPosts = postMapper.selectList(new LambdaQueryWrapper<Post>()
                .eq(Post::getUserId, userId)
                .eq(Post::getStatus, 1));
        List<Long> postIds = userPosts.stream().map(Post::getId).collect(Collectors.toList());
        long postLikes = likeMapper.countByTargetIds(Like.TARGET_POST, postIds);

        // Count likes received on all comments
        List<Comment> userComments = commentMapper.selectList(new LambdaQueryWrapper<Comment>()
                .eq(Comment::getUserId, userId)
                .eq(Comment::getStatus, 1));
        List<Long> commentIds = userComments.stream().map(Comment::getId).collect(Collectors.toList());
        long commentLikes = likeMapper.countByTargetIds(Like.TARGET_COMMENT, commentIds);

        long totalLikes = postLikes + commentLikes;

        // Get user's recent posts (up to 20)
        List<Post> recentPosts = postMapper.selectList(new LambdaQueryWrapper<Post>()
                .eq(Post::getUserId, userId)
                .eq(Post::getStatus, 1)
                .orderByDesc(Post::getCreatedAt)
                .last("LIMIT 20"));

        List<Map<String, Object>> postItems = new ArrayList<>();
        for (Post p : recentPosts) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", p.getId());
            item.put("title", p.getTitle());
            item.put("view_count", p.getViewCount() != null ? p.getViewCount() : 0L);
            item.put("like_count", likeMapper.countByTarget(Like.TARGET_POST, p.getId()));
            item.put("created_at", p.getCreatedAt() != null ? p.getCreatedAt().toString() : "");
            Category cat = categoryMapper.selectById(p.getCategoryId());
            if (cat != null) {
                item.put("category", Map.of("id", cat.getId(), "name", cat.getName()));
            }
            postItems.add(item);
        }

        return Map.of(
                "id", user.getId(),
                "username", user.getUsername(),
                "avatar", user.getAvatar() != null ? user.getAvatar() : "",
                "bio", user.getBio() != null ? user.getBio() : "",
                "role", user.getRole(),
                "created_at", user.getCreatedAt() != null ? user.getCreatedAt().toString() : "",
                "post_count", postCount,
                "comment_count", commentCount,
                "like_count", totalLikes,
                "posts", postItems
        );
    }
}
