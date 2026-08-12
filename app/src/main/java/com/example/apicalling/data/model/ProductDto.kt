package com.example.apicalling.data.model

import com.google.gson.annotations.SerializedName

data class ProductListResponse(
    @SerializedName("products") val products: List<ProductDto>
)

data class ProductDto(
    @SerializedName("id") val id: Int,
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String,
    @SerializedName("price") val price: Double,
    @SerializedName("discountPercentage") val discountPercentage: Double,
    @SerializedName("rating") val rating: Double,
    @SerializedName("stock") val stock: Int,
    @SerializedName("brand") val brand: String?,
    @SerializedName("category") val category: String,
    @SerializedName("thumbnail") val thumbnail: String,
    @SerializedName("images") val images: List<String>,
    @SerializedName("reviews") val reviews: List<ReviewDto>? = emptyList()
)

data class ReviewDto(
    @SerializedName("rating") val rating: Int,
    @SerializedName("comment") val comment: String,
    @SerializedName("date") val date: String,
    @SerializedName("reviewerName") val reviewerName: String,
    @SerializedName("reviewerEmail") val reviewerEmail: String
)
