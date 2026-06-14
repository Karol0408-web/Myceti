package com.example.myceti.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.MyCETIApp
import com.example.myceti.R
import com.example.myceti.data.model.Clase
import java.util.Calendar

object AlarmScheduler {

    /**
     * Programa una alarma para cada clase de la lista.
     * Se activa 15 minutos antes de la hora de inicio.
     * Solo programa alarmas para horas futuras.
     */
    fun programarAlarmas(context: Context, clases: List<Clase>, diaSemana: Int) {
        val alarmManager = context.getSystemService(AlarmManager::class.java)

        clases.forEach { clase ->
            val (hora, min) = clase.horaInicio.split(":").map { it.toInt() }
            val trigger = Calendar.getInstance().apply {
                set(Calendar.DAY_OF_WEEK, diaSemana)
                set(Calendar.HOUR_OF_DAY, hora)
                set(Calendar.MINUTE, min - 15)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                // Si ya pasó hoy, programar para la próxima semana
                if (timeInMillis < System.currentTimeMillis()) add(Calendar.WEEK_OF_YEAR, 1)
            }

            val intent = Intent(context, AlarmReceiver::class.java).apply {
                putExtra("materia", clase.materia)
                putExtra("salon", clase.salon)
                putExtra("hora", clase.horaInicio)
            }
            val requestCode = (clase.id + diaSemana.toString()).hashCode()
            val pending = PendingIntent.getBroadcast(
                context, requestCode, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, trigger.timeInMillis, pending)
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, trigger.timeInMillis, pending)
            }
        }
    }

    fun cancelarAlarmas(context: Context, clases: List<Clase>, diaSemana: Int) {
        val alarmManager = context.getSystemService(AlarmManager::class.java)
        clases.forEach { clase ->
            val requestCode = (clase.id + diaSemana.toString()).hashCode()
            val intent = Intent(context, AlarmReceiver::class.java)
            val pending = PendingIntent.getBroadcast(
                context, requestCode, intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            pending?.let { alarmManager.cancel(it) }
        }
    }
}

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val materia = intent.getStringExtra("materia") ?: "Clase"
        val salon   = intent.getStringExtra("salon")   ?: ""
        val hora    = intent.getStringExtra("hora")    ?: ""

        val notif = NotificationCompat.Builder(context, MyCETIApp.CHANNEL_CLASES)
            .setSmallIcon(R.drawable.ic_bell)
            .setContentTitle("⏰ $materia en 15 minutos")
            .setContentText("Salón $salon · $hora")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL) // sonido + vibración + luces
            .setAutoCancel(true)
            .build()

        val nm = NotificationManagerCompat.from(context)
        nm.notify(System.currentTimeMillis().toInt(), notif)
    }
}