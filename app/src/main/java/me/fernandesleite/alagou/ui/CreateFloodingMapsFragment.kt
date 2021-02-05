package me.fernandesleite.alagou.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.appbar.MaterialToolbar
import me.fernandesleite.alagou.R
import me.fernandesleite.alagou.models.Flooding


class CreateFloodingMapsFragment : Fragment() {

    private lateinit var viewModel: MapsViewModel
    private lateinit var marker: Marker
    private lateinit var map: GoogleMap

    private val callback = OnMapReadyCallback { googleMap ->
        googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(requireContext(), R.raw.map_style_main))
        map = googleMap
        val args = CreateFloodingMapsFragmentArgs.fromBundle(requireArguments())
        val latLng = LatLng(args.latitude.toDouble(), args.longitude.toDouble())
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, args.zoom))
        googleMap.uiSettings.isScrollGesturesEnabled = false
        setMapLongClick(googleMap)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel =  ViewModelProvider(this).get(MapsViewModel::class.java)
        return inflater.inflate(R.layout.fragment_create_flooding_maps, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val toolbar = view.findViewById<MaterialToolbar>(R.id.toolbar)
        NavigationUI.setupWithNavController(
            toolbar,
            NavHostFragment.findNavController(requireParentFragment())
        )
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
        val btnCriar: Button = view.findViewById<Button>(R.id.btn_criar_ponto_alagamento)
        val btnRemover = view.findViewById<Button>(R.id.btn_remover_ponto)
        btnRemover.setOnClickListener {
            marker.remove()
            setMapLongClick(map)
            btnRemover.visibility = View.INVISIBLE
            btnCriar.isEnabled = false
        }
        btnCriar.setOnClickListener {
            viewModel.createFlooding(
                Flooding(
                    "test",
                    marker.position.latitude,
                    marker.position.longitude,
                    1,
                    "test",
                    "test"
                )
            )
            NavHostFragment.findNavController(requireParentFragment()).navigateUp()
        }
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

    private fun setMapLongClick(map: GoogleMap) {
        map.setOnMapClickListener { latLng ->
            view?.findViewById<Button>(R.id.btn_criar_ponto_alagamento)?.isEnabled = true
            view?.findViewById<Button>(R.id.btn_remover_ponto)?.visibility = View.VISIBLE
            marker = map.addMarker(
                generateHomeMarker(requireContext())
                    .position(latLng)
            )
            map.setOnMapClickListener(null)
        }

    }
}
