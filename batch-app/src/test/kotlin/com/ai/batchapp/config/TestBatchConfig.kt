package com.ai.batchapp.config

import org.springframework.batch.core.Job
import org.springframework.batch.test.JobLauncherTestUtils
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary

@TestConfiguration
class TestBatchConfig {

    @Bean
    @Primary
    fun productJobLauncherTestUtils(
        @Qualifier("productIndexingJob") productIndexingJob: Job
    ): JobLauncherTestUtils {
        val utils = JobLauncherTestUtils()
        utils.job = productIndexingJob
        return utils
    }

    @Bean
    fun userJobLauncherTestUtils(
        @Qualifier("userIndexingJob") userIndexingJob: Job
    ): JobLauncherTestUtils {
        val utils = JobLauncherTestUtils()
        utils.job = userIndexingJob
        return utils
    }
}
