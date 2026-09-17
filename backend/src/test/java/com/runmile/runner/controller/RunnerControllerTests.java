package com.runmile.runner.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.runmile.global.ApiException;
import com.runmile.global.type.Course;
import com.runmile.runner.dto.RunnerResponse;
import com.runmile.runner.service.RunnerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(RunnerController.class)
class RunnerControllerTests {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RunnerService runnerService;

    @Test
    void 참가자_조회_응답을_반환한다() throws Exception {
        when(runnerService.getRunner(1L))
                .thenReturn(new RunnerResponse(1L, "RUNNER_00001", Course.FULL));

        mockMvc.perform(get("/api/v1/runners/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.runnerCode").value("RUNNER_00001"))
                .andExpect(jsonPath("$.course").value("FULL"));
    }

    @Test
    void 참가자가_없으면_404_오류를_반환한다() throws Exception {
        when(runnerService.getRunner(999L))
                .thenThrow(new ApiException(
                        HttpStatus.NOT_FOUND,
                        "RUNNER_NOT_FOUND",
                        "참가자를 찾을 수 없습니다."
                ));

        mockMvc.perform(get("/api/v1/runners/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RUNNER_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("참가자를 찾을 수 없습니다."));
    }
}
