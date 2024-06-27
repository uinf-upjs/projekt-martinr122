package sk.upjs.cookwell

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import sk.upjs.cookwell.entities.CalendarEvent
import sk.upjs.cookwell.entities.Ingredient
import sk.upjs.cookwell.entities.IngredientType
import sk.upjs.cookwell.entities.Procedure
import sk.upjs.cookwell.entities.Recipe
import sk.upjs.cookwell.entities.RecipeCalendarEvent
import sk.upjs.cookwell.entities.ShoppingList


@Database(
    entities = [Recipe::class, Ingredient::class, IngredientType::class, Procedure::class, CalendarEvent::class, RecipeCalendarEvent::class, ShoppingList::class],
    version = 5,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class CookWellDatabase : RoomDatabase() {
    abstract fun cookWellDao(): CookWellDao

    companion object {
        @Volatile
        private var INSTANCE: CookWellDatabase? = null

        fun getDatabase(context: Context): CookWellDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CookWellDatabase::class.java,
                    "cookwell_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}