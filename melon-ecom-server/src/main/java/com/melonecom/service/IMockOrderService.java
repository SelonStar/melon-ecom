package com.melonecom.service;

import com.melonecom.result.Result;

public interface IMockOrderService {

    /**
     * 该接口用于本地模拟订单数据，生成销量数据，为首页推荐和热销排序提供数据支撑。
     */
    Result<?> mockOrder(Integer count);
}
