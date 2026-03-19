package com.melonecom.model.dto;
import lombok.Data;

@Data
public class CommentReplyDTO {
    private Long parentId;   // 被回复的评论 id
    private String content;
}
