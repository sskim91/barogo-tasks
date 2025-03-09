# Barogo 배달 API 서비스

## 프로젝트 개요

본 프로젝트는 배달 서비스를 위한 RESTful API를 제공하는 Spring Boot 애플리케이션입니다. 회원 관리(가입, 로그인), 배달 조회, 배달 주문 수정 등의 기능을 제공하며, Spring Security와 JWT를 활용한 인증 시스템을 구현하였습니다.

## 주요 기능

### 1. 회원 관리
- **회원 가입**: 사용자 ID, 비밀번호, 이름 정보를 받아 회원 가입 처리
- **로그인**: 사용자 인증 후 JWT 액세스 토큰 및 리프레시 토큰 발급
- **토큰 갱신**: 리프레시 토큰을 사용한 액세스 토큰 재발급
- **로그아웃**: 리프레시 토큰 무효화를 통한 로그아웃 처리

### 2. 배달 관리
- **배달 조회**: 기간 기반 배달 목록 조회 (최대 3일)
- **배달 주소 변경**: 배달 상태에 따른 도착지 주소 변경

## 기술 스택

- **Framework**: Spring Boot 3.3.9
- **Language**: Java 17
- **Build Tool**: Gradle
- **Database**: H2 Database (인메모리)
- **Authentication**: Spring Security, JWT
- **Documentation**: Spring REST Docs
- **Testing**: JUnit 5, Mockito

## 프로젝트 구조

프로젝트는 계층형 아키텍처를 따르며, 다음과 같은 주요 패키지로 구성되어 있습니다:

```
src/
├── main/
│   ├── java/com/barogo/app/
│   │   ├── config/        - 애플리케이션 설정 클래스
│   │   │   ├── jwt/       - JWT 관련 설정
│   │   │   └── security/  - 보안 관련 설정
│   │   ├── controller/    - API 엔드포인트 컨트롤러
│   │   ├── domain/        - 도메인 엔티티 클래스
│   │   ├── dto/           - 데이터 전송 객체
│   │   │   ├── request/   - 요청 DTO
│   │   │   └── response/  - 응답 DTO
│   │   ├── exception/     - 예외 처리 클래스
│   │   ├── repository/    - 데이터 접근 인터페이스
│   │   └── service/       - 비즈니스 로직 서비스
│   └── resources/         - 애플리케이션 리소스 및 설정 파일
└── test/                  - 테스트 코드
    ├── java/              - 단위 및 통합 테스트
    └── docs/              - API 문서화 테스트
```

## 실행 방법

### 개발 환경 설정
- JDK 17 이상 설치
- Gradle 설치 (또는 프로젝트의 gradlew 사용)

### 애플리케이션 실행
```bash
# 프로젝트 클론
git clone https://github.com/yourusername/barogo-tasks.git
cd barogo-tasks

# 애플리케이션 빌드 및 실행
./gradlew bootRun
```

애플리케이션은 기본적으로 `http://localhost:8080`에서 실행됩니다.

### API 문서 생성
```bash
./gradlew asciidoctor
```
생성된 API 문서는 `build/docs/asciidoc` 디렉토리에서 확인할 수 있으며, 애플리케이션 실행 시 `http://localhost:8080/docs/index.html`에서도 접근 가능합니다.

## API 엔드포인트

### 회원 API
- `POST /api/v1/users/signup` - 회원 가입
- `POST /api/v1/users/login` - 로그인
- `POST /api/v1/users/refresh` - 토큰 갱신
- `POST /api/v1/users/logout` - 로그아웃

### 배달 API
- `GET /api/v1/deliveries` - 배달 조회
- `PATCH /api/v1/deliveries/{deliveryId}/destination` - 배달 주소 변경

## 인증 시스템

### JWT 토큰 관리
- **액세스 토큰**: API 접근을 위한 단기 토큰 (유효기간: 1시간)
- **리프레시 토큰**: 액세스 토큰 갱신을 위한 장기 토큰 (유효기간: 7일)
- **토큰 저장**: 리프레시 토큰은 데이터베이스에 저장되어 관리됨

### 인증 절차
1. 사용자 로그인 시 액세스 토큰과 리프레시 토큰 발급
2. API 요청 시 Authorization 헤더에 Bearer 토큰 포함
3. 액세스 토큰 만료 시 리프레시 토큰을 사용하여 새 액세스 토큰 발급
4. 로그아웃 시 리프레시 토큰 무효화

## 테스트

프로젝트는 다음과 같은 테스트 범주를 포함합니다:

- **단위 테스트**: 서비스 및 컨트롤러의 개별 기능 테스트
- **통합 테스트**: 전체 API 흐름 테스트
- **문서화 테스트**: API 문서 생성을 위한 테스트

테스트 실행:
```bash
./gradlew test
```