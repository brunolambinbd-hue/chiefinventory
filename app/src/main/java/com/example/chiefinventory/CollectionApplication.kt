package com.example.chiefinventory

import android.app.Application
import com.example.chiefinventory.data.AppDatabase
import com.example.chiefinventory.repo.CollectionRepository
import com.example.chiefinventory.repo.IngredientRepository
import com.example.chiefinventory.repo.LocationRepository
import com.example.chiefinventory.utils.GlobalExceptionHandler

/**
 * Application class providing repository singletons.
 */
open class CollectionApplication : Application() {
    val database: AppDatabase by lazy { AppDatabase.getDatabase(this) }

    private var _repository: CollectionRepository? = null
    open var repository: CollectionRepository
        get() = _repository ?: CollectionRepository(database.collectionDao()).also { _repository = it }
        set(value) { _repository = value }

    private var _locationRepository: LocationRepository? = null
    open var locationRepository: LocationRepository
        get() = _locationRepository ?: LocationRepository(database.locationDao()).also { _locationRepository = it }
        set(value) { _locationRepository = value }

    private var _ingredientRepository: IngredientRepository? = null
    open var ingredientRepository: IngredientRepository
        get() = _ingredientRepository ?: IngredientRepository(database.ingredientDao()).also { _ingredientRepository = it }
        set(value) { _ingredientRepository = value }

    override fun onCreate() {
        super.onCreate()
        // Rétablissement du gestionnaire d'exceptions global
        Thread.setDefaultUncaughtExceptionHandler(GlobalExceptionHandler(this))
    }
}
