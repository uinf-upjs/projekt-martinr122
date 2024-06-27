package sk.upjs.cookwell

import kotlinx.coroutines.flow.Flow
import sk.upjs.cookwell.entities.CalendarEvent
import sk.upjs.cookwell.entities.Recipe
import sk.upjs.cookwell.entities.RecipeCalendarEvent
import java.util.Date

class CalendarRepository(private val cookWellDao: CookWellDao) {
    fun upsertCalendarEvent(calendarEvent: CalendarEvent): Long {
        return cookWellDao.upsertCalendarEvent(calendarEvent)
    }

    fun upsertRecipeCalendarEvent(recipeCalendarEvent: RecipeCalendarEvent) {
        cookWellDao.upsertRecipeCalendarEvent(recipeCalendarEvent)
    }

    fun getAllCalendarEvents(): Flow<List<CalendarEvent>> {
        return cookWellDao.getAllCalendarEvents()
    }

    fun getAllCalendarEventsByDay(date: Date): Flow<List<CalendarEvent>> {
        return cookWellDao.getAllCalendarEventsByDay(date)
    }

    fun deleteCalendarEvent(event: CalendarEvent) {
        cookWellDao.deleteCalendarEventById(event.id)
    }

    fun loadRecipiesByEvent(eventId: Int): List<Recipe> {
        return cookWellDao.loadRecipiesByEvent(eventId)
    }

}