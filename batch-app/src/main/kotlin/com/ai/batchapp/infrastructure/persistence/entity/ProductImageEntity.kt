package com.ai.batchapp.infrastructure.persistence.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "product_images")
data class ProductImageEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    val product: ProductEntity,

    @Column(name = "image_url", nullable = false)
    val imageUrl: String,

    @Column(name = "image_type")
    val imageType: String = "PRODUCT",

    @Column(name = "display_order")
    val displayOrder: Int = 0,

    @Column(name = "alt_text")
    val altText: String? = null,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
)
