package com.barogo.app.service;

import com.barogo.app.domain.Delivery;
import com.barogo.app.domain.DeliveryStatus;
import com.barogo.app.domain.User;
import com.barogo.app.dto.request.DeliverySearchRequestDto;
import com.barogo.app.dto.response.DeliveryResponseDto;
import com.barogo.app.exception.UserNameNotFoundException;
import com.barogo.app.repository.DeliveryRepository;
import com.barogo.app.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DeliveryServiceTest {

    @Mock
    private DeliveryRepository deliveryRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private DeliveryService deliveryService;

    @Test
    @DisplayName("배달 조회: 기간 및 상태 필터 조회 테스트")
    void getDeliveriesByDateRangeWithStatusFilter() {
        // given
        String username = "testuser";
        User user = User.builder()
                .username(username)
                .password("encodedPassword")
                .name("테스트사용자")
                .build();

        LocalDateTime startDate = LocalDateTime.now().minusDays(2);
        LocalDateTime endDate = LocalDateTime.now();
        DeliveryStatus status = DeliveryStatus.RECEIVED;

        DeliverySearchRequestDto searchRequest = new DeliverySearchRequestDto();
        searchRequest.setStartDate(startDate);
        searchRequest.setEndDate(endDate);
        searchRequest.setStatus(status);

        Pageable pageable = PageRequest.of(0, 10);

        Delivery delivery1 = Delivery.builder()
                .user(user)
                .originAddress("출발지주소1")
                .destinationAddress("도착지주소1")
                .price(10000)
                .memo("배달요청사항1")
                .build();

        Delivery delivery2 = Delivery.builder()
                .user(user)
                .originAddress("출발지주소2")
                .destinationAddress("도착지주소2")
                .price(15000)
                .memo("배달요청사항2")
                .build();

        Page<Delivery> deliveryPage = new PageImpl<>(Arrays.asList(delivery1, delivery2));

        given(userRepository.findByUsername(anyString())).willReturn(Optional.of(user));
        given(deliveryRepository.findByUserAndRequestedAtBetweenAndStatus(
                any(User.class), any(LocalDateTime.class), any(LocalDateTime.class),
                any(DeliveryStatus.class), any(Pageable.class)))
                .willReturn(deliveryPage);

        // when
        Page<DeliveryResponseDto> result = deliveryService.getDeliveriesByDateRange(username, searchRequest, pageable);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        verify(userRepository, times(1)).findByUsername(anyString());
        verify(deliveryRepository, times(1)).findByUserAndRequestedAtBetweenAndStatus(
                any(User.class), any(LocalDateTime.class), any(LocalDateTime.class),
                any(DeliveryStatus.class), any(Pageable.class));
    }

    @Test
    @DisplayName("배달 조회: 기간만으로 조회 테스트")
    void getDeliveriesByDateRangeWithoutStatusFilter() {
        // given
        String username = "testuser";
        User user = User.builder()
                .username(username)
                .password("encodedPassword")
                .name("테스트사용자")
                .build();

        LocalDateTime startDate = LocalDateTime.now().minusDays(2);
        LocalDateTime endDate = LocalDateTime.now();

        DeliverySearchRequestDto searchRequest = new DeliverySearchRequestDto();
        searchRequest.setStartDate(startDate);
        searchRequest.setEndDate(endDate);
        searchRequest.setStatus(null);

        Pageable pageable = PageRequest.of(0, 10);

        Delivery delivery1 = Delivery.builder()
                .user(user)
                .originAddress("출발지주소1")
                .destinationAddress("도착지주소1")
                .price(10000)
                .memo("배달요청사항1")
                .build();

        Page<Delivery> deliveryPage = new PageImpl<>(Arrays.asList(delivery1));

        given(userRepository.findByUsername(anyString())).willReturn(Optional.of(user));
        given(deliveryRepository.findByUserAndRequestedAtBetween(
                any(User.class), any(LocalDateTime.class), any(LocalDateTime.class), any(Pageable.class)))
                .willReturn(deliveryPage);

        // when
        Page<DeliveryResponseDto> result = deliveryService.getDeliveriesByDateRange(username, searchRequest, pageable);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        verify(userRepository, times(1)).findByUsername(anyString());
        verify(deliveryRepository, times(1)).findByUserAndRequestedAtBetween(
                any(User.class), any(LocalDateTime.class), any(LocalDateTime.class), any(Pageable.class));
    }

    @Test
    @DisplayName("배달 조회: 존재하지 않는 사용자명으로 조회 시 예외 발생")
    void getDeliveriesByDateRangeWithNonExistingUsername() {
        // given
        String username = "nonexistinguser";

        LocalDateTime startDate = LocalDateTime.now().minusDays(2);
        LocalDateTime endDate = LocalDateTime.now();

        DeliverySearchRequestDto searchRequest = new DeliverySearchRequestDto();
        searchRequest.setStartDate(startDate);
        searchRequest.setEndDate(endDate);

        Pageable pageable = PageRequest.of(0, 10);

        given(userRepository.findByUsername(anyString())).willReturn(Optional.empty());

        // when & then
        assertThrows(UserNameNotFoundException.class,
                () -> deliveryService.getDeliveriesByDateRange(username, searchRequest, pageable));

        verify(userRepository, times(1)).findByUsername(anyString());
        verify(deliveryRepository, times(0)).findByUserAndRequestedAtBetween(
                any(User.class), any(LocalDateTime.class), any(LocalDateTime.class), any(Pageable.class));
    }

    @Test
    @DisplayName("배달 조회: 시작일이 종료일보다 이후인 경우 예외 발생")
    void getDeliveriesByDateRangeWithInvalidDateRange() {
        // given
        String username = "testuser";

        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime endDate = LocalDateTime.now().minusDays(2);

        DeliverySearchRequestDto searchRequest = new DeliverySearchRequestDto();
        searchRequest.setStartDate(startDate);
        searchRequest.setEndDate(endDate);

        Pageable pageable = PageRequest.of(0, 10);

        // when & then
        assertThrows(IllegalArgumentException.class,
                () -> deliveryService.getDeliveriesByDateRange(username, searchRequest, pageable));
    }

    @Test
    @DisplayName("배달 조회: 조회 기간이 3일을 초과하는 경우 예외 발생")
    void getDeliveriesByDateRangeWithExceedingMaxPeriod() {
        // given
        String username = "testuser";

        LocalDateTime startDate = LocalDateTime.now().minusDays(5);
        LocalDateTime endDate = LocalDateTime.now();

        DeliverySearchRequestDto searchRequest = new DeliverySearchRequestDto();
        searchRequest.setStartDate(startDate);
        searchRequest.setEndDate(endDate);

        Pageable pageable = PageRequest.of(0, 10);

        // when & then
        assertThrows(IllegalArgumentException.class,
                () -> deliveryService.getDeliveriesByDateRange(username, searchRequest, pageable));
    }

    @Test
    @DisplayName("배달 주소 변경: 성공 케이스")
    void updateDeliveryDestinationSuccess() {
        // given
        String username = "testuser";
        Long deliveryId = 1L;
        String newDestination = "새로운도착지주소";

        User user = User.builder()
                .username(username)
                .password("encodedPassword")
                .name("테스트사용자")
                .build();

        Delivery delivery = Delivery.builder()
                .user(user)
                .originAddress("출발지주소")
                .destinationAddress("도착지주소")
                .price(10000)
                .memo("배달요청사항")
                .build();

        given(userRepository.findByUsername(anyString())).willReturn(Optional.of(user));
        given(deliveryRepository.findByIdAndUser(anyLong(), any(User.class))).willReturn(Optional.of(delivery));

        // when
        DeliveryResponseDto result = deliveryService.updateDeliveryDestination(username, deliveryId, newDestination);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getDestinationAddress()).isEqualTo(newDestination);
        verify(userRepository, times(1)).findByUsername(anyString());
        verify(deliveryRepository, times(1)).findByIdAndUser(anyLong(), any(User.class));
    }

    @Test
    @DisplayName("배달 주소 변경: 존재하지 않는 배달 ID인 경우 예외 발생")
    void updateDeliveryDestinationWithNonExistingDelivery() {
        // given
        String username = "testuser";
        Long deliveryId = 999L;
        String newDestination = "새로운도착지주소";

        User user = User.builder()
                .username(username)
                .password("encodedPassword")
                .name("테스트사용자")
                .build();

        given(userRepository.findByUsername(anyString())).willReturn(Optional.of(user));
        given(deliveryRepository.findByIdAndUser(anyLong(), any(User.class))).willReturn(Optional.empty());

        // when & then
        assertThrows(IllegalStateException.class,
                () -> deliveryService.updateDeliveryDestination(username, deliveryId, newDestination));

        verify(userRepository, times(1)).findByUsername(anyString());
        verify(deliveryRepository, times(1)).findByIdAndUser(anyLong(), any(User.class));
    }

    @Test
    @DisplayName("배달 주소 변경: 배달 상태가 수정 불가능한 상태인 경우 예외 발생")
    void updateDeliveryDestinationWithUnmodifiableStatus() {
        // given
        String username = "testuser";
        Long deliveryId = 1L;
        String newDestination = "새로운도착지주소";

        User user = User.builder()
                .username(username)
                .password("encodedPassword")
                .name("테스트사용자")
                .build();

        Delivery delivery = Delivery.builder()
                .user(user)
                .originAddress("출발지주소")
                .destinationAddress("도착지주소")
                .price(10000)
                .memo("배달요청사항")
                .build();

        // 배달 상태를 'IN_TRANSIT'으로 변경 (주소 변경 불가능한 상태)
        delivery.updateStatus(DeliveryStatus.ASSIGNED);
        delivery.updateStatus(DeliveryStatus.IN_TRANSIT);

        given(userRepository.findByUsername(anyString())).willReturn(Optional.of(user));
        given(deliveryRepository.findByIdAndUser(anyLong(), any(User.class))).willReturn(Optional.of(delivery));

        // when & then
        assertThrows(IllegalStateException.class,
                () -> deliveryService.updateDeliveryDestination(username, deliveryId, newDestination));

        verify(userRepository, times(1)).findByUsername(anyString());
        verify(deliveryRepository, times(1)).findByIdAndUser(anyLong(), any(User.class));
    }
}