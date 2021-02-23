package me.fernandesleite.alagou.ui.createpoi

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.launch
import me.fernandesleite.alagou.persistence.PoiDatabase
import me.fernandesleite.alagou.repository.PoiRepository
import me.fernandesleite.alagou.repository.UserRepository

class CreatePOIViewModel(application: Application): AndroidViewModel(application) {

    private val poiRepository = PoiRepository(PoiDatabase.getInstance(application.applicationContext))
    private val userRepository = UserRepository(application.applicationContext)

    private val _currentMarkerPosition = MutableLiveData<LatLng>()
    val currentMarkerPosition: LiveData<LatLng> = _currentMarkerPosition

    private val _markerActive = MutableLiveData(false)
    val markerActive: LiveData<Boolean> = _markerActive

    private val _currentRadius = MutableLiveData<Double>()
    val currentRadius: LiveData<Double> = _currentRadius

    fun setCurrentMarkerPosition(position: LatLng?){
        _currentMarkerPosition.value = position
    }
    fun setCurrentRadius(radius: Double) {
        _currentRadius.value = radius
    }

    fun setMarkerStatus() {
        _markerActive.value = !_markerActive.value!!
    }

    fun insertPoi(nome: String, lat: Double, lng: Double , radius: Double){
        viewModelScope.launch {
            poiRepository.insertPoiCache(nome, lat, lng, radius, userRepository.getTokenId())
        }
    }
}