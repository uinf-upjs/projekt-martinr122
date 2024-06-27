package sk.upjs.cookwell

import android.content.Context
import android.icu.text.SimpleDateFormat
import android.os.Build
import android.os.Bundle
import android.util.AttributeSet
import android.widget.CalendarView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import sk.upjs.cookwell.entities.CalendarEvent
import sk.upjs.cookwell.entities.Recipe
import sk.upjs.cookwell.entities.RecipeCalendarEvent
import sk.upjs.cookwell.entities.ShoppingList
import sk.upjs.cookwell.ui.theme.CookWellTheme
import java.util.Calendar
import java.util.Date

class CalendarActivity : ComponentActivity() {

    private val calendarViewModel: CalendarViewModel by viewModels {
        CalendarViewModel.CalendarViewModelFactory(
            (application as CookWellApplication).calendarRepository,
            (application as CookWellApplication).recipeRepository,
            (application as CookWellApplication).shoppingListRepository
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CookWellTheme {
                CalendarScreen(calendarViewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun CalendarScreen(calendarViewModel: CalendarViewModel) {
    val context = LocalContext.current
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    val visibleAddDialog = remember { mutableStateOf(false) }
    val visibleAllDialog = remember { mutableStateOf(false) }
    val time = Calendar.getInstance().time
    val dateFormat = SimpleDateFormat("dd.M.yyyy")
    val current = dateFormat.format(time)
    var date by remember { mutableStateOf(current) }
    val isDarkTheme = isSystemInDarkTheme()
    var allEvents: List<CalendarEvent> by remember { mutableStateOf(emptyList()) }
    LaunchedEffect(Unit) {
        calendarViewModel.getAllCalendarEventsByDay(dateFormat.parse(date))
    }
    allEvents = calendarViewModel.events.collectAsState(initial = emptyList()).value

    var events: List<CalendarEvent> by remember { mutableStateOf(emptyList()) }
    LaunchedEffect(Unit) {
        calendarViewModel.getAllCalendarEventsByDay(dateFormat.parse(date))
    }
    events = calendarViewModel.eventsByDay.collectAsState(initial = emptyList()).value
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
                        title = { Text(context.getString(R.string.calendar)) },
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
                            IconButton(onClick = { visibleAllDialog.value = true }) {
                                Icon(
                                    imageVector = Icons.Default.List,
                                    contentDescription = "List icon"
                                )
                            }
                        }
                    )
                }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(it)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(3.dp)
                        ) {
                            Column(
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .fillMaxSize()
                            ) {
                                AndroidView(
                                    factory = { context ->
                                        CustomCalendarView(context).apply {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                                if (isDarkTheme) {
                                                    setDateTextAppearance(R.style.CalendarWhite)
                                                    weekDayTextAppearance = R.style.CalendarGrey

                                                } else {
                                                    setDateTextAppearance(R.style.CalendarBlack)
                                                    weekDayTextAppearance = R.style.CalendarDarkGrey
                                                }
                                            }
                                        }
                                    },
                                    update = { calendarView ->
                                        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
                                            date = "$dayOfMonth.${month + 1}.$year"
                                            calendarViewModel.getAllCalendarEventsByDay(
                                                dateFormat.parse(
                                                    date
                                                )

                                            )
                                        }
                                    }
                                )
                            }
                        }
                    }
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(horizontal = 8.dp)
                    ) {
                        items(events) { event ->
                            val openEventDialog = remember { mutableStateOf(false) }
                            val openEventDeleteDialog = remember { mutableStateOf(false) }
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
                                            onClick = { openEventDialog.value = true },
                                            onLongClick = {
                                                openEventDeleteDialog.value = true
                                            }
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = event.name,
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                    if (openEventDeleteDialog.value) {
                                        ConfirmDialog(
                                            onDismissRequest = {
                                                openEventDeleteDialog.value = false
                                            },
                                            onConfirmation = {
                                                calendarViewModel.deleteEvent(event)
                                                openEventDeleteDialog.value = false
                                            },
                                            title = context.getString(R.string.confirmDeleteEventTitle),
                                            text = context.getString(R.string.confirmDeleteEventText),
                                            context = context
                                        )
                                    }
                                    if (openEventDialog.value) {
                                        showEventDialog(
                                            shouldShowDialog = openEventDialog,
                                            calendarView = calendarViewModel,
                                            context = context,
                                            event = event
                                        )
                                    }
                                }
                            }

                        }
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp)
                    ) {
                        Button(
                            modifier = Modifier.align(Alignment.Center),
                            onClick = { visibleAddDialog.value = true },
                        ) {
                            Text(
                                text = context.getString(R.string.addEventButton),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }
    )
    if (visibleAddDialog.value) {
        AddEventDialog(shouldShowDialog = visibleAddDialog, date, calendarViewModel, coroutineScope)
    }
    if (visibleAllDialog.value) {
        AllEventDialog(shouldShowDialog = visibleAllDialog, calendarViewModel)
    }
}

class CustomCalendarView(context: Context, attrs: AttributeSet? = null) :
    CalendarView(context, attrs) {
    init {
        firstDayOfWeek = Calendar.MONDAY
    }
}

