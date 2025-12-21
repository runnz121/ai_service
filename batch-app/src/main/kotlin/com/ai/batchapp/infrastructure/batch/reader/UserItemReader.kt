package com.ai.batchapp.infrastructure.batch.reader

import com.ai.batchapp.infrastructure.persistence.entity.UserEntity
import jakarta.persistence.EntityManagerFactory
import org.springframework.batch.item.database.JpaPagingItemReader
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder
import org.springframework.stereotype.Component

@Component
class UserItemReader(
    private val entityManagerFactory: EntityManagerFactory
) {

    fun reader(): JpaPagingItemReader<UserEntity> {
        return JpaPagingItemReaderBuilder<UserEntity>()
            .name("userItemReader")
            .entityManagerFactory(entityManagerFactory)
            .queryString(
                """
                SELECT u FROM UserEntity u
                LEFT JOIN FETCH u.profile p
                WHERE u.deletedAt IS NULL
                ORDER BY u.id
                """.trimIndent()
            )
            .pageSize(500)
            .saveState(false)
            .build()
    }
}
