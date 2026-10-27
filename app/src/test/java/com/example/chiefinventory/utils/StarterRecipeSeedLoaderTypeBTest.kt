package com.example.chiefinventory.utils

import android.content.Context
import android.content.res.AssetManager
import com.example.chiefinventory.dao.LocationDao
import com.example.chiefinventory.dao.RecipeDao
import com.example.chiefinventory.data.AppDatabase
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.io.ByteArrayInputStream
import java.io.IOException

/**
 * Suite de tests unitaires de Type B vérifiant le chargement et l'importation BDD
 * de 100% des méthodes de [StarterRecipeSeedLoader].
 */
class StarterRecipeSeedLoaderTypeBTest {

    @Test
    fun `loadSeedRecipesFromAssetFile should read and parse json recipes from assets`() {
        val mockContext = mock<Context>()
        val mockAssets = mock<AssetManager>()

        val jsonContent = """
            [
              {
                "title": "Chicons au gratin",
                "category": "Plat principal",
                "cuisineOrigin": "Cuisine Belge",
                "prepTimeMinutes": 15,
                "cookTimeMinutes": 35,
                "servings": 4,
                "ingredients": ["4 chicons", "4 tranches de jambon"],
                "instructions": ["Rouler les chicons", "Gratiner au four"],
                "sourceUrl": "https://fr.wikibooks.org/wiki/Chicons_au_gratin"
              }
            ]
        """.trimIndent()

        whenever(mockContext.assets).thenReturn(mockAssets)
        whenever(mockAssets.open("seeds/recipes_seed_be.json"))
            .thenReturn(ByteArrayInputStream(jsonContent.toByteArray(Charsets.UTF_8)))

        val list = StarterRecipeSeedLoader.loadSeedRecipesFromAssetFile(mockContext, "seeds/recipes_seed_be.json")

        assertThat(list).hasSize(1)
        assertThat(list.first().title).isEqualTo("Chicons au gratin")
        assertThat(list.first().cuisineOrigin).isEqualTo("Cuisine Belge")
    }

    @Test
    fun `loadSeedRecipesFromAssetFile should return empty list and log error on IOException`() {
        val mockContext = mock<Context>()
        val mockAssets = mock<AssetManager>()

        whenever(mockContext.assets).thenReturn(mockAssets)
        whenever(mockAssets.open("invalid_path.json")).thenThrow(IOException("File not found"))

        val list = StarterRecipeSeedLoader.loadSeedRecipesFromAssetFile(mockContext, "invalid_path.json")

        assertThat(list).isEmpty()
    }

    @Test
    fun `loadSeedRecipesFromAssets should call loadSeedRecipesFromAssetFile for master seed`() {
        val mockContext = mock<Context>()
        val mockAssets = mock<AssetManager>()

        val jsonContent = """
            [
              {
                "title": "Waterzooï",
                "category": "Plat principal",
                "ingredients": ["1 poulet"]
              }
            ]
        """.trimIndent()

        whenever(mockContext.assets).thenReturn(mockAssets)
        whenever(mockAssets.open("starter_recipes_seed.json"))
            .thenReturn(ByteArrayInputStream(jsonContent.toByteArray(Charsets.UTF_8)))

        val list = StarterRecipeSeedLoader.loadSeedRecipesFromAssets(mockContext)

        assertThat(list).hasSize(1)
        assertThat(list.first().title).isEqualTo("Waterzooï")
    }

    @Test
    fun `importCountryPackToDatabase should load asset pack and import recipes into database`() = runTest {
        val mockContext = mock<Context>()
        val mockAssets = mock<AssetManager>()
        val mockDb = mock<AppDatabase>()
        val mockRecipeDao = mock<RecipeDao>()
        val mockLocationDao = mock<LocationDao>()

        val jsonContent = """
            [
              {
                "title": "Carbonnade flamande",
                "category": "Plat principal",
                "cuisineOrigin": "Cuisine Belge",
                "ingredients": ["1 kg de bœuf", "2 oignons", "50 cl de bière"],
                "instructions": ["Faire dorer le bœuf", "Mijoter 2h"]
              }
            ]
        """.trimIndent()

        whenever(mockContext.assets).thenReturn(mockAssets)
        whenever(mockAssets.open("seeds/recipes_seed_be.json"))
            .thenReturn(ByteArrayInputStream(jsonContent.toByteArray(Charsets.UTF_8)))

        whenever(mockDb.recipeDao()).thenReturn(mockRecipeDao)
        whenever(mockDb.locationDao()).thenReturn(mockLocationDao)
        whenever(mockRecipeDao.getAllRecipesWithIngredients()).thenReturn(emptyList())
        whenever(mockLocationDao.getAllSync()).thenReturn(emptyList())
        whenever(mockRecipeDao.insertRecipe(any())).thenReturn(101L)

        val result = StarterRecipeSeedLoader.importCountryPackToDatabase(mockContext, mockDb, "seeds/recipes_seed_be.json")

        assertThat(result.insertedCount).isEqualTo(1)
        assertThat(result.updatedCount).isEqualTo(0)
    }

