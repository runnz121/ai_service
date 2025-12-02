# Claude AI 프로젝트 컨텍스트

## 프로젝트 개요

FastMCP를 통합한 지능형 검색 추천 서비스 프로젝트입니다. 이 시스템은 LLM을 활용하여 사용자의 검색 의도를 파악하고, 다양한 외부 검색 서비스를 통합하여 맞춤형 추천을 제공합니다.

## 핵심 아키텍처

```
사용자 요청
    ↓
[API Service]
    ↓ (LLM 호출)
[Spring AI + LLM]
    ↓
[FastMCP] ← 다양한 외부 검색 API (네이버, 구글, 기타 검색 서비스)
    ↓
[Kafka Pipeline] ← 대량 데이터 처리
    ↓
[Batch App] → [Elasticsearch 색인]
    ↓
[검색 결과 제공]
```

## 모듈 구조 및 책임

### 1. api-service
**역할**: LLM 기반 검색 추천 API 서버

**책임**:
- Spring AI를 통한 LLM 호출 및 응답 처리
- 사용자 검색 쿼리 분석 및 의도 파악
- FastMCP를 통한 외부 검색 API 오케스트레이션
- REST API 엔드포인트 제공
- Kafka Producer로서 검색 로그 및 데이터 전송
- Kafka Consumer로서 실시간 이벤트 처리

**주요 기능**:
- 자연어 기반 검색 쿼리 처리
- LLM을 활용한 검색 의도 분석
- 검색 결과 추천 알고리즘
- 실시간 검색 로그 수집

### 2. batch-app
**역할**: Elasticsearch 색인 및 데이터 처리 배치 시스템

**책임**:
- Kafka Consumer로서 대량 데이터 수신
- Elasticsearch 색인 데이터 전처리 및 변환
- 대용량 데이터 배치 색인 작업
- 스케줄링 기반 정기 작업 (색인 최적화, 데이터 정리 등)
- Spring Batch를 활용한 배치 작업 관리

**주요 기능**:
- Kafka에서 수신한 데이터의 Elasticsearch 색인
- 색인 데이터 품질 관리 및 검증
- 배치 작업 모니터링 및 에러 핸들링
- 대량 데이터 처리 파이프라인

### 3. fast-mcp (추가 예정)
**역할**: MCP(Model Context Protocol) 기반 외부 API 통합 모듈

**책임**:
- FastMCP를 통한 다양한 외부 검색 서비스 연동
- 검색 API 호출 및 응답 통합
- 외부 API 오케스트레이션 및 라우팅
- API 응답 캐싱 및 최적화

**통합 예정 검색 서비스**:
- 네이버 검색 API
- 구글 검색 API
- 기타 도메인 특화 검색 서비스
- 커스텀 검색 엔드포인트

## 데이터 파이프라인

### Kafka 기반 스트리밍 파이프라인

1. **데이터 수집**
   - api-service에서 사용자 검색 쿼리, 클릭 로그, 추천 결과 전송
   - FastMCP에서 외부 API 응답 데이터 전송

2. **데이터 처리**
   - Kafka Topic을 통한 데이터 스트리밍
   - 데이터 검증 및 필터링
   - 데이터 포맷 변환 및 정규화

3. **데이터 색인**
   - batch-app에서 Kafka Consumer로 데이터 수신
   - Elasticsearch 색인 형식으로 변환
   - 벌크 색인을 통한 대량 데이터 처리

## 기술 스택

- **Language**: Kotlin 1.9.25
- **Framework**: Spring Boot 3.5.7
- **Java**: 21
- **AI**: Spring AI 1.1.0
- **Message Queue**: Apache Kafka
- **Search Engine**: Elasticsearch
- **Cache**: Redis
- **Database**: MySQL

## 코딩 컨벤션

### Kotlin 스타일
- 코틀린 공식 코딩 컨벤션 준수
- 함수형 프로그래밍 스타일 선호 (map, filter, fold 등)
- Null Safety 적극 활용 (?, !!, let, run 등)
- Data Class 활용한 불변 객체 설계

### 패키지 구조
```
src/main/kotlin
├── controller     # REST API 컨트롤러
├── service        # 비즈니스 로직
├── domain         # 도메인 모델
├── repository     # 데이터 접근 계층
├── config         # 설정 클래스
├── kafka          # Kafka Producer/Consumer
├── batch          # 배치 작업 (batch-app only)
└── mcp            # MCP 관련 (fast-mcp only)
```

