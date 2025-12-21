package com.ai.batchapp.infrastructure.batch

import com.ai.batchapp.infrastructure.persistence.entity.UserEntity
import com.ai.batchapp.infrastructure.persistence.entity.UserProfileEntity
import com.ai.batchapp.infrastructure.persistence.repository.UserJpaRepository
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
import java.time.LocalDate
import java.time.LocalDateTime

@SpringBootTest
@SpringBatchTest
@ActiveProfiles("test")
class UserIndexingJobTest {

    @Autowired
    private lateinit var jobLauncherTestUtils: JobLauncherTestUtils

    @Autowired
    private lateinit var userJpaRepository: UserJpaRepository

    @Autowired
    private lateinit var elasticsearchOperations: ElasticsearchOperations

    private val indexName = "users_search"

    @BeforeEach
    fun setUp() {
        // Clean up before each test
        userJpaRepository.deleteAll()
        val indexCoordinates = IndexCoordinates.of(indexName)
        if (elasticsearchOperations.indexOps(indexCoordinates).exists()) {
            elasticsearchOperations.indexOps(indexCoordinates).delete()
        }
        elasticsearchOperations.indexOps(indexCoordinates).create()
    }

    @AfterEach
    fun tearDown() {
        userJpaRepository.deleteAll()
    }

    @Test
    fun `should index users from MySQL to Elasticsearch`() {
        // Given: Create test user data
        val user = createTestUser(
            email = "test@example.com",
            username = "testuser",
            status = "ACTIVE"
        )
        userJpaRepository.save(user)

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
    fun `should index multiple users with batch processing`() {
        // Given: Create multiple test users
        val users = (1..15).map { i ->
            createTestUser(
                email = "user$i@example.com",
                username = "user$i",
                status = "ACTIVE"
            )
        }
        userJpaRepository.saveAll(users)

        // When: Run the batch job
        val jobParameters = JobParametersBuilder()
            .addString("runTime", LocalDateTime.now().toString())
            .toJobParameters()

        val jobExecution = jobLauncherTestUtils.launchJob(jobParameters)

        // Then: Verify all users were indexed
        assertEquals(ExitStatus.COMPLETED, jobExecution.exitStatus)

        val indexCoordinates = IndexCoordinates.of(indexName)
        val count = elasticsearchOperations.count(
            org.springframework.data.elasticsearch.core.query.Query.findAll(),
            indexCoordinates
        )
        assertEquals(15, count)
    }

    @Test
    fun `should skip soft-deleted users`() {
        // Given: Create normal and soft-deleted users
        val activeUser = createTestUser(
            email = "active@example.com",
            username = "activeuser",
            status = "ACTIVE"
        )

        val deletedUser = createTestUser(
            email = "deleted@example.com",
            username = "deleteduser",
            status = "ACTIVE"
        ).copy(deletedAt = LocalDateTime.now())

        userJpaRepository.saveAll(listOf(activeUser, deletedUser))

        // When: Run the batch job
        val jobParameters = JobParametersBuilder()
            .addString("runTime", LocalDateTime.now().toString())
            .toJobParameters()

        val jobExecution = jobLauncherTestUtils.launchJob(jobParameters)

        // Then: Only active user should be indexed
        assertEquals(ExitStatus.COMPLETED, jobExecution.exitStatus)

        val indexCoordinates = IndexCoordinates.of(indexName)
        val count = elasticsearchOperations.count(
            org.springframework.data.elasticsearch.core.query.Query.findAll(),
            indexCoordinates
        )
        assertEquals(1, count)
    }

    @Test
    fun `should process user with profile information`() {
        // Given: Create user with profile
        val user = createTestUserWithProfile(
            email = "john.doe@example.com",
            username = "johndoe",
            fullName = "John Doe",
            city = "Seoul",
            country = "South Korea",
            bio = "Software engineer and tech enthusiast"
        )
        userJpaRepository.save(user)

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

    @Test
    fun `should handle users without profiles`() {
        // Given: Create user without profile
        val user = UserEntity(
            email = "noprofile@example.com",
            username = "noprofileuser",
            password = "hashedpassword",
            status = "ACTIVE"
        )
        userJpaRepository.save(user)

        // When: Run the batch job
        val jobParameters = JobParametersBuilder()
            .addString("runTime", LocalDateTime.now().toString())
            .toJobParameters()

        val jobExecution = jobLauncherTestUtils.launchJob(jobParameters)

        // Then: Job should complete successfully even without profile
        assertEquals(ExitStatus.COMPLETED, jobExecution.exitStatus)

        val indexCoordinates = IndexCoordinates.of(indexName)
        val count = elasticsearchOperations.count(
            org.springframework.data.elasticsearch.core.query.Query.findAll(),
            indexCoordinates
        )
        assertEquals(1, count)
    }

    private fun createTestUser(
        email: String,
        username: String,
        status: String
    ): UserEntity {
        return UserEntity(
            email = email,
            username = username,
            password = "hashedpassword",
            status = status
        )
    }

    private fun createTestUserWithProfile(
        email: String,
        username: String,
        fullName: String,
        city: String,
        country: String,
        bio: String
    ): UserEntity {
        val user = UserEntity(
            email = email,
            username = username,
            password = "hashedpassword",
            status = "ACTIVE"
        )

        val profile = UserProfileEntity(
            user = user,
            fullName = fullName,
            phoneNumber = "010-1234-5678",
            address = "123 Main St",
            city = city,
            country = country,
            postalCode = "12345",
            birthDate = LocalDate.of(1990, 1, 1),
            gender = "MALE",
            profileImageUrl = "https://example.com/profile.jpg",
            bio = bio
        )

        user.profile = profile

        return user
    }
}
