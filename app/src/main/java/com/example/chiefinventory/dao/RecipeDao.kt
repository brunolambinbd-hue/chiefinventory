package com.example.chiefinventory.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.chiefinventory.model.Recipe
import com.example.chiefinventory.model.RecipeIngredient

data class RecipeCategoryInfo(
    val categoryId: Long,
    val categoryName: String,
    val recipeCount: Int
)

@Dao
interface RecipeDao {
    @Query("SELECT * FROM recipes ORDER BY updatedAt DESC")
    fun getAllRecipes(): LiveData<List<Recipe>>

    @Query("SELECT * FROM recipes WHERE locationId = :categoryId ORDER BY updatedAt DESC")
    fun getRecipesByCategory(categoryId: Long): LiveData<List<Recipe>>

    @Query("SELECT * FROM recipes WHERE id = :id")
    suspend fun getRecipeById(id: Long): Recipe?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipe(recipe: Recipe): Long

    @Update
    suspend fun updateRecipe(recipe: Recipe)

    @Delete
    suspend fun deleteRecipe(recipe: Recipe)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipeIngredients(ingredients: List<RecipeIngredient>)

    @Query("SELECT * FROM recipe_ingredients WHERE recipeId = :recipeId")
    fun getIngredientsForRecipe(recipeId: Long): LiveData<List<RecipeIngredient>>

    @Query("DELETE FROM recipe_ingredients WHERE recipeId = :recipeId")
    suspend fun deleteIngredientsByRecipeId(recipeId: Long)

    @Query("""
        SELECT l.id as categoryId, l.name as categoryName, COUNT(r.id) as recipeCount 
        FROM locations l 
        LEFT JOIN recipes r ON l.id = r.locationId 
        WHERE l.type = 'RECIPE' 
        GROUP BY l.id
    """)
    fun getRecipeCategoriesWithCount(): LiveData<List<RecipeCategoryInfo>>
}
