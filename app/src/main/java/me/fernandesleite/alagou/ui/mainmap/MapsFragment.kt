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
import me.fernandesleite.alagou.R
import me.fernandesleite.alagou.databinding.FragmentMapsBinding
import me.fernandesleite.alagou.models.Flooding
import me.fernandesleite.alagou.util.Directions
import me.fernandesleite.alagou.util.GenerateMarkerIcon

class MapsFragment : Fragment() {

    private val REQUEST_LOCATION_PERMISSION = 1
    private var startedUp = true
    private lateinit var map: GoogleMap
    private lateinit var binding: FragmentMapsBinding
    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private lateinit var viewModel: MapsViewModel
    private lateinit var navController: NavController

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this).get(MapsViewModel::class.java)
        navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment)
        binding = FragmentMapsBinding.inflate(inflater)


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

        binding.apply {
            lifecycleOwner = this@MapsFragment
            btnCriarPonto.setOnClickListener { navigateToFragment(Directions.PONTO_ALAGAMENTO) }
            btnCriarPoi.setOnClickListener { navigateToFragment(Directions.AREA_DE_INTERESSE) }
            btnTraffic.setOnClickListener { toggleTrafego() }
            return root
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        val account = GoogleSignIn.getLastSignedInAccount(requireContext())
        if (account == null) {
            binding.btnCriarPonto.fabOptionEnabled = false
            navController.navigate(R.id.loginFragment2)
        } else {
            binding.btnCriarPonto.fabOptionEnabled = true
            mapFragment?.getMapAsync(callback)
        }
    }

    // -------- Permission / Init ----------

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

    override fun onResume() {
        super.onResume()
        if (this::map.isInitialized) {
            getFloodingsInsideBounds()
        }
    }

    // --------- Callbacks ----------

    private val callback = OnMapReadyCallback { googleMap ->
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
        viewModel.floodings.observe(viewLifecycleOwner, { addMaker(it) })
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
            Directions.PONTO_ALAGAMENTO-> navController.navigate(
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

    private fun toggleTrafego() {
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

    private fun addMaker(floodings: List<Flooding>) {
        if (floodings.isEmpty()) {
            binding.bottomAppBarText.text = getString(R.string.quantityFloodingsPlaceholder_Zero)
        } else {
            binding.bottomAppBarText.text = resources.getQuantityString(
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