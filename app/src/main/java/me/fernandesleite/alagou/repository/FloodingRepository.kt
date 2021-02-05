package me.fernandesleite.alagou.repository

import android.util.Log
import androidx.lifecycle.MutableLiveData
import me.fernandesleite.alagou.models.Flooding
import me.fernandesleite.alagou.network.Api
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FloodingRepository {

    fun getFloodings(floodings: MutableLiveData<List<Flooding>>) {
        val call: Call<List<Flooding>> = Api.retrofitService.getFloodings()
        call.enqueue(object : Callback<List<Flooding>> {
            override fun onResponse(
                call: Call<List<Flooding>>,
                response: Response<List<Flooding>>
            ) {
                floodings.value = response.body()
            }

            override fun onFailure(call: Call<List<Flooding>>, t: Throwable) {
                Log.d("ViewModel", "error")
            }

        })
    }

    fun createFlooding(flooding: Flooding) {
        val call: Call<Flooding> = Api.retrofitService.createFlooding(flooding)
        call.enqueue(object : Callback<Flooding> {
            override fun onResponse(call: Call<Flooding>, response: Response<Flooding>) {
                Log.d("ViewModel", "success")

            }

            override fun onFailure(call: Call<Flooding>, t: Throwable) {
                Log.d("ViewModel", "error")
            }

        })

    }

}