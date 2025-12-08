# Claude AI 프로젝트 컨텍스트

## 프로젝트 개요

FastMCP를 통합한 지능형 검색 추천 서비스 프로젝트입니다. 이 시스템은 LLM을 활용하여 사용자의 검색 의도를 파악하고, 다양한 외부 검색 서비스를 통합하여 맞춤형 추천을 제공합니다.

## 핵심 아키텍처

### 실시간 검색 아키텍처 (구현 완료)

```
사용자 검색 요청
    ↓
[API Service] (Spring Boot + Spring AI)
    ↓ HTTP REST (RestTemplate)
[FastMCP Server] (Python FastAPI)
    ├─ 쿼리 분석 및 인텍스 선택
    ↓ HTTP REST (httpx)
[Search API] (Spring Boot + Spring Data Elasticsearch)
    ↓ ES Query DSL
[Elasticsearch 8.11.1]
    ↓
검색 결과 반환 (역순으로 전달)
```

**데이터 흐름**:
`User → API Service → FastMCP Server → Search API → Elasticsearch`

### 배치 색인 아키텍처 (향후 통합 예정)

```
[API Service / External Sources]
    ↓
[Kafka Pipeline] (Pub/Sub)
    ↓
[Batch App] (Spring Batch)
    ↓
[Elasticsearch] (Bulk Indexing)
```

## 모듈 구조 및 책임

### 1. api-service
**역할**: LLM 기반 검색 추천 API 서버

**책임**:
- Spring AI를 통한 LLM 호출 및 응답 처리
- 사용자 검색 쿼리 분석 및 의도 파악
- FastMCP Server를 통한 검색 요청 오케스트레이션
- REST API 엔드포인트 제공 (`/api/search`)
- Kafka Producer로서 검색 로그 및 데이터 전송
- Kafka Consumer로서 실시간 이벤트 처리

**주요 기능**:
- 자연어 기반 검색 쿼리 처리
- LLM을 활용한 검색 의도 분석
- 검색 결과 추천 알고리즘
- 실시간 검색 로그 수집

**구현 상세**:
- `SearchController`: GET/POST `/api/search` 엔드포인트
- `SearchService`: 검색 비즈니스 로직
- `FastMcpClient`: RestTemplate 기반 HTTP 클라이언트
- FastMCP Server와 통신하여 검색 수행

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

### 3. search-api (구현 완료)
**역할**: Elasticsearch 검색 API 서버

**기술 스택**:
- Spring Boot 3.5.7
- Spring Data Elasticsearch
- Elasticsearch 8.11.1

**책임**:
- Elasticsearch와의 직접적인 통신
- 검색 쿼리 실행 및 결과 반환
- 인덱스별 도메인 모델 관리
- CRUD 작업 및 복잡한 검색 로직 제공

**주요 기능**:
- Product 도메인 검색 API
- 전문 검색 (Full-text search)
- 카테고리 필터링
- 가격 범위 검색
- 페이징 처리

**구현 상세**:
- `ProductController`: `/api/products` 엔드포인트
  - `POST /api/products/search`: 검색 API
  - `POST /api/products`: 상품 등록
  - `GET /api/products/{id}`: 상품 조회
  - `GET /api/products/health`: 헬스 체크
- `ProductService`: 비즈니스 로직
- `ProductRepository`: Spring Data Elasticsearch Repository
- `Product`: ES Document 매핑 엔티티
  - `@Document(indexName = "products")`
  - LocalDateTime 필드에 DateFormat 설정 필수

**주의사항**:
- LocalDateTime 필드는 `@Field(format = [DateFormat.date_hour_minute_second_millis, DateFormat.epoch_millis])` 설정 필요
- Java 21 필수 (Java 11 사용 시 빌드 오류)

### 4. fastmcp-server (구현 완료)
**역할**: MCP(Model Context Protocol) 기반 지능형 검색 라우터

**기술 스택**:
- Python 3.11
- FastAPI
- httpx (HTTP 클라이언트)

**책임**:
- 사용자 쿼리 분석 및 인덱스 선택 로직
- Search API 엔드포인트 라우팅
- 검색 요청/응답 변환 및 통합
- 향후 다양한 외부 검색 API 통합 준비

