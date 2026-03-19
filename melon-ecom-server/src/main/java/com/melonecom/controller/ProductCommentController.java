package com.melonecom.controller;

import com.melonecom.model.dto.CommentAddDTO;
import com.melonecom.model.dto.ProductCommentSearchDTO;
import com.melonecom.model.vo.CommentVO;
import com.melonecom.result.PageResult;
import com.melonecom.result.Result;
import com.melonecom.service.IProductCommentService;
import com.melonecom.util.ThreadLocalUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/comment")
public class ProductCommentController {

    @Autowired
    private IProductCommentService productCommentService;

    @PostMapping("/add")
    public Result<?> add(@RequestBody CommentAddDTO dto) {
        Long userId =  ThreadLocalUtil.getUserId();
        return productCommentService.addComment(userId, dto);
    }

    @PostMapping("/list")
    public Result<PageResult<CommentVO>> list(@RequestBody ProductCommentSearchDTO dto) {
        return productCommentService.getProductComments(dto);
    }

    @PostMapping("/like/{id}")
    public Result<?> like(@PathVariable("id") Long commentId) {
        return productCommentService.like(commentId);
    }
}
