package me.fernandesleite.alagou.persistence

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "poi_table")
data class Poi(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo val nome: String,
    @ColumnInfo val lat: Double,
    @ColumnInfo val lng: Double,
    @ColumnInfo val radius: Double,
    @ColumnInfo val userTokenId: String?,
)
