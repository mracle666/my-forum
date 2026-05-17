package com.myforum.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("comment")
public class Comment {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long postId;
    private Long userId;
    private Long parentId;    // 0-回复帖子 其他-回复评论
    private Long replyTo;     // 被回复的用户ID
    private String content;
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(exist = false)
    private User user;
    @TableField(exist = false)
    private List<Comment> children;
    @TableField(exist = false)
    private Long likeCount;
    @TableField(exist = false)
    private Boolean liked;
}
