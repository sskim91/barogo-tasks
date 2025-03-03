package com.barogo.app.dto.request;

import com.barogo.app.domain.DeliveryStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class DeliverySearchRequestDto {

    @NotNull(message = "조회 시작일은 필수입니다.")
    private LocalDateTime startDate;

    @NotNull(message = "조회 종료일은 필수입니다.")
    private LocalDateTime endDate;

    private DeliveryStatus status;

    // 검증 메서드: 조회 기간이 3일을 초과하는지 확인
    public boolean isExceedingMaxPeriod() {
        return startDate != null && endDate != null &&
               startDate.plusDays(3).isBefore(endDate);
    }
}
