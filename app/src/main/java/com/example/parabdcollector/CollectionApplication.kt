package com.example.parabdcollector

import android.app.Application
import androidx.annotation.VisibleForTesting
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
    val database by lazy { AppDatabase.getDatabase(this) }

    /**
     * The singleton instance of the [CollectionRepository].
     * This property is `open` for testing and `internal` to restrict access to this module.
     */
    @VisibleForTesting
    internal open var repository: CollectionRepository? = null
        get() = field ?: CollectionRepository(database.collectionDao())

    /**
     * The singleton instance of the [LocationRepository].
     * This property is `open` for testing and `internal` to restrict access to this module.
     */
    @VisibleForTesting
    internal open var locationRepository: LocationRepository? = null
        get() = field ?: LocationRepository(database.locationDao())

    override fun onCreate() {
        super.onCreate()
        Thread.setDefaultUncaughtExceptionHandler(GlobalExceptionHandler(this))
    }
}
