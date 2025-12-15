package com.ai.batchapp.domain

import org.springframework.data.annotation.Id
import org.springframework.data.elasticsearch.annotations.Document
import org.springframework.data.elasticsearch.annotations.Field
import org.springframework.data.elasticsearch.annotations.FieldType
import org.springframework.data.elasticsearch.annotations.DateFormat
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Elasticsearch 사용자 검색 인덱스 Document
 * MySQL users, user_profiles 테이블 데이터를 기반으로 검색용 인덱스 생성
 *
 * IMPORTANT:
 * - Index mapping은 JSON 파일로 관리됨
 * - 매핑 파일 위치: src/main/resources/elasticsearch/mappings/users_search.json
 * - Nori analyzer 적용 필드: username, fullName, bio, address, searchText
 * - 인덱스는 JSON 파일을 사용하여 수동으로 생성해야 함
 *
 * @see resources/elasticsearch/mappings/users_search.json
 */
@Document(indexName = "users_search", createIndex = false)
data class UserDocument(
    @Id
    val id: String? = null,

    // 기본 사용자 정보 (users 테이블)
    @Field(type = FieldType.Keyword)
    val email: String,

    @Field(type = FieldType.Text, analyzer = "standard")
    val username: String,

    @Field(type = FieldType.Keyword)
    val status: String = "ACTIVE",

    // 사용자 프로필 정보 (user_profiles 테이블)
    @Field(type = FieldType.Text, analyzer = "standard")
    val fullName: String? = null,

    @Field(type = FieldType.Keyword)
    val phoneNumber: String? = null,

    @Field(type = FieldType.Text)
    val address: String? = null,

    @Field(type = FieldType.Keyword)
    val city: String? = null,

    @Field(type = FieldType.Keyword)
    val country: String? = null,

    @Field(type = FieldType.Keyword)
    val postalCode: String? = null,

    @Field(
        type = FieldType.Date,
        format = [DateFormat.date]
    )
    val birthDate: LocalDate? = null,

    @Field(type = FieldType.Keyword)
    val gender: String? = null,

    @Field(type = FieldType.Keyword)
    val profileImageUrl: String? = null,

    @Field(type = FieldType.Text, analyzer = "standard")
    val bio: String? = null,

    // 검색 최적화 필드
    @Field(type = FieldType.Text, analyzer = "standard")
    val searchText: String? = null, // username + fullName + email + bio 통합 검색용

    // 사용자 활동 통계 (선택적, 향후 확장)
    @Field(type = FieldType.Integer)
    val searchCount: Int? = null,

    @Field(type = FieldType.Integer)
    val purchaseCount: Int? = null,

    @Field(
        type = FieldType.Date,
        format = [DateFormat.date_hour_minute_second_millis, DateFormat.epoch_millis]
    )
    val lastActivityAt: LocalDateTime? = null,

    // 선호 카테고리 (사용자 검색 로그 분석 기반)
    @Field(type = FieldType.Keyword)
    val preferredCategories: List<String>? = null,

    // 타임스탬프
    @Field(
        type = FieldType.Date,
        format = [DateFormat.date_hour_minute_second_millis, DateFormat.epoch_millis]
    )
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Field(
        type = FieldType.Date,
        format = [DateFormat.date_hour_minute_second_millis, DateFormat.epoch_millis]
    )
    val updatedAt: LocalDateTime = LocalDateTime.now(),

    @Field(
        type = FieldType.Date,
        format = [DateFormat.date_hour_minute_second_millis, DateFormat.epoch_millis]
    )
    val indexedAt: LocalDateTime = LocalDateTime.now(),

    @Field(
        type = FieldType.Date,
        format = [DateFormat.date_hour_minute_second_millis, DateFormat.epoch_millis]
    )
    val deletedAt: LocalDateTime? = null
) {
    companion object {
        /**
         * 검색 텍스트 생성 헬퍼 함수
         */
        fun buildSearchText(
            username: String,
            fullName: String?,
            email: String,
            bio: String?,
            city: String?,
            country: String?
        ): String {
            return listOfNotNull(
                username,
                fullName,
                email,
                bio,
                city,
                country
            ).joinToString(" ")
        }
    }

    /**
     * soft delete 확인 함수
     */
    fun isDeleted(): Boolean = deletedAt != null

    /**
     * 활성 사용자 여부 확인
     */
    fun isActive(): Boolean = status == "ACTIVE" && !isDeleted()
}
