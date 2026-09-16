package com.runmile.infrastructure.blockchain;

import org.springframework.stereotype.Component;

@Component
public class MockDaeguChainAdapter implements BlockchainVerificationPort {
    @Override
    public boolean isNftVerified(Long runnerId) {
        return runnerId != null && runnerId > 0;
    }
}

