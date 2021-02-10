package me.fernandesleite.alagou.ui.createflooding

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import me.fernandesleite.alagou.models.Flooding
import me.fernandesleite.alagou.models.FloodingPost
import me.fernandesleite.alagou.repository.FloodingRepository
import me.fernandesleite.alagou.repository.UserRepository
import me.fernandesleite.alagou.util.LatLong

class CreateFloodingViewModel(application: Application) : AndroidViewModel(application){
    private val floodingRepository = FloodingRepository()
    private val userRepository = UserRepository(application.applicationContext)

    val latLong = MutableLiveData<LatLong>()

    fun setLatlong(latitude: Double, longitude: Double){
        latLong.value = LatLong(latitude, longitude)
    }

    fun createFlooding(flooding: FloodingPost) {
        floodingRepository.createFlooding(flooding)
    }

    fun getTokenId():String? {
        return userRepository.getTokenId()
    }
}