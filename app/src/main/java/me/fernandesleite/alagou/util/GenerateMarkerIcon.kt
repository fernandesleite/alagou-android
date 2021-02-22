package me.fernandesleite.alagou.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.MarkerOptions

class GenerateMarkerIcon {
    companion object {
        fun generateMarker(context: Context, drawable: Int): MarkerOptions {
            return MarkerOptions()
                .icon(BitmapDescriptorFactory.fromBitmap(generateSmallIcon(context, drawable)))
        }

        private fun generateSmallIcon(context: Context, drawable: Int): Bitmap {
            val height = 110
            val width = 86
            val bitmap = BitmapFactory.decodeResource(context.resources, drawable)
            return Bitmap.createScaledBitmap(bitmap, width, height, false)
        }
    }
}