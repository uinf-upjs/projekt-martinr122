package sk.upjs.cookwell

import kotlinx.coroutines.flow.Flow
import sk.upjs.cookwell.entities.ShoppingList
import java.util.Date

class ShoppingListRepository(private val cookWellDao: CookWellDao) {
    fun getAllItems(): Flow<List<ShoppingList>> {
        return cookWellDao.getAllShoppingItems()
    }

    fun upsertShoppingListItem(item: ShoppingList) {
        cookWellDao.upsertShoppingItem(item)
    }

    fun deleteShoppingListItem(itemId: Int) {
        cookWellDao.deleteShoppingListItembyId(itemId)
    }

    fun getEventDate(itemId: Int): Date {
        return cookWellDao.getEventDate(itemId)
    }

    fun getTypeByIngredientId(id: Int): String {
        return cookWellDao.getTypeByIngredientId(id)
    }

    fun deleteAllShoppingItems() {
        cookWellDao.deleteAllShoppingItems()
    }

}