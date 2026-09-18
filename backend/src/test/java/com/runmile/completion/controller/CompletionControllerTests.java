package com.runmile.completion.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.runmile.completion.dto.CompletionResponse;
import com.runmile.completion.dto.NftVerificationResponse;
import com.runmile.completion.service.CompletionService;
import com.runmile.completion.service.NftVerificationService;
import com.runmile.global.ApiException;
import com.runmile.global.type.Course;
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
    private NftVerificationService nftVerificationService;

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
    void NFT_조회_응답을_반환한다() throws Exception {
        when(nftVerificationService.getNftVerification(1L)).thenReturn(new NftVerificationResponse(
                "0x" + "ab".repeat(32),
                "POLYGON_AMOY",
                true
        ));

        mockMvc.perform(get("/api/v1/runners/1/nft"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tokenId").value("0x" + "ab".repeat(32)))
                .andExpect(jsonPath("$.network").value("POLYGON_AMOY"))
                .andExpect(jsonPath("$.verified").value(true));
    }

    @Test
    void NFT_기록이_없으면_404_오류를_반환한다() throws Exception {
        when(nftVerificationService.getNftVerification(999L)).thenThrow(new ApiException(
                HttpStatus.NOT_FOUND,
                "NFT_NOT_FOUND",
                "NFT 기록을 찾을 수 없습니다."
        ));

        mockMvc.perform(get("/api/v1/runners/999/nft"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NFT_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("NFT 기록을 찾을 수 없습니다."));
    }
}
