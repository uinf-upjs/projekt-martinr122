package sk.upjs.cookwell.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import java.io.Serializable

@Entity(tableName = "recipe_calendar_event",
    primaryKeys = ["recipe_id", "calendar_event_id"],
    foreignKeys = [
        ForeignKey(
            entity = Recipe::class,
            parentColumns = ["id"],
            childColumns = ["recipe_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = CalendarEvent::class,
            parentColumns = ["id"],
            childColumns = ["calendar_event_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class RecipeCalendarEvent(
    @ColumnInfo(name = "recipe_id")
    val recipeId: Int,

    @ColumnInfo(name = "calendar_event_id")
    val calendarEventId: Int
): Serializable