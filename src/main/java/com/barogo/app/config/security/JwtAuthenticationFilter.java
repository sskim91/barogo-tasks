package com.barogo.app.config.security;

import com.barogo.app.config.jwt.JwtTokenProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(final HttpServletRequest request, final HttpServletResponse response, final FilterChain filterChain) throws ServletException, IOException {
        try {
            // 요청에서 JWT 토큰 추출
            String jwt = jwtTokenProvider.resolveToken(request);

            // JWT 토큰이 존재하고 유효한 경우
            if (StringUtils.hasText(jwt) && jwtTokenProvider.validateToken(jwt)) {
                // 토큰으로부터 인증 정보 추출
                Authentication authentication = jwtTokenProvider.getAuthentication(jwt);
                // 보안 컨텍스트에 인증 정보 설정
                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.debug("Security Context에 '{}' 인증 정보를 저장했습니다, uri: {}", authentication.getName(), request.getRequestURI());
            } else {
                log.debug("유효한 JWT 토큰이 없습니다, uri: {}", request.getRequestURI());
            }
        } catch (Exception e) {
            log.error("Spring Security Filter Chain에서 사용자 인증을 설정할 수 없습니다", e);
        }

        // 다음 필터 체인 실행
        filterChain.doFilter(request, response);
    }
}
