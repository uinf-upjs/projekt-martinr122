package sk.upjs.cookwell

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import androidx.compose.runtime.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import sk.upjs.cookwell.entities.Recipe
import sk.upjs.cookwell.ui.theme.CookWellTheme

@Suppress("DEPRECATION")
class ReceptActivity : ComponentActivity() {

    private val recipeViewModel: RecipeViewModel by viewModels {
        RecipeViewModel.ReceptViewModelFactory((application as CookWellApplication).recipeRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CookWellTheme {
                val recipe = intent.getSerializableExtra("recipe") as Recipe
                var fetchedRecipe: Recipe? by remember { mutableStateOf(null) }
                LaunchedEffect(Unit) {
                    fetchedRecipe = withContext(Dispatchers.IO) {
                        recipeViewModel.fetchDataForRecipe(recipe)
                    }
                }
                fetchedRecipe?.let {
                    ViewReceptActivityScreen(it)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewReceptActivityScreen(newRecipe: Recipe) {
    val context = LocalContext.current
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    var recipe by remember { mutableStateOf(newRecipe) }

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
                                val intent = Intent(context, EditReceptActivity::class.java)
                                intent.putExtra("recipe", recipe)
                                context.startActivity(intent)
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit icon"
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
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = context.getString(R.string.name),
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = recipe.name,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(4.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = context.getString(R.string.description),
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = recipe.description,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(4.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = context.getString(R.string.ingredients),
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        recipe.ingredientsWithType.forEach { ingredient ->
                            if (ingredient.ingredient.name != "") {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(4.dp)
                                ) {
                                    Text(
                                        text = ingredient.ingredient.name,
                                        modifier = Modifier.weight(1f),
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        text = ingredient.ingredient.quantity.toString(),
                                        modifier = Modifier.weight(1f),
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        text = ingredient.ingredientType.type,
                                        modifier = Modifier.weight(1f),
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = context.getString(R.string.procedure),
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        recipe.procedures.forEachIndexed { index, process ->
                            if (process.procedure != "") {
                                Text(
                                    text = "${index + 1}. ${process.procedure}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    )
}

