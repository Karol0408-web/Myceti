package com.example.myceti.ui.alarmas

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myceti.R
import com.example.myceti.data.model.Clase
import com.example.myceti.databinding.FragmentAlarmasBinding
import com.example.myceti.util.AlarmReceiver
import com.example.myceti.util.AlarmScheduler
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.getValue



class AlarmasFragment : Fragment() {

    private var _binding: FragmentAlarmasBinding? = null
    private val binding get() = _binding!!
    private val vm: AlarmasViewModel by viewModels()
    private lateinit var adapter: AlarmasAdapter

    private val notifPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) {}

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAlarmasBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Solicitar permiso de notificaciones (Android 13+)
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED) {
            notifPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        adapter = AlarmasAdapter()
        binding.rvAlarmas.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@AlarmasFragment.adapter
        }

        // Reloj en tiempo real
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        val sdfDate = SimpleDateFormat("EEEE dd 'de' MMMM 'de' yyyy", Locale("es","MX"))
        binding.tvHora.text = sdf.format(Date())
        binding.tvFecha.text = sdfDate.format(Date()).replaceFirstChar { it.uppercase() }

        val hoy = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)

        lifecycleScope.launch {
            val result = vm.getClasesHoy(hoy.toLong())
            result.onSuccess { clases ->
                adapter.submitList(clases)
                // Programar alarmas automáticamente
                AlarmScheduler.programarAlarmas(requireContext(), clases, hoy)
            }
        }



    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}