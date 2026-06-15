package com.example.myceti.ui.alarmas

import android.Manifest
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myceti.databinding.FragmentAlarmasBinding
import com.example.myceti.util.AlarmScheduler
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Timestamp
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AlarmasFragment : Fragment() {

    private var _binding: FragmentAlarmasBinding? = null
    private val binding get() = _binding!!
    private val vm: AlarmasViewModel by viewModels()
    private lateinit var adapterClases: AlarmasAdapter
    private lateinit var adapterPersonalizadas: AlarmasPersonalizadasAdapter

    private val notifPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) {}

    private val relojHandler = Handler(Looper.getMainLooper())
    private lateinit var relojRunnable: Runnable

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAlarmasBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED) {
            notifPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        // Adapter clases del horario
        adapterClases = AlarmasAdapter()
        binding.rvAlarmas.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = adapterClases
        }

        // Adapter alarmas personalizadas
        adapterPersonalizadas = AlarmasPersonalizadasAdapter { alarma ->
            lifecycleScope.launch {
                vm.eliminarAlarma(alarma.id).onSuccess {
                    Snackbar.make(binding.root, "Alarma eliminada", Snackbar.LENGTH_SHORT).show()
                    cargarAlarmasPersonalizadas()
                }
            }
        }
        binding.rvAlarmasPersonalizadas.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = adapterPersonalizadas
        }

        // Reloj en tiempo real
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        val sdfDate = SimpleDateFormat("EEEE dd 'de' MMMM 'de' yyyy", Locale("es", "MX"))
        relojRunnable = object : Runnable {
            override fun run() {
                val ahora = Date()
                binding.tvHora.text = sdf.format(ahora)
                binding.tvFecha.text = sdfDate.format(ahora).replaceFirstChar { it.uppercase() }
                relojHandler.postDelayed(this, 1000)
            }
        }
        relojHandler.post(relojRunnable)

        val hoy = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)

        lifecycleScope.launch {
            vm.getClasesHoy(hoy.toLong()).onSuccess { clases ->
                adapterClases.submitList(clases)
                AlarmScheduler.programarAlarmas(requireContext(), clases, hoy)
            }
        }

        cargarAlarmasPersonalizadas()

        binding.btnAgregarAlarma.setOnClickListener {
            mostrarDialogoNuevaAlarma()
        }
    }

    private fun cargarAlarmasPersonalizadas() {
        lifecycleScope.launch {
            vm.getAlarmas().onSuccess { adapterPersonalizadas.submitList(it) }
        }
    }

    private fun mostrarDialogoNuevaAlarma() {
        val calendarioSeleccionado = Calendar.getInstance()
        val etTitulo = EditText(requireContext()).apply {
            hint = "Título (ej: Examen de Cálculo)"
            setPadding(48, 24, 48, 8)
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Nueva alarma")
            .setView(etTitulo)
            .setPositiveButton("Elegir fecha y hora") { _, _ ->
                val titulo = etTitulo.text.toString().trim()
                if (titulo.isEmpty()) {
                    Snackbar.make(binding.root, "Escribe un título", Snackbar.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                DatePickerDialog(requireContext(), { _, year, month, day ->
                    calendarioSeleccionado.set(year, month, day)
                    TimePickerDialog(requireContext(), { _, hour, minute ->
                        calendarioSeleccionado.set(Calendar.HOUR_OF_DAY, hour)
                        calendarioSeleccionado.set(Calendar.MINUTE, minute)
                        calendarioSeleccionado.set(Calendar.SECOND, 0)
                        guardarAlarma(titulo, calendarioSeleccionado)
                    }, calendarioSeleccionado.get(Calendar.HOUR_OF_DAY),
                        calendarioSeleccionado.get(Calendar.MINUTE), true).show()
                }, calendarioSeleccionado.get(Calendar.YEAR),
                    calendarioSeleccionado.get(Calendar.MONTH),
                    calendarioSeleccionado.get(Calendar.DAY_OF_MONTH)).show()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun guardarAlarma(titulo: String, calendario: Calendar) {
        lifecycleScope.launch {
            val timestamp = Timestamp(calendario.time)
            vm.guardarAlarma(titulo, timestamp).onSuccess { alarma ->
                AlarmScheduler.programarAlarmaPersonalizada(
                    requireContext(), alarma, calendario.timeInMillis
                )
                Snackbar.make(binding.root, "Alarma guardada", Snackbar.LENGTH_SHORT).show()
                cargarAlarmasPersonalizadas()
            }.onFailure {
                Snackbar.make(binding.root, "Error: ${it.message}", Snackbar.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        relojHandler.removeCallbacks(relojRunnable)
        _binding = null
    }
}