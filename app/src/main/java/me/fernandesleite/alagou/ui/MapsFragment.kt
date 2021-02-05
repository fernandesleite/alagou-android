package me.fernandesleite.alagou.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import me.fernandesleite.alagou.R
import me.fernandesleite.alagou.databinding.FragmentMapsBinding

class MapsFragment : Fragment() {

    private val REQUEST_LOCATION_PERMISSION = 1
    private var startedUp = true
    private lateinit var map: GoogleMap
    private lateinit var binding: FragmentMapsBinding
    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private lateinit var viewModel: MapsViewModel
    private lateinit var navController: NavController

    private val callback = OnMapReadyCallback { googleMap ->
        googleMap.setMapStyle(
            MapStyleOptions.loadRawResourceStyle(
                requireContext(),
                R.raw.map_style_main
            )
        )
        map = googleMap
        enableLocation(map)
        viewModel.flooding.observe(viewLifecycleOwner, Observer { floodings ->
            binding.bottomAppBarText.text = getString(
                R.string.quantityFloodingsPlaceholder,
                floodings.size
            )
            floodings.forEach {
                map.addMarker(
                    generateHomeMarker(requireContext()).position(
                        LatLng(
                            it.latitude,
                            it.longitude
                        )
                    )
                )
            }
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = ViewModelProvider(this).get(MapsViewModel::class.java)
        binding = FragmentMapsBinding.inflate(inflater)
        navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment)
        binding.btnCriarPonto.setOnClickListener { navigateToCreateFloodingMap() }
        binding.btnTraffic.setOnClickListener {
            map.isTrafficEnabled = !map.isTrafficEnabled
            if (map.isTrafficEnabled) {
                binding.btnTraffic.label.labelText = "Tráfego ativado"
                binding.btnTraffic.fabOptionColor =
                    ContextCompat.getColor(requireContext(), R.color.active)
            } else {
                binding.btnTraffic.label.labelText = "Tráfego desativado"
                binding.btnTraffic.fabOptionColor =
                    ContextCompat.getColor(requireContext(), R.color.colorAccent)
            }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
    }

    override fun onResume() {
        super.onResume()
        viewModel.getFloodings()
    }

    private fun generateHomeMarker(context: Context): MarkerOptions {
        return MarkerOptions()
            .icon(BitmapDescriptorFactory.fromBitmap(generateSmallIcon(context)))
    }

    private fun generateSmallIcon(context: Context): Bitmap {
        val height = 100
        val width = 100
        val bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.ic_map_maker)
        return Bitmap.createScaledBitmap(bitmap, width, height, false)
    }

    private fun enableLocation(map: GoogleMap): Boolean {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            if (startedUp) {
                startUpLocationCamera(map)
                map.isMyLocationEnabled = true
                startedUp = !startedUp
            }
            return true
        } else {
            requestPermission()
            return false
        }

    }


    private fun requestPermission() {
        requestPermissions(
            arrayOf<String>(Manifest.permission.ACCESS_FINE_LOCATION),
            REQUEST_LOCATION_PERMISSION
        )
    }

    @SuppressLint("MissingPermission")
    private fun startUpLocationCamera(map: GoogleMap) {
        val service: LocationManager =
            context?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val enabled: Boolean = service
            .isProviderEnabled(LocationManager.GPS_PROVIDER)
        var currentLoc: Location?

        if (enabled) {
            mFusedLocationClient =
                LocationServices.getFusedLocationProviderClient(requireActivity())
            mFusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                currentLoc = location
                if (location != null) {
                    moveCameraCurrentLocation(currentLoc)
                    enableLocation(map)
                } else {
                    val locationListener = object : LocationListener {
                        override fun onLocationChanged(currentLoc: Location?) {
                            moveCameraCurrentLocation(currentLoc)
                            enableLocation(map)
                            service.removeUpdates(this)
                        }

                        override fun onStatusChanged(
                            provider: String?,
                            status: Int,
                            extras: Bundle?
                        ) {
                        }

                        override fun onProviderEnabled(provider: String?) {}
                        override fun onProviderDisabled(provider: String?) {}
                    }
                    service.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER, 0,
                        0f, locationListener
                    )
                }

            }
        } else {
            navController.navigate(R.id.action_mapsFragment_to_requestLocationFragment)
            Toast.makeText(activity, "Location Off", Toast.LENGTH_LONG).show()
        }

    }

    private fun moveCameraCurrentLocation(currentLoc: Location?) {
        val currentLocationLatLng = LatLng(
            currentLoc!!.latitude,
            currentLoc.longitude
        )
        map.moveCamera(
            CameraUpdateFactory.newLatLngZoom(
                currentLocationLatLng,
                15f
            )
        )
    }

    private fun navigateToCreateFloodingMap() {
        navController.navigate(
            MapsFragmentDirections.actionMapsFragmentToCreateFloodingMapsFragment(
                map.cameraPosition.target.latitude.toFloat(),
                map.cameraPosition.target.longitude.toFloat(),
                map.cameraPosition.zoom
            )
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.contains(PackageManager.PERMISSION_GRANTED)) {
                enableLocation(map)
            }
        }
    }
}