package com.barogo.app.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class SignUpRequestDto {

    @NotBlank(message = "사용자 ID는 필수입니다.")
    @Size(min = 4, max = 50, message = "사용자 ID는 4~50자 사이여야 합니다.")
    private String username;

    @NotBlank(message = "비밀번호는 필수입니다.")
    @Size(min = 12, message = "비밀번호는 최소 12자 이상이어야 합니다.")
    @Pattern(
            regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!]).*$|" +
                    "^(?=.*[0-9])(?=.*[a-z])(?=.*[@#$%^&+=!]).*$|" +
                    "^(?=.*[0-9])(?=.*[A-Z])(?=.*[@#$%^&+=!]).*$|" +
                    "^(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!]).*$",
            message = "비밀번호는 영어 대문자, 영어 소문자, 숫자, 특수문자 중 3종류 이상을 포함해야 합니다."
    )
    private String password;

    @NotBlank(message = "사용자 이름은 필수입니다.")
    @Size(min = 2, max = 50, message = "사용자 이름은 2~50자 사이여야 합니다.")
    private String name;
}
