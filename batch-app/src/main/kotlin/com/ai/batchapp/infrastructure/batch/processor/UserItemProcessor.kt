package com.ai.batchapp.infrastructure.batch.processor

import com.ai.batchapp.domain.UserDocument
import com.ai.batchapp.infrastructure.persistence.entity.UserEntity
import org.slf4j.LoggerFactory
import org.springframework.batch.item.ItemProcessor
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class UserItemProcessor : ItemProcessor<UserEntity, UserDocument> {

    private val logger = LoggerFactory.getLogger(javaClass)

    override fun process(item: UserEntity): UserDocument? {
        return try {
            val profile = item.profile

            val searchText = UserDocument.buildSearchText(
                username = item.username,
                fullName = profile?.fullName,
                email = item.email,
                bio = profile?.bio,
                city = profile?.city,
                country = profile?.country
            )

            UserDocument(
                id = item.id?.toString(),
                email = item.email,
                username = item.username,
                status = item.status,
                fullName = profile?.fullName,
                phoneNumber = profile?.phoneNumber,
                address = profile?.address,
                city = profile?.city,
                country = profile?.country,
                postalCode = profile?.postalCode,
                birthDate = profile?.birthDate,
                gender = profile?.gender,
                profileImageUrl = profile?.profileImageUrl,
                bio = profile?.bio,
                searchText = searchText,
                searchCount = null,
                purchaseCount = null,
                lastActivityAt = null,
                preferredCategories = null,
                createdAt = item.createdAt,
                updatedAt = item.updatedAt,
                indexedAt = LocalDateTime.now(),
                deletedAt = item.deletedAt
            )
        } catch (e: Exception) {
            logger.error("Error processing user id=${item.id}: ${e.message}", e)
            null
        }
    }
}
