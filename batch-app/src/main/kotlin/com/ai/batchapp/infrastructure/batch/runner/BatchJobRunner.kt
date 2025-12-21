package com.ai.batchapp.infrastructure.batch.runner

import org.slf4j.LoggerFactory
import org.springframework.batch.core.Job
import org.springframework.batch.core.JobParameters
import org.springframework.batch.core.JobParametersBuilder
import org.springframework.batch.core.launch.JobLauncher
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class BatchJobRunner(
    private val jobLauncher: JobLauncher,
    @Qualifier("productIndexingJob") private val productIndexingJob: Job,
    @Qualifier("userIndexingJob") private val userIndexingJob: Job
) : CommandLineRunner {

    private val logger = LoggerFactory.getLogger(javaClass)

    override fun run(vararg args: String?) {
        if (args.isEmpty()) {
            logger.info("No job specified. Available jobs: product, user")
            logger.info("Usage: --job=<product|user>")
            return
        }

        val jobName = args.firstOrNull { it?.startsWith("--job=") == true }
            ?.substringAfter("--job=")

        when (jobName) {
            "product" -> runProductIndexingJob(args)
            "user" -> runUserIndexingJob(args)
            else -> logger.info("Unknown job: $jobName. Available: product, user")
        }
    }

    private fun runProductIndexingJob(args: Array<out String?>) {
        logger.info("Starting ProductIndexingJob...")
        val jobParameters = buildJobParameters(args)

        try {
            val execution = jobLauncher.run(productIndexingJob, jobParameters)
            logger.info("ProductIndexingJob completed with status: ${execution.status}")
            logger.info("Exit status: ${execution.exitStatus}")
        } catch (e: Exception) {
            logger.error("ProductIndexingJob failed: ${e.message}", e)
        }
    }

    private fun runUserIndexingJob(args: Array<out String?>) {
        logger.info("Starting UserIndexingJob...")
        val jobParameters = buildJobParameters(args)

        try {
            val execution = jobLauncher.run(userIndexingJob, jobParameters)
            logger.info("UserIndexingJob completed with status: ${execution.status}")
            logger.info("Exit status: ${execution.exitStatus}")
        } catch (e: Exception) {
            logger.error("UserIndexingJob failed: ${e.message}", e)
        }
    }

    private fun buildJobParameters(args: Array<out String?>): JobParameters {
        val builder = JobParametersBuilder()
        builder.addString("runTime", LocalDateTime.now().toString())

        args.forEach { arg ->
            arg?.let {
                if (it.startsWith("--") && it.contains("=") && !it.startsWith("--job=")) {
                    val (key, value) = it.substring(2).split("=", limit = 2)
                    builder.addString(key, value)
                }
            }
        }

        return builder.toJobParameters()
    }
}
