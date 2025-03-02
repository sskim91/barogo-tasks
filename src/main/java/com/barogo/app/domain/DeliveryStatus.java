package com.barogo.app.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DeliveryStatus {

    /**
     * 배달 접수 상태
     * 주문이 접수되었지만 아직 배달원이 배정되지 않은 상태
     */
    RECEIVED("접수됨"),

    /**
     * 배달원 배정 상태
     * 배달원이 배정되었지만 아직 픽업하지 않은 상태
     */
    ASSIGNED("배달원 배정됨"),

    /**
     * 배달 중 상태
     * 배달원이 상품을 픽업하여 배달 중인 상태
     */
    IN_TRANSIT("배달 중"),

    /**
     * 배달 완료 상태
     * 배달이 완료된 상태
     */
    DELIVERED("배달 완료"),

    /**
     * 배달 취소 상태
     * 배달이 취소된 상태
     */
    CANCELLED("배달 취소");

    private final String description;
}
