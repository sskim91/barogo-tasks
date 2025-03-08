package com.barogo.app.controller;

import com.barogo.app.config.TestSecurityConfig;
import com.barogo.app.domain.DeliveryStatus;
import com.barogo.app.dto.request.DeliverySearchRequestDto;
import com.barogo.app.dto.request.UpdateDestinationRequestDto;
import com.barogo.app.dto.response.DeliveryResponseDto;
import com.barogo.app.exception.UserNameNotFoundException;
import com.barogo.app.service.DeliveryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(DeliveryController.class)
@Import(TestSecurityConfig.class)
class DeliveryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private DeliveryService deliveryService;

    @Test
    @DisplayName("배달 조회 API - 성공 케이스")
    @WithMockUser(username = "testuser")
    void getDeliveriesTest() throws Exception {
        // given
        LocalDateTime startDate = LocalDateTime.now().minusDays(2);
        LocalDateTime endDate = LocalDateTime.now();

        DeliveryResponseDto delivery1 = DeliveryResponseDto.builder()
                .id(1L)
                .status(DeliveryStatus.RECEIVED)
                .originAddress("출발지주소1")
                .destinationAddress("도착지주소1")
                .requestedAt(LocalDateTime.now().minusDays(1))
                .price(10000)
                .build();

        DeliveryResponseDto delivery2 = DeliveryResponseDto.builder()
                .id(2L)
                .status(DeliveryStatus.ASSIGNED)
                .originAddress("출발지주소2")
                .destinationAddress("도착지주소2")
                .requestedAt(LocalDateTime.now().minusHours(12))
                .price(15000)
                .build();

        Page<DeliveryResponseDto> deliveryPage = new PageImpl<>(Arrays.asList(delivery1, delivery2));

        given(deliveryService.getDeliveriesByDateRange(anyString(), any(DeliverySearchRequestDto.class), any(Pageable.class)))
                .willReturn(deliveryPage);

        // when & then
        mockMvc.perform(get("/api/v1/deliveries")
                        .param("startDate", startDate.format(DateTimeFormatter.ISO_DATE_TIME))
                        .param("endDate", endDate.format(DateTimeFormatter.ISO_DATE_TIME)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content.length()").value(2))
                .andExpect(jsonPath("$.data.content[0].id").value(1))
                .andExpect(jsonPath("$.data.content[0].status").value("RECEIVED"))
                .andExpect(jsonPath("$.data.content[1].id").value(2))
                .andExpect(jsonPath("$.data.content[1].status").value("ASSIGNED"));
    }

    @Test
    @DisplayName("배달 조회 API - 상태 필터 적용")
    @WithMockUser(username = "testuser")
    void getDeliveriesWithStatusFilterTest() throws Exception {
        // given
        LocalDateTime startDate = LocalDateTime.now().minusDays(2);
        LocalDateTime endDate = LocalDateTime.now();

        DeliveryResponseDto delivery = DeliveryResponseDto.builder()
                .id(1L)
                .status(DeliveryStatus.RECEIVED)
                .originAddress("출발지주소1")
                .destinationAddress("도착지주소1")
                .requestedAt(LocalDateTime.now().minusDays(1))
                .price(10000)
                .build();

        Page<DeliveryResponseDto> deliveryPage = new PageImpl<>(Arrays.asList(delivery));

        given(deliveryService.getDeliveriesByDateRange(anyString(), any(DeliverySearchRequestDto.class), any(Pageable.class)))
                .willReturn(deliveryPage);

        // when & then
        mockMvc.perform(get("/api/v1/deliveries")
                        .param("startDate", startDate.format(DateTimeFormatter.ISO_DATE_TIME))
                        .param("endDate", endDate.format(DateTimeFormatter.ISO_DATE_TIME))
                        .param("status", "RECEIVED"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content.length()").value(1))
                .andExpect(jsonPath("$.data.content[0].status").value("RECEIVED"));
    }

    @Test
    @DisplayName("배달 조회 API - 유효하지 않은 날짜 범위")
    @WithMockUser(username = "testuser")
    void getDeliveriesWithInvalidDateRangeTest() throws Exception {
        // given
        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime endDate = LocalDateTime.now().minusDays(2); // 종료일이 시작일보다 이전

        doThrow(new IllegalArgumentException("시작일은 종료일보다 이전이어야 합니다."))
                .when(deliveryService).getDeliveriesByDateRange(anyString(), any(DeliverySearchRequestDto.class), any(Pageable.class));

        // when & then
        mockMvc.perform(get("/api/v1/deliveries")
                        .param("startDate", startDate.format(DateTimeFormatter.ISO_DATE_TIME))
                        .param("endDate", endDate.format(DateTimeFormatter.ISO_DATE_TIME)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("배달 조회 API - 조회 기간 초과")
    @WithMockUser(username = "testuser")
    void getDeliveriesWithExceedingMaxPeriodTest() throws Exception {
        // given
        LocalDateTime startDate = LocalDateTime.now().minusDays(5); // 5일 전
        LocalDateTime endDate = LocalDateTime.now(); // 현재

        doThrow(new IllegalArgumentException("조회 기간은 최대 3일까지만 가능합니다."))
                .when(deliveryService).getDeliveriesByDateRange(anyString(), any(DeliverySearchRequestDto.class), any(Pageable.class));

        // when & then
        mockMvc.perform(get("/api/v1/deliveries")
                        .param("startDate", startDate.format(DateTimeFormatter.ISO_DATE_TIME))
                        .param("endDate", endDate.format(DateTimeFormatter.ISO_DATE_TIME)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("배달 조회 API - 존재하지 않는 사용자")
    @WithMockUser(username = "nonexistentuser")
    void getDeliveriesWithNonExistentUserTest() throws Exception {
        // given
        LocalDateTime startDate = LocalDateTime.now().minusDays(2);
        LocalDateTime endDate = LocalDateTime.now();

        doThrow(new UserNameNotFoundException("사용자를 찾을 수 없습니다: nonexistentuser"))
                .when(deliveryService).getDeliveriesByDateRange(anyString(), any(DeliverySearchRequestDto.class), any(Pageable.class));

        // when & then
        mockMvc.perform(get("/api/v1/deliveries")
                        .param("startDate", startDate.format(DateTimeFormatter.ISO_DATE_TIME))
                        .param("endDate", endDate.format(DateTimeFormatter.ISO_DATE_TIME)))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("배달 주소 변경 API - 성공 케이스")
    @WithMockUser(username = "testuser")
    void updateDeliveryDestinationTest() throws Exception {
        // given
        Long deliveryId = 1L;
        String newDestinationAddress = "새로운도착지주소";

        UpdateDestinationRequestDto requestDto = new UpdateDestinationRequestDto();
        requestDto.setDestinationAddress(newDestinationAddress);

        DeliveryResponseDto updatedDelivery = DeliveryResponseDto.builder()
                .id(deliveryId)
                .status(DeliveryStatus.RECEIVED)
                .originAddress("출발지주소")
                .destinationAddress(newDestinationAddress) // 변경된 주소
                .requestedAt(LocalDateTime.now().minusDays(1))
                .price(10000)
                .build();

        given(deliveryService.updateDeliveryDestination(anyString(), anyLong(), anyString()))
                .willReturn(updatedDelivery);

        // when & then
        mockMvc.perform(patch("/api/v1/deliveries/{deliveryId}/destination", deliveryId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(deliveryId))
                .andExpect(jsonPath("$.data.destinationAddress").value(newDestinationAddress));
    }

    @Test
    @DisplayName("배달 주소 변경 API - 존재하지 않는 배달 ID")
    @WithMockUser(username = "testuser")
    void updateDeliveryDestinationWithNonExistentDeliveryTest() throws Exception {
        // given
        Long nonExistentDeliveryId = 999L;
        String newDestinationAddress = "새로운도착지주소";

        UpdateDestinationRequestDto requestDto = new UpdateDestinationRequestDto();
        requestDto.setDestinationAddress(newDestinationAddress);

        doThrow(new IllegalStateException("해당 배달을 찾을 수 없거나 접근 권한이 없습니다."))
                .when(deliveryService).updateDeliveryDestination(anyString(), anyLong(), anyString());

        // when & then
        mockMvc.perform(patch("/api/v1/deliveries/{deliveryId}/destination", nonExistentDeliveryId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("배달 주소 변경 API - 수정 불가능한 상태")
    @WithMockUser(username = "testuser")
    void updateDeliveryDestinationWithUnmodifiableStatusTest() throws Exception {
        // given
        Long deliveryId = 1L;
        String newDestinationAddress = "새로운도착지주소";

        UpdateDestinationRequestDto requestDto = new UpdateDestinationRequestDto();
        requestDto.setDestinationAddress(newDestinationAddress);

        doThrow(new IllegalStateException("배달이 진행 중이거나 완료된 경우에는 주소를 변경할 수 없습니다. 현재 상태: 배달 중"))
                .when(deliveryService).updateDeliveryDestination(anyString(), anyLong(), anyString());

        // when & then
        mockMvc.perform(patch("/api/v1/deliveries/{deliveryId}/destination", deliveryId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

}