**주요 기능**:
- 쿼리 키워드 분석을 통한 최적 인덱스 결정
- 동적 엔드포인트 선택
- 검색 결과 통합 및 표준 응답 형식 제공

**구현 상세**:
- `POST /mcp/search`: 통합 검색 엔드포인트
- `GET /health`: 헬스 체크
- `determine_best_index()`: 쿼리 분석 함수
  - 키워드 기반 인덱스 선택 (예: product, item, buy → products 인덱스)
  - 카테고리 정보 활용
- `call_search_api()`: Search API 호출 함수

**향후 통합 예정 검색 서비스**:
- 네이버 검색 API
- 구글 검색 API
- 기타 도메인 특화 검색 서비스
- 커스텀 외부 검색 엔드포인트

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

- **Language**:
  - Kotlin 1.9.25 (api-service, batch-app, search-api)
  - Python 3.11 (fastmcp-server)
- **Framework**:
  - Spring Boot 3.5.7
  - FastAPI (fastmcp-server)
- **Java**: 21
- **AI**: Spring AI 1.1.0
- **Message Queue**: Apache Kafka
- **Search Engine**: Elasticsearch 8.11.1
- **Cache**: Redis
- **Database**: MySQL
- **HTTP Client**:
  - RestTemplate (Spring modules)
  - httpx (Python fastmcp-server)
- **Containerization**: Docker, Docker Compose

## 코딩 컨벤션

### Kotlin 스타일
- 코틀린 공식 코딩 컨벤션 준수
- 함수형 프로그래밍 스타일 선호 (map, filter, fold 등)
- Null Safety 적극 활용 (?, !!, let, run 등)
- Data Class 활용한 불변 객체 설계

### 패키지 구조

**Spring 모듈** (api-service, batch-app, search-api):
```
src/main/kotlin/com/ai/{module}
├── presentation
│   └── controller     # REST API 컨트롤러
├── application
│   └── service        # 비즈니스 로직
├── domain             # 도메인 모델
├── infrastructure
│   ├── repository     # 데이터 접근 계층
│   ├── config         # 설정 클래스
│   ├── kafka          # Kafka Producer/Consumer
│   ├── fastmcp        # FastMCP 클라이언트 (api-service only)
│   └── batch          # 배치 작업 (batch-app only)
```

**Python 모듈** (fastmcp-server):
```
fastmcp-server/
├── main.py            # FastAPI 애플리케이션
├── requirements.txt   # 의존성 관리
├── Dockerfile         # 컨테이너 이미지 빌드
└── README.md
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

### Search API 개발 시
1. Elasticsearch Document 매핑 시 날짜 필드 DateFormat 설정 필수
2. Repository 메서드는 Pageable 파라미터로 페이징 지원
3. 복잡한 쿼리는 Query DSL 사용 고려
4. Java 21 이상 버전 사용 필수

### FastMCP Server 개발 시
1. 쿼리 분석 로직은 키워드 기반으로 시작, 향후 ML 모델 적용 고려
2. Search API 호출 시 타임아웃 설정 (기본 5초)
3. 외부 API 통합 시 서킷 브레이커 패턴 적용
4. 비동기 HTTP 클라이언트 (httpx) 활용
5. API Rate Limiting 고려

## 환경 설정

### Kafka 토픽 구조
- `search-query`: 사용자 검색 쿼리
- `search-result`: 검색 결과 데이터
- `click-log`: 사용자 클릭 로그
- `external-api-response`: 외부 API 응답 데이터
- `indexing-job`: Elasticsearch 색인 작업

### Elasticsearch 인덱스 구조

**현재 구현된 인덱스**:
- `products`: 상품 검색 인덱스 (구현 완료)
  - 필드: id, name, description, category, price, stock, createdAt
  - 전문 검색, 카테고리 필터링, 가격 범위 검색 지원

**향후 추가 예정 인덱스**:
- `search-logs`: 검색 로그 분석용
- `recommendations`: 추천 결과 데이터
- `users`: 사용자 프로필
- `contents`: 콘텐츠 검색용

## 로컬 개발 환경

### 전체 스택 실행 (Docker Compose)
```bash
# Java 21 설정
export JAVA_HOME=/Users/pupu/Library/Java/JavaVirtualMachines/corretto-21.0.3/Contents/Home

