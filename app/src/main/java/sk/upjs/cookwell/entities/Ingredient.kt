package sk.upjs.cookwell.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.io.Serializable


@Entity(tableName = "ingredient",
    foreignKeys = [ForeignKey(
        entity = Recipe::class,
        parentColumns = ["id"],
        childColumns = ["recipe_id"],
        onDelete = ForeignKey.CASCADE
    ),ForeignKey(
        entity = IngredientType::class,
        parentColumns = ["id"],
        childColumns = ["ingredient_type_id"],
        onDelete = ForeignKey.NO_ACTION
    )]
)
data class Ingredient(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "quantity")
    val quantity: Int,

    @ColumnInfo(name = "ingredient_type_id")
    val ingredientTypeId: Int,

    @ColumnInfo(name = "recipe_id")
    var recipeId: Int
): Serializable
