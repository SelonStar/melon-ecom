package com.melonecom.model.dto;

import lombok.Data;

@Data
public class WarehouseSearchDTO {

    /** 分页 */
    private Integer page = 1;
    private Integer pageSize = 10;

    /** 状态筛选：1-启用 0-停用 */
    private Integer status;

    /** 关键词（仓库名称/编码模糊匹配） */
    private String keyword;
}
