package com.barogo.app.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@RequiredArgsConstructor
public enum DeliveryStatus {

    RECEIVED("접수됨"),        //주문이 접수되었지만 아직 배달원이 배정되지 않은 상태
    ASSIGNED("배달원 배정됨"),   //배달원이 배정되었지만 아직 픽업하지 않은 상태
    IN_TRANSIT("배달 중"),     //배달원이 상품을 픽업하여 배달 중인 상태
    DELIVERED("배달 완료"),     //배달이 완료된 상태
    CANCELLED("배달 취소");     //배달이 취소된 상태

    private final String description;

    // 각 상태에서 전환 가능한 다음 상태들을 정의
    private static final Map<DeliveryStatus, List<DeliveryStatus>> ALLOWED_TRANSITIONS = new HashMap<>();

    static {
        ALLOWED_TRANSITIONS.put(RECEIVED, List.of(ASSIGNED, CANCELLED));
        ALLOWED_TRANSITIONS.put(ASSIGNED, List.of(IN_TRANSIT, CANCELLED));
        ALLOWED_TRANSITIONS.put(IN_TRANSIT, Collections.singletonList(DELIVERED));
        ALLOWED_TRANSITIONS.put(DELIVERED, Collections.emptyList());
        ALLOWED_TRANSITIONS.put(CANCELLED, Collections.emptyList());
    }

    // 현재 상태에서 목표 상태로 전환이 가능한지 확인하는 메서드
    public boolean canTransitionTo(DeliveryStatus targetStatus) {
        return ALLOWED_TRANSITIONS.getOrDefault(this, Collections.emptyList())
                .contains(targetStatus);
    }
}
