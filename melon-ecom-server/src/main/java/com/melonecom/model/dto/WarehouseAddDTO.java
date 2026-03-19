package com.melonecom.model.dto;

import lombok.Data;

@Data
public class WarehouseAddDTO {

    /** 仓库编码（唯一） */
    private String code;

    /** 仓库名称 */
    private String name;

    /** 省 */
    private String province;

    /** 市 */
    private String city;

    /** 区/县 */
    private String district;

    /** 详细地址 */
    private String address;

    /** 联系人 */
    private String contactName;

    /** 联系电话 */
    private String contactPhone;

    /** 状态：1-启用 0-停用 */
    private Integer status;
}
