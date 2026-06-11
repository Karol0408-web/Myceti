package com.example.myceti.ui.credencial

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.myceti.R
import com.example.myceti.databinding.FragmentCredencialBinding
import kotlinx.coroutines.launch
import kotlin.getValue

class CredencialFragment : Fragment() {

    private var _binding: FragmentCredencialBinding? = null
    private val binding get() = _binding!!
    private val vm: CredencialViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCredencialBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lifecycleScope.launch {
            val usuario = vm.getUsuario()
            usuario?.let {
                binding.tvNombreCredencial.text = it.nombre.uppercase()
                binding.tvNoRegistro.text       = it.noRegistro
                if (it.fotoUrl.isNotEmpty()) {
                    Glide.with(this@CredencialFragment)
                        .load(it.fotoUrl)
                        .placeholder(R.drawable.ic_person)
                        .circleCrop()
                        .into(binding.ivFoto)
                }
            }
        }

        binding.btnEscanear.setOnClickListener {
            startActivity(Intent(requireContext(), ScannerActivity::class.java))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}