package com.barogo.app.service;

import com.barogo.app.config.jwt.JwtTokenProvider;
import com.barogo.app.domain.User;
import com.barogo.app.dto.request.LoginRequestDto;
import com.barogo.app.dto.request.SignUpRequestDto;
import com.barogo.app.dto.response.TokenResponseDto;
import com.barogo.app.dto.response.UserInfoResponseDto;
import com.barogo.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 회원 가입 처리
     *
     * @param signUpRequest 회원 가입 요청 DTO
     * @return 생성된 사용자 정보
     * @throws IllegalStateException 이미 사용 중인 사용자 ID인 경우
     */
    @Transactional
    public UserInfoResponseDto signUp(SignUpRequestDto signUpRequest) {
        // 사용자명 중복 확인
        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            throw new IllegalStateException("이미 사용 중인 사용자 ID입니다.");
        }

        // 사용자 엔티티 생성
        User user = User.builder()
                .username(signUpRequest.getUsername())
                .password(passwordEncoder.encode(signUpRequest.getPassword()))
                .name(signUpRequest.getName())
                .build();

        // 사용자 저장
        User savedUser = userRepository.save(user);

        // 응답 DTO 반환
        return UserInfoResponseDto.from(savedUser);
    }

    /**
     * 로그인 처리
     *
     * @param loginRequest 로그인 요청 DTO
     * @return 토큰 정보
     * @throws BadCredentialsException 아이디 또는 비밀번호가 일치하지 않는 경우
     */
    @Transactional(readOnly = true)
    public TokenResponseDto login(LoginRequestDto loginRequest) {
        // 사용자 조회
        User user = userRepository.findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new BadCredentialsException("아이디 또는 비밀번호가 일치하지 않습니다."));

        // 비밀번호 검증
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("아이디 또는 비밀번호가 일치하지 않습니다.");
        }

        // JWT 토큰 생성
        String token = jwtTokenProvider.createToken(user.getUsername());
        Long expiresIn = jwtTokenProvider.getTokenValidityInSeconds();

        // 토큰 정보 반환
        return new TokenResponseDto(token, "Bearer", expiresIn);
    }
}
