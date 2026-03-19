package com.melonecom.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.melonecom.mapper.CategoryMapper;
import com.melonecom.model.entity.Category;
import com.melonecom.model.vo.CategoryVO;
import com.melonecom.result.Result;
import com.melonecom.service.ICategoryService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements ICategoryService {

    private static final String LEGACY_CATEGORY_NAME = "图书音像";
    private static final String TARGET_CATEGORY_NAME = "时尚潮玩";

    private final CategoryMapper categoryMapper;

    public CategoryServiceImpl(CategoryMapper categoryMapper) {
        this.categoryMapper = categoryMapper;
    }

    @Override
    public Result<List<CategoryVO>> getCategoryTree() {
        // 查询所有启用的分类
        List<Category> categories = categoryMapper.selectList(
                new LambdaQueryWrapper<Category>()
                        .eq(Category::getStatus, 1)
                        .orderByAsc(Category::getSort)
                        .orderByAsc(Category::getCategoryId)
        );

        List<CategoryVO> voList = categories.stream().map(this::toVO).toList();

        // 按 parentId 分组
        Map<Long, List<CategoryVO>> grouped = voList.stream()
                .collect(Collectors.groupingBy(CategoryVO::getParentId));

        // 递归构建树
        List<CategoryVO> tree = buildTree(grouped, 0L);
        return Result.success(tree);
    }

    @Override
    public Result<List<CategoryVO>> getAllCategories() {
        List<Category> categories = categoryMapper.selectList(
                new LambdaQueryWrapper<Category>()
                        .orderByAsc(Category::getSort)
                        .orderByAsc(Category::getCategoryId)
        );

        List<CategoryVO> voList = categories.stream().map(this::toVO).toList();
        return Result.success(voList);
    }

    private List<CategoryVO> buildTree(Map<Long, List<CategoryVO>> grouped, Long parentId) {
        List<CategoryVO> children = grouped.getOrDefault(parentId, new ArrayList<>());
        for (CategoryVO child : children) {
            child.setChildren(buildTree(grouped, child.getCategoryId()));
        }
        return children;
    }

    private CategoryVO toVO(Category category) {
        CategoryVO vo = new CategoryVO();
        BeanUtils.copyProperties(category, vo);
        vo.setName(normalizeCategoryName(vo.getName()));
        return vo;
    }

    private String normalizeCategoryName(String name) {
        if (LEGACY_CATEGORY_NAME.equals(name)) {
            return TARGET_CATEGORY_NAME;
        }
        return name;
    }
}
