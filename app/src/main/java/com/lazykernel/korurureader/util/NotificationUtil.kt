package com.lazykernel.korurureader.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.lazykernel.korurureader.MainActivity
import com.lazykernel.korurureader.R

class NotificationUtil {
    companion object {
        val instance = NotificationUtil()
    }

    private val CHANNEL_ID: String = "KORURU_SCREENSHOT_CHANNEL"
    private val SCREENSHOT_NOTIF_ID: Int = 2000
    private var ssBuilder: NotificationCompat.Builder? = null

    fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = MainActivity.context.getString(R.string.ss_channel_name)
            val descriptionText = MainActivity.context.getString(R.string.ss_channel_desc)
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager = MainActivity.context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun buildScreenshotNotification(intent: Intent) {
        val pendingIntent = PendingIntent.getActivity(MainActivity.context, 0, intent, 0)
        ssBuilder = NotificationCompat.Builder(MainActivity.context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notif)
                .setContentTitle("Koruru")
                .setContentText("Screenshot current view to Koruru")
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setContentIntent(pendingIntent)
                .setAutoCancel(false)
                .setOngoing(true)
    }

    fun showScreenshotNotification() {
        if (ssBuilder == null) {
            Toast.makeText(MainActivity.context, "Cannot show screenshot notification, builder was null", Toast.LENGTH_LONG).show()
            return
        }

        with(NotificationManagerCompat.from(MainActivity.context)) {
            notify(SCREENSHOT_NOTIF_ID, ssBuilder!!.build())
        }
    }
}