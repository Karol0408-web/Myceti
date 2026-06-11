package com.example.myceti.data.model

data class Usuario(
    val uid: String = "",
    val nombre: String = "",
    val correo: String = "",
    val noRegistro: String = "",
    val grupo: String = "",
    val plantel: String = "",
    val fotoUrl: String = "",
    val codigoBarras: String = ""
)

data class Clase(
    val id: String = "",
    val materia: String = "",
    val maestro: String = "",
    val salon: String = "",
    val edificio: String = "",
    val horaInicio: String = "",   // "07:00"
    val horaFin: String = "",      // "08:40"
    val diasSemana: List<Long> = emptyList() // 2=Lun,3=Mar,4=Mié,5=Jue,6=Vie (Calendar.DAY_OF_WEEK)
)


