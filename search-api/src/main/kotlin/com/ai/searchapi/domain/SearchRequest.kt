package com.ai.searchapi.domain

data class SearchRequest(
    val keyword: String,
    val category: String? = null,
    val minPrice: Double? = null,
    val maxPrice: Double? = null,
    val page: Int = 0,
    val size: Int = 10
)

data class SearchResponse(
    val products: List<Product>,
    val totalElements: Long,
    val totalPages: Int,
    val currentPage: Int
)

data class ProductRequest(
    val name: String,
    val description: String,
    val category: String,
    val price: Double,
    val stock: Int
)
