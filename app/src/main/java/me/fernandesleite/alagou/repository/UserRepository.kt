package me.fernandesleite.alagou.repository

import android.content.Context
import androidx.preference.PreferenceManager
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient

class UserRepository(val context: Context) {
    fun getTokenId() : String?{
        return PreferenceManager.getDefaultSharedPreferences(context).getString("idToken", "DEFAULT")

    }

    fun setTokenId(result: GoogleSignInAccount) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString("idToken", result.idToken).apply()
    }
}