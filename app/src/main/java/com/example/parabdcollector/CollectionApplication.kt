package com.example.parabdcollector

import android.app.Application
import com.example.parabdcollector.data.AppDatabase
import com.example.parabdcollector.repo.CollectionRepository
import com.example.parabdcollector.repo.LocationRepository
import com.example.parabdcollector.utils.GlobalExceptionHandler

/**
 * The base Application class for the project.
 *
 * This class serves as a dependency container, providing singleton instances of the database
 * and repositories to the rest of the application. It is made `open` to allow for replacement
 * of its properties during instrumented testing.
 */
open class CollectionApplication : Application() {
    /**
     * Lazily-initialized singleton instance of the Room database.
     */
    val database: AppDatabase by lazy { AppDatabase.getDatabase(this) }

    private var _repository: CollectionRepository? = null
    /**
     * The singleton instance of the [CollectionRepository].
     * Can be reassigned in tests.
     */
    open var repository: CollectionRepository
        get() = _repository ?: CollectionRepository(database.collectionDao()).also { _repository = it }
        set(value) { _repository = value }

    private var _locationRepository: LocationRepository? = null
    /**
     * The singleton instance of the [LocationRepository].
     * Can be reassigned in tests.
     */
    open var locationRepository: LocationRepository
        get() = _locationRepository ?: LocationRepository(database.locationDao()).also { _locationRepository = it }
        set(value) { _locationRepository = value }

    override fun onCreate() {
        super.onCreate()
        Thread.setDefaultUncaughtExceptionHandler(GlobalExceptionHandler(this))
    }
}
