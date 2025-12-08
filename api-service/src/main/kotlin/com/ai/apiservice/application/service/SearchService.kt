package com.ai.apiservice.application.service

import com.ai.apiservice.infrastructure.fastmcp.FastMcpClient
import com.ai.apiservice.infrastructure.fastmcp.MCPSearchResponse
import org.springframework.stereotype.Service

@Service
class SearchService(
    private val fastMcpClient: FastMcpClient
) {

    fun search(query: String, category: String? = null): MCPSearchResponse {
        return fastMcpClient.search(query, category)
    }
}
