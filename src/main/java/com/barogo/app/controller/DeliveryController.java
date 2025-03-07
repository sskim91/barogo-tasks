package com.barogo.app.controller;

import com.barogo.app.dto.request.DeliverySearchRequestDto;
import com.barogo.app.dto.response.ApiResponse;
import com.barogo.app.dto.response.DeliveryResponseDto;
import com.barogo.app.service.DeliveryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * @author sskim
 */
@RestController
@RequestMapping("/api/v1/deliveries")
@RequiredArgsConstructor
public class DeliveryController {

    private final DeliveryService deliveryService;

    /**
     * 배달 조회 API
     * 기간 기반으로 배달 목록을 조회합니다. 선택적으로 배달 상태로 필터링 가능합니다.
     *
     * @param userDetails   인증된 사용자 정보
     * @param searchRequest 검색 조건 (시작일, 종료일, 상태)
     * @param pageable      페이지네이션 정보
     * @return 페이지네이션된 배달 목록
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<DeliveryResponseDto>>> getDeliveries(@AuthenticationPrincipal UserDetails userDetails,
                                                                                @Valid @ModelAttribute DeliverySearchRequestDto searchRequest,
                                                                                @PageableDefault(size = 10, sort = "requestedAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<DeliveryResponseDto> deliveries = deliveryService.getDeliveriesByDateRange(
                userDetails.getUsername(), searchRequest, pageable);

        return ResponseEntity.ok(
                ApiResponse.success(deliveries, "배달 조회가 성공적으로 완료되었습니다.")
        );
    }

    /**
     * 배달 주소 변경 API
     * 배달 주문의 도착지 주소를 변경합니다. 배달 상태가 변경 가능한 상태일 경우에만 처리됩니다.
     *
     * @param userDetails        인증된 사용자 정보
     * @param deliveryId         배달 ID
     * @param destinationAddress 새 도착지 주소
     * @return 업데이트된 배달 정보
     */
    @PatchMapping("/{deliveryId}/destination")
    public ResponseEntity<ApiResponse<DeliveryResponseDto>> updateDeliveryDestination(@AuthenticationPrincipal UserDetails userDetails,
                                                                                      @PathVariable Long deliveryId,
                                                                                      @RequestParam String destinationAddress) {

        DeliveryResponseDto updatedDelivery = deliveryService.updateDeliveryDestination(
                userDetails.getUsername(), deliveryId, destinationAddress);

        return ResponseEntity.ok(
                ApiResponse.success(updatedDelivery, "배달 주소가 성공적으로 변경되었습니다.")
        );
    }
}
