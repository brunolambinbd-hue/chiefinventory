package com.example.parabdcollector

import android.app.Application
import com.example.parabdcollector.db.AppDatabase
import com.example.parabdcollector.repo.CollectionRepository
import com.example.parabdcollector.repo.LocationRepository

class CollectionApplication : Application() {
    // L'utilisation de "lazy" garantit que la base de données et le repository ne sont créés qu'une seule fois.
    val database by lazy { AppDatabase.getDatabase(this) }
    val repository by lazy { CollectionRepository(database.collectionDao()) }
    val locationRepository by lazy { LocationRepository(database.locationDao()) }
}