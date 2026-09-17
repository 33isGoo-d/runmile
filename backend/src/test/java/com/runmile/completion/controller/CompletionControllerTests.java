package com.runmile.completion.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.runmile.completion.dto.CompletionResponse;
import com.runmile.completion.service.CompletionService;
import com.runmile.global.ApiException;
import com.runmile.global.type.Course;
import com.runmile.infrastructure.blockchain.BlockchainVerificationPort;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(CompletionController.class)
class CompletionControllerTests {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CompletionService completionService;

    @MockBean
    private BlockchainVerificationPort blockchainVerificationPort;

    @Test
    void 완주_조회_응답을_반환한다() throws Exception {
        when(completionService.getCompletion(1L)).thenReturn(new CompletionResponse(
                true,
                Course.FULL,
                12840,
                LocalDateTime.parse("2026-02-22T12:30:00")
        ));

        mockMvc.perform(get("/api/v1/runners/1/completion"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.completed").value(true))
                .andExpect(jsonPath("$.course").value("FULL"))
                .andExpect(jsonPath("$.finishTimeSeconds").value(12840))
                .andExpect(jsonPath("$.completedAt").value("2026-02-22T12:30:00"));
    }

    @Test
    void 완주_기록이_없으면_404_오류를_반환한다() throws Exception {
        when(completionService.getCompletion(999L)).thenThrow(new ApiException(
                HttpStatus.NOT_FOUND,
                "COMPLETION_NOT_FOUND",
                "완주 기록을 찾을 수 없습니다."
        ));

        mockMvc.perform(get("/api/v1/runners/999/completion"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("COMPLETION_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("완주 기록을 찾을 수 없습니다."));
    }

    @Test
    void 기존_NFT_조회_동작을_유지한다() throws Exception {
        when(blockchainVerificationPort.isNftVerified(1L)).thenReturn(true);

        mockMvc.perform(get("/api/v1/runners/1/nft"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tokenId").value("DAEGU-MARATHON-2026-00001"))
                .andExpect(jsonPath("$.network").value("DAEGU_CHAIN_MOCK"))
                .andExpect(jsonPath("$.verified").value(true));
    }
}
