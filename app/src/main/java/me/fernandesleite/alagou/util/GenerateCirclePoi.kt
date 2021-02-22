package me.fernandesleite.alagou.util

import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng

class GenerateCirclePoi {
    companion object{
        fun generateCircle(latLng: LatLng, radius: Double) : CircleOptions{
            return CircleOptions().center(latLng).radius(radius).fillColor(0x55000000).strokeWidth(5f)
        }
    }
}