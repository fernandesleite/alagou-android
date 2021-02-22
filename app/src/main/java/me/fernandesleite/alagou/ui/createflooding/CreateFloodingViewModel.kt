package me.fernandesleite.alagou.ui.createflooding

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.maps.model.LatLng
import me.fernandesleite.alagou.models.FloodingPost
import me.fernandesleite.alagou.repository.FloodingRepository
import me.fernandesleite.alagou.repository.UserRepository

class CreateFloodingViewModel(application: Application) : AndroidViewModel(application) {
    private val floodingRepository = FloodingRepository()
    private val userRepository = UserRepository(application.applicationContext)

    val latLong = MutableLiveData<LatLng>()

    fun setLatlong(latitude: Double, longitude: Double) {
        latLong.value = LatLng(latitude, longitude)
    }

    fun createFlooding(flooding: FloodingPost) {
        floodingRepository.createFlooding(flooding)
    }

    fun getTokenId(): String? {
        return userRepository.getTokenId()
    }
}