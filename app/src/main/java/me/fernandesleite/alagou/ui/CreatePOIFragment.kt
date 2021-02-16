package me.fernandesleite.alagou.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
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
import me.fernandesleite.alagou.databinding.FragmentCreatePoiBinding
import me.fernandesleite.alagou.util.GenerateMarkerIcon

class CreatePOIFragment : Fragment() {

    private lateinit var poi: Circle
    private lateinit var marker: Marker
    private var minRadius = 100.0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentCreatePoiBinding.inflate(inflater)
        binding.apply {
            NavigationUI.setupWithNavController(
                toolbar,
                NavHostFragment.findNavController(requireParentFragment())
            )
            toolbar.setOnMenuItemClickListener(menuItemListener(toolbar, slider))
            slider.addOnChangeListener(sliderListener())
            val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
            mapFragment?.getMapAsync(cb(this))
            return root
        }
    }

    private fun cb(binding: FragmentCreatePoiBinding) = OnMapReadyCallback { googleMap ->
        val args = CreatePOIFragmentArgs.fromBundle(requireArguments())
        val latLng = LatLng(args.latitude.toDouble(), args.longitude.toDouble())
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, args.zoom))
        googleMap.setMapStyle(
            MapStyleOptions.loadRawResourceStyle(
                requireContext(),
                R.raw.map_style_main
            )
        )
        googleMap.setOnMarkerDragListener(onDragListener())
        setMapClick(googleMap, binding)

    }
    private fun onDragListener() = object: GoogleMap.OnMarkerDragListener{
        override fun onMarkerDragStart(p0: Marker?) {}
        override fun onMarkerDrag(p0: Marker?) {
            if (p0 != null) {
                poi.center = LatLng(p0.position.latitude, p0.position.longitude)
            }
        }
        override fun onMarkerDragEnd(p0: Marker?) {}
    }

    private fun sliderListener() = Slider.OnChangeListener { slider, value, fromUser ->
        minRadius = value.toDouble()
        poi.radius = value.toDouble()
    }

    private fun menuItemListener(
        toolbar: MaterialToolbar,
        slider: Slider
    ) = Toolbar.OnMenuItemClickListener {
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

    private fun setMapClick(map: GoogleMap, binding: FragmentCreatePoiBinding) {
        map.setOnMapClickListener { latLng ->
            binding.toolbar.inflateMenu(R.menu.menu_confirm_flood)
            if (this::marker.isInitialized) {
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
            binding.slider.visibility = View.VISIBLE
            poi.center = LatLng(marker.position.latitude, marker.position.longitude)
        }
    }
}