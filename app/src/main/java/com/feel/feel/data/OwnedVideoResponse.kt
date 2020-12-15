package com.feel.feel.data

data class OwnedVideoResponse(
    val email: String,
    val id: String,
    val name: String,
    val ownVideos: List<OwnVideo>
)