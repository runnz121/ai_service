package com.ai.batchapp.infrastructure.batch

import com.ai.batchapp.infrastructure.batch.processor.ProductItemProcessor
import com.ai.batchapp.infrastructure.batch.processor.UserItemProcessor
import com.ai.batchapp.infrastructure.persistence.entity.ProductDetailEntity
import com.ai.batchapp.infrastructure.persistence.entity.ProductEntity
import com.ai.batchapp.infrastructure.persistence.entity.ProductImageEntity
import com.ai.batchapp.infrastructure.persistence.entity.UserEntity
import com.ai.batchapp.infrastructure.persistence.entity.UserProfileEntity
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import java.math.BigDecimal
import java.time.LocalDate

@SpringBootTest
@ActiveProfiles("test")
class BatchComponentsIntegrationTest {

    @Autowired
    private lateinit var productItemProcessor: ProductItemProcessor

    @Autowired
    private lateinit var userItemProcessor: UserItemProcessor

    @Test
    fun `ProductItemProcessor should transform entity to document correctly`() {
        // Given
        val product = ProductEntity(
            id = 1L,
            name = "Test Product",
            category = "Electronics",
            brand = "TestBrand",
            sku = "TEST-001",
            price = BigDecimal("999.99"),
            discountPrice = BigDecimal("899.99"),
            stock = 100,
            status = "AVAILABLE",
            rating = BigDecimal("4.5"),
            reviewCount = 50
        )

        val detail = ProductDetailEntity(
            id = 1L,
            product = product,
            description = "Test description",
            longDescription = "Long test description",
            specifications = mapOf("cpu" to "Intel i7", "ram" to "16GB"),
            features = listOf("Feature 1", "Feature 2"),
            manufacturer = "TestBrand",
            tags = listOf("tag1", "tag2"),
            metaKeywords = "keyword1,keyword2,keyword3"
        )

        val images = listOf(
            ProductImageEntity(
                id = 1L,
                product = product,
                imageUrl = "https://example.com/thumb.jpg",
                imageType = "THUMBNAIL",
                displayOrder = 1
            ),
            ProductImageEntity(
                id = 2L,
                product = product,
                imageUrl = "https://example.com/image.jpg",
                imageType = "PRODUCT",
                displayOrder = 2
            )
        )

        product.detail = detail
        product.images = images

        // When
        val document = productItemProcessor.process(product)

        // Then
        assertNotNull(document)
        assertEquals("1", document?.id)
        assertEquals("Test Product", document?.name)
        assertEquals("Electronics", document?.category)
        assertEquals("TestBrand", document?.brand)
        assertEquals(999.99, document?.price)
        assertEquals(899.99, document?.discountPrice)
        assertEquals(100, document?.stock)
        assertEquals("AVAILABLE", document?.status)
        assertEquals(4.5, document?.rating)
        assertEquals(50, document?.reviewCount)

        // Verify detail fields
        assertEquals("Test description", document?.description)
        assertEquals("Long test description", document?.longDescription)
        assertEquals(mapOf("cpu" to "Intel i7", "ram" to "16GB"), document?.specifications)
        assertEquals(listOf("Feature 1", "Feature 2"), document?.features)
        assertEquals(listOf("tag1", "tag2"), document?.tags)
        assertEquals(listOf("keyword1", "keyword2", "keyword3"), document?.metaKeywords)

        // Verify images
        assertEquals(2, document?.imageUrls?.size)
        assertEquals("https://example.com/thumb.jpg", document?.thumbnailUrl)

        // Verify searchText was built
        assertNotNull(document?.searchText)
        assertTrue(document?.searchText?.contains("Test Product") == true)
    }

    @Test
    fun `UserItemProcessor should transform entity to document correctly`() {
        // Given
        val user = UserEntity(
            id = 1L,
            email = "test@example.com",
            username = "testuser",
            password = "hashedpassword",
            status = "ACTIVE"
        )

        val profile = UserProfileEntity(
            id = 1L,
            user = user,
            fullName = "Test User",
            phoneNumber = "010-1234-5678",
            address = "123 Test Street",
            city = "Seoul",
            country = "South Korea",
            postalCode = "12345",
            birthDate = LocalDate.of(1990, 1, 1),
            gender = "MALE",
            profileImageUrl = "https://example.com/profile.jpg",
            bio = "Test bio"
        )

        user.profile = profile

        // When
        val document = userItemProcessor.process(user)

        // Then
        assertNotNull(document)
        assertEquals("1", document?.id)
        assertEquals("test@example.com", document?.email)
        assertEquals("testuser", document?.username)
        assertEquals("ACTIVE", document?.status)

        // Verify profile fields
        assertEquals("Test User", document?.fullName)
        assertEquals("010-1234-5678", document?.phoneNumber)
        assertEquals("123 Test Street", document?.address)
        assertEquals("Seoul", document?.city)
        assertEquals("South Korea", document?.country)
        assertEquals("12345", document?.postalCode)
        assertEquals(LocalDate.of(1990, 1, 1), document?.birthDate)
        assertEquals("MALE", document?.gender)
        assertEquals("https://example.com/profile.jpg", document?.profileImageUrl)
        assertEquals("Test bio", document?.bio)

        // Verify searchText was built
        assertNotNull(document?.searchText)
        assertTrue(document?.searchText?.contains("testuser") == true)
        assertTrue(document?.searchText?.contains("Test User") == true)
    }

    @Test
    fun `ProductItemProcessor should handle product without details gracefully`() {
        // Given
        val product = ProductEntity(
            id = 1L,
            name = "Simple Product",
            category = "Simple",
            price = BigDecimal("100.00"),
            stock = 10,
            status = "AVAILABLE"
        )

        // When
        val document = productItemProcessor.process(product)

        // Then
        assertNotNull(document)
        assertEquals("Simple Product", document?.name)
        assertNull(document?.description)
        assertNull(document?.specifications)
        assertEquals(0, document?.imageUrls?.size)
    }

    @Test
    fun `UserItemProcessor should handle user without profile gracefully`() {
        // Given
        val user = UserEntity(
            id = 1L,
            email = "simple@example.com",
            username = "simpleuser",
            password = "hashedpassword",
            status = "ACTIVE"
        )

        // When
        val document = userItemProcessor.process(user)

        // Then
        assertNotNull(document)
        assertEquals("simple@example.com", document?.email)
        assertEquals("simpleuser", document?.username)
        assertNull(document?.fullName)
        assertNull(document?.city)
    }
}
