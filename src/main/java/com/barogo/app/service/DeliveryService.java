package com.barogo.app.service;

import com.barogo.app.domain.Delivery;
import com.barogo.app.domain.User;
import com.barogo.app.dto.request.DeliverySearchRequestDto;
import com.barogo.app.dto.response.DeliveryResponseDto;
import com.barogo.app.exception.UserNameNotFoundException;
import com.barogo.app.repository.DeliveryRepository;
import com.barogo.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeliveryService {

    private final DeliveryRepository deliveryRepository;
    private final UserRepository userRepository;

    /**
     * 사용자명으로 사용자 조회
     *
     * @param username 사용자명
     * @return 사용자 엔티티
     * @throws UserNameNotFoundException 사용자를 찾을 수 없는 경우
     */
    private User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNameNotFoundException("사용자를 찾을 수 없습니다: " + username));
    }

    /**
     * 기간 기반 배달 목록 조회
     *
     * @param username      현재 인증된 사용자명
     * @param searchRequest 검색 조건 (시작일, 종료일, 상태)
     * @param pageable      페이지네이션 정보
     * @return 페이지네이션된 배달 목록
     */
    public Page<DeliveryResponseDto> getDeliveriesByDateRange(String username, DeliverySearchRequestDto searchRequest, Pageable pageable) {

        // 검색 기간 유효성 검증
        validateSearchPeriod(searchRequest);

        // 배달 상태 필터 적용 여부에 따라 다른 쿼리 사용
        Page<Delivery> deliveries;
        if (searchRequest.getStatus() != null) {
            // 상태 필터가 있는 경우, 커스텀 쿼리 메서드를 호출해야 함
            // 아래 메서드는 Repository에 추가 구현 필요
            deliveries = deliveryRepository.findByUsernameAndRequestedAtBetweenAndStatus(
                    username,
                    searchRequest.getStartDate(),
                    searchRequest.getEndDate(),
                    searchRequest.getStatus(),
                    pageable
            );
        } else {
            // 상태 필터가 없는 경우
            deliveries = deliveryRepository.findByUsernameAndRequestedAtBetween(
                    username,
                    searchRequest.getStartDate(),
                    searchRequest.getEndDate(),
                    pageable
            );
        }
        return deliveries.map(DeliveryResponseDto::from);
    }

    /**
     * 배달 주소 변경
     *
     * @param username           현재 인증된 사용자명
     * @param deliveryId         배달 ID
     * @param destinationAddress 새 도착지 주소
     * @return 업데이트된 배달 정보
     * @throws IllegalStateException 배달 상태가 변경 불가능한 상태인 경우
     */
    @Transactional
    public DeliveryResponseDto updateDeliveryDestination(String username, Long deliveryId, String destinationAddress) {
        // 사용자 조회
        User user = getUserByUsername(username);

        // 사용자의 배달인지 확인하면서 배달 조회
        Delivery delivery = deliveryRepository.findByIdAndUser(deliveryId, user)
                .orElseThrow(() -> new IllegalStateException("해당 배달을 찾을 수 없거나 접근 권한이 없습니다."));

        // 배달 주소 변경 (내부적으로 상태 검증 로직 포함)
        delivery.updateDestinationAddress(destinationAddress);

        // 변경된 배달 정보 반환
        return DeliveryResponseDto.from(delivery);
    }


    private void validateSearchPeriod(DeliverySearchRequestDto searchRequest) {
        // 시작일이 종료일보다 이후인 경우
        if (searchRequest.getStartDate().isAfter(searchRequest.getEndDate())) {
            throw new IllegalArgumentException("시작일은 종료일보다 이전이어야 합니다.");
        }

        // 조회 기간이 3일을 초과하는 경우
        if (searchRequest.isExceedingMaxPeriod()) {
            throw new IllegalArgumentException("조회 기간은 최대 3일까지만 가능합니다.");
        }
    }
}
