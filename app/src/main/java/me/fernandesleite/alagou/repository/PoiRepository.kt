package me.fernandesleite.alagou.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.fernandesleite.alagou.persistence.Poi
import me.fernandesleite.alagou.persistence.PoiDatabase

class PoiRepository(private val database: PoiDatabase) {
    suspend fun refreshPoiCache(): List<Poi> =
        withContext(Dispatchers.IO) {
            database.poiDao.getAllPoi()
        }
    suspend fun insertPoiCache(nome: String, lat: Double, lng: Double, radius: Double){
        withContext(Dispatchers.IO) {
            database.poiDao.insert(Poi(nome = nome, radius = radius,  lat = lat, lng = lng))
        }
    }
}