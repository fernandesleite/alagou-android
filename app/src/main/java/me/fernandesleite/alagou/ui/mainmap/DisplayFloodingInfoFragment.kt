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
import com.google.android.material.appbar.MaterialToolbar
import me.fernandesleite.alagou.R
import me.fernandesleite.alagou.models.Flooding
import me.fernandesleite.alagou.util.GenerateMarkerIcon


class DisplayFloodingInfoFragment : Fragment() {

    private lateinit var viewModel: MapsViewModel
    private lateinit var miniMap: MapView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val args = DisplayFloodingInfoFragmentArgs.fromBundle(requireArguments())
        viewModel = ViewModelProvider(this).get(MapsViewModel::class.java)
        viewModel.getFlooding(args.id)
        val view = inflater.inflate(R.layout.fragment_display_flooding_info, container, false)
        miniMap = view.findViewById<MapView>(R.id.miniMap)
        miniMap.onCreate(savedInstanceState)
        miniMap.onResume()
        miniMap.getMapAsync(callback)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val toolbar = view.findViewById<MaterialToolbar>(R.id.toolbar)
        NavigationUI.setupWithNavController(
            toolbar,
            NavHostFragment.findNavController(requireParentFragment())
        )
        viewModel.flooding.observe(viewLifecycleOwner, { showInfo(it, view) })
    }

    override fun onResume() {
        super.onResume()
        miniMap.onResume()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        miniMap.onDestroy()
    }

    override fun onPause() {
        super.onPause()
        miniMap.onPause()
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

    private fun showInfo(flooding: Flooding, view: View) {
        val geo =
            Geocoder(requireContext()).getFromLocation(flooding.latitude, flooding.longitude, 1)
        view.findViewById<TextView>(R.id.observacoes).text = flooding.note
        view.findViewById<TextView>(R.id.criado_por).text =
            getString(R.string.criado_por, flooding.user)
        view.findViewById<TextView>(R.id.address).text = geo[0].getAddressLine(0)
        view.findViewById<LinearLayout>(R.id.rotas_container)
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
            GenerateMarkerIcon.generateMarker(requireContext())
                .position(LatLng(it.latitude, it.longitude))
        )
    }
}