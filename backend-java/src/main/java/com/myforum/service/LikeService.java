package com.myforum.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.myforum.common.BusinessException;
import com.myforum.mapper.*;
import com.myforum.model.dto.WsEvent;
import com.myforum.model.entity.*;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class LikeService {

    private final LikeMapper likeMapper;
    private final PostMapper postMapper;
    private final CommentMapper commentMapper;
    private final UserMapper userMapper;
    private final NotificationService notificationService;
    private final SimpMessagingTemplate messaging;

    @Transactional
    public boolean toggle(Long userId, Integer targetType, Long targetId) {
        if (targetType != Like.TARGET_POST && targetType != Like.TARGET_COMMENT) {
            throw new BusinessException("无效的目标类型");
        }

        Like existing = likeMapper.selectOne(new LambdaQueryWrapper<Like>()
                .eq(Like::getUserId, userId)
                .eq(Like::getTargetType, targetType)
                .eq(Like::getTargetId, targetId));

        if (existing != null) {
            likeMapper.deleteById(existing.getId());
            long newCount = likeMapper.countByTarget(targetType, targetId);
            if (targetType == Like.TARGET_POST) {
                messaging.convertAndSend("/topic/post/" + targetId,
                        new WsEvent("like_changed", Map.of("target_type", targetType, "target_id", targetId, "count", newCount, "liked", false)));
            }
            return false;
        }

        Like like = new Like();
        like.setUserId(userId);
        like.setTargetType(targetType);
        like.setTargetId(targetId);
        likeMapper.insert(like);

        long newCount = likeMapper.countByTarget(targetType, targetId);
        Long postId = targetType == Like.TARGET_POST ? targetId : null;
        if (targetType == Like.TARGET_COMMENT) {
            Comment c = commentMapper.selectById(targetId);
            if (c != null) postId = c.getPostId();
        }
        if (postId != null) {
            messaging.convertAndSend("/topic/post/" + postId,
                    new WsEvent("like_changed", Map.of("target_type", targetType, "target_id", targetId, "count", newCount, "liked", true)));
        }

        // 通知被点赞的人
        User liker = userMapper.selectById(userId);
        String likerName = liker != null ? liker.getUsername() : "用户";
        if (targetType == Like.TARGET_POST) {
            Post post = postMapper.selectById(targetId);
            if (post != null && !post.getUserId().equals(userId)) {
                notificationService.create(post.getUserId(), userId, Notification.TYPE_LIKE,
                        likerName + " 点赞了你的帖子", post.getId());
            }
        } else {
            Comment comment = commentMapper.selectById(targetId);
            if (comment != null && !comment.getUserId().equals(userId)) {
                notificationService.create(comment.getUserId(), userId, Notification.TYPE_LIKE,
                        likerName + " 点赞了你的回复", comment.getPostId());
            }
        }

        return true;
    }
}
