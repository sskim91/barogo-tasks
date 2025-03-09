package com.barogo.app.service;

import com.barogo.app.config.jwt.JwtTokenProvider;
import com.barogo.app.domain.RefreshToken;
import com.barogo.app.domain.User;
import com.barogo.app.dto.request.LoginRequestDto;
import com.barogo.app.dto.request.SignUpRequestDto;
import com.barogo.app.dto.response.TokenResponseDto;
import com.barogo.app.dto.response.UserInfoResponseDto;
import com.barogo.app.repository.RefreshTokenRepository;
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
    private final RefreshTokenRepository refreshTokenRepository;
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

        // JWT 액세스 토큰 생성
        String accessToken = jwtTokenProvider.createToken(user.getUsername());
        Long accessTokenExpiresIn = jwtTokenProvider.getTokenValidityInSeconds();

        // 리프레시 토큰 생성
        String refreshToken = jwtTokenProvider.createRefreshToken();
        Long refreshTokenExpiresIn = jwtTokenProvider.getRefreshTokenValidityInSeconds();


        // 기존 리프레시 토큰이 있으면 삭제
        refreshTokenRepository.findByUsername(user.getUsername())
                .ifPresent(token -> refreshTokenRepository.delete(token));

        // 새 리프레시 토큰 저장
        RefreshToken refreshTokenEntity = RefreshToken.builder()
                .token(refreshToken)
                .username(user.getUsername())
                .expiryDate(jwtTokenProvider.getExpiryDateFromToken(refreshToken))
                .build();

        refreshTokenRepository.save(refreshTokenEntity);

        // 토큰 정보 반환
        return new TokenResponseDto(accessToken, refreshToken, "Bearer", accessTokenExpiresIn, refreshTokenExpiresIn);
    }

    // 토큰 갱신 메서드 추가
    @Transactional
    public TokenResponseDto refreshToken(String refreshToken) {
        // 리프레시 토큰 유효성 검증
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new IllegalArgumentException("유효하지 않은 리프레시 토큰입니다.");
        }

        // 데이터베이스에서 리프레시 토큰 조회
        RefreshToken refreshTokenEntity = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 리프레시 토큰입니다."));

        // 토큰 만료 확인
        if (refreshTokenEntity.isExpired()) {
            refreshTokenRepository.delete(refreshTokenEntity);
            throw new IllegalArgumentException("만료된 리프레시 토큰입니다. 다시 로그인해주세요.");
        }

        // 새 액세스 토큰 생성
        String username = refreshTokenEntity.getUsername();
        String newAccessToken = jwtTokenProvider.createToken(username);
        Long accessTokenExpiresIn = jwtTokenProvider.getTokenValidityInSeconds();
        Long refreshTokenExpiresIn = jwtTokenProvider.getRefreshTokenValidityInSeconds();

        // 응답 반환 (리프레시 토큰은 재사용, 갱신 정보 포함)
        return new TokenResponseDto(
                newAccessToken,
                refreshToken,
                "Bearer",
                accessTokenExpiresIn,
                refreshTokenExpiresIn
        );
    }

    // 로그아웃 메서드 추가
    @Transactional
    public void logout(String username) {
        refreshTokenRepository.findByUsername(username)
                .ifPresent(refreshTokenRepository::delete);
    }
}
