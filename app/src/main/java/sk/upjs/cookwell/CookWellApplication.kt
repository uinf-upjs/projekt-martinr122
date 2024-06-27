package sk.upjs.cookwell

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context

class CookWellApplication : Application() {
    val database by lazy { CookWellDatabase.getDatabase(this) }
    val recipeRepository by lazy { RecipeRepository(database.cookWellDao()) }
    val calendarRepository by lazy { CalendarRepository(database.cookWellDao()) }
    val shoppingListRepository by lazy { ShoppingListRepository(database.cookWellDao()) }

    override fun onCreate() {
        super.onCreate()
        val channel = NotificationChannel(
            "main",
            "Tomorrow event",
            NotificationManager.IMPORTANCE_HIGH
        )
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

}