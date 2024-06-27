package sk.upjs.cookwell

import kotlinx.coroutines.flow.Flow
import sk.upjs.cookwell.entities.Ingredient
import sk.upjs.cookwell.entities.IngredientType
import sk.upjs.cookwell.entities.Procedure
import sk.upjs.cookwell.entities.Recipe
import java.util.Date

class RecipeRepository(private val cookWellDao: CookWellDao) {

    fun getAllRecipes(): Flow<List<Recipe>> {
        return cookWellDao.getAllRecipes()
    }

    fun getIngredientsById(recipeId: Int): List<Ingredient> {
        return cookWellDao.getIngredientsById(recipeId)
    }

    fun getProcedureById(recipeId: Int): Flow<List<Procedure>> {
        return cookWellDao.getProcedureById(recipeId)
    }

    fun upsertRecipeWithAttributes(recipe: Recipe) {
        with(recipe) {
            val recipeId = cookWellDao.upsertRecipe(recipe)
            ingredientsWithType.forEach {
                it.ingredient.recipeId = recipeId.toInt()
                cookWellDao.upsertIngredient(it.ingredient)
            }
            procedures.forEach {
                it.recipeId = recipeId.toInt()
                cookWellDao.upsertProcedure(it)
            }
        }
    }

    fun upsertIngredientType(ingredientType: IngredientType) {
        cookWellDao.upsertIngredientType(ingredientType)
    }

    fun getAllIngredientType(): Flow<List<IngredientType>> {
        return cookWellDao.getAllIngredientType()
    }

    fun deleteRecipe(recipe: Recipe) {
        cookWellDao.deleteIngredientByRecipeId(recipe.id)
        cookWellDao.deleteProcedureByRecipeId(recipe.id)
        cookWellDao.deleteRecipeByRecipeId(recipe.id)
    }

    fun deleteIngredientType(typeId: Int) {
        if (cookWellDao.countIngredientTypeUsed(typeId) == 0) {
            cookWellDao.deleteIngredientTypeById(typeId)
        }

    }

    fun countEventsTommorow(start: Date): Int {
        return cookWellDao.countEventsTommorow(start)
    }

}