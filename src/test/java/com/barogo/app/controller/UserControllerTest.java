package com.barogo.app.controller;

import com.barogo.app.config.TestSecurityConfig;
import com.barogo.app.dto.request.LoginRequestDto;
import com.barogo.app.dto.request.RefreshTokenRequestDto;
import com.barogo.app.dto.request.SignUpRequestDto;
import com.barogo.app.dto.response.TokenResponseDto;
import com.barogo.app.dto.response.UserInfoResponseDto;
import com.barogo.app.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import(TestSecurityConfig.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @Test
    @DisplayName("회원가입 API 테스트")
    void signUpTest() throws Exception {
        // given
        SignUpRequestDto request = new SignUpRequestDto();
        request.setUsername("testuser");
        request.setPassword("StrongPassword123!");
        request.setName("테스트사용자");

        UserInfoResponseDto responseDto = UserInfoResponseDto.builder()
                .id(1L)
                .username("testuser")
                .name("테스트사용자")
                .createdAt(LocalDateTime.now())
                .build();

        given(userService.signUp(any(SignUpRequestDto.class))).willReturn(responseDto);

        // when & then
        mockMvc.perform(post("/api/v1/users/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.username").value("testuser"))
                .andExpect(jsonPath("$.data.name").value("테스트사용자"));
    }

    @Test
    @DisplayName("회원가입 API - 유효하지 않은 요청 테스트")
    void signUpWithInvalidRequestTest() throws Exception {
        // given
        SignUpRequestDto request = new SignUpRequestDto();
        request.setUsername("t"); // 4자 미만으로 유효하지 않음
        request.setPassword("weak"); // 비밀번호 정책 위반
        request.setName(""); // 비어있음

        // when & then
        mockMvc.perform(post("/api/v1/users/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("로그인 API 테스트")
    void loginTest() throws Exception {
        // given
        LoginRequestDto request = new LoginRequestDto();
        request.setUsername("testuser");
        request.setPassword("StrongPassword123!");

        // 변경된 TokenResponseDto에 맞게 응답 객체 생성
        TokenResponseDto responseDto = new TokenResponseDto(
                "test.jwt.token",
                "test.refresh.token",
                "Bearer",
                3600L,
                604800L
        );

        given(userService.login(any(LoginRequestDto.class))).willReturn(responseDto);

        // when & then
        mockMvc.perform(post("/api/v1/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value("test.jwt.token"))
                .andExpect(jsonPath("$.data.refreshToken").value("test.refresh.token"))
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.data.accessTokenExpiresIn").value(3600))
                .andExpect(jsonPath("$.data.refreshTokenExpiresIn").value(604800));
    }

    @Test
    @DisplayName("로그인 API - 유효하지 않은 요청 테스트")
    void loginWithInvalidRequestTest() throws Exception {
        // given
        LoginRequestDto request = new LoginRequestDto();
        request.setUsername(""); // 비어있음
        request.setPassword(""); // 비어있음

        // when & then
        mockMvc.perform(post("/api/v1/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("토큰 갱신 API 테스트")
    void refreshTokenTest() throws Exception {
        // given
        RefreshTokenRequestDto request = new RefreshTokenRequestDto();
        request.setRefreshToken("test.refresh.token");

        TokenResponseDto responseDto = new TokenResponseDto(
                "new.jwt.token",
                "test.refresh.token",
                "Bearer",
                3600L,
                604800L
        );

        given(userService.refreshToken(any(String.class))).willReturn(responseDto);

        // when & then
        mockMvc.perform(post("/api/v1/users/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value("new.jwt.token"))
                .andExpect(jsonPath("$.data.refreshToken").value("test.refresh.token"))
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.data.accessTokenExpiresIn").value(3600))
                .andExpect(jsonPath("$.data.refreshTokenExpiresIn").value(604800));
    }

    @Test
    @DisplayName("로그아웃 API 테스트")
    @WithMockUser(username = "testuser")
    void logoutTest() throws Exception {
        // given
        UserDetails userDetails = User.builder()
                .username("testuser")
                .password("password")
                .authorities(Collections.emptyList())
                .build();

        doNothing().when(userService).logout("testuser");

        // when & then
        mockMvc.perform(post("/api/v1/users/logout")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").exists());
    }
}