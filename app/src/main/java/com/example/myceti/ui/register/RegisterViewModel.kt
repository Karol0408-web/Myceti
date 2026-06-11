package com.example.myceti.ui.register

import androidx.lifecycle.ViewModel
import com.example.myceti.data.repository.FirestoreRepository

class RegisterViewModel : ViewModel() {
    private val repo = FirestoreRepository()
    suspend fun registrar(nombre: String, correo: String, password: String, noRegistro: String) =
        repo.registrar(nombre, correo, password, noRegistro)
}