# 전체 서비스 실행 (빌드 포함)
docker-compose up -d --build

# 특정 서비스만 실행
docker-compose up -d elasticsearch
docker-compose up -d search-api
docker-compose up -d fastmcp-server
```

### 서비스 포트 및 접근
- **Kafka**: http://localhost:9092
- **Kafka UI**: http://localhost:8989
- **Zookeeper**: http://localhost:2181
- **Elasticsearch**: http://localhost:9200
- **Search API**: http://localhost:8081
- **FastMCP Server**: http://localhost:8082
- **API Service**: http://localhost:8080 (로컬 실행 시)

### 개별 애플리케이션 실행 (로컬)
```bash
# Java 21 필수
export JAVA_HOME=/Users/pupu/Library/Java/JavaVirtualMachines/corretto-21.0.3/Contents/Home

# API 서비스
./gradlew :api-service:bootRun

# 배치 애플리케이션
./gradlew :batch-app:bootRun

# Search API
./gradlew :search-api:bootRun
```

### 엔드포인트 테스트 예시

**1. Search API 직접 테스트**:
```bash
# 상품 등록
curl -X POST http://localhost:8081/api/products \
  -H "Content-Type: application/json" \
  -d '{
    "name": "iPhone 15",
    "description": "Latest Apple smartphone",
    "category": "Electronics",
    "price": 1200.00,
    "stock": 50
  }'

# 상품 검색
curl -X POST http://localhost:8081/api/products/search \
  -H "Content-Type: application/json" \
  -d '{
    "keyword": "iPhone",
    "page": 0,
    "size": 10
  }'
```

**2. FastMCP Server를 통한 검색**:
```bash
curl -X POST http://localhost:8082/mcp/search \
  -H "Content-Type: application/json" \
  -d '{
    "query": "phone",
    "category": "Electronics"
  }'
```

**3. API Service를 통한 전체 흐름 테스트**:
```bash
curl -X POST http://localhost:8080/api/search \
  -H "Content-Type: application/json" \
  -d '{
    "query": "smartphone",
    "category": "Electronics"
  }'
```

## 향후 계획

### 완료된 작업
✅ **Search API 모듈 구현**
   - Spring Data Elasticsearch 통합
   - Product 도메인 검색 API
   - Docker 컨테이너 환경 구축

✅ **FastMCP Server 기본 구현**
   - Python FastAPI 서버
   - 쿼리 분석 기반 인덱스 선택 로직
   - Search API 라우팅

✅ **API Service와 FastMCP 통합**
   - RestTemplate 기반 HTTP 클라이언트
   - 전체 검색 흐름 구현

### 진행 예정 작업

1. **FastMCP 기능 확장**
   - 외부 검색 API 통합 (네이버, 구글 등)
   - ML 기반 쿼리 분석 및 인덱스 선택
   - 응답 캐싱 및 최적화
   - 서킷 브레이커 패턴 적용

2. **LLM 통합 강화**
   - Spring AI를 통한 LLM 쿼리 분석
   - OpenAI, Anthropic Claude 등 다중 LLM 지원
   - 프롬프트 엔지니어링 최적화
   - 의도 기반 검색 결과 재구성

3. **검색 품질 향상**
   - 다양한 도메인 인덱스 추가 (users, contents, logs)
   - 하이브리드 검색 (키워드 + 벡터 검색)
   - 사용자 피드백 기반 학습
   - 개인화 추천 알고리즘

4. **성능 최적화**
   - Redis 캐싱 전략 도입
   - Elasticsearch 쿼리 최적화
   - FastMCP 응답 캐싱
   - 비동기 처리 확대

5. **Kafka 파이프라인 통합**
   - 검색 로그 수집 및 분석
   - Batch App과 연동
   - 실시간 색인 업데이트

## 문제 해결 가이드

### Java 버전 오류
**증상**: `Dependency requires at least JVM runtime version 17. This build uses a Java 11 JVM.`

**해결**:
```bash
export JAVA_HOME=/Users/pupu/Library/Java/JavaVirtualMachines/corretto-21.0.3/Contents/Home
./gradlew clean build
```
- Java 21 이상 필수
- 빌드 전 JAVA_HOME 환경변수 설정 확인

### Elasticsearch LocalDateTime 변환 오류
**증상**: `Failed to convert from type [java.lang.String] to type [java.time.LocalDateTime]`

**해결**:
```kotlin
@Field(
    type = FieldType.Date,
    format = [DateFormat.date_hour_minute_second_millis, DateFormat.epoch_millis]
)
val createdAt: LocalDateTime = LocalDateTime.now()
```
- Document 엔티티의 날짜 필드에 DateFormat 명시 필수

### Docker Health Check 실패
**증상**: 컨테이너가 unhealthy 상태로 표시되며 의존 서비스 시작 안됨

**해결**:
```yaml
# docker-compose.yml
depends_on:
  - search-api  # condition 제거
