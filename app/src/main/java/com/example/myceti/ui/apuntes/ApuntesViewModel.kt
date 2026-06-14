package com.example.myceti.ui.apuntes

import androidx.lifecycle.ViewModel
import com.example.myceti.data.repository.FirestoreRepository

class ApuntesViewModel : ViewModel() {
    private val repo = FirestoreRepository()
    suspend fun subirApunte(bytes: ByteArray, materia: String) = repo.subirApunte(bytes, materia)
    suspend fun getApuntes() = repo.getApuntes()
    suspend fun eliminarApunte(id: String) = repo.eliminarApunte(id)
}