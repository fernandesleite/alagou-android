package me.fernandesleite.alagou

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.fernandesleite.alagou.models.Flooding
import me.fernandesleite.alagou.services.Api
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MapsViewModel(application: Application) : AndroidViewModel(application) {


    private val _floodings = MutableLiveData<List<Flooding>>()
    val flooding: LiveData<List<Flooding>>
        get() = _floodings

    suspend fun getFloodings() : List<Flooding> = withContext(Dispatchers.IO) {
        Api.retrofitService.getFloodings()
    }

    fun create(flooding: Flooding) {
        val call :Call<Flooding> = Api.retrofitService.createFlooding(flooding)
        call.enqueue(object : Callback<Flooding> {
            override fun onResponse(call: Call<Flooding>, response: Response<Flooding>) {
                Log.d("ViewModel", "success")

            }

            override fun onFailure(call: Call<Flooding>, t: Throwable) {
                Log.d("ViewModel", "error")
            }

        })

    }

    init {
        viewModelScope.launch {
            _floodings.value = getFloodings()
        }
    }
}