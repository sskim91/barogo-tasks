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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("회원가입 성공 테스트")
    void signUpSuccess() {
        // given
        SignUpRequestDto request = new SignUpRequestDto();
        request.setUsername("testuser");
        request.setPassword("StrongPassword123!");
        request.setName("테스트사용자");

        User savedUser = User.builder()
                .username(request.getUsername())
                .password("encodedPassword")
                .name(request.getName())
                .build();

        given(userRepository.existsByUsername(anyString())).willReturn(false);
        given(passwordEncoder.encode(anyString())).willReturn("encodedPassword");
        given(userRepository.save(any(User.class))).willReturn(savedUser);

        // when
        UserInfoResponseDto result = userService.signUp(request);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo(request.getUsername());
        assertThat(result.getName()).isEqualTo(request.getName());

        verify(userRepository, times(1)).existsByUsername(anyString());
        verify(passwordEncoder, times(1)).encode(anyString());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("중복된 사용자명으로 회원가입 시 예외 발생")
    void signUpWithDuplicateUsername() {
        // given
        SignUpRequestDto request = new SignUpRequestDto();
        request.setUsername("existinguser");
        request.setPassword("StrongPassword123!");
        request.setName("테스트사용자");

        given(userRepository.existsByUsername(anyString())).willReturn(true);

        // when & then
        assertThrows(IllegalStateException.class, () -> userService.signUp(request));

        verify(userRepository, times(1)).existsByUsername(anyString());
        verify(userRepository, times(0)).save(any(User.class));
    }

    @Test
    @DisplayName("로그인 성공 테스트")
    void loginSuccess() {
        // given
        LoginRequestDto request = new LoginRequestDto();
        request.setUsername("testuser");
        request.setPassword("StrongPassword123!");

        User user = User.builder()
                .username(request.getUsername())
                .password("encodedPassword")
                .name("테스트사용자")
                .build();

        given(userRepository.findByUsername(anyString())).willReturn(Optional.of(user));
        given(passwordEncoder.matches(anyString(), anyString())).willReturn(true);
        given(jwtTokenProvider.createToken(anyString())).willReturn("test.jwt.token");
        given(jwtTokenProvider.getTokenValidityInSeconds()).willReturn(3600L);
        given(jwtTokenProvider.createRefreshToken()).willReturn("test.refresh.token");
        given(jwtTokenProvider.getRefreshTokenValidityInSeconds()).willReturn(604800L);
        given(jwtTokenProvider.getExpiryDateFromToken("test.refresh.token"))
                .willReturn(LocalDateTime.now().plusDays(7));

        // when
        TokenResponseDto result = userService.login(request);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getAccessToken()).isEqualTo("test.jwt.token");
        assertThat(result.getRefreshToken()).isEqualTo("test.refresh.token");
        assertThat(result.getTokenType()).isEqualTo("Bearer");
        assertThat(result.getAccessTokenExpiresIn()).isEqualTo(3600L);
        assertThat(result.getRefreshTokenExpiresIn()).isEqualTo(604800L);


        verify(userRepository, times(1)).findByUsername(anyString());
        verify(passwordEncoder, times(1)).matches(anyString(), anyString());
        verify(jwtTokenProvider, times(1)).createToken(anyString());
        verify(jwtTokenProvider, times(1)).createRefreshToken();
        verify(refreshTokenRepository, times(1)).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("존재하지 않는 사용자로 로그인 시 예외 발생")
    void loginWithNonExistingUser() {
        // given
        LoginRequestDto request = new LoginRequestDto();
        request.setUsername("nonexistinguser");
        request.setPassword("StrongPassword123!");

        given(userRepository.findByUsername(anyString())).willReturn(Optional.empty());

        // when & then
        assertThrows(BadCredentialsException.class, () -> userService.login(request));

        verify(userRepository, times(1)).findByUsername(anyString());
        verify(passwordEncoder, times(0)).matches(anyString(), anyString());
        verify(jwtTokenProvider, times(0)).createToken(anyString());
    }

    @Test
    @DisplayName("잘못된 비밀번호로 로그인 시 예외 발생")
    void loginWithIncorrectPassword() {
        // given
        LoginRequestDto request = new LoginRequestDto();
        request.setUsername("testuser");
        request.setPassword("WrongPassword123!");

        User user = User.builder()
                .username(request.getUsername())
                .password("encodedPassword")
                .name("테스트사용자")
                .build();

        given(userRepository.findByUsername(anyString())).willReturn(Optional.of(user));
        given(passwordEncoder.matches(anyString(), anyString())).willReturn(false);

        // when & then
        assertThrows(BadCredentialsException.class, () -> userService.login(request));

        verify(userRepository, times(1)).findByUsername(anyString());
        verify(passwordEncoder, times(1)).matches(anyString(), anyString());
        verify(jwtTokenProvider, times(0)).createToken(anyString());
    }

}