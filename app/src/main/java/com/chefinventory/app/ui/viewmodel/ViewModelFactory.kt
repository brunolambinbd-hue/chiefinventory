package com.chefinventory.app.ui.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.chefinventory.app.repo.IngredientRepository
import com.chefinventory.app.repo.LocationRepository
import com.chefinventory.app.repo.RecipeRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

class ViewModelFactory(
    private val application: Application,
    private val locationRepository: LocationRepository,
    private val ingredientRepository: IngredientRepository,
    private val recipeRepository: RecipeRepository? = null,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(MainViewModel::class.java) -> MainViewModel(ingredientRepository, recipeRepository) as T
            modelClass.isAssignableFrom(LocationViewModel::class.java) -> LocationViewModel(locationRepository) as T
            modelClass.isAssignableFrom(InventoryViewModel::class.java) -> InventoryViewModel(application) as T
            modelClass.isAssignableFrom(BackupViewModel::class.java) -> BackupViewModel(application) as T
            modelClass.isAssignableFrom(IngredientViewModel::class.java) -> IngredientViewModel(ingredientRepository, locationRepository) as T
            modelClass.isAssignableFrom(EditIngredientViewModel::class.java) -> EditIngredientViewModel(application, ingredientRepository, locationRepository) as T
            modelClass.isAssignableFrom(MatchRecipeViewModel::class.java) -> MatchRecipeViewModel(application, ioDispatcher) as T
            modelClass.isAssignableFrom(SetupInventoryViewModel::class.java) -> SetupInventoryViewModel(application, ioDispatcher = ioDispatcher) as T
            modelClass.isAssignableFrom(ShoppingListViewModel::class.java) -> ShoppingListViewModel(application, ioDispatcher = ioDispatcher) as T
            modelClass.isAssignableFrom(SearchRecipeViewModel::class.java) -> {
                val repo = if (recipeRepository != null) {
                    recipeRepository
                } else {
                    throw IllegalArgumentException("RecipeRepository required")
                }
                SearchRecipeViewModel(repo, ingredientRepository) as T
            }
            modelClass.isAssignableFrom(EditRecipeViewModel::class.java) -> {
                val repo = if (recipeRepository != null) {
                    recipeRepository
                } else {
                    throw IllegalArgumentException("RecipeRepository required")
                }
                EditRecipeViewModel(repo, locationRepository) as T
            }
            modelClass.isAssignableFrom(RecipeViewModel::class.java) -> {
                val repo = if (recipeRepository != null) {
                    recipeRepository
                } else {
                    throw IllegalArgumentException("RecipeRepository required")
                }
                RecipeViewModel(repo) as T
            }
            modelClass.isAssignableFrom(RecipeCategoryViewModel::class.java) -> {
                val repo = if (recipeRepository != null) {
                    recipeRepository
                } else {
                    throw IllegalArgumentException("RecipeRepository required")
                }
                RecipeCategoryViewModel(repo) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
        }
    }
}
