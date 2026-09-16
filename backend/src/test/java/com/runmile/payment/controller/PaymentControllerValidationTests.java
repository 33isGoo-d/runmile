package com.runmile.payment.controller;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.runmile.infrastructure.payment.PaymentGatewayPort;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(PaymentController.class)
class PaymentControllerValidationTests {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PaymentGatewayPort paymentGatewayPort;

    @Test
    void rejectsRequestWhenRunnerIdIsMissing() throws Exception {
        mockMvc.perform(post("/api/v1/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "merchantId": 10,
                                  "totalAmount": 35000,
                                  "runmileAmount": 10000
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"))
                .andExpect(jsonPath("$.message").value("runnerId: must not be null"));

        verifyNoInteractions(paymentGatewayPort);
    }

    @Test
    void rejectsRequestWhenTotalAmountIsNotPositive() throws Exception {
        mockMvc.perform(post("/api/v1/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "runnerId": 1,
                                  "merchantId": 10,
                                  "totalAmount": 0,
                                  "runmileAmount": 0
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"))
                .andExpect(jsonPath("$.message").value("totalAmount: must be greater than 0"));

        verifyNoInteractions(paymentGatewayPort);
    }

    @Test
    void rejectsUnreadableJsonWithTheStandardErrorFormat() throws Exception {
        mockMvc.perform(post("/api/v1/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ invalid json }"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"))
                .andExpect(jsonPath("$.message").value("요청 본문을 읽을 수 없습니다."));

        verifyNoInteractions(paymentGatewayPort);
    }
}
