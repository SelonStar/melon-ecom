package com.melonecom.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.melonecom.model.entity.Sku;
import com.melonecom.model.vo.MockOrderSkuCandidateVO;
import com.melonecom.model.vo.TryOnCandidateSkuVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SkuMapper extends BaseMapper<Sku> {
    List<Sku> selectSkusByProductId(@Param("productId") Long productId);
    Integer existsSkuCode(@Param("skuCode") String skuCode,
                          @Param("excludeId") Long excludeId);
    List<MockOrderSkuCandidateVO> selectMockOrderSkuCandidates();
    int increaseSalesCount(@Param("skuId") Long skuId,
                           @Param("quantity") Integer quantity);

    Long countTryOnCandidateSkus(@Param("slotType") String slotType,
                                 @Param("keyword") String keyword);

    List<TryOnCandidateSkuVO> selectTryOnCandidateSkus(@Param("slotType") String slotType,
                                                       @Param("keyword") String keyword,
                                                       @Param("offset") Integer offset,
                                                       @Param("limit") Integer limit);
}
