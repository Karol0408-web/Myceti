package com.example.myceti.ui.credencial

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
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
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream

class CredencialFragment : Fragment() {

    private var _binding: FragmentCredencialBinding? = null
    private val binding get() = _binding!!
    private val vm: CredencialViewModel by viewModels()

    // Recibe el string del escáner, lo pinta en texto y genera las barras visuales
    private val scannerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val codigo = result.data?.getStringExtra("codigoBarras") ?: return@registerForActivityResult

            // Mostrar texto plano
            binding.tvCodigoBarras.text = codigo
            binding.tvCodigoBarras.visibility = View.VISIBLE

            // Generar y pintar las barras gráficas
            try {
                val bitmapBarras = generarCodigoBarras(codigo)
                binding.ivCodigoBarrasVisual.setImageBitmap(bitmapBarras)
                binding.ivCodigoBarrasVisual.visibility = View.VISIBLE
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Captura la foto de la cámara, la muestra y la manda al ViewModel
    private val camaraLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val foto = result.data?.extras?.get("data") as? Bitmap ?: return@registerForActivityResult

            // Mostrar localmente de inmediato de forma circular
            Glide.with(this).load(foto).circleCrop().into(binding.ivFoto)

            // Subir al Storage a través del ViewModel
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

        // Cargar datos guardados en Firestore al abrir la pantalla
        lifecycleScope.launch {
            val usuario = vm.getUsuario()
            usuario?.let {
                binding.tvNombreCredencial.text = it.nombre.uppercase()
                binding.tvNoRegistro.text = it.noRegistro

                // Cargar foto si ya existe en Firebase Storage
                if (it.fotoUrl.isNotEmpty()) {
                    Glide.with(this@CredencialFragment)
                        .load(it.fotoUrl)
                        .placeholder(R.drawable.ic_person)
                        .circleCrop()
                        .into(binding.ivFoto)
                }

                // Cargar código de barras si ya existe en Firestore
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

        // Evento para abrir la cámara de escaneo
        binding.btnEscanear.setOnClickListener {
            scannerLauncher.launch(
                Intent(requireContext(), ScannerActivity::class.java)
            )
        }

        // Evento para cambiar la foto de perfil
        binding.ivFoto.setOnClickListener {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            camaraLauncher.launch(intent)
        }
    }

    // Transforma cualquier String en un código de barras gráfico de tipo CODE_128
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