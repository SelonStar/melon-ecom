package com.melonecom.controller;

import com.melonecom.model.dto.*;
import com.melonecom.model.vo.ProductAdminDetailVO;
import com.melonecom.model.vo.ProductAdminVO;
import com.melonecom.result.PageResult;
import com.melonecom.result.Result;
import com.melonecom.service.IProductService;
import com.melonecom.service.MinioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/admin/product")
public class AdminProductController {

    @Autowired
    private IProductService productService;

    @Autowired
    private MinioService minioService;

    @GetMapping("/getAllProductsCount")
    public Result<Long> getAllProductsCount(@RequestParam(required = false) Integer status,
                                            @RequestParam(required = false) Long categoryId,
                                            @RequestParam(required = false) Long brandId,
                                            @RequestParam(required = false) String keyword) {
        return productService.getAllProductsCount(status, categoryId, brandId, keyword);
    }

    @PostMapping("/getAllProducts")
    public Result<PageResult<ProductAdminVO>> getAllProducts(@RequestBody ProductSearchDTO dto) {
        return productService.getAllProducts(dto);
    }

    @GetMapping("/detail/{id}")
    public Result<ProductAdminDetailVO> getProductDetail(@PathVariable("id") Long productId) {
        return productService.getProductDetail(productId);
    }

    @PostMapping("/addProduct")
    public Result<?> addProduct(@RequestBody ProductAddDTO dto) {
        return productService.addProduct(dto);
    }

    @PutMapping("/updateProduct")
    public Result<?> updateProduct(@RequestBody ProductUpdateDTO dto) {
        return productService.updateProduct(dto);
    }

    @PatchMapping("/updateProductStatus/{id}")
    public Result<?> updateProductStatus(@PathVariable("id") Long productId,
                                         @RequestParam("status") Integer status) {
        return productService.updateProductStatus(productId, status);
    }

    @DeleteMapping("/deleteProduct/{id}")
    public Result<?> deleteProduct(@PathVariable("id") Long productId) {
        return productService.deleteProduct(productId);
    }

    // SKU
    @PostMapping("/addSku")
    public Result<?> addSku(@RequestBody SkuAddDTO dto) {
        return productService.addSku(dto);
    }

    @PutMapping("/updateSku")
    public Result<?> updateSku(@RequestBody SkuUpdateDTO dto) {
        return productService.updateSku(dto);
    }

    @PatchMapping("/updateSkuStatus/{id}")
    public Result<?> updateSkuStatus(@PathVariable("id") Long skuId,
                                     @RequestParam("status") Integer status) {
        return productService.updateSkuStatus(skuId, status);
    }

    @DeleteMapping("/deleteSku/{id}")
    public Result<?> deleteSku(@PathVariable("id") Long skuId) {
        return productService.deleteSku(skuId);
    }

    @PostMapping("/uploadMainImage")
    public Result<String> uploadMainImage(@RequestParam("file") MultipartFile file,
                                          @RequestParam(value = "folder", defaultValue = "products") String folder) {
        String url = minioService.uploadFile(file, folder);
        return Result.success("upload success", url);
    }
}
