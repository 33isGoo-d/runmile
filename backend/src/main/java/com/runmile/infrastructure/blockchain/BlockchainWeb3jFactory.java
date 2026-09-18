package com.runmile.infrastructure.blockchain;

import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;

final class BlockchainWeb3jFactory {
    private BlockchainWeb3jFactory() {
    }

    static Web3j create(String rpcUrl, long timeoutSeconds) {
        if (rpcUrl == null || rpcUrl.isBlank()) {
            throw new IllegalArgumentException("DAEGU_CHAIN_RPC_URL은 필수입니다.");
        }
        if (timeoutSeconds <= 0) {
            throw new IllegalArgumentException("DAEGU_CHAIN_RPC_TIMEOUT_SECONDS는 1 이상이어야 합니다.");
        }

        OkHttpClient client = HttpService.getOkHttpClientBuilder()
                .connectTimeout(timeoutSeconds, TimeUnit.SECONDS)
                .readTimeout(timeoutSeconds, TimeUnit.SECONDS)
                .writeTimeout(timeoutSeconds, TimeUnit.SECONDS)
                .callTimeout(timeoutSeconds * 2, TimeUnit.SECONDS)
                .build();
        return Web3j.build(new HttpService(rpcUrl, client));
    }
}
