package com.ai.apiservice.presentation.controller

import com.ai.apiservice.application.service.SearchService
import com.ai.apiservice.infrastructure.fastmcp.MCPSearchResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/search")
class SearchController(
    private val searchService: SearchService
) {

    @GetMapping
    fun search(
        @RequestParam query: String,
        @RequestParam(required = false) category: String?
    ): ResponseEntity<MCPSearchResponse> {
        val response = searchService.search(query, category)
        return ResponseEntity.ok(response)
    }

    @PostMapping
    fun searchPost(@RequestBody request: SearchRequest): ResponseEntity<MCPSearchResponse> {
        val response = searchService.search(request.query, request.category)
        return ResponseEntity.ok(response)
    }
}

data class SearchRequest(
    val query: String,
    val category: String? = null
)
