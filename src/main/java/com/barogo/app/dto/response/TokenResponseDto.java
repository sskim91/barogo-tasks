package com.barogo.app.dto.response;

import lombok.Getter;

@Getter
public class TokenResponseDto {

    private String accessToken;
    private String tokenType;
    private Long expiresIn;

    public TokenResponseDto(final String accessToken, final String tokenType, final Long expiresIn) {
        this.accessToken = accessToken;
        this.tokenType = tokenType;
        this.expiresIn = expiresIn;
    }
}
