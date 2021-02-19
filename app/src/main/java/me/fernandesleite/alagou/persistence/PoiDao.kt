package me.fernandesleite.alagou.persistence

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface PoiDao {
    @Query("SELECT * FROM poi_table")
    fun getAllPoi(): List<Poi>
    @Query("SELECT * FROM poi_table WHERE id = :id")
    fun getPoi(id: Int): Poi
    @Insert
    fun insert(poi: Poi)
    @Delete
    fun delete(poi: Poi)
}