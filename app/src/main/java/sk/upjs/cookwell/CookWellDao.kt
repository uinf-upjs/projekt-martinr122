package sk.upjs.cookwell

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy

import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import sk.upjs.cookwell.entities.CalendarEvent
import sk.upjs.cookwell.entities.Ingredient
import sk.upjs.cookwell.entities.IngredientType
import sk.upjs.cookwell.entities.Procedure
import sk.upjs.cookwell.entities.Recipe
import sk.upjs.cookwell.entities.RecipeCalendarEvent
import sk.upjs.cookwell.entities.ShoppingList
import java.util.Date


@Dao
interface CookWellDao {

    @Query("SELECT * FROM Recipe")
    fun getAllRecipes(): Flow<List<Recipe>>

    @Query("SELECT * FROM ingredient WHERE recipe_id = :recipeId")
    fun getIngredientsById(recipeId: Int): List<Ingredient>

    @Query("SELECT * FROM procedure WHERE recipe_id = :recipeId")
    fun getProcedureById(recipeId: Int): Flow<List<Procedure>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsertRecipe(recipe: Recipe): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsertIngredient(ingredient: Ingredient)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsertProcedure(procedure: Procedure)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsertIngredientType(ingredientType: IngredientType)

    @Query("SELECT * FROM ingredient_type")
    fun getAllIngredientType(): Flow<List<IngredientType>>

    @Query("DELETE FROM ingredient WHERE recipe_id = :recipeId")
    fun deleteIngredientByRecipeId(recipeId: Int)

    @Query("DELETE FROM procedure WHERE recipe_id = :recipeId")
    fun deleteProcedureByRecipeId(recipeId: Int)

    @Query("DELETE FROM recipe WHERE id = :recipeId")
    fun deleteRecipeByRecipeId(recipeId: Int)

    @Query("DELETE FROM ingredient_type WHERE id = :typeId")
    fun deleteIngredientTypeById(typeId: Int)

    @Query("SELECT COUNT(*) FROM ingredient WHERE ingredient_type_id = :typeId")
    fun countIngredientTypeUsed(typeId: Int): Int

    @Query("SELECT * FROM shopping_list ORDER BY event_id DESC")
    fun getAllShoppingItems(): Flow<List<ShoppingList>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsertShoppingItem(item: ShoppingList): Long

    @Query("DELETE FROM shopping_list WHERE id = :itemId")
    fun deleteShoppingListItembyId(itemId: Int)

    @Query("SELECT * FROM calendar_event ORDER BY date")
    fun getAllCalendarEvents(): Flow<List<CalendarEvent>>

    @Query("SELECT * FROM calendar_event WHERE date = :date")
    fun getAllCalendarEventsByDay(date: Date): Flow<List<CalendarEvent>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsertCalendarEvent(event: CalendarEvent): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsertRecipeCalendarEvent(recipeCalendarEvent: RecipeCalendarEvent): Long

    @Query("SELECT date FROM calendar_event ce JOIN shopping_list sl ON ce.id = sl.event_id WHERE sl.event_id = :id")
    fun getEventDate(id: Int): Date

    @Query("SELECT it.type FROM ingredient_type it JOIN ingredient i ON i.ingredient_type_id = it.id WHERE i.id = :id")
    fun getTypeByIngredientId(id: Int): String

    @Query("DELETE FROM shopping_list WHERE id>1")
    fun deleteAllShoppingItems()

    @Query("DELETE FROM calendar_event WHERE id = :id")
    fun deleteCalendarEventById(id: Int)

    @Query("SELECT r.* FROM recipe r JOIN recipe_calendar_event rce ON r.id = rce.recipe_id WHERE rce.calendar_event_id = :id")
    fun loadRecipiesByEvent(id: Int): List<Recipe>

    @Query("SELECT COUNT(*) FROM calendar_event WHERE date = :start")
    fun countEventsTommorow(start: Date): Int
}