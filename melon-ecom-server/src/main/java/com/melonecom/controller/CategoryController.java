package com.melonecom.controller;

import com.melonecom.model.vo.CategoryVO;
import com.melonecom.result.Result;
import com.melonecom.service.ICategoryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class CategoryController {

    private final ICategoryService categoryService;

    public CategoryController(ICategoryService categoryService) {
        this.categoryService = categoryService;
    }

    /**
     * 获取分类树（公开接口，前台/后台共用）
     */
    @GetMapping("/category/tree")
    public Result<List<CategoryVO>> getCategoryTree() {
        return categoryService.getCategoryTree();
    }

    /**
     * 获取所有分类（管理端，含禁用的）
     */
    @GetMapping("/admin/category/list")
    public Result<List<CategoryVO>> getAllCategories() {
        return categoryService.getAllCategories();
    }
}
