package com.example.myceti.ui.home

import androidx.lifecycle.ViewModel
import com.example.myceti.data.repository.FirestoreRepository

class HomeViewModel : ViewModel() {
    private val repo = FirestoreRepository()
    suspend fun getUsuario() = repo.getUsuarioActual()
    fun logout() = repo.logout()
}