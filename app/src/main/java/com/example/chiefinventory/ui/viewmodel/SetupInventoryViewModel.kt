package com.example.chiefinventory.ui.viewmodel

import android.app.Application
import android.content.res.Resources
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.chiefinventory.dao.IngredientDao
import com.example.chiefinventory.dao.LocationDao
import com.example.chiefinventory.data.AppDatabase
import com.example.chiefinventory.model.Ingredient
import com.example.chiefinventory.ui.adapter.SelectedCatalogEntry
import com.example.chiefinventory.utils.StandardCatalogLoader
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class SetupInventoryViewModel @JvmOverloads constructor(
    application: Application,
    private val ingredientDao: IngredientDao = AppDatabase.getDatabase(application).ingredientDao(),
    private val locationDao: LocationDao = AppDatabase.getDatabase(application).locationDao(),
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : AndroidViewModel(application) {

    private val _catalogEntries = MutableLiveData<List<SelectedCatalogEntry>>()
    val catalogEntries: LiveData<List<SelectedCatalogEntry>> = _catalogEntries

    private val _saveStatus = MutableLiveData<Boolean>()
    val saveStatus: LiveData<Boolean> = _saveStatus

    fun loadCatalog(res: Resources, forceSelectAll: Boolean = false): Job =
        viewModelScope.launch(ioDispatcher) {
            val items = StandardCatalogLoader.loadFromResources(res)
            val existing = ingredientDao.getAllSync()

            val isFreshSetup = if (forceSelectAll) {
                true
            } else {
                existing.isEmpty()
            }

            val entries = items.map { item ->
                val match = existing.find { it.name.equals(item.name, ignoreCase = true) }
                val isSelected = determineEntrySelection(
                    isFreshSetup = isFreshSetup,
                    stockPolicy = item.stockPolicy,
                    hasMatch = match != null,
                    matchQuantity = match?.quantity
                )
                val currentQty = determineEntryQuantity(
                    matchQuantity = match?.quantity,
                    defaultQuantity = item.defaultQuantity
                )
                SelectedCatalogEntry(item, isSelected = isSelected, currentQuantity = currentQty)
            }
            _catalogEntries.postValue(entries)
        }

    fun saveSelectedIngredients(catalogEntries: List<SelectedCatalogEntry>): Job? {
        if (catalogEntries.isEmpty()) {
            return null
        }

        return viewModelScope.launch(ioDispatcher) {
            val locations = locationDao.getAllSync()
            val existingList = ingredientDao.getAllSync()

            for (entry in catalogEntries) {
                val cleanName = entry.item.name.lowercase().trim()
                val targetLocName = entry.item.defaultLocationName
                val loc = locations.find { location ->
                    location.name.contains(targetLocName, ignoreCase = true)
                }

                val targetQty = calculateTargetQuantity(
                    isSelected = entry.isSelected,
                    currentQuantity = entry.currentQuantity
                )

                val matches = existingList.filter { it.name.lowercase().trim() == cleanName }

                if (matches.isNotEmpty()) {
                    val primary = matches.first()
                    val targetLocationId = if (loc != null) {
                        loc.id
                    } else {
                        primary.locationId
                    }

                    ingredientDao.update(primary.copy(
                        name = entry.item.name,
                        quantity = targetQty,
                        unit = entry.item.defaultUnit,
                        category = entry.item.category,
                        locationId = targetLocationId,
                        stockPolicy = entry.item.stockPolicy
                    ))

                    for (duplicate in matches.drop(1)) {
                        ingredientDao.delete(duplicate)
                    }
                } else if (targetQty > 0.0) {
                    val targetLocationId = if (loc != null) {
                        loc.id
                    } else {
                        null
                    }

                    val ingredient = Ingredient(
                        name = entry.item.name,
                        quantity = targetQty,
                        unit = entry.item.defaultUnit,
                        category = entry.item.category,
                        locationId = targetLocationId,
                        stockPolicy = entry.item.stockPolicy
                    )
                    ingredientDao.insert(ingredient)
                }
            }
            _saveStatus.postValue(true)
        }
    }

    companion object {
        fun determineEntrySelection(
            isFreshSetup: Boolean,
            stockPolicy: String,
            hasMatch: Boolean,
            matchQuantity: Double?
        ): Boolean {
            if (isFreshSetup) {
                return stockPolicy.equals("PERMANENT", ignoreCase = true)
            } else {
                return hasMatch && matchQuantity != null && matchQuantity > 0.0
            }
        }

        fun determineEntryQuantity(matchQuantity: Double?, defaultQuantity: Double): Double {
            if (matchQuantity != null && matchQuantity > 0.0) {
                return matchQuantity
            } else {
                return defaultQuantity
            }
        }

        fun calculateTargetQuantity(isSelected: Boolean, currentQuantity: Double): Double {
            if (isSelected && currentQuantity > 0.0) {
                return currentQuantity
            } else {
                return 0.0
            }
        }
    }
}
