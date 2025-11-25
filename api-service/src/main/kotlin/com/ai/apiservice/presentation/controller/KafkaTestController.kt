package com.ai.apiservice.presentation.controller

import com.ai.apiservice.infrastructure.kafka.KafkaProducer
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/kafka")
class KafkaTestController(
    private val kafkaProducer: KafkaProducer
) {

    @PostMapping("/send")
    fun sendMessage(
        @RequestParam topic: String,
        @RequestParam(required = false) key: String?,
        @RequestBody message: String
    ): Map<String, String> {
        if (key != null) {
            kafkaProducer.sendMessage(topic, key, message)
        } else {
            kafkaProducer.sendMessage(topic, message)
        }

        return mapOf(
            "status" to "Message sent",
            "topic" to topic,
            "key" to (key ?: "null"),
            "message" to message
        )
    }
}
