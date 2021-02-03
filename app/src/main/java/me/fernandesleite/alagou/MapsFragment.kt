package me.fernandesleite.alagou

import android.annotation.SuppressLint
import androidx.fragment.app.Fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.location.FusedLocationProviderClient

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices

class MapsFragment : Fragment() {

    private val TAG = "Maps Fragment"

    private val REQUEST_LOCATION_PERMISSION = 1

    private lateinit var map: GoogleMap

    private lateinit var mFusedLocationClient: FusedLocationProviderClient


    private val callback = OnMapReadyCallback { googleMap ->
        map = googleMap
        requestPermission(googleMap)
        setMyLocation(map)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        return inflater.inflate(R.layout.fragment_maps, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
    }

    private fun enableLocation(map: GoogleMap): Boolean {
         if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED) {
            map.isMyLocationEnabled = true
            return true
        }
        return false
    }


    private fun requestPermission(map: GoogleMap) {
        if (!enableLocation(map)){
            requestPermissions(
                arrayOf<String>(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION
            )
        }
    }

    @SuppressLint("MissingPermission")
    private fun setMyLocation(map: GoogleMap) {
        var currentLoc: Location?
        val zoomLevel = 15f
            mFusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                currentLoc = location
                if (location != null) {
                    val currentLocationLatLng = LatLng(
                        currentLoc!!.latitude,
                        currentLoc!!.longitude
                    )
                    map.moveCamera(
                        CameraUpdateFactory.newLatLngZoom(
                            currentLocationLatLng,
                            zoomLevel
                        )
                    )
                    enableLocation(map)
                } else {
                    Toast.makeText(activity, "Location Off", Toast.LENGTH_LONG).show()
                }

        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.contains(PackageManager.PERMISSION_GRANTED)) {
                setMyLocation(map)
            }
        }
    }
}