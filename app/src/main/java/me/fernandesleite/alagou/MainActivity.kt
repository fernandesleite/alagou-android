package me.fernandesleite.alagou

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.google.android.gms.location.LocationRequest

class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            LocationRequest.PRIORITY_HIGH_ACCURACY -> {
                if (resultCode == Activity.RESULT_OK) {
                    Log.e("Status: ", "On")
                    navController.navigate(R.id.action_requestLocationFragment_to_mapsFragment2)
                } else {
                    Log.e("Status: ", "Off")
                    navController.navigate(R.id.action_requestLocationFragment_to_mapsFragment2)
                    Toast.makeText(applicationContext, "Alerta: GPS Desligado", Toast.LENGTH_LONG)
                        .show()
                }
            }
        }
    }

}