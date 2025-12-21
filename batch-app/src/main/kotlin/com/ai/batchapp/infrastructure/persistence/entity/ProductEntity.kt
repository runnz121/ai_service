package com.ai.batchapp.infrastructure.persistence.entity

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
@Table(name = "products")
data class ProductEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false)
    val name: String,

    @Column(nullable = false)
    val category: String,

    val brand: String? = null,
    val sku: String? = null,

    @Column(nullable = false, precision = 15, scale = 2)
    val price: BigDecimal,

    @Column(name = "discount_price", precision = 15, scale = 2)
    val discountPrice: BigDecimal? = null,

    @Column(nullable = false)
    val stock: Int = 0,

    @Column(nullable = false)
    val status: String = "AVAILABLE",

    @Column(precision = 3, scale = 2)
    val rating: BigDecimal? = null,

    @Column(name = "review_count")
    val reviewCount: Int = 0,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    val updatedAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "deleted_at")
    val deletedAt: LocalDateTime? = null,

    @OneToOne(mappedBy = "product", fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
    var detail: ProductDetailEntity? = null,

    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
    var images: List<ProductImageEntity> = emptyList()
)
