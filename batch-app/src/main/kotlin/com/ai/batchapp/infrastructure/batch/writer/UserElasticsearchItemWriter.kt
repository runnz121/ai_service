package com.ai.batchapp.infrastructure.batch.writer

import com.ai.batchapp.domain.UserDocument
import org.slf4j.LoggerFactory
import org.springframework.batch.item.Chunk
import org.springframework.batch.item.ItemWriter
import org.springframework.data.elasticsearch.core.ElasticsearchOperations
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates
import org.springframework.data.elasticsearch.core.query.IndexQuery
import org.springframework.data.elasticsearch.core.query.IndexQueryBuilder
import org.springframework.stereotype.Component

@Component
class UserElasticsearchItemWriter(
    private val elasticsearchOperations: ElasticsearchOperations
) : ItemWriter<UserDocument> {

    private val logger = LoggerFactory.getLogger(javaClass)

    companion object {
        const val INDEX_NAME = "users_search"
    }

    override fun write(chunk: Chunk<out UserDocument>) {
        if (chunk.isEmpty) {
            logger.debug("No users to index")
            return
        }

        try {
            val queries: List<IndexQuery> = chunk.items.map { document ->
                IndexQueryBuilder()
                    .withId(document.id ?: "")
                    .withObject(document)
                    .build()
            }

            val indexCoordinates = IndexCoordinates.of(INDEX_NAME)
            elasticsearchOperations.bulkIndex(queries, indexCoordinates)

            logger.info("Indexed ${chunk.size()} users to Elasticsearch")

        } catch (e: Exception) {
            logger.error("Error writing users to Elasticsearch: ${e.message}", e)
            throw e
        }
    }
}
