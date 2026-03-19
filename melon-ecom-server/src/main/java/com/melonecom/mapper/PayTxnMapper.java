package com.melonecom.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.melonecom.model.entity.PayTxn;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;

@Mapper
public interface PayTxnMapper extends BaseMapper<PayTxn> {

    /**
     * 幂等插入支付流水（order_no 唯一）
     * 1 = 插入成功
     * 0 = 已存在该 orderNo（幂等命中）
     */
    int insertIgnore(@Param("orderNo") String orderNo,
                     @Param("userId") Long userId,
                     @Param("payType") Integer payType,
                     @Param("amount") BigDecimal amount,
                     @Param("status") Integer status);

    /**
     * CAS 更新支付流水状态：from -> to
     * 1 = 成功更新（状态流转成功）
     * 0 = 状态不匹配（重复支付/重复回调/并发冲突）
     */
    int updateStatusCas(@Param("orderNo") String orderNo,
                        @Param("fromStatus") Integer fromStatus,
                        @Param("toStatus") Integer toStatus);

    /**
     * 按 orderNo 查询支付流水（唯一）
     */
    PayTxn selectByOrderNo(@Param("orderNo") String orderNo);
}
