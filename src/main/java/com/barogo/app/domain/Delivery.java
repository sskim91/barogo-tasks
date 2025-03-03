package com.barogo.app.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Entity
@Table(name = "deliveries")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Delivery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 배달 주문자 정보
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * 배달 상태
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeliveryStatus status;

    /**
     * 출발지 주소
     */
    @Column(nullable = false, length = 255)
    private String originAddress;

    /**
     * 도착지 주소
     */
    @Column(nullable = false, length = 255)
    private String destinationAddress;

    /**
     * 주문 요청 시간
     */
    @Column(nullable = false)
    private LocalDateTime requestedAt;

    /**
     * 예상 배달 완료 시간
     */
    @Column
    private LocalDateTime estimatedDeliveryTime;

    /**
     * 실제 배달 완료 시간
     */
    @Column
    private LocalDateTime completedAt;

    /**
     * 배달 요금
     */
    @Column(nullable = false)
    private Integer price;

    /**
     * 배달 요청 사항
     */
    @Column(length = 500)
    private String memo;

    /**
     * 생성 시간
     */
    @Column(nullable = false)
    private LocalDateTime createdAt;

    /**
     * 수정 시간
     */
    @Column(nullable = false)
    private LocalDateTime updatedAt;


    @Builder
    public Delivery(User user, String originAddress, String destinationAddress, Integer price, String memo) {
        this.user = user;
        this.status = DeliveryStatus.RECEIVED; // 초기 상태는 항상 '접수됨'
        this.originAddress = originAddress;
        this.destinationAddress = destinationAddress;
        this.requestedAt = LocalDateTime.now();
        this.price = price;
        this.memo = memo;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 도착지 주소 변경
     * RECEIVED, ASSIGNED 상태에서만 변경 가능
     *
     * @param destinationAddress 새 도착지 주소
     * @return 업데이트된 배달 객체
     * @throws IllegalStateException 변경할 수 없는 상태인 경우
     */
    public Delivery updateDestinationAddress(String destinationAddress) {
        validateAddressUpdateable();
        this.destinationAddress = destinationAddress;
        this.updatedAt = LocalDateTime.now();
        return this;
    }

    /**
     * 배달 상태 변경
     *
     * @param status 새 배달 상태
     * @return 업데이트된 배달 객체
     */
    public Delivery updateStatus(DeliveryStatus status) {
        validateStatusTransition(status);
        this.status = status;

        // 배달 완료 상태로 변경 시 완료 시간 설정
        if (status == DeliveryStatus.DELIVERED) {
            this.completedAt = LocalDateTime.now();
        }

        this.updatedAt = LocalDateTime.now();
        return this;
    }

    /**
     * 주소 변경이 가능한 상태인지 검증
     */
    private void validateAddressUpdateable() {
        List<DeliveryStatus> updateableStatuses = Arrays.asList(
                DeliveryStatus.RECEIVED,
                DeliveryStatus.ASSIGNED
        );

        if (!updateableStatuses.contains(this.status)) {
            throw new IllegalStateException("배달이 진행 중이거나 완료된 경우에는 주소를 변경할 수 없습니다. 현재 상태: " + this.status.getDescription());
        }
    }

    /**
     * 상태 전이가 유효한지 검증
     */
    private void validateStatusTransition(DeliveryStatus newStatus) {
        // 현재 상태가 동일하면 변경 없음 (중복 호출 허용)
        if (this.status == newStatus) {
            return;
        }

        // 상태 전이 가능 여부 확인
        if (!this.status.canTransitionTo(newStatus)) {
            throw new IllegalStateException(
                    String.format("현재 상태(%s)에서 %s 상태로 변경할 수 없습니다.",
                            this.status.getDescription(),
                            newStatus.getDescription())
            );
        }
    }

    /**
     * 배달이 특정 사용자의 것인지 검증
     */
    public boolean isOwnedBy(User user) {
        return this.user.getId().equals(user.getId());
    }
}
