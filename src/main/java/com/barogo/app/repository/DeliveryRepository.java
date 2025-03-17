package com.barogo.app.repository;

import com.barogo.app.domain.Delivery;
import com.barogo.app.domain.DeliveryStatus;
import com.barogo.app.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface DeliveryRepository extends JpaRepository<Delivery, Long> {

    Optional<Delivery> findByIdAndUser(Long id, User user);

    /**
     * 사용자명과 날짜 범위로 배달 목록을 조회합니다.
     * JOIN을 사용하여 사용자 필터링만 수행하고 Delivery 엔티티만 반환합니다.
     *
     * @param username 사용자명
     * @param start 조회 시작일
     * @param end 조회 종료일
     * @param pageable 페이지 정보
     * @return 배달 목록
     */
    @Query("SELECT d FROM Delivery d JOIN d.user u WHERE u.username = :username AND d.requestedAt BETWEEN :start AND :end")
    Page<Delivery> findByUsernameAndRequestedAtBetween(
            @Param("username") String username,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            Pageable pageable);

    /**
     * 사용자명, 날짜 범위, 배달 상태로 배달 목록을 조회합니다.
     * JOIN을 사용하여 사용자 필터링만 수행하고 Delivery 엔티티만 반환합니다.
     *
     * @param username 사용자명
     * @param start 조회 시작일
     * @param end 조회 종료일
     * @param status 배달 상태
     * @param pageable 페이지 정보
     * @return 배달 목록
     */
    @Query("SELECT d FROM Delivery d JOIN d.user u WHERE u.username = :username AND d.requestedAt BETWEEN :start AND :end AND d.status = :status")
    Page<Delivery> findByUsernameAndRequestedAtBetweenAndStatus(
            @Param("username") String username,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("status") DeliveryStatus status,
            Pageable pageable);
}
