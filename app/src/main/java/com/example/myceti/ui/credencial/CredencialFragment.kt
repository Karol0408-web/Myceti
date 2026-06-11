package com.example.myceti.ui.credencial

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.myceti.R
import com.example.myceti.databinding.FragmentCredencialBinding
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream

class CredencialFragment : Fragment() {

    private var _binding: FragmentCredencialBinding? = null
    private val binding get() = _binding!!
    private val vm: CredencialViewModel by viewModels()

    // Lanzador para recibir el código de barras del Scanner
    private val scannerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val codigo = result.data?.getStringExtra("codigoBarras") ?: return@registerForActivityResult
            binding.tvCodigoBarras.text = codigo
            binding.tvCodigoBarras.visibility = View.VISIBLE
        }
    }

    // Lanzador para tomar foto con la cámara
    private val camaraLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val foto = result.data?.extras?.get("data") as? Bitmap ?: return@registerForActivityResult
            // Mostrar foto inmediatamente
            Glide.with(this).load(foto).circleCrop().into(binding.ivFoto)
            // Subir a Firebase Storage
            lifecycleScope.launch {
                val stream = ByteArrayOutputStream()
                foto.compress(Bitmap.CompressFormat.JPEG, 80, stream)
                vm.subirFoto(stream.toByteArray())
            }
        }
    }

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
                binding.tvNoRegistro.text = it.noRegistro

                // Foto de perfil
                if (it.fotoUrl.isNotEmpty()) {
                    Glide.with(this@CredencialFragment)
                        .load(it.fotoUrl)
                        .placeholder(R.drawable.ic_person)
                        .circleCrop()
                        .into(binding.ivFoto)
                }

                // Código de barras guardado
                if (it.codigoBarras.isNotEmpty()) {
                    binding.tvCodigoBarras.text = it.codigoBarras
                    binding.tvCodigoBarras.visibility = View.VISIBLE
                }
            }
        }

        // Botón escanear credencial física
        binding.btnEscanear.setOnClickListener {
            scannerLauncher.launch(
                Intent(requireContext(), ScannerActivity::class.java)
            )
        }

        // Tap en la foto para cambiarla
        binding.ivFoto.setOnClickListener {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            camaraLauncher.launch(intent)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}