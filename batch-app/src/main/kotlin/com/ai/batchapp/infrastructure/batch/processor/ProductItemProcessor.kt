package com.ai.batchapp.infrastructure.batch.processor

import com.ai.batchapp.domain.ProductDocument
import com.ai.batchapp.infrastructure.persistence.entity.ProductEntity
import org.slf4j.LoggerFactory
import org.springframework.batch.item.ItemProcessor
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class ProductItemProcessor : ItemProcessor<ProductEntity, ProductDocument> {

    private val logger = LoggerFactory.getLogger(javaClass)

    override fun process(item: ProductEntity): ProductDocument? {
        return try {
            val searchText = ProductDocument.buildSearchText(
                name = item.name,
                description = item.detail?.description,
                longDescription = item.detail?.longDescription,
                tags = item.detail?.tags,
                features = item.detail?.features
            )

            val imageUrls = item.images
                .sortedBy { it.displayOrder }
                .map { it.imageUrl }

            val thumbnailUrl = item.images
                .firstOrNull { it.imageType == "THUMBNAIL" }
                ?.imageUrl
                ?: imageUrls.firstOrNull()

            ProductDocument(
                id = item.id?.toString(),
                name = item.name,
                category = item.category,
                brand = item.brand,
                sku = item.sku,
                price = item.price.toDouble(),
                discountPrice = item.discountPrice?.toDouble(),
                stock = item.stock,
                status = item.status,
                rating = item.rating?.toDouble(),
                reviewCount = item.reviewCount,
                description = item.detail?.description,
                longDescription = item.detail?.longDescription,
                specifications = item.detail?.specifications,
                features = item.detail?.features,
                dimensions = item.detail?.dimensions,
                weight = item.detail?.weight?.toDouble(),
                manufacturer = item.detail?.manufacturer,
                originCountry = item.detail?.originCountry,
                warrantyPeriod = item.detail?.warrantyPeriod,
                tags = item.detail?.tags,
                searchText = searchText,
                metaTitle = item.detail?.metaTitle,
                metaDescription = item.detail?.metaDescription,
                metaKeywords = item.detail?.metaKeywords?.split(",")?.map { it.trim() },
                imageUrls = imageUrls,
                thumbnailUrl = thumbnailUrl,
                createdAt = item.createdAt,
                updatedAt = item.updatedAt,
                indexedAt = LocalDateTime.now()
            )
        } catch (e: Exception) {
            logger.error("Error processing product id=${item.id}: ${e.message}", e)
            null
        }
    }
}
