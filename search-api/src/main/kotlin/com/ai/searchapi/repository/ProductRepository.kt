package com.ai.searchapi.repository

import com.ai.searchapi.domain.Product
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository
import org.springframework.stereotype.Repository

@Repository
interface ProductRepository : ElasticsearchRepository<Product, String> {

    fun findByNameContaining(name: String, pageable: Pageable): Page<Product>

    fun findByCategory(category: String, pageable: Pageable): Page<Product>

    fun findByNameContainingAndCategory(name: String, category: String, pageable: Pageable): Page<Product>

    fun findByPriceBetween(minPrice: Double, maxPrice: Double, pageable: Pageable): Page<Product>
}
