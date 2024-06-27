package sk.upjs.cookwell

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import sk.upjs.cookwell.entities.Ingredient
import sk.upjs.cookwell.entities.IngredientType
import sk.upjs.cookwell.entities.IngredientWithType
import sk.upjs.cookwell.entities.Procedure
import sk.upjs.cookwell.entities.Recipe
import sk.upjs.cookwell.ui.theme.CookWellTheme

class EditReceptActivity : ComponentActivity() {
    private val recipeViewModel: RecipeViewModel by viewModels {
        RecipeViewModel.ReceptViewModelFactory((application as CookWellApplication).recipeRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CookWellTheme {
                var recipe = intent.getSerializableExtra("recipe") as Recipe
                if (recipe.name == "") {
                    recipe = createEmptyRecipeWithAttributes()
                }
                EditReceptActivityScreen(recipe, recipeViewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditReceptActivityScreen(recipe: Recipe, viewModel: RecipeViewModel) {
    val context = LocalContext.current
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Column {
                    MenuItem(
                        context.getString(R.string.recipes),
                        Icons.Default.ThumbUp,
                        drawerState,
                        coroutineScope,
                        context,
                        MainActivity::class.java
                    )
                    MenuItem(
                        context.getString(R.string.shopping_list),
                        Icons.Default.ShoppingCart,
                        drawerState,
                        coroutineScope,
                        context,
                        ShoppingActivity::class.java
                    )
                    MenuItem(
                        context.getString(R.string.calendar),
                        Icons.Default.DateRange,
                        drawerState,
                        coroutineScope,
                        context,
                        CalendarActivity::class.java
                    )
                }
            }
        },
        content = {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text(context.getString(R.string.recipe)) },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary),
                        navigationIcon = {
                            IconButton(onClick = { coroutineScope.launch { drawerState.open() } }) {
                                Icon(
                                    imageVector = Icons.Outlined.Menu,
                                    contentDescription = "Menu Icon"
                                )
                            }
                        },
                        actions = {
                            IconButton(onClick = {
                                openActivity(context, MainActivity::class.java)
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close icon"
                                )
                            }
                        }
                    )
                }
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(it)
                        .verticalScroll(rememberScrollState())
                ) {
                    recipeEdit(recipe, context, viewModel)
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun recipeEdit(recipe: Recipe, context: Context, viewModel: RecipeViewModel) {

    val title = remember { mutableStateOf(recipe.name) }
    val description = remember { mutableStateOf(recipe.description) }
    val ingredients = remember { mutableStateOf(recipe.ingredientsWithType) }
    val procedure = remember { mutableStateOf(recipe.procedures) }

    LaunchedEffect(Unit) {
        viewModel.loadAllIngredientType()
    }
    val ingredientType = viewModel.types.collectAsState(initial = emptyList()).value

    val expandedIngredient =
        remember { mutableStateListOf(*Array(ingredients.value.size) { false }) }

    val visibleDialog = remember { mutableStateOf(false) }
    if (visibleDialog.value) {
        AddTypeDialog(shouldShowDialog = visibleDialog, viewModel)
    }

    if (ingredients.value.isNotEmpty() && ingredients.value.last().ingredient.name.isNotEmpty()) {
        ingredients.value = ingredients.value.toMutableList().apply {
            add(
                IngredientWithType(
                    Ingredient(
                        name = "",
                        quantity = 1,
                        ingredientTypeId = 0,
                        recipeId = recipe.id
                    ),
                    if (ingredientType.isNotEmpty())
                        IngredientType(
                            id = ingredientType.first().id,
                            type = ingredientType.first().type
                        )
                    else
                        IngredientType(id = 0, type = "")
                )
            )
        }
    }
    if (procedure.value.isNotEmpty() && procedure.value.last().procedure.isNotEmpty()) {
        procedure.value = procedure.value.toMutableList().apply {
            add(Procedure(procedure = "", recipeId = recipe.id))
        }
    }
    if (procedure.value.isEmpty()) {
        procedure.value = procedure.value.toMutableList().apply {
            add(Procedure(procedure = "", recipeId = recipe.id))
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        TextField(
            value = title.value,
            onValueChange = { title.value = it },
            label = { Text(context.getString(R.string.name)) },
            modifier = Modifier.fillMaxWidth(),
            textStyle = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = description.value,
            onValueChange = { description.value = it },
            label = { Text(context.getString(R.string.description)) },
            modifier = Modifier.fillMaxWidth(),
            textStyle = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(context.getString(R.string.ingredients), style = MaterialTheme.typography.bodyLarge)
        ingredients.value.forEachIndexed { index, ingredient ->
            var selectedText by remember { mutableStateOf(ingredient.ingredientType.type) }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (expandedIngredient.size <= index) {
                    expandedIngredient.addAll(List(index - expandedIngredient.size + 1) { false })
                }

                TextField(
                    value = ingredient.ingredient.name,
                    label = { Text(context.getString(R.string.ingredient)) },
                    onValueChange = { newName ->
                        val newList = ingredients.value.toMutableList()

                        if (newName.isEmpty() && index == ingredients.value.size - 2) {
                            newList.removeAt(index + 1)
                            expandedIngredient.removeAt(index + 1)
                        }

                        if (newName.isNotEmpty() && index == ingredients.value.size - 1) {
                            newList.add(
                                IngredientWithType(
                                    Ingredient(
                                        name = "",
                                        quantity = 0,
                                        ingredientTypeId = 0,
                                        recipeId = recipe.id
                                    ),
                                    IngredientType(id = 0, type = "")

                                )
                            )
                            expandedIngredient.add(false)
                        }

                        newList[index] =
                            ingredient.copy(ingredient = ingredient.ingredient.copy(name = newName))
                        ingredients.value = newList
                    },
                    modifier = Modifier.weight(1f),
                    textStyle = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.width(8.dp))
                TextField(
                    value = ingredient.ingredient.quantity.toString(),
                    onValueChange = { newQuantity ->
                        if (newQuantity.isNotEmpty()) {
                            val newQuantityInt = newQuantity.toIntOrNull() ?: 0
                            val newList = ingredients.value.toMutableList()
                            newList[index] =
                                ingredient.copy(ingredient = ingredient.ingredient.copy(quantity = newQuantityInt))
                            ingredients.value = newList
                        }
                    },
                    modifier = Modifier.width(60.dp),
                    maxLines = 1,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    textStyle = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.width(8.dp))

                ExposedDropdownMenuBox(
                    expanded = expandedIngredient[index],
                    onExpandedChange = { expandedIngredient[index] = !expandedIngredient[index] },
                    modifier = Modifier.width(80.dp)
                ) {
                    TextField(
                        value = selectedText,
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier.menuAnchor()

                    )

                    ExposedDropdownMenu(
                        expanded = expandedIngredient[index],
                        onDismissRequest = { expandedIngredient[index] = false }
                    ) {
                        if (ingredientType.isNotEmpty()) {
                            val openAlertDialog = remember { mutableStateOf(false) }
                            ingredientType.forEach { item ->
                                DropdownMenuItem(
                                    text = {
                                        Text(text = item.type,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .combinedClickable(
                                                    onClick = {
                                                        selectedText = item.type
                                                        expandedIngredient[index] = false
                                                        val newList =
                                                            ingredients.value.toMutableList()
                                                        newList[index] = ingredient.copy(
                                                            ingredient = ingredient.ingredient.copy(
                                                                ingredientTypeId = item.id
                                                            ),
                                                            ingredientType = IngredientType(
                                                                id = item.id,
                                                                type = item.type
                                                            )
                                                        )
                                                        ingredients.value = newList
                                                    },
                                                    onLongClick = {
                                                        openAlertDialog.value = true
                                                    }
                                                ))
                                    },
                                    onClick = {
                                        selectedText = item.type
                                        expandedIngredient[index] = false
                                        val newList = ingredients.value.toMutableList()
                                        newList[index] = ingredient.copy(
                                            ingredient = ingredient.ingredient.copy(ingredientTypeId = item.id),
                                            ingredientType = IngredientType(
                                                id = item.id,
                                                type = item.type
                                            )
                                        )
                                        ingredients.value = newList
                                    }
                                )
                                if (openAlertDialog.value) {
                                    ConfirmDialog(
                                        onDismissRequest = { openAlertDialog.value = false },
                                        onConfirmation = {
                                            viewModel.deleteIngredientType(item.id)
                                            openAlertDialog.value = false
                                        },
                                        title = context.getString(R.string.confirmDeleteIngredientTypeTitle),
                                        text = context.getString(R.string.confirmDeleteIngredientTypeText),
                                        context = context
                                    )
                                }
                            }
                        }
                        DropdownMenuItem(
                            text = { Text(text = "+") },
                            onClick = {
                                visibleDialog.value = true
                            }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text("Postup", style = MaterialTheme.typography.bodyLarge)
        procedure.value.forEachIndexed { index, step ->
            TextField(
                value = step.procedure,
                onValueChange = { newStep ->
                    val newList = procedure.value.toMutableList()
                    if (newStep.isEmpty() && index == procedure.value.size - 2) {
                        newList.removeAt(index + 1)
                    }
                    if (newStep.isNotEmpty() && index == procedure.value.size - 1) {
                        newList.add(Procedure(procedure = "", recipeId = recipe.id))
                    }
                    newList[index] = step.copy(procedure = newStep)
                    procedure.value = newList
                },
                label = { Text("${index + 1}.") },
                modifier = Modifier.fillMaxWidth(),
                textStyle = MaterialTheme.typography.bodyMedium,
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(modifier = Modifier.align(Alignment.End),
            onClick = {
                val newTitle = title.value.trim()
                val newDescription = description.value.trim()
                var newIngredients = ingredients.value
                var newProcedure = procedure.value

                newIngredients =
                    newIngredients.filter { it.ingredient.name.isNotBlank() }.toMutableList()
                newProcedure = newProcedure.filter { it.procedure.isNotBlank() }.toMutableList()

                if (newTitle.isBlank()) {
                    Toast.makeText(
                        context,
                        context.getString(R.string.missingName),
                        Toast.LENGTH_SHORT
                    ).show()
                    return@Button
                }
                if (newIngredients.size == 0) {
                    Toast.makeText(
                        context,
                        context.getString(R.string.missingIngredients),
                        Toast.LENGTH_SHORT
                    ).show()
                    return@Button
                }
                for (ingredient in newIngredients) {
                    if (ingredient.ingredient.quantity < 1 || ingredient.ingredientType.type == "") {
                        Toast.makeText(
                            context,
                            context.getString(R.string.missingIngredient),
                            Toast.LENGTH_SHORT
                        ).show()
                        return@Button
                    }
                }
                val newRecipe = Recipe(
                    id = recipe.id,
                    name = newTitle,
                    description = newDescription,
                    photo = recipe.photo,
                    ingredientsWithType = newIngredients,
                    procedures = newProcedure
                )
                viewModel.upsertRecipeWithAttributes(newRecipe)
                val intent = Intent(context, MainActivity::class.java)
                context.startActivity(intent)

            }
        ) {
            Text(
                if (recipe.id == 0) context.getString(R.string.add) else context.getString(R.string.edit),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun AddTypeDialog(shouldShowDialog: MutableState<Boolean>, viewModel: RecipeViewModel) {
    val context = LocalContext.current
    var newType by remember { mutableStateOf("") }

    if (shouldShowDialog.value) {
        AlertDialog(
            onDismissRequest = {
                shouldShowDialog.value = false
            },
            title = { Text(text = context.getString(R.string.addType)) },
            text = { TextField(value = newType, onValueChange = { newType = it }) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.upsertIngredientType(IngredientType(id = 0, type = newType))
                        shouldShowDialog.value = false
                    }
                ) {
                    Text(text = context.getString(R.string.add))
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        shouldShowDialog.value = false
                    }
                ) {
                    Text(text = context.getString(R.string.close))
                }
            }
        )
    }
}
