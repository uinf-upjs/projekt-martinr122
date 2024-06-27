package sk.upjs.cookwell

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import sk.upjs.cookwell.entities.ShoppingList
import java.util.Date

class ShoppingListViewModel(private val repository: ShoppingListRepository) : ViewModel() {
    class ShoppingListViewModelFactory(private val repository: ShoppingListRepository) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ShoppingListViewModel::class.java)) {
                return ShoppingListViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }

    private val _items: MutableStateFlow<List<ShoppingList>> = MutableStateFlow(emptyList())
    val items: Flow<List<ShoppingList>> = _items

    fun loadAllItems() {
        viewModelScope.launch {
            repository.getAllItems().collect { itemsList ->
                _items.value = itemsList
            }
        }
    }

    fun upsertShoppingListItem(item: ShoppingList) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.upsertShoppingListItem(item)
        }
    }

    fun deleteShoppingListItem(itemId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteShoppingListItem(itemId)
        }
    }

    fun getEventDate(itemId: Int): Date {
        return repository.getEventDate(itemId)
    }

    fun getTypeByIngredientId(id: Int): String {
        return repository.getTypeByIngredientId(id)
    }

    fun deleteAllShoppingItems() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteAllShoppingItems()
        }
    }

}