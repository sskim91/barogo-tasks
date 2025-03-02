package com.barogo.app.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

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
}
