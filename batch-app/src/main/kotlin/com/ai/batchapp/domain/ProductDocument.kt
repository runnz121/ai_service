package com.ai.batchapp.domain

import org.springframework.data.annotation.Id
import org.springframework.data.elasticsearch.annotations.Document
import org.springframework.data.elasticsearch.annotations.Field
import org.springframework.data.elasticsearch.annotations.FieldType
import org.springframework.data.elasticsearch.annotations.DateFormat
import java.time.LocalDateTime

/**
 * Elasticsearch 상품 검색 인덱스 Document
 * MySQL products, product_details 테이블 데이터를 기반으로 검색용 인덱스 생성
 *
 * IMPORTANT:
 * - Index mapping은 JSON 파일로 관리됨
 * - 매핑 파일 위치: src/main/resources/elasticsearch/mappings/products_search.json
 * - Nori analyzer 적용 필드: name, description, longDescription, searchText
 * - 인덱스는 JSON 파일을 사용하여 수동으로 생성해야 함
 *
 * @see resources/elasticsearch/mappings/products_search.json
 */
@Document(indexName = "products_search", createIndex = false)
data class ProductDocument(
    @Id
    val id: String? = null,

    // 기본 상품 정보 (products 테이블)
    @Field(type = FieldType.Text, analyzer = "standard")
    val name: String,

    @Field(type = FieldType.Keyword)
    val category: String,

    @Field(type = FieldType.Keyword)
    val brand: String? = null,

    @Field(type = FieldType.Keyword)
    val sku: String? = null,

    @Field(type = FieldType.Double)
    val price: Double,

    @Field(type = FieldType.Double)
    val discountPrice: Double? = null,

    @Field(type = FieldType.Integer)
    val stock: Int = 0,

    @Field(type = FieldType.Keyword)
    val status: String = "AVAILABLE",

    @Field(type = FieldType.Double)
    val rating: Double? = null,

    @Field(type = FieldType.Integer)
    val reviewCount: Int = 0,

    // 상세 정보 (product_details 테이블)
    @Field(type = FieldType.Text, analyzer = "standard")
    val description: String? = null,

    @Field(type = FieldType.Text, analyzer = "standard")
    val longDescription: String? = null,

    @Field(type = FieldType.Object)
    val specifications: Map<String, Any>? = null,

    @Field(type = FieldType.Keyword)
    val features: List<String>? = null,

    @Field(type = FieldType.Keyword)
    val dimensions: String? = null,

    @Field(type = FieldType.Double)
    val weight: Double? = null,

    @Field(type = FieldType.Keyword)
    val manufacturer: String? = null,

    @Field(type = FieldType.Keyword)
    val originCountry: String? = null,

    @Field(type = FieldType.Integer)
    val warrantyPeriod: Int? = null,

    @Field(type = FieldType.Keyword)
    val tags: List<String>? = null,

    // 검색 최적화 필드
    @Field(type = FieldType.Text, analyzer = "standard")
    val searchText: String? = null, // name + description + tags 통합 검색용

    // 메타 정보
    @Field(type = FieldType.Keyword)
    val metaTitle: String? = null,

    @Field(type = FieldType.Text)
    val metaDescription: String? = null,

    @Field(type = FieldType.Keyword)
    val metaKeywords: List<String>? = null,

    // 이미지 URL 목록
    @Field(type = FieldType.Keyword)
    val imageUrls: List<String>? = null,

    @Field(type = FieldType.Keyword)
    val thumbnailUrl: String? = null,

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
    val indexedAt: LocalDateTime = LocalDateTime.now()
) {
    companion object {
        /**
         * 검색 텍스트 생성 헬퍼 함수
         */
        fun buildSearchText(
            name: String,
            description: String?,
            longDescription: String?,
            tags: List<String>?,
            features: List<String>?
        ): String {
            return listOfNotNull(
                name,
                description,
                longDescription,
                tags?.joinToString(" "),
                features?.joinToString(" ")
            ).joinToString(" ")
        }
    }
}
