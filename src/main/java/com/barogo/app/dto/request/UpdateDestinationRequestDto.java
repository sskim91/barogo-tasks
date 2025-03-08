package com.barogo.app.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateDestinationRequestDto {

    @NotBlank(message = "도착지 주소는 필수입니다.")
    private String destinationAddress;
}
