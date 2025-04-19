package com.animesocial.platform.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 创建评论的请求DTO
 */
@Data
public class CreateCommentRequest {
    /**
     * 帖子ID
     */
    @NotNull(message = "帖子ID不能为空")
    private Integer postId;
    
    /**
     * 评论内容
     */
    @NotBlank(message = "评论内容不能为空")
    @Size(max = 500, message = "评论内容不能超过500个字符")
    private String content;
    
    /**
     * 父评论ID
     * 如果是对评论的回复，则此字段不为空
     */
    private Integer parentId;
} 