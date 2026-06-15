package com.example.myceti.ui.login

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.myceti.MainActivity
import com.example.myceti.R
import com.example.myceti.databinding.ActivityLoginBinding
import com.example.myceti.ui.register.RegisterActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val vm: LoginViewModel by viewModels()

    private val sharedPref by lazy {
        getSharedPreferences("login_prefs", MODE_PRIVATE)
    }

    private val googleLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val idToken = account.idToken ?: return@registerForActivityResult
            setLoading(true)
            lifecycleScope.launch {
                val r = vm.loginConGoogle(idToken)
                setLoading(false)
                r.onSuccess { goHome() }
                    .onFailure { Snackbar.make(binding.root, it.message ?: "Error", Snackbar.LENGTH_LONG).show() }
            }
        } catch (e: ApiException) {
            Snackbar.make(binding.root, "Error Google: ${e.message}", Snackbar.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        cargarDatosGuardados()

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
            guardarDatosSiRecordar(correo, pass)
            setLoading(true)
            lifecycleScope.launch {
                val result = vm.login(correo, pass)
                setLoading(false)
                result.onSuccess { goHome() }
                    .onFailure { Snackbar.make(binding.root, it.message ?: "Error", Snackbar.LENGTH_LONG).show() }
            }
        }

        binding.btnGoogle.setOnClickListener {
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()
            val client = GoogleSignIn.getClient(this, gso)
            googleLauncher.launch(client.signInIntent)
        }

        binding.tvCrearCuenta.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        binding.chkRecordar.setOnCheckedChangeListener { _, isChecked ->
            if (!isChecked) limpiarDatosGuardados()
        }
    }

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

    private fun guardarDatosSiRecordar(registro: String, password: String) {
        sharedPref.edit().apply {
            if (binding.chkRecordar.isChecked) {
                putString("registro", registro)
                putString("password", password)
                putBoolean("recordar", true)
            } else {
                remove("registro")
                remove("password")
                putBoolean("recordar", false)
            }
            apply()
        }
    }

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