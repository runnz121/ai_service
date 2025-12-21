package com.ai.batchapp.infrastructure.batch.config

import com.ai.batchapp.domain.UserDocument
import com.ai.batchapp.infrastructure.batch.processor.UserItemProcessor
import com.ai.batchapp.infrastructure.batch.reader.UserItemReader
import com.ai.batchapp.infrastructure.batch.writer.UserElasticsearchItemWriter
import com.ai.batchapp.infrastructure.persistence.entity.UserEntity
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.transaction.PlatformTransactionManager

@Configuration
class UserIndexingJobConfig(
    private val jobRepository: JobRepository,
    private val transactionManager: PlatformTransactionManager,
    private val userItemReader: UserItemReader,
    private val userItemProcessor: UserItemProcessor,
    private val userElasticsearchItemWriter: UserElasticsearchItemWriter
) {

    companion object {
        const val JOB_NAME = "userIndexingJob"
        const val STEP_NAME = "userIndexingStep"
        const val CHUNK_SIZE = 500
    }

    @Bean(name = [JOB_NAME])
    fun userIndexingJob(): Job {
        return JobBuilder(JOB_NAME, jobRepository)
            .start(userIndexingStep())
            .build()
    }

    @Bean(name = [STEP_NAME])
    fun userIndexingStep(): Step {
        return StepBuilder(STEP_NAME, jobRepository)
            .chunk<UserEntity, UserDocument>(CHUNK_SIZE, transactionManager)
            .reader(userItemReader.reader())
            .processor(userItemProcessor)
            .writer(userElasticsearchItemWriter)
            .faultTolerant()
            .skip(Exception::class.java)
            .skipLimit(100)
            .build()
    }
}
