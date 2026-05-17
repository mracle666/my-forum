package com.myforum.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("notification")
public class Notification {
    public static final int TYPE_COMMENT = 1;
    public static final int TYPE_LIKE = 2;
    public static final int TYPE_SYSTEM = 3;
    public static final int TYPE_MESSAGE = 4;

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long fromId;
    private Integer type;       // 1-回复 2-点赞 3-系统通知 4-私信
    private String content;
    private Long targetId;
    private Integer isRead;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(exist = false)
    private User fromUser;
}
