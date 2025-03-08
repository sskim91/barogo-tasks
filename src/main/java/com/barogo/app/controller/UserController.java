package com.barogo.app.controller;

import com.barogo.app.dto.request.LoginRequestDto;
import com.barogo.app.dto.request.SignUpRequestDto;
import com.barogo.app.dto.response.ApiResponse;
import com.barogo.app.dto.response.TokenResponseDto;
import com.barogo.app.dto.response.UserInfoResponseDto;
import com.barogo.app.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author sskim
 */
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 회원 가입 API
     *
     * @param signUpRequest 회원 가입 요청 DTO
     * @return 생성된 사용자 정보
     */
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<UserInfoResponseDto>> signUp(@Valid @RequestBody SignUpRequestDto signUpRequest) {

        UserInfoResponseDto userInfo = userService.signUp(signUpRequest);

        return ResponseEntity.ok(
                ApiResponse.success(userInfo, "회원 가입이 성공적으로 완료되었습니다.")
        );
    }

    /**
     * 로그인 API
     *
     * @param loginRequest 로그인 요청 DTO
     * @return 토큰 정보
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<TokenResponseDto>> login(@Valid @RequestBody LoginRequestDto loginRequest) {

        TokenResponseDto token = userService.login(loginRequest);

        return ResponseEntity.ok(
                ApiResponse.success(token, "로그인이 성공적으로 완료되었습니다.")
        );
    }
}
