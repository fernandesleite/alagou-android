package me.fernandesleite.alagou.models

data class Flooding(
    val _id: String,
    val latitude: Double,
    val longitude: Double,
    val note: String,
    val user: String?
)