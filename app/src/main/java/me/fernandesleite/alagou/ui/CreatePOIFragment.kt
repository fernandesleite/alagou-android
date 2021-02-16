package me.fernandesleite.alagou.ui

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.LinearLayout
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.slider.Slider
import me.fernandesleite.alagou.R
import me.fernandesleite.alagou.util.GenerateMarkerIcon
import kotlin.math.max
import kotlin.math.min


class CreatePOIFragment : Fragment() {

    private lateinit var map: GoogleMap
    private lateinit var navController: NavController
    private lateinit var toolbar: MaterialToolbar
    private lateinit var poi: Circle
    private lateinit var marker: Marker
    private lateinit var slider: LinearLayout
    private var minRadius = 100.0

    private val callback = OnMapReadyCallback { googleMap ->
        val args = CreatePOIFragmentArgs.fromBundle(requireArguments())
        var latLng = LatLng(args.latitude.toDouble(), args.longitude.toDouble())
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, args.zoom))
        googleMap.setMapStyle(
            MapStyleOptions.loadRawResourceStyle(
                requireContext(),
                R.raw.map_style_main
            )
        )
        googleMap.setOnMarkerDragListener(object  : GoogleMap.OnMarkerDragListener {
            override fun onMarkerDragStart(p0: Marker?) {
            }

            override fun onMarkerDrag(p0: Marker?) {
                if(p0 != null) {
                    latLng = LatLng(p0.position.latitude, p0.position.longitude)
                    poi.center = latLng
                }
                Log.i("Test", "test")

            }
            override fun onMarkerDragEnd(p0: Marker?) {
            }

        })
        setMapClick(googleMap)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment)
        val view = inflater.inflate(R.layout.fragment_create_poi, container, false)
        toolbar = view.findViewById(R.id.toolbar)
        slider = view.findViewById(R.id.slider_container)
        NavigationUI.setupWithNavController(
            toolbar,
            NavHostFragment.findNavController(requireParentFragment())
        )
        toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.action_cancel -> {
                    marker.remove()
                    poi.remove()
                    toolbar.menu.clear()
                    slider.visibility = View.GONE
                    toolbar.setNavigationIcon(R.drawable.quantum_ic_arrow_back_grey600_24)
                    true
                }
                R.id.action_salvar -> {

                    true
                }
                else -> {
                    true
                }
            }
        }

        val slider = view.findViewById<Slider>(R.id.slider)
        slider.addOnChangeListener { slider, value, fromUser ->
            minRadius = value.toDouble()
            poi.radius = value.toDouble()
        }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?

        mapFragment?.getMapAsync(callback)
    }

    private fun setMapClick(map: GoogleMap) {

        map.setOnMapClickListener { latLng ->
            toolbar.inflateMenu(R.menu.menu_confirm_flood)

            if(this::marker.isInitialized){
                marker.remove()
                poi.remove()
            }
            poi = map.addCircle(
                CircleOptions().center(latLng).radius(minRadius).fillColor(0x55000000)
            )
            marker = map.addMarker(
                GenerateMarkerIcon.generateMarker(requireContext())
                    .position(latLng).draggable(true)
            )
            map.uiSettings.isScrollGesturesEnabled = false
            map.animateCamera(
                CameraUpdateFactory.newLatLng(
                    LatLng(
                        marker.position.latitude,
                        marker.position.longitude
                    )
                )
            )
            slider.visibility = View.VISIBLE
            val latLng = LatLng(marker.position.latitude, marker.position.longitude)
            poi.center = latLng
        }
    }
}