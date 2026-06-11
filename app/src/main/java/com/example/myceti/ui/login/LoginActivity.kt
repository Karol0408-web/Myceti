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
import kotlin.getValue

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val vm: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
