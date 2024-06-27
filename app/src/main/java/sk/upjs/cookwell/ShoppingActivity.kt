package sk.upjs.cookwell

import android.icu.text.SimpleDateFormat
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material3.DismissDirection
import androidx.compose.material3.DismissValue
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismiss
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDismissState
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import sk.upjs.cookwell.entities.ShoppingList
import sk.upjs.cookwell.ui.theme.CookWellTheme
import sk.upjs.cookwell.ui.theme.DeleteRed

class ShoppingActivity : ComponentActivity() {
    private val shoppingListViewModel: ShoppingListViewModel by viewModels {
        ShoppingListViewModel.ShoppingListViewModelFactory((application as CookWellApplication).shoppingListRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CookWellTheme {
                ShoppingScene(shoppingListViewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingScene(viewModel: ShoppingListViewModel) {
    val context = LocalContext.current
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    var newItemName by remember { mutableStateOf("") }
    var openAlertDialog = remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        viewModel.loadAllItems()
    }

    val shoppingListItems = viewModel.items.collectAsState(initial = emptyList()).value

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
                        title = { Text(context.getString(R.string.shopping_list)) },
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
                            IconButton(onClick = { openAlertDialog.value = true }) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete all icon"
                                )
                            }
                        }
                    )
                },
                content = { paddingValues ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .padding(16.dp)
                                .background(MaterialTheme.colorScheme.secondary)
                        ) {
                            LazyColumn(
                                state = listState,
                                contentPadding = PaddingValues(10.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                itemsIndexed(
                                    items = shoppingListItems,
                                    key = { _, item -> item.id }
                                ) { _, item ->
                                    val dismissState = rememberDismissState(
                                        confirmValueChange = {
                                            if (it == DismissValue.DismissedToStart) {
                                                viewModel.deleteShoppingListItem(item.id)
                                            }
                                            true
                                        }
                                    )
                                    var date by remember { mutableStateOf("") }
                                    var type by remember { mutableStateOf("") }

                                    LaunchedEffect(Unit) {
                                        if (item.eventId != null && item.ingredientId != null) {
                                            val dateEvent = withContext(Dispatchers.IO) {
                                                viewModel.getEventDate(item.eventId)
                                            }
                                            date = SimpleDateFormat("dd.M.yyyy").format(dateEvent)
                                            val ingredientType = withContext(Dispatchers.IO) {
                                                viewModel.getTypeByIngredientId(item.ingredientId)
                                            }
                                            type = ingredientType
                                        }
                                    }
                                    SwipeToDismiss(
                                        state = dismissState,
                                        background = {
                                            val color = when (dismissState.dismissDirection) {
                                                DismissDirection.StartToEnd -> Color.Transparent
                                                DismissDirection.EndToStart -> DeleteRed
                                                null -> MaterialTheme.colorScheme.secondary
                                            }
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .background(color = color)
                                                    .padding(16.dp)
                                            )
                                        },
                                        dismissContent = {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .background(color = MaterialTheme.colorScheme.secondary)
                                                    .padding(16.dp)
                                            ) {
                                                IconButton(
                                                    onClick = {
                                                        if (item.quantity == 1) {
                                                            viewModel.deleteShoppingListItem(
                                                                item.id
                                                            )
                                                        } else {
                                                            var newQuantity: Int
                                                            newQuantity =
                                                                item.quantity - 1
                                                            viewModel.upsertShoppingListItem(
                                                                ShoppingList(
                                                                    id = item.id,
                                                                    name = item.name,
                                                                    quantity = newQuantity,
                                                                    eventId = item.eventId,
                                                                    ingredientId = item.ingredientId
                                                                )
                                                            )
                                                        }
                                                    },
                                                    modifier = Modifier.size(48.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.ArrowDropDown,
                                                        contentDescription = "Add item"
                                                    )
                                                }

                                                Column(
                                                    modifier = Modifier.weight(1f),
                                                    horizontalAlignment = Alignment.CenterHorizontally
                                                ) {
                                                    Text(
                                                        text = item.name,
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        color = MaterialTheme.colorScheme.onPrimary
                                                    )
                                                    Text(
                                                        text = if (type.isNotEmpty()) context.getString(
                                                            R.string.quantity
                                                        ) + ": ${item.quantity} (${type})" else context.getString(
                                                            R.string.quantity
                                                        ) + ": ${item.quantity}",
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        color = MaterialTheme.colorScheme.onPrimary
                                                    )
                                                    if (date.isNotEmpty()) {
                                                        Text(
                                                            text = context.getString(R.string.until) + ": ${date}",
                                                            style = MaterialTheme.typography.bodyMedium,
                                                            color = MaterialTheme.colorScheme.onPrimary
                                                        )
                                                    }
                                                }

                                                IconButton(
                                                    onClick = {
                                                        var newQuantity: Int
                                                        newQuantity =
                                                            item.quantity + 1
                                                        viewModel.upsertShoppingListItem(
                                                            ShoppingList(
                                                                id = item.id,
                                                                name = item.name,
                                                                quantity = newQuantity,
                                                                eventId = item.eventId,
                                                                ingredientId = item.ingredientId
                                                            )
                                                        )
                                                    },
                                                    modifier = Modifier.size(48.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Add,
                                                        contentDescription = "Add",
                                                        tint = MaterialTheme.colorScheme.onPrimary
                                                    )
                                                }
                                            }
                                        },
                                        directions = setOf(DismissDirection.EndToStart)
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(18.dp))
                        OutlinedTextField(
                            value = newItemName,
                            onValueChange = { newItemName = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .background(MaterialTheme.colorScheme.secondary),
                            label = { Text(context.getString(R.string.addItem)) },
                            trailingIcon = {
                                IconButton(onClick = {
                                    if (newItemName.isNotBlank()) {
                                        val newItem = ShoppingList(0, newItemName.trim(), 1)
                                        viewModel.upsertShoppingListItem(newItem)
                                        newItemName = ""
                                    }
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.Send,
                                        contentDescription = "Add item"
                                    )
                                }
                            },
                            keyboardOptions = KeyboardOptions.Default.copy(
                                imeAction = ImeAction.Send
                            ),
                            keyboardActions = KeyboardActions(
                                onSend = {
                                    if (newItemName.isNotBlank()) {
                                        val newItem = ShoppingList(0, newItemName.trim(), 1)
                                        viewModel.upsertShoppingListItem(newItem)
                                        newItemName = ""
                                    }
                                }
                            )
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            )
        }
    )
    if (openAlertDialog.value) {
        ConfirmDialog(
            title = context.getString(R.string.removeListTitle),
            text = context.getString(R.string.removeListText),
            onDismissRequest = { openAlertDialog.value = false },
            onConfirmation = {
                viewModel.deleteAllShoppingItems()
                openAlertDialog.value = false
            },
            context = context
        )
    }
}

