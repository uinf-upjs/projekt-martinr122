package sk.upjs.cookwell.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import androidx.room.Relation
import java.io.Serializable

@Entity(tableName = "recipe")
data class Recipe(
    @PrimaryKey(autoGenerate = true)
    var id: Int,

    @ColumnInfo(name = "name")
    var name: String,

    @ColumnInfo(name = "description")
    var description: String,

    @ColumnInfo(name = "photo", typeAffinity = ColumnInfo.BLOB)
    var photo: ByteArray,

) : Serializable{
    @Ignore
    var ingredientsWithType: MutableList<IngredientWithType> = mutableListOf()

    @Ignore
    var procedures: MutableList<Procedure> = mutableListOf()
    constructor(
        id: Int,
        name: String,
        description: String,
        photo: ByteArray,
        ingredientsWithType: MutableList<IngredientWithType>,
        procedures: MutableList<Procedure>
    ) : this(id, name, description, photo) {
        this.ingredientsWithType = ingredientsWithType
        this.procedures = procedures
    }
}