### 네이밍 규칙
- Controller: `*Controller`
- Service: `*Service`
- Repository: `*Repository`
- Kafka Consumer: `*Consumer`
- Kafka Producer: `*Producer`
- Config: `*Config`

## 중요 설계 결정사항

### 1. 비동기 처리
- Kafka를 통한 이벤트 기반 비동기 처리
- 실시간 요구사항이 없는 작업은 Kafka로 전송 후 배치 처리
- Coroutine 활용한 비동기 API 호출

### 2. 데이터 일관성
- Kafka의 at-least-once 전송 보장
- 멱등성(Idempotency) 처리를 통한 중복 방지
- 트랜잭션 처리가 필요한 경우 Outbox Pattern 고려

### 3. 확장성
- Kafka 파티셔닝을 통한 수평 확장
- Elasticsearch 샤딩을 통한 색인 분산
- 모듈별 독립 배포 가능한 구조

### 4. 모니터링
- Kafka 메시지 처리 상태 추적
- 배치 작업 성공/실패 로깅
- LLM API 호출 성능 및 비용 모니터링

## 개발 가이드라인

### API Service 개발 시
1. LLM 호출 시 타임아웃 및 재시도 로직 구현
2. FastMCP 호출 결과는 Redis에 캐싱하여 중복 호출 방지
3. 사용자 요청은 즉시 응답하고, 상세 처리는 Kafka로 비동기 처리
4. API 응답 시간은 3초 이내 유지

### Batch App 개발 시
1. Kafka Consumer의 배치 사이즈 및 처리 간격 최적화
2. Elasticsearch 벌크 색인 사용 (최소 100건 단위)
3. 배치 작업 실패 시 재처리 로직 구현
4. 데이터 품질 검증 후 색인 진행

### FastMCP 개발 시
1. 외부 API 호출은 서킷 브레이커 패턴 적용
2. API 응답 타임아웃 설정 (기본 5초)
3. 외부 API 장애 시 폴백(fallback) 전략 수립
4. API Rate Limiting 고려

## 환경 설정

### Kafka 토픽 구조
- `search-query`: 사용자 검색 쿼리
- `search-result`: 검색 결과 데이터
- `click-log`: 사용자 클릭 로그
- `external-api-response`: 외부 API 응답 데이터
- `indexing-job`: Elasticsearch 색인 작업

### Elasticsearch 인덱스 구조
- `search-documents`: 검색 대상 문서
- `search-logs`: 검색 로그 분석용
- `recommendations`: 추천 결과 데이터

## 로컬 개발 환경

### Kafka 실행
```bash
docker-compose up -d
```
- Kafka UI: http://localhost:8989

### 애플리케이션 실행
```bash
# API 서비스
./gradlew :api-service:bootRun

# 배치 애플리케이션
./gradlew :batch-app:bootRun
```

## 향후 계획

1. **FastMCP 모듈 구현**
   - MCP 프로토콜 기반 외부 API 통합
   - 다양한 검색 서비스 연동

2. **LLM 통합**
   - OpenAI, Anthropic Claude 등 다중 LLM 지원
   - 프롬프트 엔지니어링 최적화

3. **검색 품질 향상**
   - 사용자 피드백 기반 학습
   - 개인화 추천 알고리즘

4. **성능 최적화**
   - Redis 캐싱 전략 고도화
   - Elasticsearch 쿼리 최적화
   - Kafka 처리량 튜닝

## 문제 해결 가이드

### Kafka 연결 실패
- docker-compose로 Kafka가 실행 중인지 확인
- application.yml의 bootstrap-servers 설정 확인

### LLM API 오류
- API 키 설정 확인
- Rate Limiting 확인
- 타임아웃 설정 검토

### Elasticsearch 색인 실패
- 인덱스 매핑 확인
- 데이터 형식 검증
- 벌크 요청 사이즈 조정

## 참고 문서

- [Spring AI Documentation](https://docs.spring.io/spring-ai/reference/)
- [FastMCP Specification](https://modelcontextprotocol.io/)
- [Apache Kafka Documentation](https://kafka.apache.org/documentation/)
- [Elasticsearch Guide](https://www.elastic.co/guide/en/elasticsearch/reference/current/index.html)
