package com.runmile.completion.controller;

import com.runmile.completion.dto.CompletionResponse;
import com.runmile.completion.dto.NftVerificationResponse;
import com.runmile.completion.service.CompletionService;
import com.runmile.completion.service.NftVerificationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/runners")
public class CompletionController {
    private final CompletionService completionService;
    private final NftVerificationService nftVerificationService;

    public CompletionController(
            CompletionService completionService,
            NftVerificationService nftVerificationService
    ) {
        this.completionService = completionService;
        this.nftVerificationService = nftVerificationService;
    }

    @GetMapping("/{runnerId}/completion")
    public CompletionResponse getCompletion(@PathVariable Long runnerId) {
        return completionService.getCompletion(runnerId);
    }

    @GetMapping("/{runnerId}/nft")
    public NftVerificationResponse getNft(@PathVariable Long runnerId) {
        return nftVerificationService.getNftVerification(runnerId);
    }
}