```
- 엄격한 health check 조건 제거
- 또는 health check 엔드포인트 구현 확인

### FastMCP Server 연결 오류
**증상**: API Service에서 FastMCP Server 호출 실패

**확인 사항**:
1. FastMCP Server 컨테이너 실행 상태 확인
   ```bash
   docker-compose ps
   docker logs fastmcp-server
   ```
2. 네트워크 연결 확인
   ```bash
   docker network inspect ai_service_kafka-network
   ```
3. application.yml의 fastmcp.url 설정 확인
   - 로컬: `http://localhost:8082`
   - 도커: `http://fastmcp-server:8082`

### Kafka 연결 실패
**해결**:
- docker-compose로 Kafka가 실행 중인지 확인
- application.yml의 bootstrap-servers 설정 확인

### LLM API 오류
**해결**:
- API 키 설정 확인
- Rate Limiting 확인
- 타임아웃 설정 검토

### Elasticsearch 색인 실패
**해결**:
- 인덱스 매핑 확인
- 데이터 형식 검증
- 벌크 요청 사이즈 조정
- ES 컨테이너 로그 확인: `docker logs elasticsearch`

## 구현 상세

### 전체 검색 흐름

**1단계: API Service (사용자 요청 수신)**
```kotlin
// SearchController.kt
@RestController
@RequestMapping("/api/search")
class SearchController(private val searchService: SearchService) {
    @PostMapping
    fun searchPost(@RequestBody request: SearchRequest): ResponseEntity<MCPSearchResponse> {
        val response = searchService.search(request.query, request.category)
        return ResponseEntity.ok(response)
    }
}
```

**2단계: FastMCP Client (HTTP 요청)**
```kotlin
// FastMcpClient.kt
@Component
class FastMcpClient(
    @Value("\${fastmcp.url:http://localhost:8082}")
    private val fastMcpUrl: String,
    private val restTemplate: RestTemplate
) {
    fun search(query: String, category: String? = null): MCPSearchResponse {
        val url = "$fastMcpUrl/mcp/search"
        val request = MCPSearchRequest(query = query, category = category)
        return restTemplate.postForObject(url, request, MCPSearchResponse::class.java)
            ?: throw RuntimeException("Failed to get response from FastMCP")
    }
}
```

**3단계: FastMCP Server (쿼리 분석 및 라우팅)**
```python
# main.py
def determine_best_index(query: str, category: Optional[str] = None) -> tuple[str, str]:
    """쿼리를 분석하여 가장 적합한 인덱스와 엔드포인트를 결정"""
    query_lower = query.lower()

    # 키워드 기반 인덱스 선택
    if any(keyword in query_lower for keyword in ['product', 'item', 'buy', 'purchase']):
        return "products", "/api/products/search"

    if category:
        return "products", "/api/products/search"

    return "products", "/api/products/search"

@app.post("/mcp/search", response_model=MCPResponse)
async def mcp_search(request: SearchRequest):
    index_name, endpoint = determine_best_index(request.query, request.category)

    # Search API 호출
    search_payload = {
        "keyword": request.query,
        "page": 0,
        "size": 10
    }
    if request.category:
        search_payload["category"] = request.category

    search_result = await call_search_api(endpoint, search_payload)

    return MCPResponse(
        success=True,
        data=search_result,
        source=f"elasticsearch:{index_name}",
        error=None
    )
```

