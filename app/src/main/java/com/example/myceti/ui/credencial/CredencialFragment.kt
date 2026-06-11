package com.example.myceti.ui.credencial

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.myceti.R
import com.example.myceti.databinding.FragmentCredencialBinding
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import kotlinx.coroutines.launch

class CredencialFragment : Fragment() {

    private var _binding: FragmentCredencialBinding? = null
    private val binding get() = _binding!!
    private val vm: CredencialViewModel by viewModels()

    // Launcher para escáner
    private val scannerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val codigo = result.data?.getStringExtra("codigoBarras") ?: return@registerForActivityResult

            binding.tvCodigoBarras.text = codigo
            binding.tvCodigoBarras.visibility = View.VISIBLE

            try {
                val bitmapBarras = generarCodigoBarras(codigo)
                binding.ivCodigoBarrasVisual.setImageBitmap(bitmapBarras)
                binding.ivCodigoBarrasVisual.visibility = View.VISIBLE
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Launcher para cámara
    private val camaraLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val foto = result.data?.extras?.get("data") as? Bitmap ?: return@registerForActivityResult

            // Mostrar localmente
            Glide.with(this).load(foto).circleCrop().into(binding.ivFoto)

            // Convertir a Base64 y subir
            lifecycleScope.launch {
                vm.subirFotoPerfil(foto)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCredencialBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Cargar datos del usuario
        lifecycleScope.launch {
            val usuario = vm.getUsuario()
            usuario?.let {
                binding.tvNombreCredencial.text = it.nombre.uppercase()
                binding.tvNoRegistro.text = it.noRegistro

                // ✅ CORRECCIÓN: Cargar foto desde Base64 correctamente
                if (it.fotoBase64.isNotEmpty()) {
                    try {
                        val imageBytes = Base64.decode(it.fotoBase64, Base64.NO_WRAP)
                        val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)

                        Glide.with(this@CredencialFragment)
                            .load(bitmap)
                            .placeholder(R.drawable.ic_person)
                            .circleCrop()
                            .into(binding.ivFoto)
                    } catch (e: Exception) {
                        // Fallback: intentar como URL normal
                        if (it.fotoUrl.isNotEmpty()) {
                            Glide.with(this@CredencialFragment)
                                .load(it.fotoUrl)
                                .placeholder(R.drawable.ic_person)
                                .circleCrop()
                                .into(binding.ivFoto)
                        }
                    }
                }

                // Cargar código de barras
                if (it.codigoBarras.isNotEmpty()) {
                    binding.tvCodigoBarras.text = it.codigoBarras
                    binding.tvCodigoBarras.visibility = View.VISIBLE

                    try {
                        val bitmapBarras = generarCodigoBarras(it.codigoBarras)
                        binding.ivCodigoBarrasVisual.setImageBitmap(bitmapBarras)
                        binding.ivCodigoBarrasVisual.visibility = View.VISIBLE
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }

        // Botón escanear
        binding.btnEscanear.setOnClickListener {
            scannerLauncher.launch(Intent(requireContext(), ScannerActivity::class.java))
        }

        // Foto de perfil-click
        binding.ivFoto.setOnClickListener {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            camaraLauncher.launch(intent)
        }
    }

    private fun generarCodigoBarras(codigo: String): Bitmap {
        val bitMatrix: BitMatrix = MultiFormatWriter().encode(
            codigo,
            BarcodeFormat.CODE_128,
            400,
            100
        )
        val width = bitMatrix.width
        val height = bitMatrix.height
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)

        for (x in 0 until width) {
            for (y in 0 until height) {
                bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
            }
        }
        return bitmap
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}