package com.chefinventory.app

import androidx.test.core.app.ApplicationProvider
import com.chefinventory.app.repo.IngredientRepository
import com.chefinventory.app.repo.LocationRepository
import com.chefinventory.app.repo.RecipeRepository
import com.chefinventory.app.utils.GlobalExceptionHandler
import com.google.common.truth.Truth.assertThat
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Suite de tests unitaires de Type B vérifiant l'initialisation et les singletons de [ChefInventoryApplication].
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ChefInventoryApplicationTest {

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testChefInventoryApplication_SingletonsAndRepositories() {
        val app = ApplicationProvider.getApplicationContext<ChefInventoryApplication>()
        app.onCreate()

        // 1. Database lazy property
        assertThat(app.database).isNotNull()

        // 2. Default LocationRepository getter and custom setter
        val defaultLocRepo = app.locationRepository
        assertThat(defaultLocRepo).isNotNull()

        val mockLocRepo = mock<LocationRepository>()
        app.locationRepository = mockLocRepo
        assertThat(app.locationRepository).isEqualTo(mockLocRepo)

        // 3. Default IngredientRepository getter and custom setter
        val defaultIngrRepo = app.ingredientRepository
        assertThat(defaultIngrRepo).isNotNull()

        val mockIngrRepo = mock<IngredientRepository>()
        app.ingredientRepository = mockIngrRepo
        assertThat(app.ingredientRepository).isEqualTo(mockIngrRepo)

        // 4. Default RecipeRepository getter and custom setter
        val defaultRecipeRepo = app.recipeRepository
        assertThat(defaultRecipeRepo).isNotNull()

        val mockRecipeRepo = mock<RecipeRepository>()
        app.recipeRepository = mockRecipeRepo
        assertThat(app.recipeRepository).isEqualTo(mockRecipeRepo)

        // 5. Uncaught exception handler
        val handler = Thread.getDefaultUncaughtExceptionHandler()
        assertThat(handler).isInstanceOf(GlobalExceptionHandler::class.java)
    }
}
