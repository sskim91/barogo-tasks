package com.barogo.app.dto.response;

import lombok.Getter;

@Getter
public class TokenResponseDto {

    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private Long accessTokenExpiresIn;
    private Long refreshTokenExpiresIn;  // 추가됨

    public TokenResponseDto(final String accessToken, final String refreshToken, final String tokenType, final Long accessTokenExpiresIn, final Long refreshTokenExpiresIn) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.tokenType = tokenType;
        this.accessTokenExpiresIn = accessTokenExpiresIn;
        this.refreshTokenExpiresIn = refreshTokenExpiresIn;
    }
}
