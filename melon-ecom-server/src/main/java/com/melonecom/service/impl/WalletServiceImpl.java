package com.melonecom.service.impl;

import com.melonecom.mapper.UserWalletMapper;
import com.melonecom.mapper.WalletTxnMapper;
import com.melonecom.model.entity.UserWallet;
import com.melonecom.model.entity.WalletTxn;
import com.melonecom.result.Result;
import com.melonecom.service.IWalletService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class WalletServiceImpl implements IWalletService {

    private final UserWalletMapper userWalletMapper;
    private final WalletTxnMapper walletTxnMapper;

    public WalletServiceImpl(UserWalletMapper userWalletMapper, WalletTxnMapper walletTxnMapper) {
        this.userWalletMapper = userWalletMapper;
        this.walletTxnMapper = walletTxnMapper;
    }

    @Override
    public Result<?> getBalance(Long userId) {
        if (userId == null) return Result.error("未登录");
        UserWallet wallet = userWalletMapper.selectById(userId);
        return Result.success(wallet == null ? BigDecimal.ZERO : wallet.getBalance());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<?> adminAdjust(Long userId, BigDecimal amount, String type, String remark) {
        if (!"ADD".equals(type) && !"SUB".equals(type)) {
            return Result.error("type 只支持 ADD / SUB");
        }

        // 钱包不存在则初始化
        UserWallet wallet = userWalletMapper.selectById(userId);
        if (wallet == null) {
            if ("SUB".equals(type)) return Result.error("该用户尚无钱包，无法扣减");
            wallet = new UserWallet();
            wallet.setUserId(userId);
            wallet.setBalance(BigDecimal.ZERO);
            wallet.setVersion(0);
            userWalletMapper.insert(wallet);
        }

        // 乐观锁重试
        int maxRetry = 5;
        BigDecimal balanceAfter = null;
        for (int i = 0; i < maxRetry; i++) {
            UserWallet latest = userWalletMapper.selectById(userId);
            if (latest == null) return Result.error("钱包不存在");

            int rows;
            if ("ADD".equals(type)) {
                rows = userWalletMapper.addBalance(userId, amount, latest.getVersion());
                if (rows == 1) { balanceAfter = latest.getBalance().add(amount); break; }
            } else {
                if (latest.getBalance().compareTo(amount) < 0) return Result.error("余额不足，无法扣减");
                rows = userWalletMapper.deductBalance(userId, amount, latest.getVersion());
                if (rows == 1) { balanceAfter = latest.getBalance().subtract(amount); break; }
            }
        }
        if (balanceAfter == null) return Result.error("操作失败（并发冲突），请重试");

        // 写流水
        WalletTxn txn = new WalletTxn();
        txn.setUserId(userId);
        txn.setAmount(amount);
        txn.setType(type);
        txn.setBalanceAfter(balanceAfter);
        txn.setRemark(remark);
        walletTxnMapper.insert(txn);

        return Result.success(balanceAfter);
    }
}
