package me.fernandesleite.alagou.ui.createflooding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.Marker
import com.google.android.material.appbar.MaterialToolbar
import me.fernandesleite.alagou.R
import me.fernandesleite.alagou.databinding.FragmentCreateFloodingMapsBinding
import me.fernandesleite.alagou.util.GenerateMarkerIcon


class CreateFloodingMapsFragment : Fragment() {

    private lateinit var viewModel: CreateFloodingViewModel
    private lateinit var marker: Marker
    private lateinit var map: GoogleMap
    private lateinit var navController: NavController

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment)
        viewModel = ViewModelProvider(requireActivity()).get(CreateFloodingViewModel::class.java)
        val binding = FragmentCreateFloodingMapsBinding.inflate(inflater)
        binding.apply {
            NavigationUI.setupWithNavController(
                toolbar,
                NavHostFragment.findNavController(requireParentFragment())
            )
            toolbar.setOnMenuItemClickListener {
                menuListenerSaveCancel(it, toolbar)
            }
            val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
            mapFragment?.getMapAsync(callback(toolbar))
        }
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        map.uiSettings.isScrollGesturesEnabled = true
        if (this::marker.isInitialized) {
            marker.remove()
        }
    }

    // --------- Callbacks ----------

    private fun callback(toolbar: MaterialToolbar) = OnMapReadyCallback { googleMap ->
        googleMap.setMapStyle(
            MapStyleOptions.loadRawResourceStyle(
                requireContext(),
                R.raw.map_style_main
            )
        )
        map = googleMap
        val args = CreateFloodingMapsFragmentArgs.fromBundle(requireArguments())
        val latLng = LatLng(args.latitude.toDouble(), args.longitude.toDouble())
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, args.zoom))
        setMapClick(googleMap, toolbar)
    }

    private fun menuListenerSaveCancel(it: MenuItem, toolbar: MaterialToolbar): Boolean {
        when (it.itemId) {
            R.id.action_cancel -> {
                marker.remove()
                setMapClick(map, toolbar)
                map.uiSettings.isScrollGesturesEnabled = true
                toolbar.menu.clear()
                toolbar.setNavigationIcon(R.drawable.quantum_ic_arrow_back_grey600_24)
                return true
            }
            R.id.action_salvar -> {
                viewModel.setLatlong(marker.position.latitude, marker.position.longitude)
                navigateToDetails()
                return true
            }
            else -> {
                return true
            }
        }
    }

    private fun setMapClick(map: GoogleMap, toolbar: MaterialToolbar) {
        map.setOnMapClickListener { latLng ->
            marker = map.addMarker(
                GenerateMarkerIcon.generateMarker(requireContext(), R.drawable.ic_marker)
                    .position(latLng)
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
            map.setOnMapClickListener(null)
            toolbar.inflateMenu(R.menu.menu_confirm_flood)
            toolbar.navigationIcon = null
        }
        setHasOptionsMenu(true)
    }

    private fun navigateToDetails() {
        navController.navigate(R.id.createFloodingDetailsFragment)
    }
}
