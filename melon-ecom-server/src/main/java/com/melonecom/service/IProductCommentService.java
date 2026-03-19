package com.melonecom.service;

import com.melonecom.model.dto.*;
import com.melonecom.model.entity.ProductComment;
import com.melonecom.model.vo.CommentVO;
import com.melonecom.result.PageResult;
import com.melonecom.result.Result;

public interface IProductCommentService {

    // 前台
    Result<?> addComment(Long userId, CommentAddDTO dto);

    Result<PageResult<CommentVO>> getProductComments(ProductCommentSearchDTO dto);

    // 后台
    Result<PageResult<ProductComment>> getAllComments(AdminCommentSearchDTO dto);

    Result<?> hideComment(Long commentId);   // status=0
    Result<?> showComment(Long commentId);   // status=1

    Result<?> reply(Long adminUserId, CommentReplyDTO dto);

    // 点赞（简化：只加计数，不做明细）
    Result<?> like(Long commentId);
}
