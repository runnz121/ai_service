package com.ai.searchapi.controller

import com.ai.searchapi.domain.Product
import com.ai.searchapi.domain.ProductRequest
import com.ai.searchapi.domain.SearchRequest
import com.ai.searchapi.domain.SearchResponse
import com.ai.searchapi.service.ProductService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/products")
class ProductController(
    private val productService: ProductService
) {

    @PostMapping("/search")
    fun search(@RequestBody request: SearchRequest): ResponseEntity<SearchResponse> {
        val response = productService.search(request)
        return ResponseEntity.ok(response)
    }

    @PostMapping
    fun create(@RequestBody request: ProductRequest): ResponseEntity<Product> {
        val product = productService.create(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(product)
    }

    @GetMapping("/{id}")
    fun findById(@PathVariable id: String): ResponseEntity<Product> {
        val product = productService.findById(id)
            ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(product)
    }

    @GetMapping
    fun findAll(): ResponseEntity<List<Product>> {
        val products = productService.findAll()
        return ResponseEntity.ok(products)
    }

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: String): ResponseEntity<Void> {
        productService.delete(id)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/health")
    fun health(): ResponseEntity<Map<String, String>> {
        return ResponseEntity.ok(mapOf("status" to "UP"))
    }
}
