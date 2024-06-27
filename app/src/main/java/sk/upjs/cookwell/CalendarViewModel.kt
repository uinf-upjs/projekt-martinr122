package sk.upjs.cookwell

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import sk.upjs.cookwell.entities.CalendarEvent
import sk.upjs.cookwell.entities.Ingredient
import sk.upjs.cookwell.entities.Recipe
import sk.upjs.cookwell.entities.RecipeCalendarEvent
import sk.upjs.cookwell.entities.ShoppingList
import java.util.Date

class CalendarViewModel(
    private val calendarRepository: CalendarRepository,
    private val recipeRepository: RecipeRepository,
    private val shoppingListRepository: ShoppingListRepository
) : ViewModel() {
    class CalendarViewModelFactory(
        private val calendarRepository: CalendarRepository,
        private val recipeRepository: RecipeRepository,
        private val shoppingListRepository: ShoppingListRepository
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(CalendarViewModel::class.java)) {
                return CalendarViewModel(
                    calendarRepository,
                    recipeRepository,
                    shoppingListRepository
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }

    private val _recipes: MutableStateFlow<List<Recipe>> = MutableStateFlow(emptyList())
    val recipes: Flow<List<Recipe>> = _recipes

    fun loadAllRecipes() {
        viewModelScope.launch {
            recipeRepository.getAllRecipes().collect { recipesList ->
                _recipes.value = recipesList
            }
        }
    }


    private val _events: MutableStateFlow<List<CalendarEvent>> = MutableStateFlow(emptyList())
    val events: Flow<List<CalendarEvent>> = _events

    fun loadAllEvents() {
        viewModelScope.launch {
            calendarRepository.getAllCalendarEvents().collect { events ->
                _events.value = events
            }
        }
    }

    private val _eventsByDay: MutableStateFlow<List<CalendarEvent>> = MutableStateFlow(emptyList())
    val eventsByDay: Flow<List<CalendarEvent>> = _eventsByDay

    fun getAllCalendarEventsByDay(date: Date) {
        viewModelScope.launch {
            calendarRepository.getAllCalendarEventsByDay(date).collect { events ->
                _eventsByDay.value = events
            }
        }
    }

    fun upsertCalendarEvent(calendarEvent: CalendarEvent): Long {
        return calendarRepository.upsertCalendarEvent(calendarEvent)
    }

    fun upsertRecipeCalendarEvent(recipeCalendarEvent: RecipeCalendarEvent) {
        viewModelScope.launch(Dispatchers.IO) {
            calendarRepository.upsertRecipeCalendarEvent(recipeCalendarEvent)
        }
    }

    fun getIngredientsById(recipeId: Int): List<Ingredient> {
        return recipeRepository.getIngredientsById(recipeId)
    }

    fun upsertShoppingListItem(item: ShoppingList) {
        viewModelScope.launch(Dispatchers.IO) {
            shoppingListRepository.upsertShoppingListItem(item)
        }
    }

    fun deleteEvent(event: CalendarEvent) {
        viewModelScope.launch(Dispatchers.IO) {
            calendarRepository.deleteCalendarEvent(event)
        }
    }

    fun loadRecipiesByEvent(event: CalendarEvent): List<Recipe> {
        return calendarRepository.loadRecipiesByEvent(event.id)
    }

}