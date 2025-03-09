package com.barogo.app.config.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
class JwtAuthenticationEntryPointTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("인증 헤더 없는 요청 시 401 응답")
    public void requestWithoutAuthHeader() throws Exception {
        // 현재 날짜 기준 검색 기간 설정
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startDate = now.minusDays(1);

        // 인증 헤더 없이 보호된 API 요청
        mockMvc.perform(get("/api/v1/deliveries")
                        .param("startDate", startDate.format(DateTimeFormatter.ISO_DATE_TIME))
                        .param("endDate", now.format(DateTimeFormatter.ISO_DATE_TIME))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("잘못된 토큰으로 요청 시 401 응답")
    public void requestWithInvalidToken() throws Exception {
        // 현재 날짜 기준 검색 기간 설정
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startDate = now.minusDays(1);
        LocalDateTime endDate = now;

        // 잘못된 토큰으로 보호된 API 요청
        mockMvc.perform(get("/api/v1/deliveries")
                        .param("startDate", startDate.format(DateTimeFormatter.ISO_DATE_TIME))
                        .param("endDate", endDate.format(DateTimeFormatter.ISO_DATE_TIME))
                        .header("Authorization", "Bearer invalid.token.value")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").exists());
    }
}