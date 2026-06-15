package com.example.myceti.ui.alarmas

import androidx.lifecycle.ViewModel
import com.example.myceti.data.model.AlarmaPersonalizada
import com.example.myceti.data.model.Clase
import com.example.myceti.data.repository.FirestoreRepository
import com.google.firebase.Timestamp

class AlarmasViewModel : ViewModel() {
    private val repo = FirestoreRepository()

    suspend fun getClasesHoy(diaSemana: Long): Result<List<Clase>> =
        repo.getUsuarioActual()?.grupo?.let { grupo ->
            repo.getHorario(grupo).map { clases ->
                clases.filter { diaSemana in it.diasSemana }
                    .sortedBy { it.horaInicio }
            }
        } ?: Result.failure(Exception("Sin grupo asignado"))

    suspend fun guardarAlarma(titulo: String, fecha: Timestamp) =
        repo.guardarAlarma(titulo, fecha)

    suspend fun getAlarmas() = repo.getAlarmas()

    suspend fun eliminarAlarma(id: String) = repo.eliminarAlarma(id)
}