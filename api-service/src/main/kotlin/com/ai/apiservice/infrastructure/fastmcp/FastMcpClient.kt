package com.ai.apiservice.infrastructure.fastmcp

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.postForObject

@Component
class FastMcpClient(
    @Value("\${fastmcp.url:http://localhost:8082}")
    private val fastMcpUrl: String,
    private val restTemplate: RestTemplate
) {

    fun search(query: String, category: String? = null): MCPSearchResponse {
        val url = "$fastMcpUrl/mcp/search"
        val request = MCPSearchRequest(query = query, category = category)

        return restTemplate.postForObject(url, request, MCPSearchResponse::class.java)
            ?: throw RuntimeException("Failed to get response from FastMCP")
    }
}

data class MCPSearchRequest(
    val query: String,
    val category: String? = null,
    val indexType: String? = null
)

data class MCPSearchResponse(
    val success: Boolean,
    val data: Any?,
    val error: String?,
    val source: String?
)
