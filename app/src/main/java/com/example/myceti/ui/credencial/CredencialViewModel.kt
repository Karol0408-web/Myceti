package com.example.myceti.ui.credencial

import android.graphics.Bitmap
import android.util.Base64
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myceti.data.model.Usuario
import com.example.myceti.data.repository.FirestoreRepository
import kotlinx.coroutines.launch

class CredencialViewModel : ViewModel() {

    private val repo = FirestoreRepository()

    suspend fun getUsuario(): Usuario? {
        return repo.getUsuarioActual()
    }

    suspend fun subirFotoPerfil(bitmap: Bitmap) {
        // Convertir bitmap a Base64
        val outputStream = java.io.ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        val imageBytes = outputStream.toByteArray()

        repo.subirFotoPerfil(imageBytes)
    }
}