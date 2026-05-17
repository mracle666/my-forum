package com.myforum.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CommentRequest {
    @NotNull(message = "帖子ID不能为空")
    private Long postId;

    private Long parentId = 0L;
    private Long replyTo = 0L;

    @NotBlank(message = "回复内容不能为空")
    private String content;
}
