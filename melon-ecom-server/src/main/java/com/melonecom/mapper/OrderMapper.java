package com.melonecom.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.melonecom.model.entity.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface OrderMapper extends BaseMapper<Order> {

    /** 按订单号查询（用于支付回调/查单） */
    Order selectByOrderNo(@Param("orderNo") String orderNo);

    /** 用户订单列表（不分页，简单场景） */
    List<Order> selectByUserId(@Param("userId") Long userId);

    /**
     * 支付成功：状态 0 -> 1（幂等 + 状态机安全）
     * 只允许从“未支付”变为“已支付”
     * @return 影响行数（1=成功，0=失败/已处理）
     */
    int markPaid(@Param("orderNo") String orderNo,
                 @Param("payTime") LocalDateTime payTime);

    /**
     * 取消订单：状态 0 -> 2（未支付才能取消）
     * @return 影响行数
     */
    int cancelUnpaid(@Param("orderNo") String orderNo);

    /**
     * 批量取消超时未支付订单（定时任务用）
     * @param beforeTime 截止时间（如 now - 30min）
     * @return 影响行数
     */
    int cancelTimeoutUnpaid(@Param("beforeTime") LocalDateTime beforeTime);
}
