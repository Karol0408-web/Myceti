package com.example.myceti.ui.home

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myceti.R
import com.example.myceti.databinding.FragmentHomeBinding
import com.example.myceti.ui.login.LoginActivity
import kotlinx.coroutines.launch


class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val vm: HomeViewModel by viewModels()


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding.rvComunicados.apply {
            layoutManager = LinearLayoutManager(requireContext())

        }

        lifecycleScope.launch {
            val usuario = vm.getUsuario()
            usuario?.let {
                binding.tvNombre.text = it.nombre
                binding.tvNoControl.text = "No. Control: ${it.noRegistro} · ${it.plantel}"
            }

        }

        binding.btnLogout.setOnClickListener {
            vm.logout()
            startActivity(Intent(requireContext(), LoginActivity::class.java))
            requireActivity().finishAffinity()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

