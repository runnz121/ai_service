package com.ai.batchapp.infrastructure.persistence.entity

import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
@Table(name = "product_details")
data class ProductDetailEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false, unique = true)
    val product: ProductEntity,

    @Column(columnDefinition = "TEXT")
    val description: String? = null,

    @Column(name = "long_description", columnDefinition = "LONGTEXT")
    val longDescription: String? = null,

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "JSON")
    val specifications: Map<String, Any>? = null,

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "JSON")
    val features: List<String>? = null,

    val dimensions: String? = null,

    @Column(precision = 10, scale = 2)
    val weight: BigDecimal? = null,

    val manufacturer: String? = null,

    @Column(name = "origin_country")
    val originCountry: String? = null,

    @Column(name = "warranty_period")
    val warrantyPeriod: Int? = null,

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "JSON")
    val tags: List<String>? = null,

    @Column(name = "meta_title")
    val metaTitle: String? = null,

    @Column(name = "meta_description", columnDefinition = "TEXT")
    val metaDescription: String? = null,

    @Column(name = "meta_keywords")
    val metaKeywords: String? = null,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    val updatedAt: LocalDateTime = LocalDateTime.now()
)
