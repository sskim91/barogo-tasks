package com.barogo.app.docs;

import com.barogo.app.domain.DeliveryStatus;
import com.barogo.app.dto.request.UpdateDestinationRequestDto;
import com.barogo.app.dto.response.DeliveryResponseDto;
import com.barogo.app.dto.response.PageResponseDto;
import com.barogo.app.service.DeliveryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.patch;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.JsonFieldType.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ExtendWith(RestDocumentationExtension.class)
public class DeliveryControllerDocTest {

    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private DeliveryService deliveryService;

    @BeforeEach
    void setUp(final WebApplicationContext webApplicationContext,
               final RestDocumentationContextProvider restDocumentation) {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(
                        documentationConfiguration(restDocumentation)
                                .operationPreprocessors()
                                .withRequestDefaults(prettyPrint())
                                .withResponseDefaults(prettyPrint())
                )
                .build();
    }

    @Test
    @DisplayName("배달 조회 API 문서화")
    @WithMockUser(username = "testuser")
    void getDeliveriesDocTest() throws Exception {
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

        List<DeliveryResponseDto> deliveries = List.of(delivery1);
        Page<DeliveryResponseDto> pageResult = new PageImpl<>(deliveries);

        PageResponseDto<DeliveryResponseDto> responseDto = new PageResponseDto<>(
                pageResult.getContent(),
                pageResult.getNumber(),
                pageResult.getSize(),
                pageResult.getTotalElements(),
                pageResult.getTotalPages()
        );

        given(deliveryService.getDeliveriesByDateRange(anyString(), any(), any(Pageable.class)))
                .willReturn(pageResult);

        // when & then
        mockMvc.perform(RestDocumentationRequestBuilders.get("/api/v1/deliveries")
                        .param("startDate", startDate.format(DateTimeFormatter.ISO_DATE_TIME))
                        .param("endDate", endDate.format(DateTimeFormatter.ISO_DATE_TIME))
                        .param("status", "RECEIVED"))
                .andExpect(status().isOk())
                .andDo(document("delivery-search",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        queryParameters(
                                parameterWithName("startDate").description("조회 시작일 (ISO DateTime 형식)"),
                                parameterWithName("endDate").description("조회 종료일 (ISO DateTime 형식)"),
                                parameterWithName("status").description("배달 상태 필터 (선택사항)").optional(),
                                parameterWithName("page").description("페이지 번호 (0부터 시작, 기본값 0)").optional(),
                                parameterWithName("size").description("페이지 크기 (기본값 10)").optional(),
                                parameterWithName("sort").description("정렬 기준 (기본값: requestedAt,DESC)").optional()
                        ),
                        responseFields(
                                fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("요청 성공 여부"),
                                fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지"),
                                fieldWithPath("data.content").type(JsonFieldType.ARRAY).description("배달 목록"),
                                fieldWithPath("data.content[].id").type(JsonFieldType.NUMBER).description("배달 고유 번호"),
                                fieldWithPath("data.content[].status").type(JsonFieldType.STRING).description("배달 상태"),
                                fieldWithPath("data.content[].originAddress").type(JsonFieldType.STRING).description("출발지 주소"),
                                fieldWithPath("data.content[].destinationAddress").type(JsonFieldType.STRING).description("도착지 주소"),
                                fieldWithPath("data.content[].requestedAt").type(JsonFieldType.STRING).description("주문 요청 시간"),
                                fieldWithPath("data.content[].price").type(JsonFieldType.NUMBER).description("배달 요금"),
                                fieldWithPath("data.content[].estimatedDeliveryTime").type(JsonFieldType.NULL).description("예상 배달 완료 시간").optional(),
                                fieldWithPath("data.content[].completedAt").type(JsonFieldType.NULL).description("실제 배달 완료 시간").optional(),
                                fieldWithPath("data.content[].memo").type(JsonFieldType.NULL).description("배달 요청 사항").optional(),
                                fieldWithPath("data.pageNumber").type(JsonFieldType.NUMBER).description("현재 페이지 번호"),
                                fieldWithPath("data.pageSize").type(JsonFieldType.NUMBER).description("페이지 크기"),
                                fieldWithPath("data.totalElements").type(JsonFieldType.NUMBER).description("전체 요소 수"),
                                fieldWithPath("data.totalPages").type(JsonFieldType.NUMBER).description("전체 페이지 수")
                        )
                ));
    }

    @Test
    @DisplayName("배달 주소 변경 API 문서화")
    @WithMockUser(username = "testuser")
    void updateDeliveryDestinationDocTest() throws Exception {
        // given
        Long deliveryId = 1L;
        String newDestinationAddress = "새로운도착지주소";

        UpdateDestinationRequestDto requestDto = new UpdateDestinationRequestDto();
        requestDto.setDestinationAddress(newDestinationAddress);

        DeliveryResponseDto updatedDelivery = DeliveryResponseDto.builder()
                .id(deliveryId)
                .status(DeliveryStatus.RECEIVED)
                .originAddress("출발지주소")
                .destinationAddress(newDestinationAddress)
                .requestedAt(LocalDateTime.now().minusDays(1))
                .price(10000)
                .build();

        given(deliveryService.updateDeliveryDestination(anyString(), anyLong(), anyString()))
                .willReturn(updatedDelivery);

        // when & then
        mockMvc.perform(patch("/api/v1/deliveries/{deliveryId}/destination", deliveryId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andDo(document("delivery-update-destination",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("deliveryId").description("배달 고유 번호")
                        ),
                        requestFields(
                                fieldWithPath("destinationAddress").description("새 도착지 주소")
                        ),
                        responseFields(
                                fieldWithPath("success").type(BOOLEAN).description("요청 성공 여부"),
                                fieldWithPath("message").type(STRING).description("응답 메시지"),
                                fieldWithPath("data.id").type(NUMBER).description("배달 고유 번호"),
                                fieldWithPath("data.status").type(STRING).description("배달 상태"),
                                fieldWithPath("data.originAddress").type(STRING).description("출발지 주소"),
                                fieldWithPath("data.destinationAddress").type(STRING).description("변경된 도착지 주소"),
                                fieldWithPath("data.requestedAt").type(STRING).description("주문 요청 시간"),
                                fieldWithPath("data.price").type(NUMBER).description("배달 요금"),
                                fieldWithPath("data.estimatedDeliveryTime").type(NULL).description("예상 배달 완료 시간").optional(),
                                fieldWithPath("data.completedAt").type(NULL).description("실제 배달 완료 시간").optional(),
                                fieldWithPath("data.memo").type(NULL).description("배달 요청 사항").optional()
                        )
                ));
    }
}
