package me.fernandesleite.alagou.ui.mainmap

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import me.fernandesleite.alagou.models.Flooding
import me.fernandesleite.alagou.repository.FloodingRepository

class MapsViewModel(application: Application) : AndroidViewModel(application) {

    private val floodingRepository = FloodingRepository()
    private val _floodings = MutableLiveData<List<Flooding>>()
    val floodings: LiveData<List<Flooding>>
        get() = _floodings
    private val _flooding = MutableLiveData<Flooding>()
    val flooding: LiveData<Flooding>
        get() = _flooding

    fun getFloodings() {
        floodingRepository.getFloodings(_floodings)
    }

    fun getFlooding(id: String){
        floodingRepository.getFlooding(id, _flooding)
    }
}