**4단계: Search API (Elasticsearch 검색)**
```kotlin
// ProductController.kt
@PostMapping("/search")
fun search(@RequestBody request: SearchRequest): ResponseEntity<SearchResponse> {
    val result = productService.search(request)
    return ResponseEntity.ok(result)
}

// ProductService.kt
fun search(request: SearchRequest): SearchResponse {
    val pageable = PageRequest.of(request.page, request.size)

    val page = when {
        request.keyword != null && request.category != null ->
            productRepository.findByNameContainingAndCategory(
                request.keyword, request.category, pageable
            )
        request.keyword != null ->
            productRepository.findByNameContaining(request.keyword, pageable)
        request.category != null ->
            productRepository.findByCategory(request.category, pageable)
        else -> productRepository.findAll(pageable)
    }

    return SearchResponse(
        content = page.content,
        totalElements = page.totalElements,
        totalPages = page.totalPages,
        currentPage = page.number
    )
}
```

**5단계: Elasticsearch (데이터 검색)**
```kotlin
// Product.kt - Document 매핑
@Document(indexName = "products")
data class Product(
    @Id
    val id: String? = null,

    @Field(type = FieldType.Text, analyzer = "standard")
    val name: String,

    @Field(type = FieldType.Text, analyzer = "standard")
    val description: String,

    @Field(type = FieldType.Keyword)
    val category: String,

    @Field(type = FieldType.Double)
    val price: Double,

    @Field(type = FieldType.Date,
           format = [DateFormat.date_hour_minute_second_millis, DateFormat.epoch_millis])
    val createdAt: LocalDateTime = LocalDateTime.now()
)
```

### Docker 컨테이너 구성

```yaml
# docker-compose.yml
services:
  elasticsearch:
    image: docker.elastic.co/elasticsearch/elasticsearch:8.11.1
    environment:
      - discovery.type=single-node
      - xpack.security.enabled=false
    ports:
      - "9200:9200"
    healthcheck:
      test: ["CMD-SHELL", "curl -f http://localhost:9200/_cluster/health"]
      interval: 30s

  search-api:
    build:
      context: .
      dockerfile: search-api/Dockerfile
    depends_on:
      elasticsearch:
        condition: service_healthy
    environment:
      - SPRING_ELASTICSEARCH_URIS=http://elasticsearch:9200
    ports:
      - "8081:8081"

  fastmcp-server:
    build:
      context: ./fastmcp-server
      dockerfile: Dockerfile
    depends_on:
      - search-api
    ports:
      - "8082:8082"
```

### 주요 설계 패턴

1. **레이어드 아키텍처**
   - Presentation Layer (Controller)
   - Application Layer (Service)
   - Infrastructure Layer (Repository, External Clients)

2. **마이크로서비스 패턴**
   - 각 모듈이 독립적으로 실행 가능
   - HTTP REST API로 통신
   - Docker 컨테이너로 격리

3. **API Gateway 패턴**
   - FastMCP Server가 Gateway 역할
   - 요청 라우팅 및 변환
   - 인덱스 선택 로직 중앙화

4. **Repository 패턴**
   - Spring Data Elasticsearch Repository
   - 데이터 접근 로직 추상화

## 참고 문서

- [Spring AI Documentation](https://docs.spring.io/spring-ai/reference/)
- [FastMCP Specification](https://modelcontextprotocol.io/)
- [Apache Kafka Documentation](https://kafka.apache.org/documentation/)
- [Elasticsearch Guide](https://www.elastic.co/guide/en/elasticsearch/reference/current/index.html)
- [Spring Data Elasticsearch](https://docs.spring.io/spring-data/elasticsearch/reference/)
- [FastAPI Documentation](https://fastapi.tiangolo.com/)
