package com.barogo.app.dto.response;

import com.barogo.app.domain.User;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class UserInfoResponseDto {

    private Long id;
    private String username;
    private String name;
    private LocalDateTime createdAt;

    public static UserInfoResponseDto from(User user) {
        return UserInfoResponseDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .name(user.getName())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
