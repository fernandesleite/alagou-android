package me.fernandesleite.alagou.ui.mainmap

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.launch
import me.fernandesleite.alagou.models.Flooding
import me.fernandesleite.alagou.models.User
import me.fernandesleite.alagou.persistence.Poi
import me.fernandesleite.alagou.persistence.PoiDatabase
import me.fernandesleite.alagou.repository.FloodingRepository
import me.fernandesleite.alagou.repository.PoiRepository
import me.fernandesleite.alagou.repository.UserRepository

class MapsViewModel(application: Application) : AndroidViewModel(application) {

    private val floodingRepository = FloodingRepository()
    private val userRepository = UserRepository(application.applicationContext)
    private val poiRepository =
        PoiRepository(PoiDatabase.getInstance(application.applicationContext))

    private val _currentPosition = MutableLiveData<LatLng>()
    val currentPosition: LiveData<LatLng>
        get() = _currentPosition

    private val _dataLoading = MutableLiveData(false)
    val dataLoading: LiveData<Boolean> = _dataLoading

    private val _trafficMap = MutableLiveData(false)
    val trafficMap: LiveData<Boolean> = _trafficMap

    private val _floodings = MutableLiveData<List<Flooding>>()
    val floodings: LiveData<List<Flooding>>
        get() = _floodings

    private val _flooding = MutableLiveData<Flooding>()
    val flooding: LiveData<Flooding>
        get() = _flooding

    private val _poiList = MutableLiveData<List<Poi>>()
    val poiList: LiveData<List<Poi>>
        get() = _poiList

    fun refreshPoiCache() {
        viewModelScope.launch {
            _poiList.value = poiRepository.refreshPoiCache(userRepository.getTokenId())
        }
    }

    fun deletePoi(poi: Poi){
        viewModelScope.launch {
            poiRepository.deletePoi(poi)
        }
    }

    fun started() {
        _dataLoading.value = true
    }

    fun toggleTraffic() {
        _trafficMap.value = !_trafficMap.value!!
    }

    fun getFloodings(minLat: Double, maxLat: Double, minLng: Double, maxLng: Double) {
        floodingRepository.getFloodings(_floodings, minLat, maxLat, minLng, maxLng)
    }

    fun getFlooding(id: String) {
        floodingRepository.getFlooding(id, _flooding)
    }

    fun setTokenId(result: GoogleSignInAccount) {
        userRepository.setTokenId(result)
    }

    fun createUser(user: User) {
        userRepository.createUser(user)
    }

    fun getUserNameToken(): String? {
        return userRepository.getUserNameToken()
    }

    fun getUserEmailToken(): String? {
        return userRepository.getUserEmailToken()
    }

    fun setCurrentPosition(latLng: LatLng) {
        _currentPosition.value = latLng
    }
}