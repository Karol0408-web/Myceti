package com

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build

class MyCETIApp : Application() {
    override fun onCreate() {
        super.onCreate()
        crearCanalNotificaciones()
    }

    private fun crearCanalNotificaciones() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val canal = NotificationChannel(
                CHANNEL_CLASES,
                "Alarmas de Clase",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Recordatorios 15 min antes de cada clase"
            }
            val canalComunicados = NotificationChannel(
                CHANNEL_COMUNICADOS,
                "Comunicados CETI",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Avisos oficiales de la dirección"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(canal)
            manager.createNotificationChannel(canalComunicados)
        }
    }

    companion object {
        const val CHANNEL_CLASES = "channel_clases"
        const val CHANNEL_COMUNICADOS = "channel_comunicados"
    }
}