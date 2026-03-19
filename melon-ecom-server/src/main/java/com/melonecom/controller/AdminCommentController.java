package com.melonecom.controller;

import com.melonecom.model.dto.AdminCommentSearchDTO;
import com.melonecom.model.dto.CommentReplyDTO;
import com.melonecom.model.entity.ProductComment;
import com.melonecom.result.PageResult;
import com.melonecom.result.Result;
import com.melonecom.service.IProductCommentService;
import com.melonecom.util.ThreadLocalUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/comment")
public class AdminCommentController {

    @Autowired
    private IProductCommentService productCommentService;

    @PostMapping("/getAllComments")
    public Result<PageResult<ProductComment>> getAll(@RequestBody AdminCommentSearchDTO dto) {
        return productCommentService.getAllComments(dto);
    }

    @PatchMapping("/hide/{id}")
    public Result<?> hide(@PathVariable("id") Long commentId) {
        return productCommentService.hideComment(commentId);
    }

    @PatchMapping("/show/{id}")
    public Result<?> show(@PathVariable("id") Long commentId) {
        return productCommentService.showComment(commentId);
    }

    @PostMapping("/reply")
    public Result<?> reply(@RequestBody CommentReplyDTO dto) {
        Long adminUserId =ThreadLocalUtil.getAdminId();//TODO
        return productCommentService.reply(adminUserId, dto);
    }
}
