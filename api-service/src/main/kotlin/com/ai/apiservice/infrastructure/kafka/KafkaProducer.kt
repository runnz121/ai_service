package com.ai.apiservice.infrastructure.kafka

import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.SendResult
import org.springframework.stereotype.Component
import java.util.concurrent.CompletableFuture

@Component
class KafkaProducer(
    private val kafkaTemplate: KafkaTemplate<String, String>
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    fun sendMessage(topic: String, key: String?, message: String) {
        val future: CompletableFuture<SendResult<String, String>> = if (key != null) {
            kafkaTemplate.send(topic, key, message)
        } else {
            kafkaTemplate.send(topic, message)
        }

        future.whenComplete { result, ex ->
            if (ex == null) {
                logger.info(
                    "Message sent successfully to topic: {}, partition: {}, offset: {}, key: {}",
                    topic,
                    result.recordMetadata.partition(),
                    result.recordMetadata.offset(),
                    key
                )
            } else {
                logger.error("Failed to send message to topic: $topic", ex)
            }
        }
    }

    fun sendMessage(topic: String, message: String) {
        sendMessage(topic, null, message)
    }
}
