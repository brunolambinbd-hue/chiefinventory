package com.example.chiefinventory.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.chiefinventory.data.AppDatabase
import com.example.chiefinventory.model.CollectionItem
import com.example.chiefinventory.model.Ingredient
import com.example.chiefinventory.model.Location
import com.example.chiefinventory.model.LocationType
import com.example.chiefinventory.model.Recipe
import com.example.chiefinventory.model.RecipeIngredient
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Suite de tests unitaires Robolectric de Type B assurant l'exécution réelle des DAOs Room
 * ([CollectionDao], [IngredientDao], [LocationDao], [RecipeDao]) sur une base de données SQLite en mémoire.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class RoomDaoExecutionRobolectricTypeBTest {

    private lateinit var db: AppDatabase
    private lateinit var collectionDao: CollectionDao
    private lateinit var ingredientDao: IngredientDao
    private lateinit var locationDao: LocationDao
    private lateinit var recipeDao: RecipeDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()

        collectionDao = db.collectionDao()
        ingredientDao = db.ingredientDao()
        locationDao = db.locationDao()
        recipeDao = db.recipeDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun `IngredientDao CRUD operations should insert update and query in memory database`() = runTest {
        val ingredient = Ingredient(name = "Beurre doux", quantity = 250.0, unit = "g", minThreshold = 100.0, packSize = 1.0)
        val id = ingredientDao.insert(ingredient)

        assertThat(id).isAtLeast(1L)

        val retrieved = ingredientDao.getById(id)
        assertThat(retrieved).isNotNull()
        assertThat(retrieved!!.name).isEqualTo("Beurre doux")

        val updated = retrieved.copy(quantity = 500.0)
        ingredientDao.update(updated)

        val reQueried = ingredientDao.getById(id)
        assertThat(reQueried!!.quantity).isEqualTo(500.0)

        val allSync = ingredientDao.getAllSync()
        assertThat(allSync).hasSize(1)

        ingredientDao.delete(reQueried)
        assertThat(ingredientDao.getAllSync()).isEmpty()
    }

    @Test
    fun `LocationDao CRUD and hierarchy operations should function in memory database`() = runTest {
        val rootLoc = Location(name = "Cuisine", parentId = null, type = LocationType.INGREDIENT)
        locationDao.insert(rootLoc)

        val all = locationDao.getAllSync()
        assertThat(all).hasSize(1)
        assertThat(all.first().name).isEqualTo("Cuisine")

        val childLoc = Location(name = "Frigo", parentId = all.first().id, type = LocationType.INGREDIENT)
        locationDao.insert(childLoc)

        val updatedAll = locationDao.getAllSync()
        assertThat(updatedAll).hasSize(2)

        locationDao.updateLocationParent(updatedAll[1].id, null)
        locationDao.delete(updatedAll[0])
    }

    @Test
    fun `RecipeDao and RecipeIngredients CRUD operations should function in memory database`() = runTest {
        val recipe = Recipe(title = "Tarte Tatin", preparationTimeMinutes = 20, cookingTimeMinutes = 30, servings = 6)
        val recipeId = recipeDao.insertRecipe(recipe)

        assertThat(recipeId).isAtLeast(1L)

        val ingredients = listOf(
            RecipeIngredient(recipeId = recipeId, ingredientName = "Pommes", quantityRequired = 6.0, unit = "pièces"),
            RecipeIngredient(recipeId = recipeId, ingredientName = "Beurre", quantityRequired = 100.0, unit = "g")
        )
        recipeDao.insertRecipeIngredients(ingredients)

        val retrievedRecipe = recipeDao.getRecipeById(recipeId)
        assertThat(retrievedRecipe).isNotNull()
        assertThat(retrievedRecipe!!.title).isEqualTo("Tarte Tatin")

        val allWithIngr = recipeDao.getAllRecipesWithIngredients()
        assertThat(allWithIngr).hasSize(1)
        assertThat(allWithIngr.first().ingredients).hasSize(2)

        recipeDao.deleteIngredientsByRecipeId(recipeId)
        recipeDao.deleteRecipe(retrievedRecipe)

        assertThat(recipeDao.getAllRecipesWithIngredients()).isEmpty()
    }

    @Test
    fun `CollectionDao CRUD operations should function in memory database`() = runTest {
        val item = CollectionItem(id = 555L, titre = "Album Spirou", isPossessed = true)
        collectionDao.insert(item)

        val retrieved = collectionDao.getItemById(555L)
        assertThat(retrieved).isNotNull()
        assertThat(retrieved!!.titre).isEqualTo("Album Spirou")

        val searchList = collectionDao.search("%Spirou%")
        assertThat(searchList).hasSize(1)

        collectionDao.delete(retrieved)
        assertThat(collectionDao.getAllSuspend()).isEmpty()
    }
}
