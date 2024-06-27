package sk.upjs.cookwell.entities

import androidx.room.DatabaseView
import androidx.room.Embedded
import androidx.room.Relation
import java.io.Serializable

data class IngredientWithType(
    @Embedded
    val ingredient: Ingredient,

    @Relation(
        parentColumn = "id",
        entityColumn = "id"
    )
    val ingredientType: IngredientType
):Serializable
