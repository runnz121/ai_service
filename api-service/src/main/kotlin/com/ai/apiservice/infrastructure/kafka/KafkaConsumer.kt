package com.ai.apiservice.infrastructure.kafka

import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Component

@Component
class KafkaConsumer {
    private val logger = LoggerFactory.getLogger(javaClass)

    @KafkaListener(
        topics = ["test-topic"],
        groupId = "\${spring.kafka.consumer.group-id}"
    )
    fun consumeMessage(
        @Payload message: String,
        @Header(KafkaHeaders.RECEIVED_TOPIC) topic: String,
        @Header(KafkaHeaders.RECEIVED_PARTITION) partition: Int,
        @Header(KafkaHeaders.OFFSET) offset: Long,
        @Header(value = KafkaHeaders.RECEIVED_KEY, required = false) key: String?,
        acknowledgment: Acknowledgment
    ) {
        try {
            logger.info(
                "Received message from topic: {}, partition: {}, offset: {}, key: {}, message: {}",
                topic, partition, offset, key, message
            )

            // 메시지 처리 로직
            processMessage(message)

            // 수동 커밋
            acknowledgment.acknowledge()
        } catch (e: Exception) {
            logger.error("Error processing message from topic: $topic", e)
            // 에러 처리 로직 (재시도, DLQ 전송 등)
        }
    }

    private fun processMessage(message: String) {
        // 실제 비즈니스 로직 구현
        logger.debug("Processing message: $message")
    }
}
