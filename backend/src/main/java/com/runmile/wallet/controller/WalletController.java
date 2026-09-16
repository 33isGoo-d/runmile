package com.runmile.wallet.controller;

import com.runmile.global.ApiException;
import com.runmile.wallet.dto.RunMileIssueRequest;
import jakarta.validation.Valid;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
    @PostMapping("/{runnerId}/runmile/issue")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> issueRunMile(@PathVariable Long runnerId, @Valid @RequestBody RunMileIssueRequest request) {
        long amount = request.amount();
        if (runnerId != 1L) {
            throw new ApiException(HttpStatus.CONFLICT, "RUNMILE_ALREADY_ISSUED", "이미 완주 RunMile이 지급되었습니다.");
        }
        return Map.of("issuedAmount", amount, "balance", amount);
    }

    @GetMapping("/{runnerId}/wallet")
    public Map<String, Object> getWallet(@PathVariable Long runnerId) {
        return Map.of("balance", 10000, "totalIssued", 10000, "totalUsed", 0);
    }

    @GetMapping("/{runnerId}/runmile/transactions")
    public List<Map<String, Object>> getTransactions(@PathVariable Long runnerId) {
        Map<String, Object> issueTransaction = new LinkedHashMap<>();
        issueTransaction.put("id", 1);
        issueTransaction.put("type", "ISSUE");
        issueTransaction.put("amount", 10000);
        issueTransaction.put("paymentId", null);
        issueTransaction.put("createdAt", "2026-09-18T10:00:00");
        return List.of(issueTransaction);
    }
}
