package com.example.parabdcollector

import android.app.Application
import androidx.annotation.VisibleForTesting
import com.example.parabdcollector.data.AppDatabase
import com.example.parabdcollector.repo.CollectionRepository
import com.example.parabdcollector.repo.LocationRepository

open class CollectionApplication : Application() {
    // L'utilisation de "lazy" garantit que la base de données et le repository ne sont créés qu'une seule fois.
    val database by lazy { AppDatabase.getDatabase(this) }
    
    // On utilise "open" et on fournit un setter visible pour les tests
    // pour pouvoir injecter des mocks.
    @VisibleForTesting
    open var repository: CollectionRepository? = null
        get() = field ?: CollectionRepository(database.collectionDao())

    @VisibleForTesting
    open var locationRepository: LocationRepository? = null
        get() = field ?: LocationRepository(database.locationDao())
}
