package com.feel.feel.data

data class UserInfoResponse(
    val balance: Int,
    val earning: Float,
    val email: String,
    val id: String,
    val myVideos: List<MyVideo>,
    val name: String,
    val ownVideos: List<OwnVideoX>
)