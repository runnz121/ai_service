package com.ai.batchapp.infrastructure.batch.config

import com.ai.batchapp.domain.ProductDocument
import com.ai.batchapp.infrastructure.batch.processor.ProductItemProcessor
import com.ai.batchapp.infrastructure.batch.reader.ProductItemReader
import com.ai.batchapp.infrastructure.batch.writer.ProductElasticsearchItemWriter
import com.ai.batchapp.infrastructure.persistence.entity.ProductEntity
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.transaction.PlatformTransactionManager

@Configuration
class ProductIndexingJobConfig(
    private val jobRepository: JobRepository,
    private val transactionManager: PlatformTransactionManager,
    private val productItemReader: ProductItemReader,
    private val productItemProcessor: ProductItemProcessor,
    private val productElasticsearchItemWriter: ProductElasticsearchItemWriter
) {

    companion object {
        const val JOB_NAME = "productIndexingJob"
        const val STEP_NAME = "productIndexingStep"
        const val CHUNK_SIZE = 500
    }

    @Bean(name = [JOB_NAME])
    fun productIndexingJob(): Job {
        return JobBuilder(JOB_NAME, jobRepository)
            .start(productIndexingStep())
            .build()
    }

    @Bean(name = [STEP_NAME])
    fun productIndexingStep(): Step {
        return StepBuilder(STEP_NAME, jobRepository)
            .chunk<ProductEntity, ProductDocument>(CHUNK_SIZE, transactionManager)
            .reader(productItemReader.reader())
            .processor(productItemProcessor)
            .writer(productElasticsearchItemWriter)
            .faultTolerant()
            .skip(Exception::class.java)
            .skipLimit(100)
            .build()
    }
}
