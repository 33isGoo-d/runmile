package com.runmile.completion.dto;

import com.runmile.completion.domain.NftRecord;

public record NftVerificationResponse(String tokenId, String network, boolean verified) {
    public static NftVerificationResponse from(NftRecord nftRecord, boolean verified) {
        return new NftVerificationResponse(
                nftRecord.getNftTokenId(),
                nftRecord.getNetwork(),
                verified
        );
    }
}
