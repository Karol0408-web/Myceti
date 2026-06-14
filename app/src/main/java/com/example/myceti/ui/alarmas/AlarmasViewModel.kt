package com.example.myceti.ui.alarmas

import androidx.lifecycle.ViewModel
import com.example.myceti.data.model.Clase
import com.example.myceti.data.repository.FirestoreRepository

class AlarmasViewModel : ViewModel() {
    private val repo = FirestoreRepository()

    suspend fun getClasesHoy(diaSemana: Long): Result<List<Clase>> =
        repo.getUsuarioActual()?.grupo?.let { grupo ->
            repo.getHorario(grupo).map { clases ->
                clases.filter { diaSemana in it.diasSemana }
                    .sortedBy { it.horaInicio }
            }
        } ?: Result.failure(Exception("Sin grupo asignado"))


}