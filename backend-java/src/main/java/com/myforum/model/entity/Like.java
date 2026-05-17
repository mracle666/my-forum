package com.myforum.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("`like`")
public class Like {
    public static final int TARGET_POST = 1;
    public static final int TARGET_COMMENT = 2;

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Integer targetType;  // 1-帖子 2-回复
    private Long targetId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
