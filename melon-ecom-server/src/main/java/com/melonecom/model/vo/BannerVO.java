package com.melonecom.model.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class BannerVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long bannerId;

    private String title;

    private String bannerUrl;

    private String linkUrl;

    /** 跳转类型：NONE / PRODUCT / CATEGORY */
    private String jumpType;

    /** 跳转目标 ID */
    private Long jumpTargetId;

    /** 跳转目标名称（商品名 / 分类名），由 Service 层填充 */
    private String jumpTargetName;

    /** 状态：1启用 0禁用 */
    private Integer status;
}
