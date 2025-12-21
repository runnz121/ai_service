package com.ai.batchapp.infrastructure.batch

import com.ai.batchapp.infrastructure.persistence.entity.ProductDetailEntity
import com.ai.batchapp.infrastructure.persistence.entity.ProductEntity
import com.ai.batchapp.infrastructure.persistence.entity.ProductImageEntity
import com.ai.batchapp.infrastructure.persistence.repository.ProductJpaRepository
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.batch.core.ExitStatus
import org.springframework.batch.core.JobParameters
import org.springframework.batch.core.JobParametersBuilder
import org.springframework.batch.test.JobLauncherTestUtils
import org.springframework.batch.test.context.SpringBatchTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.elasticsearch.core.ElasticsearchOperations
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates
import org.springframework.test.context.ActiveProfiles
import java.math.BigDecimal
import java.time.LocalDateTime

@SpringBootTest
@SpringBatchTest
@ActiveProfiles("test")
class ProductIndexingJobTest {

    @Autowired
    private lateinit var jobLauncherTestUtils: JobLauncherTestUtils

    @Autowired
    private lateinit var productJpaRepository: ProductJpaRepository

    @Autowired
    private lateinit var elasticsearchOperations: ElasticsearchOperations

    private val indexName = "products_search"

    @BeforeEach
    fun setUp() {
        // Clean up before each test
        productJpaRepository.deleteAll()
        val indexCoordinates = IndexCoordinates.of(indexName)
        if (elasticsearchOperations.indexOps(indexCoordinates).exists()) {
            elasticsearchOperations.indexOps(indexCoordinates).delete()
        }
        elasticsearchOperations.indexOps(indexCoordinates).create()
    }

    @AfterEach
    fun tearDown() {
        productJpaRepository.deleteAll()
    }

    @Test
    fun `should index products from MySQL to Elasticsearch`() {
        // Given: Create test product data
        val product = createTestProduct(
            name = "Test iPhone 15",
            category = "Electronics",
            brand = "Apple",
            price = BigDecimal("1200.00")
        )
        productJpaRepository.save(product)

        // When: Run the batch job
        val jobParameters: JobParameters = JobParametersBuilder()
            .addString("runTime", LocalDateTime.now().toString())
            .toJobParameters()

        val jobExecution = jobLauncherTestUtils.launchJob(jobParameters)

        // Then: Verify job completed successfully
        assertEquals(ExitStatus.COMPLETED, jobExecution.exitStatus)
        assertEquals(1, jobExecution.stepExecutions.size)

        // Verify data was indexed to Elasticsearch
        val indexCoordinates = IndexCoordinates.of(indexName)
        val count = elasticsearchOperations.count(
            org.springframework.data.elasticsearch.core.query.Query.findAll(),
            indexCoordinates
        )
        assertEquals(1, count)
    }

    @Test
    fun `should index multiple products with batch processing`() {
        // Given: Create multiple test products
        val products = (1..10).map { i ->
            createTestProduct(
                name = "Test Product $i",
                category = "Category $i",
                brand = "Brand $i",
                price = BigDecimal("${i * 100}.00")
            )
        }
        productJpaRepository.saveAll(products)

        // When: Run the batch job
        val jobParameters = JobParametersBuilder()
            .addString("runTime", LocalDateTime.now().toString())
            .toJobParameters()

        val jobExecution = jobLauncherTestUtils.launchJob(jobParameters)

        // Then: Verify all products were indexed
        assertEquals(ExitStatus.COMPLETED, jobExecution.exitStatus)

        val indexCoordinates = IndexCoordinates.of(indexName)
        val count = elasticsearchOperations.count(
            org.springframework.data.elasticsearch.core.query.Query.findAll(),
            indexCoordinates
        )
        assertEquals(10, count)
    }

