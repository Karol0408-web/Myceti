package com.example.myceti.ui.register

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.myceti.MainActivity
import com.example.myceti.databinding.ActivityRegisterBinding
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import kotlin.getValue

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private val vm: RegisterViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnCrear.setOnClickListener {
            val noReg   = binding.etRegistro.text.toString().trim()
            val nombre  = binding.etNombre.text.toString().trim()
            val correo  = binding.etCorreo.text.toString().trim()
            val pass    = binding.etPassword.text.toString()

            if (noReg.isEmpty() || nombre.isEmpty() || correo.isEmpty() || pass.isEmpty()) {
                Snackbar.make(binding.root, "Completa todos los campos", Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (!correo.endsWith("@ceti.mx")) {
                Snackbar.make(binding.root, "Usa tu correo institucional @ceti.mx", Snackbar.LENGTH_LONG).show()
                return@setOnClickListener
            }
            if (pass.length < 6) {
                Snackbar.make(binding.root, "La contraseña debe tener al menos 6 caracteres", Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            setLoading(true)
            lifecycleScope.launch {
                val result = vm.registrar(nombre, correo, pass, noReg)
                setLoading(false)
                result.onSuccess {
                    startActivity(Intent(this@RegisterActivity, MainActivity::class.java))
                    finishAffinity()
                }.onFailure {
                    Snackbar.make(binding.root, it.message ?: "Error al registrar", Snackbar.LENGTH_LONG).show()
                }
            }
        }

        binding.tvVolver.setOnClickListener { finish() }
    }

    private fun setLoading(loading: Boolean) {
        binding.btnCrear.isEnabled = !loading
        binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
    }
}