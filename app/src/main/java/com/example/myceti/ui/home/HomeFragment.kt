package com.example.myceti.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myceti.R
import com.example.myceti.databinding.FragmentHomeBinding
import com.example.myceti.ui.login.LoginActivity
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val vm: HomeViewModel by viewModels()
    private lateinit var adapter: ComunicadosAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupClickListeners()
        cargarDatosUsuario()
        cargarComunicados()
    }

    private fun setupRecyclerView() {
        adapter = ComunicadosAdapter()
        binding.rvComunicados.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@HomeFragment.adapter
        }
    }

    private fun setupClickListeners() {
        // Usando Navigation Component para navegar entre Fragments
        binding.cardCredencial.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_credencialFragment)
        }

        binding.cardHorario.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_horarioFragment)
        }

        binding.cardRecordatorios.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_alarmasFragment)
        }

        // Logout - Esto sí usa startActivity porque cambia de Activity
        binding.btnLogout.setOnClickListener {
            vm.logout()
            startActivity(Intent(requireContext(), LoginActivity::class.java))
            requireActivity().finishAffinity()
        }
    }

    private fun cargarDatosUsuario() {
        lifecycleScope.launch {
            val usuario = vm.getUsuario()
            usuario?.let {
                binding.tvNombre.text = it.nombre
                binding.tvNoControl.text = "No. Control: ${it.noRegistro} · ${it.plantel}"
            }
        }
    }

    private fun cargarComunicados() {
        lifecycleScope.launch {
            vm.getComunicados().onSuccess { lista ->
                adapter.submitList(lista)
            }.onFailure {
                Toast.makeText(requireContext(), "Error al cargar comunicados", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}