package com.ai.searchapi.service

import com.ai.searchapi.domain.Product
import com.ai.searchapi.domain.ProductRequest
import com.ai.searchapi.domain.SearchRequest
import com.ai.searchapi.domain.SearchResponse
import com.ai.searchapi.repository.ProductRepository
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service

@Service
class ProductService(
    private val productRepository: ProductRepository
) {

    fun search(request: SearchRequest): SearchResponse {
        val pageable = PageRequest.of(request.page, request.size)

        val page = when {
            request.category != null && request.keyword.isNotBlank() -> {
                productRepository.findByNameContainingAndCategory(
                    request.keyword,
                    request.category,
                    pageable
                )
            }
            request.category != null -> {
                productRepository.findByCategory(request.category, pageable)
            }
            request.keyword.isNotBlank() -> {
                productRepository.findByNameContaining(request.keyword, pageable)
            }
            request.minPrice != null && request.maxPrice != null -> {
                productRepository.findByPriceBetween(
                    request.minPrice,
                    request.maxPrice,
                    pageable
                )
            }
            else -> {
                productRepository.findAll(pageable)
            }
        }

        return SearchResponse(
            products = page.content,
            totalElements = page.totalElements,
            totalPages = page.totalPages,
            currentPage = page.number
        )
    }

    fun create(request: ProductRequest): Product {
        val product = Product(
            name = request.name,
            description = request.description,
            category = request.category,
            price = request.price,
            stock = request.stock
        )
        return productRepository.save(product)
    }

    fun findById(id: String): Product? {
        return productRepository.findById(id).orElse(null)
    }

    fun findAll(): List<Product> {
        return productRepository.findAll().toList()
    }

    fun delete(id: String) {
        productRepository.deleteById(id)
    }
}
