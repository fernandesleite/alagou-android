package me.fernandesleite.alagou.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import me.fernandesleite.alagou.models.Flooding
import me.fernandesleite.alagou.repository.FloodingRepository

class MapsViewModel(application: Application) : AndroidViewModel(application) {

    private val floodingRepository = FloodingRepository()
    private val _floodings = MutableLiveData<List<Flooding>>()
    val flooding: LiveData<List<Flooding>>
        get() = _floodings

    fun createFlooding(flooding: Flooding) {
        floodingRepository.createFlooding(flooding)
    }

    fun getFloodings() {
        floodingRepository.getFloodings(_floodings)
    }
}