package com.ai.batchapp.infrastructure.elasticsearch

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.data.elasticsearch.client.ClientConfiguration
import org.springframework.data.elasticsearch.client.elc.ElasticsearchConfiguration

@Configuration
class ElasticsearchConfig(
    @Value("\${spring.elasticsearch.uris}")
    private val elasticsearchUris: String
) : ElasticsearchConfiguration() {

    override fun clientConfiguration(): ClientConfiguration {
        return ClientConfiguration.builder()
            .connectedTo(elasticsearchUris.replace("http://", ""))
            .build()
    }
}
