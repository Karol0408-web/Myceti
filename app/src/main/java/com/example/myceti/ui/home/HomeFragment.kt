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
import com.example.myceti.databinding.FragmentHomeBinding
import com.example.myceti.ui.login.LoginActivity
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val vm: HomeViewModel by viewModels()
    private lateinit var adapter: ComunicadosAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // TEMPORAL — solo para obtener el token
        com.google.firebase.messaging.FirebaseMessaging.getInstance().token
            .addOnSuccessListener { token ->
                android.util.Log.d("FCM_TOKEN", token)
            }
        com.google.firebase.messaging.FirebaseMessaging.getInstance().token
            .addOnSuccessListener { token ->
                android.util.Log.d("FCM_TOKEN", "TOKEN: $token")
            }
            .addOnFailureListener { e ->
                android.util.Log.e("FCM_TOKEN", "Error: ${e.message}")
            }
        adapter = ComunicadosAdapter()
        binding.rvComunicados.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@HomeFragment.adapter
        }

        lifecycleScope.launch {
            val usuario = vm.getUsuario()
            usuario?.let {
                binding.tvNombre.text = it.nombre
                binding.tvNoControl.text = "No. Control: ${it.noRegistro} · ${it.plantel}"
            }

            vm.getComunicados().onSuccess { lista ->
                adapter.submitList(lista)
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