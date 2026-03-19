package com.melonecom.controller;

import com.melonecom.model.dto.ProductAddDTO;
import com.melonecom.model.dto.ProductSearchDTO;
import com.melonecom.model.dto.ProductUpdateDTO;
import com.melonecom.result.Result;
import com.melonecom.service.IProductService;
import com.melonecom.service.MinioService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/product")
public class ProductController {

    private final IProductService productService;
    private final MinioService minioService;

    public ProductController(IProductService productService, MinioService minioService) {
        this.productService = productService;
        this.minioService = minioService;
    }

    /**
     * 前台商品分页（公开接口）
     */
    @GetMapping("/page")
    public Result<?> getProductPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "12") Integer pageSize,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword) {
        ProductSearchDTO dto = new ProductSearchDTO();
        dto.setPage(pageNum);
        dto.setPageSize(pageSize);
        dto.setStatus(1); // 只查上架商品
        dto.setCategoryId(categoryId);
        dto.setKeyword(keyword);
        return productService.getAllProducts(dto);
    }

    /**
     * 商品详情
     */
    @GetMapping("/detail/{productId}")
    public Result<?> getProductDetail(@PathVariable Long productId) {
        return productService.getProductDetail(productId);
    }

    /**
     * 商品列表（后台）
     */
    @PostMapping("/admin/list")
    public Result<?> getAllProducts(@RequestBody ProductSearchDTO dto) {
        return productService.getAllProducts(dto);
    }

    /**
     * 新增商品
     */
    @PostMapping("/admin/add")
    public Result<?> addProduct(@RequestBody ProductAddDTO dto) {
        return productService.addProduct(dto);
    }

    /**
     * 更新商品
     */
    @PostMapping("/admin/update")
    public Result<?> updateProduct(@RequestBody ProductUpdateDTO dto) {
        return productService.updateProduct(dto);
    }

    /**
     * 上传商品主图
     */
    // @PostMapping("/admin/uploadMainImage")
    // public Result<String> uploadMainImage(@RequestParam("file") MultipartFile file,
    //                                       @RequestParam(value = "folder", defaultValue = "product") String folder) {
    //     String url = minioService.uploadFile(file, folder);
    //     return Result.success("upload success", url);
    // }
}
