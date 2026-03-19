package com.melonecom.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.melonecom.model.entity.Product;
import com.melonecom.model.vo.ProductAdminVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ProductMapper extends BaseMapper<Product> {
    Long countProducts(@Param("status") Integer status,
                       @Param("categoryId") Long categoryId,
                       @Param("brandId") Long brandId,
                       @Param("keywordList") List<String> keywordList);

    List<ProductAdminVO> selectProductsPage(@Param("status") Integer status,
                                            @Param("categoryId") Long categoryId,
                                            @Param("brandId") Long brandId,
                                            @Param("keywordList") List<String> keywordList,
                                            @Param("offset") Integer offset,
                                            @Param("limit") Integer limit);
    int increaseSalesCount(@Param("productId") Long productId,
                           @Param("quantity") Integer quantity);
    int incrProductCommentStats(@Param("productId") Long productId,
                            @Param("deltaCount") Integer deltaCount,
                            @Param("deltaRatingSum") Integer deltaRatingSum);

}
