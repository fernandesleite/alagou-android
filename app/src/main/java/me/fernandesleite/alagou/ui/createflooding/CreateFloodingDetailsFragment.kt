package me.fernandesleite.alagou.ui.createflooding

import android.location.Geocoder
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.textfield.TextInputEditText
import me.fernandesleite.alagou.R
import me.fernandesleite.alagou.databinding.FragmentCreateFloodingDetailsBinding
import me.fernandesleite.alagou.models.FloodingPost

class CreateFloodingDetailsFragment : Fragment() {

    private lateinit var viewModel: CreateFloodingViewModel
    private lateinit var latLong: LatLng

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentCreateFloodingDetailsBinding.inflate(inflater)
        viewModel = ViewModelProvider(requireActivity()).get(CreateFloodingViewModel::class.java)
        binding.apply {
            NavigationUI.setupWithNavController(
                toolbar,
                NavHostFragment.findNavController(requireParentFragment())
            )
            viewModel.latLong.observe(viewLifecycleOwner, { fillLocalizacao(it, localizacao) })
            btnCriarPontoAlagamento.setOnClickListener { createFloodingListener(observacoes) }
        }
        return binding.root
    }

    // --------- Callbacks ----------

    private fun fillLocalizacao(it: LatLng, localizacao: TextView) {
        val geo = Geocoder(requireContext()).getFromLocation(it.latitude, it.longitude, 10)
        localizacao.text = geo[0].getAddressLine(0)
        latLong = it
    }

    private fun createFloodingListener(observacoes: TextInputEditText) {
        viewModel.createFlooding(
            FloodingPost(
                latLong.latitude,
                latLong.longitude,
                observacoes.text.toString(),
                viewModel.getTokenId()
            )
        )
        NavHostFragment.findNavController(requireParentFragment())
            .navigate(R.id.action_createFloodingDetailsFragment_to_mapsFragment)
    }
}