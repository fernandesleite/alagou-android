package me.fernandesleite.alagou.repository

import android.content.Context
import android.util.Log
import androidx.preference.PreferenceManager
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import me.fernandesleite.alagou.models.User
import me.fernandesleite.alagou.network.Api
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class UserRepository(val context: Context) {
    fun getTokenId(): String? {
        return PreferenceManager.getDefaultSharedPreferences(context)
            .getString("idToken", "DEFAULT")

    }

    fun setTokenId(result: GoogleSignInAccount) {
        PreferenceManager.getDefaultSharedPreferences(context).edit()
            .putString("idToken", result.idToken).apply()
    }

    fun createUser(user: User) {
        val call: Call<User> = Api.retrofitService.createUser(user)
        call.enqueue(object : Callback<User> {
            override fun onResponse(call: Call<User>, response: Response<User>) {
                Log.d("ViewModel", "success")
            }

            override fun onFailure(call: Call<User>, t: Throwable) {
                Log.d("ViewModel", "error")
            }

        })
    }
}