    @Test
    fun `importCountryPackToDatabase should return 0 inserted when seed recipes list is empty`() = runTest {
        val mockContext = mock<Context>()
        val mockAssets = mock<AssetManager>()
        val mockDb = mock<AppDatabase>()

        whenever(mockContext.assets).thenReturn(mockAssets)
        whenever(mockAssets.open("empty_pack.json")).thenThrow(IOException("Empty"))

        val result = StarterRecipeSeedLoader.importCountryPackToDatabase(mockContext, mockDb, "empty_pack.json")

        assertThat(result.insertedCount).isEqualTo(0)
        assertThat(result.updatedCount).isEqualTo(0)
    }

    @Test
    fun `importSeedRecipesToDatabase should load master assets seed and import into database`() = runTest {
        val mockContext = mock<Context>()
        val mockAssets = mock<AssetManager>()
        val mockDb = mock<AppDatabase>()
        val mockRecipeDao = mock<RecipeDao>()
        val mockLocationDao = mock<LocationDao>()

        val jsonContent = """
            [
              {
                "title": "Tarte Tatin",
                "category": "Dessert",
                "cuisineOrigin": "Cuisine Française",
                "ingredients": ["6 pommes", "100g de beurre"],
                "instructions": ["Caraméliser les pommes", "Cuire au four"]
              }
            ]
        """.trimIndent()

        whenever(mockContext.assets).thenReturn(mockAssets)
        whenever(mockAssets.open("starter_recipes_seed.json"))
            .thenReturn(ByteArrayInputStream(jsonContent.toByteArray(Charsets.UTF_8)))

        whenever(mockDb.recipeDao()).thenReturn(mockRecipeDao)
        whenever(mockDb.locationDao()).thenReturn(mockLocationDao)
        whenever(mockRecipeDao.getAllRecipesWithIngredients()).thenReturn(emptyList())
        whenever(mockLocationDao.getAllSync()).thenReturn(emptyList())
        whenever(mockRecipeDao.insertRecipe(any())).thenReturn(202L)

        val result = StarterRecipeSeedLoader.importSeedRecipesToDatabase(mockContext, mockDb)

        assertThat(result.insertedCount).isEqualTo(1)
    }

    @Test
    fun `importSeedRecipesToDatabase should return 0 inserted when master assets seed is empty`() = runTest {
        val mockContext = mock<Context>()
        val mockAssets = mock<AssetManager>()
        val mockDb = mock<AppDatabase>()

        whenever(mockContext.assets).thenReturn(mockAssets)
        whenever(mockAssets.open("starter_recipes_seed.json")).thenThrow(IOException("Not found"))

        val result = StarterRecipeSeedLoader.importSeedRecipesToDatabase(mockContext, mockDb)

        assertThat(result.insertedCount).isEqualTo(0)
        assertThat(result.updatedCount).isEqualTo(0)
    }

    @Test
    fun `importSeedListToDatabase should update existing recipe and inject missing ingredients`() = runTest {
        val mockDb = mock<AppDatabase>()
        val mockRecipeDao = mock<RecipeDao>()
        val mockLocationDao = mock<LocationDao>()

        val existingRecipe = com.example.chiefinventory.model.Recipe(id = 55L, title = "Gaufre Liégeoise", instructions = "")
        val existingWithIngr = com.example.chiefinventory.dao.RecipeWithIngredients(recipe = existingRecipe, ingredients = emptyList())

        whenever(mockDb.recipeDao()).thenReturn(mockRecipeDao)
        whenever(mockDb.locationDao()).thenReturn(mockLocationDao)
        whenever(mockRecipeDao.getAllRecipesWithIngredients()).thenReturn(listOf(existingWithIngr))
        whenever(mockLocationDao.getAllSync()).thenReturn(emptyList())

        val seeds = listOf(
            StarterRecipeSeedLoader.SeedRecipe(
                title = "Gaufre Liégeoise",
                category = "Dessert",
                cuisineOrigin = "Cuisine Belge",
                prepTimeMinutes = 10,
                cookTimeMinutes = 20,
                servings = 6,
                ingredients = listOf("250 g de farine", "100 g de sucre perlé"),
                instructions = listOf("Cuire au gaufrier"),
                sourceUrl = "https://fr.wikibooks.org/wiki/Gaufre"
            )
        )

        val result = StarterRecipeSeedLoader.importSeedListToDatabase(seeds, mockDb)

        assertThat(result.updatedCount).isEqualTo(1)
        assertThat(result.insertedCount).isEqualTo(0)
    }
}
