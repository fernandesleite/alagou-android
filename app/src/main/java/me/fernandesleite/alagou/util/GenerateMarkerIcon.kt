package me.fernandesleite.alagou.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.MarkerOptions
import me.fernandesleite.alagou.R

class GenerateMarkerIcon {
    companion object {
        fun generateMarker(context: Context): MarkerOptions {
            return MarkerOptions()
                .icon(BitmapDescriptorFactory.fromBitmap(generateSmallIcon(context)))
        }

        private fun generateSmallIcon(context: Context): Bitmap {
            val height = 110
            val width = 86
            val bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.ic_marker)
            return Bitmap.createScaledBitmap(bitmap, width, height, false)
        }
    }
}