@Composable
fun AddEventDialog(
    shouldShowDialog: MutableState<Boolean>,
    date: String,
    calendarView: CalendarViewModel,
    scope: CoroutineScope
) {
    val context = LocalContext.current
    var newEvent by remember { mutableStateOf("") }
    var recipes: List<Recipe> by remember { mutableStateOf(emptyList()) }
    var recipesToSave: MutableList<Recipe> = remember { mutableStateListOf() }
    val dateFormat = SimpleDateFormat("dd.M.yyyy")
    val formatedDate: Date = dateFormat.parse(date)


    LaunchedEffect(Unit) {
        calendarView.loadAllRecipes()
    }
     recipes = calendarView.recipes.collectAsState(initial = emptyList()).value

    if (shouldShowDialog.value) {
        AlertDialog(
            onDismissRequest = {
                shouldShowDialog.value = false
            },
            title = { Text(text = context.getString(R.string.addEvent)) },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    TextField(
                        value = newEvent,
                        onValueChange = { newEvent = it },
                        label = { Text(context.getString(R.string.nameEvent)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Box(
                        modifier = Modifier
                            .background(
                                MaterialTheme.colorScheme.secondary,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.onPrimary,
                                shape = RoundedCornerShape(10.dp)
                            )
                            .padding(16.dp)
                            .fillMaxWidth()
                            .heightIn(max = 250.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (recipes.isEmpty()) {
                            Text(text = context.getString(R.string.emptyRecipes))
                        }
                        LazyColumn(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            items(recipes) { recipe ->
                                var isClicked by remember { mutableStateOf(false) }
                                Button(
                                    onClick = {
                                        if (!isClicked) {
                                            recipesToSave.add(recipe)
                                            isClicked = true
                                        } else {
                                            recipesToSave.remove(recipe)
                                            isClicked = false
                                        }


                                    },
                                    modifier = Modifier.fillMaxWidth(0.80f),
                                    colors = ButtonDefaults.buttonColors(
                                        if (isClicked) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.primary
                                    )
                                ) {
                                    Text(text = recipe.name)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newEvent.isBlank()) {
                            Toast.makeText(
                                context,
                                context.getString(R.string.missingName),
                                Toast.LENGTH_SHORT
                            ).show()
                            return@Button
                        }
                        if (recipesToSave.isEmpty()) {
                            Toast.makeText(
                                context,
                                context.getString(R.string.missingRecipes),
                                Toast.LENGTH_SHORT
                            ).show()
                            return@Button
                        }
                        val event = CalendarEvent(id = 0, name = newEvent, formatedDate)
                        scope.launch {
                            val newCalendar = withContext(Dispatchers.IO) {
                                calendarView.upsertCalendarEvent(event)
                            }
                            for (recipe in recipesToSave) {
                                calendarView.upsertRecipeCalendarEvent(
                                    RecipeCalendarEvent(
                                        recipe.id,
                                        newCalendar.toInt()
                                    )
                                )
                                val ingredients = withContext(Dispatchers.IO) {
                                    calendarView.getIngredientsById(recipe.id)
                                }
                                for (ingredient in ingredients) {
                                    calendarView.upsertShoppingListItem(
                                        ShoppingList(
                                            id = 0,
                                            name = ingredient.name,
                                            quantity = ingredient.quantity,
                                            ingredientId = ingredient.id,
                                            eventId = newCalendar.toInt()
                                        )
                                    )
                                }
                            }

                        }
                        calendarView.getAllCalendarEventsByDay(dateFormat.parse(date))
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

@Composable
fun AllEventDialog(
    shouldShowDialog: MutableState<Boolean>,
    calendarView: CalendarViewModel
) {
    val context = LocalContext.current
    var events: List<CalendarEvent> by remember { mutableStateOf(emptyList()) }

    LaunchedEffect(Unit) {
        calendarView.loadAllEvents()
    }

    events = calendarView.events.collectAsState(initial = emptyList()).value

    val calendar = Calendar.getInstance()
    calendar.add(Calendar.DAY_OF_MONTH, -1)

    val filteredEvents = events.filter { event ->
        event.date.after(calendar.time)
    }
    if (shouldShowDialog.value) {
        AlertDialog(
            onDismissRequest = {
                shouldShowDialog.value = false
            },
            title = { Text(text = context.getString(R.string.nextEvents)) },
            text = {
                Box(
                    modifier = Modifier
                        .background(
                            MaterialTheme.colorScheme.secondary,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.onPrimary,
                            shape = RoundedCornerShape(10.dp)
                        )
                        .padding(16.dp)
                        .fillMaxWidth()
                        .heightIn(max = 250.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (filteredEvents.isEmpty()) {
                        Text(text = context.getString(R.string.emptyEvents))
                    } else {
                        LazyColumn(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            items(filteredEvents) { event ->
                                Text(
                                    text = "${event.name} - ${
                                        SimpleDateFormat("dd.M.yyyy").format(
                                            event.date
                                        )
                                    }",
                                    modifier = Modifier.align(Alignment.Center)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
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

@Composable
fun showEventDialog(
    shouldShowDialog: MutableState<Boolean>,
    calendarView: CalendarViewModel,
    context: Context,
    event: CalendarEvent
) {
    var recepies by remember { mutableStateOf<List<Recipe>>(emptyList()) }
    LaunchedEffect(Unit) {
        recepies = withContext(Dispatchers.IO) {
            calendarView.loadRecipiesByEvent(event)

        }
    }

    AlertDialog(
        onDismissRequest = { shouldShowDialog.value = false },
        title = { Text(text = event.name, style = MaterialTheme.typography.bodyLarge) },
        text = {
            Column {
                Text(
                    text = "${context.getString(R.string.date)}: ${
                        SimpleDateFormat("dd.M.yyyy").format(
                            event.date
                        )
                    }"
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${context.getString(R.string.recipes)}: ",
                    style = MaterialTheme.typography.bodyLarge
                )
                recepies.forEach { recipe ->
                    Text(text = recipe.name, modifier = Modifier.padding(8.dp))
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { shouldShowDialog.value = false }
            ) {
                Text(text = context.getString(R.string.confirm))
            }
        }
    )
}