package com.runmile.wallet.dto;

import com.runmile.wallet.domain.RunMileWallet;

public record WalletResponse(long balance, long totalIssued, long totalUsed) {
    public static WalletResponse from(RunMileWallet wallet) {
        return new WalletResponse(wallet.getBalance(), wallet.getTotalIssued(), wallet.getTotalUsed());
    }
}
