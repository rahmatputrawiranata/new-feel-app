package com.feel.feel.data

data class ChargeResponse(
    val amount: Int,
    val createdAt: String,
    val id: String,
    val status: String,
    val token: String,
    val updatedAt: String,
    val user: User
)