    @Test
    fun `should skip soft-deleted products`() {
        // Given: Create normal and soft-deleted products
        val activeProduct = createTestProduct(
            name = "Active Product",
            category = "Electronics",
            brand = "Brand A",
            price = BigDecimal("500.00")
        )

        val deletedProduct = createTestProduct(
            name = "Deleted Product",
            category = "Electronics",
            brand = "Brand B",
            price = BigDecimal("600.00")
        ).copy(deletedAt = LocalDateTime.now())

        productJpaRepository.saveAll(listOf(activeProduct, deletedProduct))

        // When: Run the batch job
        val jobParameters = JobParametersBuilder()
            .addString("runTime", LocalDateTime.now().toString())
            .toJobParameters()

        val jobExecution = jobLauncherTestUtils.launchJob(jobParameters)

        // Then: Only active product should be indexed
        assertEquals(ExitStatus.COMPLETED, jobExecution.exitStatus)

        val indexCoordinates = IndexCoordinates.of(indexName)
        val count = elasticsearchOperations.count(
            org.springframework.data.elasticsearch.core.query.Query.findAll(),
            indexCoordinates
        )
        assertEquals(1, count)
    }

    @Test
    fun `should process product with details and images`() {
        // Given: Create product with detail and images
        val product = createTestProductWithDetails(
            name = "iPhone 15 Pro",
            category = "Smartphones",
            brand = "Apple",
            price = BigDecimal("1199.99"),
            description = "Latest Apple smartphone",
            specifications = mapOf("chip" to "A17 Pro", "storage" to "256GB"),
            features = listOf("Dynamic Island", "Action Button", "USB-C"),
            tags = listOf("smartphone", "apple", "premium")
        )
        productJpaRepository.save(product)

        // When: Run the batch job
        val jobParameters = JobParametersBuilder()
            .addString("runTime", LocalDateTime.now().toString())
            .toJobParameters()

        val jobExecution = jobLauncherTestUtils.launchJob(jobParameters)

        // Then: Verify job completed successfully
        assertEquals(ExitStatus.COMPLETED, jobExecution.exitStatus)

        val indexCoordinates = IndexCoordinates.of(indexName)
        val count = elasticsearchOperations.count(
            org.springframework.data.elasticsearch.core.query.Query.findAll(),
            indexCoordinates
        )
        assertEquals(1, count)
    }

    private fun createTestProduct(
        name: String,
        category: String,
        brand: String,
        price: BigDecimal
    ): ProductEntity {
        return ProductEntity(
            name = name,
            category = category,
            brand = brand,
            sku = "SKU-${System.currentTimeMillis()}",
            price = price,
            stock = 100,
            status = "AVAILABLE",
            rating = BigDecimal("4.5"),
            reviewCount = 10
        )
    }

    private fun createTestProductWithDetails(
        name: String,
        category: String,
        brand: String,
        price: BigDecimal,
        description: String,
        specifications: Map<String, Any>,
        features: List<String>,
        tags: List<String>
    ): ProductEntity {
        val product = ProductEntity(
            name = name,
            category = category,
            brand = brand,
            sku = "SKU-${System.currentTimeMillis()}",
            price = price,
            stock = 50,
            status = "AVAILABLE",
            rating = BigDecimal("4.8"),
            reviewCount = 150
        )

        val detail = ProductDetailEntity(
            product = product,
            description = description,
            longDescription = "Detailed description of $name",
            specifications = specifications,
            features = features,
            manufacturer = brand,
            tags = tags,
            metaTitle = name,
            metaDescription = description
        )

        val images = listOf(
            ProductImageEntity(
                product = product,
                imageUrl = "https://example.com/image1.jpg",
                imageType = "THUMBNAIL",
                displayOrder = 1
            ),
            ProductImageEntity(
                product = product,
                imageUrl = "https://example.com/image2.jpg",
                imageType = "PRODUCT",
                displayOrder = 2
            )
        )

        product.detail = detail
        product.images = images

        return product
    }
}
