package me.fernandesleite.alagou.models

data class Flooding(
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val note: Int,
    val createAt: String,
    val user: String
)