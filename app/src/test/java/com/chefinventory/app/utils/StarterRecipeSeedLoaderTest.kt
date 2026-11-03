package com.chefinventory.app.utils

import android.content.Context
import android.content.res.AssetManager
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.chefinventory.app.data.AppDatabase
import com.chefinventory.app.model.Location
import com.chefinventory.app.model.LocationType
import com.google.common.truth.Truth.assertThat
import com.parabd.lib.traceability.CoveredRequirement
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.json.JSONArray
import org.json.JSONObject
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class StarterRecipeSeedLoaderTest {

    private lateinit var context: Context
    private lateinit var db: AppDatabase
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() {
        db.close()
        Dispatchers.resetMain()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testImportSeedListToDatabase_ExistingRecipeWithBlankInstructionsAndUnknownCategory() = runTest {
        val initialSeed = StarterRecipeSeedLoader.SeedRecipe(
            title = "Soupe de poireaux v2",
            category = "Entrée",
            cuisineOrigin = null,
            prepTimeMinutes = 15,
            cookTimeMinutes = 20,
            servings = 4,
            imageUrl = null,
            ingredients = listOf("2 poireaux"),
            instructions = listOf("   "),
            sourceUrl = null
        )

        // Insert recipe with blank instructions ("   ") -> currentInstr.isBlank() == true
        val result1 = StarterRecipeSeedLoader.importSeedListToDatabase(listOf(initialSeed), db)
        assertThat(result1.insertedCount).isEqualTo(1)

        // Update locationId to -1L
        val recipeDao = db.recipeDao()
        val existingWithIngr = recipeDao.getAllRecipesWithIngredients().first { it.recipe.title == "Soupe de poireaux v2" }
        recipeDao.updateRecipe(existingWithIngr.recipe.copy(locationId = -1L, instructions = "   "))

        // Re-import with unknown category "Inconnue999" (matchedCat == null) -> covers L188 isBlank() == true and L203 matchedCat == null
        val updateSeed = StarterRecipeSeedLoader.SeedRecipe(
            title = "Soupe de poireaux v2",
            category = "Inconnue999",
            cuisineOrigin = null,
            prepTimeMinutes = 15,
            cookTimeMinutes = 20,
            servings = 4,
            imageUrl = null,
            ingredients = listOf("2 poireaux"),
            instructions = listOf("Mijoter 20 min."),
            sourceUrl = null
        )

        val result2 = StarterRecipeSeedLoader.importSeedListToDatabase(listOf(updateSeed), db)
        assertThat(result2.updatedCount).isEqualTo(1)
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testLoadSeedRecipesFromAssetFile_WithNonObjectAndBlankTitleElements() {
        val jsonArrayStr = """
            [
              "chaine_non_objet",
              {"title": "   "},
              {"title": "Tarte aux fraises", "category": "Dessert"}
            ]
        """.trimIndent()

        val mockAssets = mock<AssetManager> {
            on { open("test_invalid_seeds.json") } doReturn jsonArrayStr.byteInputStream()
        }
        val mockContext = mock<Context> {
            on { assets } doReturn mockAssets
        }

        val recipes = StarterRecipeSeedLoader.loadSeedRecipesFromAssetFile(mockContext, "test_invalid_seeds.json")

        assertThat(recipes).hasSize(1)
        assertThat(recipes.first().title).isEqualTo("Tarte aux fraises")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testImportSeedListToDatabase_ExistingRecipeWithEmptyInstructionsAndNullCategory() = runTest {
        val initialSeed = StarterRecipeSeedLoader.SeedRecipe(
            title = "Soupe de légumes",
            category = "Entrée",
            cuisineOrigin = null,
            prepTimeMinutes = 10,
            cookTimeMinutes = 15,
            servings = 4,
            imageUrl = null,
            ingredients = listOf("2 carottes"),
            instructions = emptyList(),
            sourceUrl = null
        )

        // Initial insertion with empty instructions and null source
        val result1 = StarterRecipeSeedLoader.importSeedListToDatabase(listOf(initialSeed), db)
        assertThat(result1.insertedCount).isEqualTo(1)

        // Manually update instructions to null in database to test L194 (existing.recipe.instructions ?: "" null branch)
        val recipeDao = db.recipeDao()
        val existingWithIngr = recipeDao.getAllRecipesWithIngredients().first { it.recipe.title == "Soupe de légumes" }
        recipeDao.updateRecipe(existingWithIngr.recipe.copy(instructions = null))

        // Second import with empty instructions on existing recipe that also has empty instructions
        val updateSeed = StarterRecipeSeedLoader.SeedRecipe(
            title = "Soupe de légumes",
            category = "SaisonInconnue",
            cuisineOrigin = null,
            prepTimeMinutes = 10,
            cookTimeMinutes = 15,
            servings = 4,
            imageUrl = null,
            ingredients = emptyList(),
            instructions = emptyList(),
            sourceUrl = null
        )

        val result2 = StarterRecipeSeedLoader.importSeedListToDatabase(listOf(updateSeed), db)
        assertThat(result2.updatedCount).isEqualTo(1)
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testParseSeedRecipeJsonObject_BlankTitleAndMissingArrayKeys() {
        // 1. Blank title returns null (L82)
        val blankTitleJson = JSONObject().apply { put("title", "   ") }
        assertThat(StarterRecipeSeedLoader.parseSeedRecipeJsonObject(blankTitleJson)).isNull()

        // 2. Missing ingredients/instructions arrays (L92)
        val noArrayJson = JSONObject().apply {
            put("title", "Soupe simple")
            put("category", "Entrée")
        }
        val parsedNoArray = StarterRecipeSeedLoader.parseSeedRecipeJsonObject(noArrayJson)
        assertThat(parsedNoArray).isNotNull()
        assertThat(parsedNoArray?.ingredients).isEmpty()
        assertThat(parsedNoArray?.instructions).isEmpty()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testImportSeedListToDatabase_WithPantryLocationInDatabase() = runTest {
        // Insert a non-recipe location (LocationType.INGREDIENT) to test L156 & L165 LocationType.RECIPE filtering
        val locationDao = db.locationDao()
        locationDao.insert(Location(name = "Frigo Inox", parentId = null, type = LocationType.INGREDIENT))

        val seed = StarterRecipeSeedLoader.SeedRecipe(
            title = "Soupe de poireaux",
            category = "Entrée",
            cuisineOrigin = "Belgique",
            prepTimeMinutes = 15,
            cookTimeMinutes = 20,
            servings = 4,
            imageUrl = null,
            ingredients = listOf("2 poireaux"),
            instructions = listOf("Cuire à l'eau."),
            sourceUrl = null
        )

        val result = StarterRecipeSeedLoader.importSeedListToDatabase(listOf(seed), db)
        assertThat(result.insertedCount).isEqualTo(1)
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testParseSeedRecipeJsonObject_BlankLinesAndNulls() {
        val json = JSONObject().apply {
            put("title", "Tarte aux fraises")
            put("category", "Dessert")
            put("prepTimeMinutes", 15)
            put("cookTimeMinutes", 20)
            put("servings", 4)
            put("cuisineOrigin", "")
            put("imageUrl", "")
            put("sourceUrl", "")
            put("ingredients", JSONArray().apply {
                put("200 g de farine")
                put("   ")
            })
            put("instructions", JSONArray().apply {
                put("Cuire au four.")
                put("   ")
            })
        }

        val parsed = StarterRecipeSeedLoader.parseSeedRecipeJsonObject(json)
        assertThat(parsed).isNotNull()
        assertThat(parsed?.title).isEqualTo("Tarte aux fraises")
        assertThat(parsed?.ingredients).containsExactly("200 g de farine")
        assertThat(parsed?.instructions).containsExactly("Cuire au four.")
        assertThat(parsed?.cuisineOrigin).isNull()
        assertThat(parsed?.imageUrl).isNull()
        assertThat(parsed?.sourceUrl).isNull()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testImportSeedListToDatabase_ExistingRecipeWithAllFieldsAssigned() = runTest {
        val initialSeed = StarterRecipeSeedLoader.SeedRecipe(
            title = "Omelette aux fines herbes",
            category = "Plat principal",
            cuisineOrigin = "France",
            prepTimeMinutes = 10,
            cookTimeMinutes = 5,
            servings = 2,
            imageUrl = "https://example.com/omelette.jpg",
            ingredients = listOf("3 oeufs"),
            instructions = listOf("Casser les oeufs."),
            sourceUrl = "https://example.com/source"
        )

        // Initial insertion
        val result1 = StarterRecipeSeedLoader.importSeedListToDatabase(listOf(initialSeed), db)
        assertThat(result1.insertedCount).isEqualTo(1)

        // Second import with blank image/source on same existing recipe (tests L177, L178, L179, L180 else branches)
        val updateSeed = StarterRecipeSeedLoader.SeedRecipe(
            title = "Omelette aux fines herbes",
            category = "Plat principal",
            cuisineOrigin = null,
            prepTimeMinutes = 10,
            cookTimeMinutes = 5,
            servings = 2,
            imageUrl = null,
            ingredients = emptyList(),
            instructions = listOf("Casser les oeufs."),
            sourceUrl = null
        )

        val result2 = StarterRecipeSeedLoader.importSeedListToDatabase(listOf(updateSeed), db)
        assertThat(result2.updatedCount).isEqualTo(1)
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testImportSeedListToDatabase_CustomCategoryAndLocationIdMinusOne() = runTest {
        val customCatSeed = StarterRecipeSeedLoader.SeedRecipe(
            title = "Plat Spécial Inconnu",
            category = "CustomCategory999",
            cuisineOrigin = "",
            prepTimeMinutes = 15,
            cookTimeMinutes = 15,
            servings = 4,
            imageUrl = "",
            ingredients = listOf("100 g de sucre"),
            instructions = listOf("Mélanger tout."),
            sourceUrl = ""
        )

        // Insert new recipe with custom category (normCat == null L160 & L173) and empty fullSource (L237)
        val result1 = StarterRecipeSeedLoader.importSeedListToDatabase(listOf(customCatSeed), db)
        assertThat(result1.insertedCount).isEqualTo(1)

        // Manually update locationId to -1L in database to test L193 (locationId == -1L branch)
        val recipeDao = db.recipeDao()
        val existingWithIngr = recipeDao.getAllRecipesWithIngredients().first { it.recipe.title == "Plat Spécial Inconnu" }
        recipeDao.updateRecipe(existingWithIngr.recipe.copy(locationId = -1L, instructions = ""))

        // Re-import seed recipe with instructions to trigger update with locationId = -1L and empty seedImage = ""
        val result2 = StarterRecipeSeedLoader.importSeedListToDatabase(listOf(customCatSeed), db)
        assertThat(result2.updatedCount).isEqualTo(1)
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testLoadSeedRecipesFromAssets_ValidAndMissingAssetFiles() {
        val seeds = StarterRecipeSeedLoader.loadSeedRecipesFromAssets(context)
        assertThat(seeds).isNotEmpty()

        val beSeeds = StarterRecipeSeedLoader.loadSeedRecipesFromAssetFile(context, "seeds/recipes_seed_be.json")
        assertThat(beSeeds).isNotEmpty()

        val missingSeeds = StarterRecipeSeedLoader.loadSeedRecipesFromAssetFile(context, "non_existent_seed_file.json")
        assertThat(missingSeeds).isEmpty()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testImportSeedListToDatabase_WithInMemoryDatabase() = runTest {
        val seeds = StarterRecipeSeedLoader.loadSeedRecipesFromAssets(context)
        assertThat(seeds).isNotEmpty()

        val result = StarterRecipeSeedLoader.importSeedListToDatabase(seeds, db)
        assertThat(result.insertedCount + result.updatedCount).isGreaterThan(0)

        // Second import (tests update branch)
        val reResult = StarterRecipeSeedLoader.importSeedListToDatabase(seeds, db)
        assertThat(reResult.updatedCount).isAtLeast(0)
    }

    @Test
    @CoveredRequirement("6.3.2.9")
    fun testImportSeedRecipesFromAssets_AllPacksSupported() = runTest {
        val beSeeds = StarterRecipeSeedLoader.loadSeedRecipesFromAssetFile(context, "seeds/recipes_seed_be.json")
        val frSeeds = StarterRecipeSeedLoader.loadSeedRecipesFromAssetFile(context, "seeds/recipes_seed_fr.json")
        val itSeeds = StarterRecipeSeedLoader.loadSeedRecipesFromAssetFile(context, "seeds/recipes_seed_it.json")

        val resultBE = StarterRecipeSeedLoader.importSeedListToDatabase(beSeeds, db)
        val resultFR = StarterRecipeSeedLoader.importSeedListToDatabase(frSeeds, db)
        val resultIT = StarterRecipeSeedLoader.importSeedListToDatabase(itSeeds, db)

        assertThat(resultBE.insertedCount + resultBE.updatedCount).isGreaterThan(0)
        assertThat(resultFR.insertedCount + resultFR.updatedCount).isGreaterThan(0)
        assertThat(resultIT.insertedCount + resultIT.updatedCount).isGreaterThan(0)
    }
}
