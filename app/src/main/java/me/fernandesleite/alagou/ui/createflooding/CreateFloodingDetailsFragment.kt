package me.fernandesleite.alagou.ui.createflooding

import android.location.Geocoder
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.textfield.TextInputEditText
import me.fernandesleite.alagou.R
import me.fernandesleite.alagou.models.Flooding
import me.fernandesleite.alagou.util.LatLong

class CreateFloodingDetailsFragment : Fragment() {

    private lateinit var viewModel: CreateFloodingViewModel
    private lateinit var latLong: LatLong

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = ViewModelProvider(requireActivity()).get(CreateFloodingViewModel::class.java)
        return inflater.inflate(R.layout.fragment_create_flooding_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val toolbar = view.findViewById<MaterialToolbar>(R.id.toolbar)

        NavigationUI.setupWithNavController(
            toolbar,
            NavHostFragment.findNavController(requireParentFragment())
        )

        viewModel.latLong.observe(viewLifecycleOwner, Observer { fillLocalizacao(it, view) })

        view.findViewById<Button>(R.id.btn_criar_ponto_alagamento)
            .setOnClickListener { createFloodingListener(view) }
    }

    // --------- Callbacks ----------

    private fun fillLocalizacao(it: LatLong, view: View) {
        val localizacaoText = view.findViewById<TextView>(R.id.localizacao)
        val geo = Geocoder(requireContext()).getFromLocation(it.latitude, it.longitude, 10)
        localizacaoText.text = geo[0].getAddressLine(0)
        latLong = it
    }

    private fun createFloodingListener(view: View) {
        viewModel.createFlooding(
            Flooding(
                latLong.latitude,
                latLong.longitude,
                view.findViewById<TextInputEditText>(R.id.observacoes).text.toString(),
                "test"
            )
        )
        NavHostFragment.findNavController(requireParentFragment())
            .navigate(R.id.action_createFloodingDetailsFragment_to_mapsFragment)
    }
}