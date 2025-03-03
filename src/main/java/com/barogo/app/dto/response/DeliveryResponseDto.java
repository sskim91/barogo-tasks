package com.barogo.app.dto.response;

import com.barogo.app.domain.Delivery;
import com.barogo.app.domain.DeliveryStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class DeliveryResponseDto {
    private Long id;
    private DeliveryStatus status;
    private String originAddress;
    private String destinationAddress;
    private LocalDateTime requestedAt;
    private LocalDateTime estimatedDeliveryTime;
    private LocalDateTime completedAt;
    private Integer price;
    private String memo;

    public static DeliveryResponseDto from(Delivery delivery) {
        return DeliveryResponseDto.builder()
                .id(delivery.getId())
                .status(delivery.getStatus())
                .originAddress(delivery.getOriginAddress())
                .destinationAddress(delivery.getDestinationAddress())
                .requestedAt(delivery.getRequestedAt())
                .estimatedDeliveryTime(delivery.getEstimatedDeliveryTime())
                .completedAt(delivery.getCompletedAt())
                .price(delivery.getPrice())
                .memo(delivery.getMemo())
                .build();
    }
}
