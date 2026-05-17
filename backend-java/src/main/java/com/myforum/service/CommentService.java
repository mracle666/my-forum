package com.myforum.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.myforum.common.BusinessException;
import com.myforum.common.Result;
import com.myforum.mapper.CommentMapper;
import com.myforum.mapper.LikeMapper;
import com.myforum.mapper.PostMapper;
import com.myforum.mapper.UserMapper;
import com.myforum.model.dto.CommentRequest;
import com.myforum.model.dto.WsEvent;
import com.myforum.model.entity.Comment;
import com.myforum.model.entity.Like;
import com.myforum.model.entity.Notification;
import com.myforum.model.entity.Post;
import com.myforum.model.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentMapper commentMapper;
    private final PostMapper postMapper;
    private final UserMapper userMapper;
    private final LikeMapper likeMapper;
    private final NotificationService notificationService;
    private final SimpMessagingTemplate messaging;

    @Transactional
    public Comment create(Long userId, CommentRequest req) {
        Post post = postMapper.selectById(req.getPostId());
        if (post == null || post.getStatus() == 0) {
            throw new BusinessException("帖子不存在或已删除");
        }

        Comment comment = new Comment();
        comment.setPostId(req.getPostId());
        comment.setUserId(userId);
        comment.setParentId(req.getParentId() != null ? req.getParentId() : 0L);
        comment.setReplyTo(req.getReplyTo() != null ? req.getReplyTo() : 0L);
        comment.setContent(req.getContent());
        comment.setStatus(1);
        commentMapper.insert(comment);

        comment.setUser(userMapper.selectById(userId));
        comment.setLikeCount(0L);
        comment.setLiked(false);
        comment.setChildren(Collections.emptyList());

        // Push WebSocket event
        messaging.convertAndSend("/topic/post/" + post.getId(),
                new WsEvent("new_comment", comment));

        // 通知帖子作者
        if (!post.getUserId().equals(userId)) {
            User commentUser = userMapper.selectById(userId);
            notificationService.create(post.getUserId(), userId, Notification.TYPE_COMMENT,
                    (commentUser != null ? commentUser.getUsername() : "用户") + " 回复了你的帖子", post.getId());
        }
        // 通知被回复的用户（如果不是帖子作者也不是自己）
        if (req.getReplyTo() != null && req.getReplyTo() > 0
                && !req.getReplyTo().equals(userId) && !req.getReplyTo().equals(post.getUserId())) {
            User replyUser = userMapper.selectById(userId);
            notificationService.create(req.getReplyTo(), userId, Notification.TYPE_COMMENT,
                    (replyUser != null ? replyUser.getUsername() : "用户") + " 回复了你", post.getId());
        }

        return comment;
    }

    @Transactional
    public void delete(Long commentId, Long userId, boolean isAdmin) {
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null) throw new BusinessException(Result.CODE_NOT_FOUND, "回复不存在");
        if (!comment.getUserId().equals(userId) && !isAdmin) {
            throw new BusinessException(Result.CODE_FORBIDDEN, "无权删除此回复");
        }
        comment.setStatus(0);
        commentMapper.updateById(comment);
    }

    public List<Comment> listByPost(Long postId, Long currentUserId) {
        List<Comment> comments = commentMapper.selectList(
                new LambdaQueryWrapper<Comment>()
                        .eq(Comment::getPostId, postId)
                        .eq(Comment::getStatus, 1)
                        .orderByAsc(Comment::getCreatedAt)
        );

        List<Long> commentIds = comments.stream().map(Comment::getId).collect(Collectors.toList());
        Set<Long> likedIds = Collections.emptySet();
        if (currentUserId != null && currentUserId > 0 && !commentIds.isEmpty()) {
            likedIds = new HashSet<>(likeMapper.findLikedTargetIds(currentUserId, Like.TARGET_COMMENT, commentIds));
        }

        for (Comment c : comments) {
            c.setUser(userMapper.selectById(c.getUserId()));
            c.setLikeCount(likeMapper.countByTarget(Like.TARGET_COMMENT, c.getId()));
            c.setLiked(likedIds.contains(c.getId()));
        }

        // Build nested tree
        Map<Long, Comment> commentMap = new LinkedHashMap<>();
        List<Comment> roots = new ArrayList<>();
        for (Comment c : comments) {
            commentMap.put(c.getId(), c);
            c.setChildren(new ArrayList<>());
        }
        for (Comment c : comments) {
            if (c.getParentId() == null || c.getParentId() == 0) {
                roots.add(c);
            } else {
                Comment parent = commentMap.get(c.getParentId());
                if (parent != null) {
                    parent.getChildren().add(c);
                } else {
                    roots.add(c);
                }
            }
        }
        return roots;
    }
}
