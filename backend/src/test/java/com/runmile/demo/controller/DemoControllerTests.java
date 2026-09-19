package com.runmile.demo.controller;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.runmile.demo.service.DemoResetService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(DemoController.class)
@TestPropertySource(properties = "runmile.demo-reset.enabled=true")
class DemoControllerTests {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DemoResetService demoResetService;

    @Test
    void 확인_문구가_맞으면_데모를_초기화한다() throws Exception {
        mockMvc.perform(post("/api/v1/demo/reset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"confirmation\":\"RESET\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reset").value(true));

        verify(demoResetService).reset();
    }

    @Test
    void 확인_문구가_틀리면_요청을_거부한다() throws Exception {
        mockMvc.perform(post("/api/v1/demo/reset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"confirmation\":\"START\"}"))
                .andExpect(status().isBadRequest());

        verify(demoResetService, never()).reset();
    }
}
