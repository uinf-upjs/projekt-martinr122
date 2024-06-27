package sk.upjs.cookwell

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.runtime.*
import androidx.compose.material3.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.outlined.AddCircle
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import sk.upjs.cookwell.entities.Ingredient
import sk.upjs.cookwell.entities.IngredientType
import sk.upjs.cookwell.entities.IngredientWithType
import sk.upjs.cookwell.entities.Procedure
import sk.upjs.cookwell.entities.Recipe
import sk.upjs.cookwell.ui.theme.CookWellTheme
import java.util.Calendar


class MainActivity : ComponentActivity() {

    private val recipeViewModel: RecipeViewModel by viewModels {
        RecipeViewModel.ReceptViewModelFactory((application as CookWellApplication).recipeRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CookWellTheme {
                LaunchedEffect(Unit) {
                    var isTommorow = false
                    val events = withContext(Dispatchers.IO) {
                        val calendar = Calendar.getInstance()
                        calendar.add(Calendar.DAY_OF_MONTH, 1)
                        calendar.set(Calendar.HOUR_OF_DAY, 0)
                        calendar.set(Calendar.MINUTE, 0)
                        calendar.set(Calendar.SECOND, 0)
                        calendar.set(Calendar.MILLISECOND, 0)
                        val start = calendar.time

                        recipeViewModel.countEventsTommorow(start)
                    }

                    if (events > 0) {
                        isTommorow = true
                    }
                    if (isTommorow) {
                        val notificationService = NotificationService(applicationContext)
                        notificationService.showNotification()
                    }
                }
                MainScreen(recipeViewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun MainScreen(recipeViewModel: RecipeViewModel) {
    val context = LocalContext.current
    var recipes: List<Recipe> by remember { mutableStateOf(emptyList()) }
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        recipeViewModel.loadAllRecipes()
    }
    recipes = recipeViewModel.recipes.collectAsState(initial = emptyList()).value

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
                        title = { Text(context.getString(R.string.app_name)) },
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
                                context.startActivity(
                                    Intent(
                                        context,
                                        ShoppingActivity::class.java
                                    )
                                )
                            }) {
                                Icon(
                                    imageVector = Icons.Default.ShoppingCart,
                                    contentDescription = "Shopping cart icon"
                                )
                            }
                        }
                    )
                },
                floatingActionButton = {
                    FloatingActionButton(onClick = {
                        openEditRecipe(
                            context,
                            Recipe(
                                id = 0,
                                name = "",
                                description = "",
                                photo = byteArrayOf(),
                                ingredientsWithType = mutableListOf(),
                                procedures = mutableListOf()
                            ),

                            )

                    }) {
                        Icon(
                            imageVector = Icons.Outlined.AddCircle,
                            contentDescription = "Add Icon"
                        )
                    }
                },
                floatingActionButtonPosition = FabPosition.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(it)
                        .background(color = MaterialTheme.colorScheme.background)
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 30.dp)
                            .heightIn(max = 410.dp)
                    ) {
                        items(recipes) { recipe ->
                            val openAlertDialog = remember { mutableStateOf(false) }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .width(180.dp)
                                        .heightIn(50.dp, 300.dp)
                                        .background(
                                            color = MaterialTheme.colorScheme.primary,
                                            shape = RoundedCornerShape(16.dp)
                                        )
                                        .combinedClickable(
                                            onClick = { openViewRecipe(context, recipe) },
                                            onLongClick = {
                                                openAlertDialog.value = true
                                            }
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = recipe.name,
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                    if (openAlertDialog.value) {
                                        ConfirmDialog(
                                            onDismissRequest = { openAlertDialog.value = false },
                                            onConfirmation = {
                                                recipeViewModel.deleteRecipe(recipe)
                                                openAlertDialog.value = false
                                            },
                                            title = context.getString(R.string.confirmDeleteRecipeTitle),
                                            text = context.getString(R.string.confirmDeleteRecipeText),
                                            context = context
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    )
}

@Composable
fun MenuItem(
    text: String,
    icon: ImageVector,
    drawerState: DrawerState,
    coroutineScope: CoroutineScope,
    context: Context,
    activity: Class<*>
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clickable {
                coroutineScope.launch { drawerState.close() }
                coroutineScope.launch { openActivity(context, activity) }
            }
            .padding(15.dp)

    ) {
        Icon(imageVector = icon, contentDescription = "Icon", modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text, style = MaterialTheme.typography.titleMedium, modifier = Modifier.width(180.dp))
    }
}

fun openEditRecipe(context: Context, recipe: Recipe) {
    val intent = Intent(context, EditReceptActivity::class.java)
    intent.putExtra("recipe", recipe)
    context.startActivity(intent)
}

fun openViewRecipe(context: Context, recipe: Recipe) {
    val intent = Intent(context, ReceptActivity::class.java)
    intent.putExtra("recipe", recipe)
    context.startActivity(intent)
}

fun openActivity(context: Context, activity: Class<*>) {
    context.startActivity(Intent(context, activity))
}

@Composable
fun ConfirmDialog(
    title: String, text: String, onDismissRequest: () -> Unit,
    onConfirmation: () -> Unit, context: Context
) {
    AlertDialog(
        onDismissRequest = { onDismissRequest() },
        title = { Text(text = title) },
        text = { Text(text = text) },
        confirmButton = {
            Button(
                onClick = { onConfirmation() }
            ) {
                Text(text = context.getString(R.string.confirm))
            }
        },
        dismissButton = {
            Button(
                onClick = { onDismissRequest() }
            ) {
                Text(text = context.getString(R.string.close))
            }
        }
    )
}


fun createEmptyRecipeWithAttributes(): Recipe {
    var recipe = Recipe(
        0,
        "",
        "",
        byteArrayOf(),
        mutableListOf(IngredientWithType(Ingredient(0, "", 0, 0, 0), IngredientType(0, ""))),
        mutableListOf()
    )
    val proceduresList = listOf(Procedure(0, "", recipeId = recipe.id))
    recipe.procedures.addAll(proceduresList)
    return recipe
}