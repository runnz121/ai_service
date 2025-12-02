# AI 검색 추천 서비스

FastMCP를 활용한 지능형 검색 추천 시스템

## 프로젝트 개요

이 프로젝트는 FastMCP를 통합하여 다양한 외부 검색 서비스를 연동하고, LLM을 활용한 지능형 검색 추천 서비스를 제공합니다.
Kafka 기반의 데이터 파이프라인을 통해 대량의 데이터를 처리하고, Elasticsearch를 통해 효율적인 검색을 지원합니다.

## 기술 스택

- **Language**: Kotlin 1.9.25
- **Framework**: Spring Boot 3.5.7
- **Java**: 21
- **AI**: Spring AI 1.1.0
- **Message Queue**: Apache Kafka
- **Search Engine**: Elasticsearch
- **Cache**: Redis
- **Database**: MySQL

## 모듈 구조

### api-service
- LLM 호출 및 추천 API 제공
- Kafka Producer/Consumer 구현
- REST API 엔드포인트

### batch-app
- Elasticsearch 색인 배치 작업
- 데이터 전처리 및 변환
- 스케줄링 작업

### fast-mcp (예정)
- FastMCP 통합 모듈
- 다양한 외부 검색 API 연동
- 검색 서비스 오케스트레이션

## 아키텍처

```
[외부 검색 API] ← [FastMCP] → [API Service] → [LLM]
                                      ↓
                                  [Kafka]
                                      ↓
                              [Batch App] → [Elasticsearch]
                                      ↓
                                  [Redis Cache]
```

## 시작하기

### 사전 요구사항

- JDK 21
- Docker & Docker Compose
- Gradle

### Kafka 실행

```bash
docker-compose up -d
```

Kafka UI: http://localhost:8989

### 애플리케이션 실행

```bash
# API 서비스 실행
./gradlew :api-service:bootRun

# 배치 애플리케이션 실행
./gradlew :batch-app:bootRun
```

## 주요 기능

- **지능형 검색**: LLM을 활용한 자연어 기반 검색
- **검색 추천**: 사용자 의도를 파악한 맞춤형 추천
- **실시간 데이터 처리**: Kafka를 통한 스트리밍 파이프라인
- **대용량 색인**: Elasticsearch를 통한 효율적인 검색
- **외부 API 통합**: FastMCP를 통한 다양한 검색 서비스 연동

## 개발 상태

- [x] 프로젝트 초기 설정
- [x] 멀티 모듈 구조 (api-service, batch-app)
- [x] Kafka 환경 구성
- [x] Kafka Producer/Consumer 구현
- [ ] FastMCP 모듈 추가
- [ ] LLM 통합
- [ ] Elasticsearch 색인 배치
- [ ] 검색 추천 API 구현

## 라이센스

이 프로젝트는 사내 프로젝트입니다.
