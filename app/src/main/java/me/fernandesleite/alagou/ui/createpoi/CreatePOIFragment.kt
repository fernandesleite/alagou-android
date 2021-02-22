package me.fernandesleite.alagou.ui.createpoi

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.Circle
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.Marker
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.slider.Slider
import me.fernandesleite.alagou.R
import me.fernandesleite.alagou.databinding.FragmentCreatePoiBinding
import me.fernandesleite.alagou.util.GenerateCirclePoi
import me.fernandesleite.alagou.util.GenerateMarkerIcon

class CreatePOIFragment : Fragment(), PoiDialogFragment.PoiDialogListener {

    private lateinit var poi: Circle
    private lateinit var viewModel: CreatePOIViewModel
    private lateinit var marker: Marker
    private var minRadius = 100.0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this).get(CreatePOIViewModel::class.java)
        val binding = FragmentCreatePoiBinding.inflate(inflater)
        binding.apply {
            NavigationUI.setupWithNavController(
                toolbar,
                NavHostFragment.findNavController(requireParentFragment())
            )
            toolbar.setOnMenuItemClickListener(menuItemListener(toolbar, sliderContainer))
            val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
            mapFragment?.getMapAsync(cb(this))
            return root
        }
    }

    private fun cb(binding: FragmentCreatePoiBinding) = OnMapReadyCallback { googleMap ->
        val args = CreatePOIFragmentArgs.fromBundle(requireArguments())
        var latLng = LatLng(args.latitude.toDouble(), args.longitude.toDouble())
        viewModel.currentMarkerPosition.observe(viewLifecycleOwner, { latLng = it ?: latLng })
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, args.zoom))
        googleMap.setMapStyle(
            MapStyleOptions.loadRawResourceStyle(
                requireContext(),
                R.raw.map_style_main
            )
        )
        googleMap.setOnMarkerDragListener(onDragListener())
        googleMap.setOnMapClickListener {
            viewModel.setCurrentMarkerPosition(it)
            viewModel.setMarkerStatus()
        }
        setMapClick(googleMap, binding)
    }

    private fun onDragListener() = object : GoogleMap.OnMarkerDragListener {
        override fun onMarkerDragStart(p0: Marker?) {}
        override fun onMarkerDrag(p0: Marker?) {
            if (p0 != null) {
                poi.center = LatLng(p0.position.latitude, p0.position.longitude)
            }
        }

        override fun onMarkerDragEnd(p0: Marker?) {
            if (p0 != null) {
                viewModel.setCurrentMarkerPosition(
                    LatLng(
                        p0.position.latitude,
                        p0.position.longitude
                    )
                )
            }
        }
    }

    private fun sliderListener() = Slider.OnChangeListener { _, value, _ ->
        minRadius = value.toDouble()
        viewModel.setCurrentRadius(value.toDouble())
    }

    private fun menuItemListener(
        toolbar: MaterialToolbar,
        sliderContainer: LinearLayout
    ) = Toolbar.OnMenuItemClickListener {
        when (it.itemId) {
            R.id.action_cancel -> {
                marker.remove()
                poi.remove()
                toolbar.menu.clear()
                viewModel.setMarkerStatus()
                sliderContainer.visibility = View.GONE
                toolbar.setNavigationIcon(R.drawable.quantum_ic_arrow_back_grey600_24)
                true
            }
            R.id.action_salvar -> {
                val dialog = PoiDialogFragment()
                dialog.setTargetFragment(this, 0)
                dialog.show(parentFragmentManager, "PoiDialogFragment")
                true
            }
            else -> {
                true
            }
        }
    }

    private fun setMapClick(map: GoogleMap, binding: FragmentCreatePoiBinding) {
        viewModel.markerActive.observe(viewLifecycleOwner, {
            if (it) {
                viewModel.currentMarkerPosition.observe(viewLifecycleOwner, { latLng ->
                    if (this::marker.isInitialized) {
                        marker.remove()
                        poi.remove()
                    }
                    poi = map.addCircle(GenerateCirclePoi.generateCircle(latLng, minRadius))
                    viewModel.currentRadius.observe(viewLifecycleOwner, { poi.radius = it })
                    marker = map.addMarker(
                        GenerateMarkerIcon.generateMarker(
                            requireContext(),
                            R.drawable.ic_priority_high_red_48dp
                        )
                            .position(latLng).draggable(true)
                    )
                    poi.center = LatLng(marker.position.latitude, marker.position.longitude)
                    if (!binding.toolbar.menu.hasVisibleItems()) {
                        binding.toolbar.inflateMenu(R.menu.menu_confirm_flood)
                        binding.toolbar.navigationIcon = null
                    }
                    binding.slider.addOnChangeListener(sliderListener())
                    binding.sliderContainer.visibility = View.VISIBLE
                })
            }
        })

    }

    override fun onDialogPositiveClick(dialogFragment: DialogFragment, nome: String) {
        viewModel.insertPoi(nome, poi.center.latitude, poi.center.longitude, poi.radius)
        findNavController().popBackStack()
    }
}