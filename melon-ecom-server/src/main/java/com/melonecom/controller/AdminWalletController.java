package com.melonecom.controller;

import com.melonecom.model.dto.WalletAdjustDTO;
import com.melonecom.result.Result;
import com.melonecom.service.IWalletService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/wallet")
public class AdminWalletController {

    private final IWalletService walletService;

    public AdminWalletController(IWalletService walletService) {
        this.walletService = walletService;
    }

    /** 查询指定用户余额 */
    @GetMapping("/balance/{userId}")
    public Result<?> balance(@PathVariable Long userId) {
        return walletService.getBalance(userId);
    }

    /** 调整余额（ADD / SUB） */
    @PostMapping("/adjust")
    public Result<?> adjust(@RequestBody @Valid WalletAdjustDTO dto) {
        return walletService.adminAdjust(dto.getUserId(), dto.getAmount(), dto.getType(), dto.getRemark());
    }
}
