package me.fernandesleite.alagou.ui.mainmap

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.nambimobile.widgets.efab.FabOption
import me.fernandesleite.alagou.R
import me.fernandesleite.alagou.databinding.FragmentMapsBinding
import me.fernandesleite.alagou.models.Flooding
import me.fernandesleite.alagou.util.Directions
import me.fernandesleite.alagou.util.GenerateMarkerIcon

class MapsFragment : Fragment() {

    private val REQUEST_LOCATION_PERMISSION = 1
    private lateinit var map: GoogleMap
    private lateinit var viewModel: MapsViewModel
    private lateinit var navController: NavController

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(requireActivity()).get(MapsViewModel::class.java)
        navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment)
        val binding = FragmentMapsBinding.inflate(inflater)
        Places.initialize(requireContext(), resources.getString(R.string.google_maps_api_key))
        val autocompleteFragment =
            childFragmentManager.findFragmentById(R.id.autocomplete_fragment)
                    as AutocompleteSupportFragment
        autocompleteFragment.setPlaceFields(listOf(Place.Field.LAT_LNG))
        autocompleteFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(p0: Place) {
                map.animateCamera(CameraUpdateFactory.newLatLng(p0.latLng))
            }

            override fun onError(p0: Status) {
                Log.i("TAG", "Place: $p0")
            }
        })

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        val account = GoogleSignIn.getLastSignedInAccount(requireContext())
        binding.apply {
            lifecycleOwner = this@MapsFragment
            btnCriarPonto.setOnClickListener { navigateToFragment(Directions.PONTO_ALAGAMENTO) }
            btnCriarPoi.setOnClickListener { navigateToFragment(Directions.AREA_DE_INTERESSE) }
            btnTraffic.setOnClickListener { viewModel.toggleTraffic() }
            if (account == null) {
                btnCriarPonto.fabOptionEnabled = false
                navController.navigate(R.id.loginFragment2)
            } else {
                btnCriarPonto.fabOptionEnabled = true
                mapFragment?.getMapAsync(callback(bottomAppBarText, btnTraffic))
            }
            return root
        }
    }

    override fun onResume() {
        super.onResume()
        if (this::map.isInitialized) {
            getFloodingsInsideBounds()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.setCurrentPosition(map.cameraPosition.target)
    }

    // -------- Permission / Init ----------

    private fun enableLocation(map: GoogleMap): Boolean {
        return if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            map.isMyLocationEnabled = true
            startUpLocationCamera()
            true
        } else {
            requestPermission()
            false
        }
    }

    private fun requestPermission() {
        requestPermissions(
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            REQUEST_LOCATION_PERMISSION
        )
    }

    @SuppressLint("MissingPermission")
    private fun lastLocationListener(location: Location?, service: LocationManager) {
        if (location != null) {
            viewModel.dataLoading.observe(viewLifecycleOwner, { loaded ->
                if (!loaded) {
                    moveCameraCurrentLocation(LatLng(location.latitude, location.longitude))
                    enableLocation(map)
                    viewModel.started()
                } else {
                    viewModel.currentPosition.observe(
                        viewLifecycleOwner,
                        { moveCameraCurrentLocation(it) })
                }
            })
        } else {
            val locationListener = object : LocationListener {
                override fun onLocationChanged(location: Location?) {
                    moveCameraCurrentLocation(LatLng(location!!.latitude, location.longitude))
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

    @SuppressLint("MissingPermission")
    private fun startUpLocationCamera() {
        val service: LocationManager =
            context?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val enabled: Boolean = service
            .isProviderEnabled(LocationManager.GPS_PROVIDER)
        if (enabled) {
            val mFusedLocationClient =
                LocationServices.getFusedLocationProviderClient(requireActivity())
            mFusedLocationClient.lastLocation.addOnSuccessListener {
                lastLocationListener(
                    it,
                    service
                )
            }
        } else {
            navController.navigate(R.id.action_mapsFragment_to_requestLocationFragment)
            Toast.makeText(activity, "Location Off", Toast.LENGTH_LONG).show()
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
                enableLocation(map)
            }
        }
    }

    // --------- Callbacks ----------

    private fun callback(bottomAppBarText: TextView, btnTraffic: FabOption) =
        OnMapReadyCallback { googleMap ->
            val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
            val mapView = mapFragment?.view
            // change My Location button from behind searchbar
            val locationButton =
                (mapView?.findViewById<View>(Integer.parseInt("1"))?.parent as View).findViewById<View>(
                    Integer.parseInt(
                        "2"
                    )
                )
            val rlp = locationButton.layoutParams as (RelativeLayout.LayoutParams)
            rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0)
            rlp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 0)
            rlp.setMargins(0, 250, 100, 0)

            googleMap.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    requireContext(),
                    R.raw.map_style_main
                )
            )
            map = googleMap
            enableLocation(map)
            map.setOnMarkerClickListener {
                navController.navigate(
                    MapsFragmentDirections.actionMapsFragmentToDisplayFloodingInfoFragment(
                        it.tag.toString()
                    )
                )
                true
            }
            map.setOnCameraIdleListener { getFloodingsInsideBounds() }
            viewModel.floodings.observe(viewLifecycleOwner, { addMaker(it, bottomAppBarText) })

            viewModel.trafficMap.observe(viewLifecycleOwner, {
                toggleTrafego(it, btnTraffic)

            })
        }

    private fun getFloodingsInsideBounds() {
        val bounds = map.projection.visibleRegion.latLngBounds
        val boundsMaxLat = bounds.northeast.latitude
        val boundsMaxLng = bounds.northeast.longitude
        val boundsMinLat = bounds.southwest.latitude
        val boundsMinLng = bounds.southwest.longitude
        viewModel.getFloodings(boundsMinLat, boundsMaxLat, boundsMinLng, boundsMaxLng)
    }

    private fun navigateToFragment(direction: Directions) {
        val lat = map.cameraPosition.target.latitude.toFloat()
        val lng = map.cameraPosition.target.longitude.toFloat()
        val zoom = map.cameraPosition.zoom
        when (direction) {
            Directions.PONTO_ALAGAMENTO -> navController.navigate(
                MapsFragmentDirections.actionMapsFragmentToCreateFloodingMapsFragment(
                    lat,
                    lng,
                    zoom
                )
            )
            Directions.AREA_DE_INTERESSE -> navController.navigate(
                MapsFragmentDirections.actionMapsFragmentToCreatePOIFragment(
                    lat,
                    lng,
                    zoom
                )
            )
        }
    }

    private fun toggleTrafego(isEnabled: Boolean, btnTraffic: FabOption) {
        map.isTrafficEnabled = isEnabled
        if (isEnabled) {
            btnTraffic.label.labelText = "Tráfego ativado"
            btnTraffic.fabOptionColor =
                ContextCompat.getColor(requireContext(), R.color.active)
        } else {
            btnTraffic.label.labelText = "Tráfego desativado"
            btnTraffic.fabOptionColor =
                ContextCompat.getColor(
                    requireContext(),
                    R.color.colorAccent
                )
        }
    }

    private fun addMaker(floodings: List<Flooding>, bottomAppBarText: TextView) {
        if (floodings.isEmpty()) {
            bottomAppBarText.text = getString(R.string.quantityFloodingsPlaceholder_Zero)
        } else {
            bottomAppBarText.text = resources.getQuantityString(
                R.plurals.quantityFloodingsPlaceholder,
                floodings.size,
                floodings.size
            )
        }

        floodings.forEach {
            map.addMarker(
                GenerateMarkerIcon.generateMarker(requireContext()).position(
                    LatLng(
                        it.latitude,
                        it.longitude
                    )
                )
            ).tag = it._id
        }
    }

    private fun moveCameraCurrentLocation(currentLoc: LatLng?) {
        map.moveCamera(
            CameraUpdateFactory.newLatLngZoom(
                currentLoc,
                15f
            )
        )
    }
}