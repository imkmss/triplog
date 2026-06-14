package com.example.triplog.data

data class TripPhoto(
    val id: Int = 0,
    val recordId: Int,
    val photoUri: String,
    val isThumbnail: Boolean = false
)