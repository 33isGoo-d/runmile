package com.runmile.completion.controller;

import com.runmile.completion.dto.CompletionResponse;
import com.runmile.completion.service.CompletionService;
import com.runmile.infrastructure.blockchain.BlockchainVerificationPort;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/runners")
public class CompletionController {
    private final CompletionService completionService;
    private final BlockchainVerificationPort blockchainVerificationPort;

    public CompletionController(
            CompletionService completionService,
            BlockchainVerificationPort blockchainVerificationPort
    ) {
        this.completionService = completionService;
        this.blockchainVerificationPort = blockchainVerificationPort;
    }

    @GetMapping("/{runnerId}/completion")
    public CompletionResponse getCompletion(@PathVariable Long runnerId) {
        return completionService.getCompletion(runnerId);
    }

    @GetMapping("/{runnerId}/nft")
    public Map<String, Object> getNft(@PathVariable Long runnerId) {
        return Map.of(
                "tokenId", "DAEGU-MARATHON-2026-%05d".formatted(runnerId),
                "network", "DAEGU_CHAIN_MOCK",
                "verified", blockchainVerificationPort.isNftVerified(runnerId)
        );
    }
}
