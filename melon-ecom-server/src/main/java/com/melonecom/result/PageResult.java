package com.melonecom.result;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

// 分页返回结果对象
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> {

    private Long total; //总条数
    private List<T> records; //当前页数据集合

}
