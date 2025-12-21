package com.ai.batchapp.infrastructure.batch.reader

import com.ai.batchapp.infrastructure.persistence.entity.ProductEntity
import jakarta.persistence.EntityManagerFactory
import org.springframework.batch.item.database.JpaPagingItemReader
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder
import org.springframework.stereotype.Component

@Component
class ProductItemReader(
    private val entityManagerFactory: EntityManagerFactory
) {

    fun reader(): JpaPagingItemReader<ProductEntity> {
        return JpaPagingItemReaderBuilder<ProductEntity>()
            .name("productItemReader")
            .entityManagerFactory(entityManagerFactory)
            .queryString(
                """
                SELECT DISTINCT p FROM ProductEntity p
                LEFT JOIN FETCH p.detail d
                LEFT JOIN FETCH p.images i
                WHERE p.deletedAt IS NULL
                ORDER BY p.id
                """.trimIndent()
            )
            .pageSize(500)
            .saveState(false)
            .build()
    }
}
