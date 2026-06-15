package com.example.myceti.ui.apuntes

import android.app.AlertDialog
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.example.myceti.databinding.FragmentApuntesBinding
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import kotlin.getValue

class ApuntesFragment : Fragment() {

    private var _binding: FragmentApuntesBinding? = null
    private val binding get() = _binding!!
    private val vm: ApuntesViewModel by viewModels()
    private lateinit var adapter: ApuntesAdapter

    private val materias = arrayOf(
        "Inteligencia Artificial", "Bases de Datos",
        "Desarrollo Móvil", "Cálculo Integral", "Programación", "Otra"
    )

    private var filtroActual = "Todos"

    private val photoPicker = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
        uri?.let { elegirMateriaYSubir(it) }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentApuntesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = ApuntesAdapter(
            onEliminar = { apunte ->
                lifecycleScope.launch {
                    vm.eliminarApunte(apunte.id).onSuccess {
                        Snackbar.make(binding.root, "Apunte eliminado", Snackbar.LENGTH_SHORT).show()
                        cargarApuntes()
                    }.onFailure {
                        Snackbar.make(binding.root, "Error al eliminar: ${it.message}", Snackbar.LENGTH_LONG).show()
                    }
                }
            },
            onVerImagen = { base64 ->
                ImagenFullscreenDialog.newInstance(base64)
                    .show(parentFragmentManager, "imagen_fullscreen")
            }
        )

        // GridLayoutManager de 3 columnas; los headers ocupan las 3
        val gridLayout = GridLayoutManager(requireContext(), 3)
        gridLayout.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return when (adapter.getItemViewType(position)) {
                    ApuntesAdapter.TYPE_HEADER -> 3
                    else -> 1
                }
            }
        }

        binding.rvApuntes.apply {
            layoutManager = gridLayout
            adapter = this@ApuntesFragment.adapter
        }

        binding.btnAgregarApunte.setOnClickListener {
            photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        // Chips de filtro
        binding.chipTodos.setOnClickListener { aplicarFiltro("Todos") }
        binding.chipIA.setOnClickListener { aplicarFiltro("Inteligencia Artificial") }
        binding.chipBD.setOnClickListener { aplicarFiltro("Bases de Datos") }
        binding.chipCalculo.setOnClickListener { aplicarFiltro("Cálculo Integral") }
        binding.chipProg.setOnClickListener { aplicarFiltro("Programación") }

        cargarApuntes()
    }

    private fun aplicarFiltro(filtro: String) {
        filtroActual = filtro
        cargarApuntes()
    }

    private fun elegirMateriaYSubir(uri: Uri) {
        AlertDialog.Builder(requireContext())
            .setTitle("¿A qué materia pertenece?")
            .setItems(materias) { _, idx -> subirApunte(uri, materias[idx]) }
            .show()
    }

    private fun subirApunte(uri: Uri, materia: String) {
        binding.progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            val bytes = requireContext().contentResolver.openInputStream(uri)?.readBytes()
            if (bytes == null) {
                binding.progressBar.visibility = View.GONE
                Snackbar.make(binding.root, "Error al leer la imagen", Snackbar.LENGTH_SHORT).show()
                return@launch
            }
            val result = vm.subirApunte(bytes, materia)
            binding.progressBar.visibility = View.GONE
            result.onSuccess {
                Snackbar.make(binding.root, "Apunte guardado en $materia", Snackbar.LENGTH_SHORT).show()
                cargarApuntes()
            }.onFailure {
                Snackbar.make(binding.root, "Error: ${it.message}", Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun cargarApuntes() {
        lifecycleScope.launch {
            vm.getApuntes().onSuccess { lista ->
                adapter.submitApuntes(lista, filtroActual)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}