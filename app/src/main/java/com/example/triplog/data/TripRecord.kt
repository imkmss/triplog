package com.example.triplog.data

data class TripRecord(
    val no: Int = 0,
    val place: String,
    val visitDate: String,
    val memo: String,
    val photoUri: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
)