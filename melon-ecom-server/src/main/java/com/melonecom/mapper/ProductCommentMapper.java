package com.melonecom.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.melonecom.model.entity.ProductComment;
import com.melonecom.model.vo.CommentVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ProductCommentMapper extends BaseMapper<ProductComment> {

    Long countProductRootComments(@Param("productId") Long productId);

    List<CommentVO> selectProductRootCommentsPage(@Param("productId") Long productId,
                                                  @Param("orderByLike") boolean orderByLike,
                                                  @Param("offset") Integer offset,
                                                  @Param("limit") Integer limit);

    List<CommentVO> selectRepliesByRootIds(@Param("rootIds") List<Long> rootIds);

    // 后台
    Long countAdminComments(@Param("productId") Long productId,
                            @Param("userId") Long userId,
                            @Param("status") Integer status,
                            @Param("keyword") String keyword);

    List<ProductComment> selectAdminCommentsPage(@Param("productId") Long productId,
                                                 @Param("userId") Long userId,
                                                 @Param("status") Integer status,
                                                 @Param("keyword") String keyword,
                                                 @Param("offset") Integer offset,
                                                 @Param("limit") Integer limit);

    // 聚合：只统计“显示的主评论”
    Long countVisibleRootComments(@Param("productId") Long productId);

    Double avgVisibleRootRating(@Param("productId") Long productId);

    // 状态变更
    int updateCommentStatus(@Param("commentId") Long commentId,
                            @Param("status") Integer status);
}
