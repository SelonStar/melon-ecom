package com.melonecom.model.vo;

import lombok.Data;

import java.util.List;

@Data
public class CategoryVO {
    private Long categoryId;
    private Long parentId;
    private String name;
    private Integer sort;
    private Integer status;
    private List<CategoryVO> children;
}
