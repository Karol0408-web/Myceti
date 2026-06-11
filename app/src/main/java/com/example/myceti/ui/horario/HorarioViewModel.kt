package com.example.myceti.ui.horario

import androidx.lifecycle.ViewModel
import com.example.myceti.data.model.Clase
import com.example.myceti.data.repository.FirestoreRepository

class HorarioViewModel : ViewModel() {
    private val repo = FirestoreRepository()

    suspend fun getClasesDelDia(diaSemana: Int): Result<List<Clase>> {
        val usuario = repo.getUsuarioActual()
            ?: return Result.failure(Exception("Usuario no autenticado"))
        val grupo = usuario.grupo.ifEmpty {
            return Result.failure(Exception("El usuario no tiene grupo asignado"))
        }
        return repo.getHorario(grupo).map { clases ->
            clases
                .filter { diaSemana.toLong() in it.diasSemana }
                .sortedBy { it.horaInicio }
        }
    }
}
