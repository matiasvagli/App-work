package com.matiasdev.elecapp.features.reminders.scheduling

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.matiasdev.elecapp.MainActivity
import com.matiasdev.elecapp.R

fun createReminderNotificationChannel(context: Context) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
    val channel = NotificationChannel(
        REMINDER_CHANNEL_ID,
        REMINDER_CHANNEL_NAME,
        NotificationManager.IMPORTANCE_DEFAULT,
    )
    context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
}

@SuppressLint("MissingPermission")
fun showVisitReminderNotification(context: Context, payload: Intent) {
    if (!notificationsAllowed(context)) return
    val visitId = payload.getStringExtra(EXTRA_VISIT_ID).orEmpty()
    val contentIntent = Intent(context, MainActivity::class.java)
        .putExtra(EXTRA_VISIT_ID, visitId)
        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
    val pendingIntent = PendingIntent.getActivity(
        context,
        pendingIntentRequestCode(visitId),
        contentIntent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )
    val clientName = payload.getStringExtra(EXTRA_CLIENT_NAME).orEmpty()
    val time = payload.getStringExtra(EXTRA_VISIT_TIME).orEmpty()
    val location = payload.getStringExtra(EXTRA_LOCATION).orEmpty()
    val reason = payload.getStringExtra(EXTRA_REASON).orEmpty()
    val body = listOf(clientName, time, location, reason)
        .filter(String::isNotBlank)
        .joinToString(" · ")

    val notification = NotificationCompat.Builder(context, REMINDER_CHANNEL_ID)
        .setSmallIcon(R.drawable.ic_notification)
        .setContentTitle("Visita eléctrica próxima")
        .setContentText(body)
        .setStyle(NotificationCompat.BigTextStyle().bigText(body))
        .setContentIntent(pendingIntent)
        .setAutoCancel(true)
        .build()

    NotificationManagerCompat.from(context).notify(pendingIntentRequestCode(visitId), notification)
}
