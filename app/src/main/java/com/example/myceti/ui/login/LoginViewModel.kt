package com.example.myceti.ui.login

import androidx.lifecycle.ViewModel
import com.example.myceti.data.model.Usuario
import com.example.myceti.data.repository.FirestoreRepository
import com.google.firebase.auth.FirebaseAuth

class LoginViewModel : ViewModel() {
    private val repo = FirestoreRepository()

    fun haySession() = FirebaseAuth.getInstance().currentUser != null

    suspend fun login(correo: String, password: String): Result<Usuario> =
        repo.login(correo, password)
}
