package com.barogo.app.docs;

import com.barogo.app.dto.request.LoginRequestDto;
import com.barogo.app.dto.request.SignUpRequestDto;
import com.barogo.app.dto.response.TokenResponseDto;
import com.barogo.app.dto.response.UserInfoResponseDto;
import com.barogo.app.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.JsonFieldType.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ExtendWith(RestDocumentationExtension.class)
public class UserControllerDocTest {

    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @BeforeEach
    void setUp(final WebApplicationContext webApplicationContext,
               final RestDocumentationContextProvider restDocumentation) {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(
                        documentationConfiguration(restDocumentation)
                                .operationPreprocessors()
                                .withRequestDefaults(prettyPrint())
                                .withResponseDefaults(prettyPrint())
                )
                .build();
    }

    @Test
    @DisplayName("회원가입 API 문서화")
    void signUpDocTest() throws Exception {
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
                .andExpect(status().isOk())
                .andDo(print())
                .andDo(document("user-signup",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestFields(
                                fieldWithPath("username").description("사용자 ID (4~50자)"),
                                fieldWithPath("password").description("비밀번호 (12자 이상, 영어 대문자, 영어 소문자, 숫자, 특수문자 중 3종류 이상)"),
                                fieldWithPath("name").description("사용자 이름 (2~50자)")
                        ),
                        responseFields(
                                fieldWithPath("success").type(BOOLEAN).description("요청 성공 여부"),
                                fieldWithPath("message").type(STRING).description("응답 메시지"),
                                fieldWithPath("data.id").type(NUMBER).description("사용자 고유 번호"),
                                fieldWithPath("data.username").type(STRING).description("사용자 ID"),
                                fieldWithPath("data.name").type(STRING).description("사용자 이름"),
                                fieldWithPath("data.createdAt").type(STRING).description("계정 생성 시간")
                        )
                ));
    }

    @Test
    @DisplayName("로그인 API 문서화")
    void loginDocTest() throws Exception {
        // given
        LoginRequestDto request = new LoginRequestDto();
        request.setUsername("testuser");
        request.setPassword("StrongPassword123!");

        TokenResponseDto responseDto = new TokenResponseDto("test.jwt.token", "Bearer", 3600L);

        given(userService.login(any(LoginRequestDto.class))).willReturn(responseDto);

        // when & then
        mockMvc.perform(post("/api/v1/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andDo(print())
                .andDo(document("user-login",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestFields(
                                fieldWithPath("username").description("사용자 ID"),
                                fieldWithPath("password").description("비밀번호")
                        ),
                        responseFields(
                                fieldWithPath("success").type(BOOLEAN).description("요청 성공 여부"),
                                fieldWithPath("message").type(STRING).description("응답 메시지"),
                                fieldWithPath("data.accessToken").type(STRING).description("JWT 액세스 토큰"),
                                fieldWithPath("data.tokenType").type(STRING).description("토큰 유형"),
                                fieldWithPath("data.expiresIn").type(NUMBER).description("토큰 만료 시간(초)")
                        )
                ));
    }

}
