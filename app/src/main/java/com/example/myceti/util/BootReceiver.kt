package com.example.myceti.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.MyCETIApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.example.myceti.R
import com.google.firebase.messaging.RemoteMessage

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // Punto de extensión para re-programar alarmas al reiniciar
        }
    }
}

class CetiFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(message: RemoteMessage) {
        val title = message.notification?.title ?: message.data["title"] ?: "Comunicado CETI"
        val body  = message.notification?.body  ?: message.data["body"]  ?: ""

        // Guardar en Firestore automáticamente
        FirebaseFirestore.getInstance()
            .collection("comunicados")
            .add(mapOf(
                "titulo" to title,
                "descripcion" to body,
                "fecha" to com.google.firebase.Timestamp.now()
            ))

        val notif = NotificationCompat.Builder(this, MyCETIApp.CHANNEL_COMUNICADOS)
            .setSmallIcon(R.drawable.ic_bell)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(this).notify(System.currentTimeMillis().toInt(), notif)
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
    }
}