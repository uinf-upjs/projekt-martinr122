package sk.upjs.cookwell

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import sk.upjs.cookwell.entities.IngredientType
import sk.upjs.cookwell.entities.IngredientWithType
import sk.upjs.cookwell.entities.Recipe
import java.util.Date

class RecipeViewModel(private val repository: RecipeRepository) : ViewModel() {


    class ReceptViewModelFactory(private val repository: RecipeRepository) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(RecipeViewModel::class.java)) {
                return RecipeViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }

    private val _recipes: MutableStateFlow<List<Recipe>> = MutableStateFlow(emptyList())
    val recipes = _recipes

    fun loadAllRecipes() {
        viewModelScope.launch {
            repository.getAllRecipes().collect { recipesList ->
                _recipes.value = recipesList
            }
        }
    }

    private val _types: MutableStateFlow<List<IngredientType>> = MutableStateFlow(emptyList())
    val types: StateFlow<List<IngredientType>> = _types

    init {
        viewModelScope.launch {
            _types.value = getAllIngredientType()
        }
    }

    fun loadAllIngredientType() {
        viewModelScope.launch {
            repository.getAllIngredientType().collect { typesList ->
                _types.value = typesList
            }
        }
    }

    suspend fun getAllIngredientType(): List<IngredientType> {
        return repository.getAllIngredientType().first()
    }

    suspend fun fetchDataForRecipe(recipe: Recipe): Recipe {
        val ingredientTypes = getAllIngredientType()
        val ingredientTypeMap = ingredientTypes.associate { it.id to it.type }

        val ingredients = repository.getIngredientsById(recipe.id)
        val procedures = repository.getProcedureById(recipe.id).first()

        val mappedIngredientsWithType = ingredients.map { ingredient ->
            IngredientWithType(
                ingredient = ingredient,
                ingredientType = IngredientType(
                    id = ingredient.ingredientTypeId,
                    type = ingredientTypeMap[ingredient.ingredientTypeId] ?: ""
                )
            )
        }

        recipe.ingredientsWithType = mappedIngredientsWithType.toMutableList()
        recipe.procedures = procedures.toMutableList()
        return recipe
    }


    fun upsertRecipeWithAttributes(recipe: Recipe) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.upsertRecipeWithAttributes(recipe)
        }
    }

    fun upsertIngredientType(ingredientType: IngredientType) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.upsertIngredientType(ingredientType)
        }
    }

    fun deleteRecipe(recipe: Recipe) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteRecipe(recipe)
        }
    }

    fun deleteIngredientType(typeId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteIngredientType(typeId)
        }
    }

    fun countEventsTommorow(start: Date): Int {
        return repository.countEventsTommorow(start)
    }
}
