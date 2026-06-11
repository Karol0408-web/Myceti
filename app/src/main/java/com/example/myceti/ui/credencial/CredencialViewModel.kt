package com.example.myceti.ui.credencial

import androidx.lifecycle.ViewModel
import com.example.myceti.data.repository.FirestoreRepository

class CredencialViewModel : ViewModel() {
    private val repo = FirestoreRepository()
    suspend fun getUsuario() = repo.getUsuarioActual()
}
