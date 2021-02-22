package me.fernandesleite.alagou.ui.mainmap

import android.content.Intent
import android.location.Geocoder
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import me.fernandesleite.alagou.R
import me.fernandesleite.alagou.databinding.FragmentDisplayFloodingInfoBinding
import me.fernandesleite.alagou.models.Flooding
import me.fernandesleite.alagou.util.GenerateMarkerIcon


class DisplayFloodingInfoFragment : Fragment() {

    private lateinit var viewModel: MapsViewModel
    private lateinit var mMiniMap: MapView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this).get(MapsViewModel::class.java)
        val binding = FragmentDisplayFloodingInfoBinding.inflate(inflater)
        binding.vModel = viewModel
        binding.lifecycleOwner = this
        binding.apply {
            mMiniMap = miniMap
            miniMap.onCreate(savedInstanceState)
            miniMap.onResume()
            miniMap.getMapAsync(callback)
            NavigationUI.setupWithNavController(
                toolbar,
                NavHostFragment.findNavController(requireParentFragment())
            )
            val args = DisplayFloodingInfoFragmentArgs.fromBundle(requireArguments())
            viewModel.getFlooding(args.id)
            viewModel.flooding.observe(
                viewLifecycleOwner,
                { showInfo(it, address, rotasContainer) })
        }
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        mMiniMap.onResume()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mMiniMap.onDestroy()
    }

    override fun onPause() {
        super.onPause()
        mMiniMap.onPause()
    }

    // --------- Callbacks ----------

    private val callback = OnMapReadyCallback { googleMap ->
        googleMap.setMapStyle(
            MapStyleOptions.loadRawResourceStyle(
                requireContext(),
                R.raw.map_style_main
            )
        )
        googleMap.uiSettings.setAllGesturesEnabled(false)
        viewModel.flooding.observe(viewLifecycleOwner, { moveCameraMap(it, googleMap) })
    }

    private fun showInfo(
        flooding: Flooding,
        address: TextView,
        rotasContainer: LinearLayout
    ) {
        val geo =
            Geocoder(requireContext()).getFromLocation(flooding.latitude, flooding.longitude, 1)
        address.text = geo[0].getAddressLine(0)
        rotasContainer
            .setOnClickListener { callGoogleMaps(flooding) }
    }

    private fun callGoogleMaps(flooding: Flooding) {
        val gmmIntentUri = Uri.parse("geo:${flooding.latitude},${flooding.longitude}")
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
        mapIntent.setPackage("com.google.android.apps.maps")
        mapIntent.resolveActivity(requireContext().packageManager)?.let {
            startActivity(mapIntent)
        }
    }

    private fun moveCameraMap(it: Flooding, googleMap: GoogleMap) {
        val cameraUpdate = CameraUpdateFactory.newLatLngZoom(
            LatLng(it.latitude, it.longitude),
            15f
        )
        googleMap.moveCamera(cameraUpdate)
        googleMap.addMarker(
            GenerateMarkerIcon.generateMarker(requireContext(), R.drawable.ic_marker)
                .position(LatLng(it.latitude, it.longitude))
        )
    }
}