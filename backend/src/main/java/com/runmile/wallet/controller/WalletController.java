package com.runmile.wallet.controller;

import com.runmile.wallet.dto.RunMileIssueResponse;
import com.runmile.wallet.dto.RunMileIssueRequest;
import com.runmile.wallet.dto.RunMileTransactionResponse;
import com.runmile.wallet.dto.WalletResponse;
import com.runmile.wallet.service.WalletService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/runners")
public class WalletController {
    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    @PostMapping("/{runnerId}/runmile/issue")
    @ResponseStatus(HttpStatus.CREATED)
    public RunMileIssueResponse issueRunMile(
            @PathVariable Long runnerId,
            @Valid @RequestBody RunMileIssueRequest request
    ) {
        return walletService.issue(runnerId, request.amount());
    }

    @GetMapping("/{runnerId}/wallet")
    public WalletResponse getWallet(@PathVariable Long runnerId) {
        return walletService.getWallet(runnerId);
    }

    @GetMapping("/{runnerId}/runmile/transactions")
    public List<RunMileTransactionResponse> getTransactions(@PathVariable Long runnerId) {
        return walletService.getTransactions(runnerId);
    }
}
