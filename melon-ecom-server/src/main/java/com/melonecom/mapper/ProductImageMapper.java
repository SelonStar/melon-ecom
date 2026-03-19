package com.melonecom.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.melonecom.model.entity.ProductImage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ProductImageMapper extends BaseMapper<ProductImage> {
    List<String> selectImageUrls(@Param("productId") Long productId);
    int deleteByProductId(@Param("productId") Long productId);
}
