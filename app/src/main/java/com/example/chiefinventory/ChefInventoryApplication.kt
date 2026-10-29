package com.example.chiefinventory

import android.app.Application
import com.example.chiefinventory.data.AppDatabase
import com.example.chiefinventory.repo.IngredientRepository
import com.example.chiefinventory.repo.LocationRepository
import com.example.chiefinventory.repo.RecipeRepository
import com.example.chiefinventory.utils.GlobalExceptionHandler

/**
 * Application class providing repository singletons.
 */
open class ChefInventoryApplication : Application() {
    val database: AppDatabase by lazy { AppDatabase.getDatabase(this) }

    private var _locationRepository: LocationRepository? = null
    open var locationRepository: LocationRepository
        get() = _locationRepository ?: LocationRepository(database.locationDao()).also { _locationRepository = it }
        set(value) { _locationRepository = value }

    private var _ingredientRepository: IngredientRepository? = null
    open var ingredientRepository: IngredientRepository
        get() = _ingredientRepository ?: IngredientRepository(database.ingredientDao()).also { _ingredientRepository = it }
        set(value) { _ingredientRepository = value }

    private var _recipeRepository: RecipeRepository? = null
    open var recipeRepository: RecipeRepository
        get() = _recipeRepository ?: RecipeRepository(database.recipeDao(), database.ingredientDao()).also { _recipeRepository = it }
        set(value) { _recipeRepository = value }

    override fun onCreate() {
        super.onCreate()
        // Rétablissement du gestionnaire d'exceptions global
        Thread.setDefaultUncaughtExceptionHandler(GlobalExceptionHandler(this))
    }
}
