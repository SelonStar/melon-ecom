package com.melonecom.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.melonecom.model.entity.UserWallet;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;

@Mapper
public interface UserWalletMapper extends BaseMapper<UserWallet> {

    /**
     * 扣减余额（乐观锁 + 防止扣成负数）
     *
     * @param userId  用户ID（主键）
     * @param amount  扣减金额
     * @param version 当前版本号
     * @return 影响行数（1=成功，0=失败：余额不足或版本冲突）
     */
    @Update("""
        UPDATE tb_user_wallet
        SET balance = balance - #{amount},
            version = version + 1
        WHERE user_id = #{userId}
          AND version = #{version}
          AND (balance - #{amount}) >= 0
    """)
    int deductBalance(@Param("userId") Long userId,
                      @Param("amount") BigDecimal amount,
                      @Param("version") Integer version);

    @Update("""
        UPDATE tb_user_wallet
        SET balance = balance + #{amount},
            version = version + 1,
            update_time = NOW()
        WHERE user_id = #{userId}
          AND version = #{version}
    """)
    int addBalance(@Param("userId") Long userId,
                   @Param("amount") BigDecimal amount,
                   @Param("version") Integer version);
}
