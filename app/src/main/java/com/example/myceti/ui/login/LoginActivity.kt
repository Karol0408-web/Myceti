package com.example.myceti.ui.login

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.myceti.MainActivity
import com.example.myceti.databinding.ActivityLoginBinding
import com.example.myceti.ui.register.RegisterActivity
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val vm: LoginViewModel by viewModels()

    // Preferencias para guardar los datos del usuario
    private val sharedPref by lazy {
        getSharedPreferences("login_prefs", MODE_PRIVATE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Cargar datos guardados si existe la preferencia
        cargarDatosGuardados()

        // Si ya hay sesión activa, ir directo al Home
        if (vm.haySession()) {
            goHome()
            return
        }

        binding.btnLogin.setOnClickListener {
            val correo = binding.etRegistro.text.toString().trim()
            val pass   = binding.etPassword.text.toString()
            if (correo.isEmpty() || pass.isEmpty()) {
                Snackbar.make(binding.root, "Completa todos los campos", Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Guardar datos si el checkbox está marcado
            guardarDatosSiRecordar(correo, pass)

            setLoading(true)
            lifecycleScope.launch {
                val result = vm.login(correo, pass)
                setLoading(false)
                result.onSuccess { goHome() }
                    .onFailure { Snackbar.make(binding.root, it.message ?: "Error", Snackbar.LENGTH_LONG).show() }
            }
        }

        binding.tvCrearCuenta.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        // Opcional: Escuchar cambios en el checkbox para limpiar datos si se desmarca
        binding.chkRecordar.setOnCheckedChangeListener { _, isChecked ->
            if (!isChecked) {
                limpiarDatosGuardados()
            }
        }
    }

    /**
     * Carga los datos guardados previamente si existen
     */
    private fun cargarDatosGuardados() {
        val registroGuardado = sharedPref.getString("registro", "")
        val passGuardado = sharedPref.getString("password", "")
        val recordar = sharedPref.getBoolean("recordar", false)

        if (recordar && !registroGuardado.isNullOrEmpty()) {
            binding.etRegistro.setText(registroGuardado)
            binding.etPassword.setText(passGuardado)
            binding.chkRecordar.isChecked = true
        }
    }

    /**
     * Guarda los datos si el checkbox está marcado
     */
    private fun guardarDatosSiRecordar(registro: String, password: String) {
        val editor = sharedPref.edit()

        if (binding.chkRecordar.isChecked) {
            editor.putString("registro", registro)
            editor.putString("password", password)
            editor.putBoolean("recordar", true)
        } else {
            // Si no está marcado, aseguramos limpiar datos previos
            editor.remove("registro")
            editor.remove("password")
            editor.putBoolean("recordar", false)
        }
        editor.apply()
    }

    /**
     * Limpia los datos guardados
     */
    private fun limpiarDatosGuardados() {
        sharedPref.edit().apply {
            remove("registro")
            remove("password")
            putBoolean("recordar", false)
            apply()
        }
    }

    private fun setLoading(loading: Boolean) {
        binding.btnLogin.isEnabled = !loading
        binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
    }

    private fun goHome() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}