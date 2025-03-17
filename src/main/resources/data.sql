-- 테스트용 사용자 추가
-- 비밀번호: TestPassword123!
INSERT INTO users (username, password, name, created_at, updated_at)
VALUES ('admin', '$2a$10$K2tvjKI6PQKdqoAjjKvTnu.4S8N3Jwmeml3xikD65i.EzCZz9u2XK', '관리자', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP());

-- 사용자1의 배달 정보 추가
INSERT INTO deliveries (user_id, status, origin_address, destination_address, requested_at, estimated_delivery_time, completed_at, price, memo, created_at, updated_at)
VALUES
    (1, 'RECEIVED', '서울시 송파구 올림픽로 300', '서울시 강남구 역삼로 123', CURRENT_TIMESTAMP(), DATEADD('HOUR', 1, CURRENT_TIMESTAMP()), null, 5000, '문 앞에 놓아주세요', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
    (1, 'ASSIGNED', '서울시 마포구 월드컵북로 396', '서울시 용산구 이태원로 200', DATEADD('HOUR', -3, CURRENT_TIMESTAMP()), DATEADD('HOUR', -2, CURRENT_TIMESTAMP()), null, 6000, '전화 부탁드립니다', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
    (1, 'IN_TRANSIT', '서울시 중구 을지로 100', '서울시 영등포구 여의도동 45', DATEADD('HOUR', -5, CURRENT_TIMESTAMP()), DATEADD('HOUR', -4, CURRENT_TIMESTAMP()), null, 7000, '부재시 경비실에 맡겨주세요', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
    (1, 'DELIVERED', '서울시 강동구 천호대로 1234', '서울시 서초구 서초대로 100', DATEADD('DAY', -1, CURRENT_TIMESTAMP()), DATEADD('HOUR', -23, CURRENT_TIMESTAMP()), DATEADD('HOUR', -22, CURRENT_TIMESTAMP()), 8000, null, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
    (1, 'CANCELLED', '서울시 은평구 불광로 123', '서울시 노원구 노원로 300', DATEADD('DAY', -2, CURRENT_TIMESTAMP()), null, null, 4500, '주문 취소합니다', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP());

-- 배달 상태가 변경 가능한 상태인 배달 추가 (RECEIVED, ASSIGNED 상태)
INSERT INTO deliveries (user_id, status, origin_address, destination_address, requested_at, estimated_delivery_time, completed_at, price, memo, created_at, updated_at)
VALUES
    (1, 'RECEIVED', '서울시 동작구 상도로 123', '서울시 관악구 관악로 123', CURRENT_TIMESTAMP(), DATEADD('HOUR', 1, CURRENT_TIMESTAMP()), null, 5500, '도착 전 연락주세요', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
    (1, 'ASSIGNED', '서울시 성북구 동소문로 123', '서울시 강북구 도봉로 123', CURRENT_TIMESTAMP(), DATEADD('HOUR', 1, CURRENT_TIMESTAMP()), null, 6500, '안전하게 배달해주세요', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP());