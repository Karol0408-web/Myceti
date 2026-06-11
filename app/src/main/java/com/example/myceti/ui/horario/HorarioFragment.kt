package com.example.myceti.ui.horario

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myceti.R
import com.example.myceti.databinding.FragmentHorarioBinding
import kotlinx.coroutines.launch
import java.util.Calendar
import kotlin.getValue

class HorarioFragment : Fragment() {

    private var _binding: FragmentHorarioBinding? = null
    private val binding get() = _binding!!
    private val vm: HorarioViewModel by viewModels()
    private lateinit var adapter: ClasesAdapter

    private val diasNombre = mapOf(
        Calendar.MONDAY    to "Lunes",
        Calendar.TUESDAY   to "Martes",
        Calendar.WEDNESDAY to "Miércoles",
        Calendar.THURSDAY  to "Jueves",
        Calendar.FRIDAY    to "Viernes"
    )

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHorarioBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = ClasesAdapter()
        binding.rvClases.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@HorarioFragment.adapter
        }

        val hoy = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
        binding.tvDia.text = diasNombre[hoy] ?: "Hoy"

        // Botones de día de la semana
        val botones = listOf(
            binding.btnLun to Calendar.MONDAY,
            binding.btnMar to Calendar.TUESDAY,
            binding.btnMie to Calendar.WEDNESDAY,
            binding.btnJue to Calendar.THURSDAY,
            binding.btnVie to Calendar.FRIDAY
        )
        botones.forEach { (btn, dia) ->
            if (dia == hoy) btn.isSelected = true
            btn.setOnClickListener {
                botones.forEach { (b, _) -> b.isSelected = false }
                btn.isSelected = true
                binding.tvDia.text = diasNombre[dia] ?: ""
                cargarClases(dia)
            }
        }

        cargarClases(hoy)
    }

    private fun cargarClases(dia: Int) {
        lifecycleScope.launch {
            binding.progressBar.visibility = View.VISIBLE
            android.util.Log.d("HORARIO", "Buscando dia=$dia")
            val result = vm.getClasesDelDia(dia)
            binding.progressBar.visibility = View.GONE
            result
                .onSuccess { clases ->
                    android.util.Log.d("HORARIO", "Total clases: ${clases.size}")
                    clases.forEach {
                        android.util.Log.d("HORARIO", "  -> ${it.materia} dias=${it.diasSemana}")
                    }
                    if (clases.isEmpty()) {
                        binding.tvError.text = "Sin clases para este día"
                        binding.tvError.visibility = View.VISIBLE
                    } else {
                        binding.tvError.visibility = View.GONE
                    }
                    adapter.submitList(clases)
                }
                .onFailure { e ->
                    android.util.Log.e("HORARIO", "Error: ${e.message}")
                    binding.tvError.text = e.message
                    binding.tvError.visibility = View.VISIBLE
                }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}