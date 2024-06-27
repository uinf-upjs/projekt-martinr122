package sk.upjs.cookwell

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.core.app.NotificationCompat
import java.text.SimpleDateFormat
import java.util.Date

class NotificationService(private val context: Context) {
    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val myIntent = Intent(context, MainActivity::class.java)
    private val pendingIntent = PendingIntent.getActivity(
        context,
        200,
        myIntent,
        PendingIntent.FLAG_IMMUTABLE
    )
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("notification", Context.MODE_PRIVATE)

    fun showNotification() {
        if (shouldShowNotification()) {
            val notification = NotificationCompat.Builder(context, "main")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(context.getString(R.string.alert))
                .setContentText(context.getString(R.string.alertText))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .build()
            notificationManager.notify(100, notification)

            val currentDate = SimpleDateFormat("dd.M.yyyy").format(Date())
            with(sharedPreferences.edit()) {
                putString("last_notification_date", currentDate)
                apply()
            }
        }
    }

    fun shouldShowNotification(): Boolean {
        val lastNotificationDate = sharedPreferences.getString("last_notification_date", null)
        if (lastNotificationDate == null) {
            return true
        }
        val currentDate = SimpleDateFormat("dd.M.yyyy").format(Date())
        return lastNotificationDate != currentDate
    }

}