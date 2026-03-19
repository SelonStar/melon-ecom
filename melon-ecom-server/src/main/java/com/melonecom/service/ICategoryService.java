package com.melonecom.service;

import com.melonecom.model.vo.CategoryVO;
import com.melonecom.result.Result;

import java.util.List;

public interface ICategoryService {

    /**
     * 获取分类树（递归构建）
     */
    Result<List<CategoryVO>> getCategoryTree();

    /**
     * 获取所有分类（扁平列表）
     */
    Result<List<CategoryVO>> getAllCategories();
}
