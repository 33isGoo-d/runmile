package com.runmile.completion.controller;

import com.runmile.infrastructure.blockchain.BlockchainVerificationPort;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/runners")
public class CompletionController {
    private final BlockchainVerificationPort blockchainVerificationPort;

    public CompletionController(BlockchainVerificationPort blockchainVerificationPort) {
        this.blockchainVerificationPort = blockchainVerificationPort;
    }

    @GetMapping("/{runnerId}/completion")
    public Map<String, Object> getCompletion(@PathVariable Long runnerId) {
        return Map.of(
                "completed", true,
                "course", "FULL",
                "finishTimeSeconds", 12840,
                "completedAt", "2026-02-22T12:30:00"
        );
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
