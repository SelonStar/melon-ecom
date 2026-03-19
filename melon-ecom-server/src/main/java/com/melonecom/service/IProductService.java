package com.melonecom.service;

import com.melonecom.model.dto.*;
import com.melonecom.model.vo.ProductAdminDetailVO;
import com.melonecom.model.vo.ProductAdminVO;
import com.melonecom.result.PageResult;
import com.melonecom.result.Result;

public interface IProductService {

    Result<Long> getAllProductsCount(Integer status, Long categoryId, Long brandId, String keyword);

    Result<PageResult<ProductAdminVO>> getAllProducts(ProductSearchDTO dto);

    Result<ProductAdminDetailVO> getProductDetail(Long productId);

    Result<?> addProduct(ProductAddDTO dto);

    Result<?> updateProduct(ProductUpdateDTO dto);

    Result<?> updateProductStatus(Long productId, Integer status);

    Result<?> deleteProduct(Long productId);

    // SKU
    Result<?> addSku(SkuAddDTO dto);

    Result<?> updateSku(SkuUpdateDTO dto);

    Result<?> updateSkuStatus(Long skuId, Integer status);

    Result<?> deleteSku(Long skuId);
}
