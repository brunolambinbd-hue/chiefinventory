package com.example.chiefinventory.ui.viewmodel

import android.app.Application
import android.content.res.Resources
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.chiefinventory.data.AppDatabase
import com.example.chiefinventory.model.Ingredient
import com.example.chiefinventory.ui.adapter.SelectedCatalogEntry
import com.example.chiefinventory.utils.StandardCatalogLoader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SetupInventoryViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val ingredientDao = db.ingredientDao()
    private val locationDao = db.locationDao()

    private val _catalogEntries = MutableLiveData<List<SelectedCatalogEntry>>()
    val catalogEntries: LiveData<List<SelectedCatalogEntry>> = _catalogEntries

    private val _saveStatus = MutableLiveData<Boolean>()
    val saveStatus: LiveData<Boolean> = _saveStatus

    fun loadCatalog(res: Resources, forceSelectAll: Boolean = false) {
        viewModelScope.launch(Dispatchers.IO) {
            val items = StandardCatalogLoader.loadFromResources(res)
            val existing = ingredientDao.getAllSync()

            val isFreshSetup = forceSelectAll || existing.isEmpty()
            val entries = items.map { item ->
                val match = existing.find { it.name.equals(item.name, ignoreCase = true) }
                val isSelected = if (isFreshSetup) {
                    item.stockPolicy.equals("PERMANENT", ignoreCase = true)
                } else {
                    match != null && (match.quantity ?: 0.0) > 0.0
                }
                val currentQty = match?.quantity?.takeIf { it > 0.0 } ?: item.defaultQuantity
                SelectedCatalogEntry(item, isSelected = isSelected, currentQuantity = currentQty)
            }
            _catalogEntries.postValue(entries)
        }
    }

    fun saveSelectedIngredients(catalogEntries: List<SelectedCatalogEntry>) {
        if (catalogEntries.isEmpty()) return

        viewModelScope.launch(Dispatchers.IO) {
            val locations = locationDao.getAllSync()
            val existingList = ingredientDao.getAllSync()

            for (entry in catalogEntries) {
                val cleanName = entry.item.name.lowercase().trim()
                val loc = locations.find { it.name.contains(entry.item.defaultLocationName, ignoreCase = true) }
                val targetQty = if (entry.isSelected && entry.currentQuantity > 0.0) entry.currentQuantity else 0.0

                val matches = existingList.filter { it.name.lowercase().trim() == cleanName }

                if (matches.isNotEmpty()) {
                    val primary = matches.first()
                    ingredientDao.update(primary.copy(
                        name = entry.item.name,
                        quantity = targetQty,
                        unit = entry.item.defaultUnit,
                        category = entry.item.category,
                        locationId = loc?.id ?: primary.locationId,
                        stockPolicy = entry.item.stockPolicy
                    ))

                    for (duplicate in matches.drop(1)) {
                        ingredientDao.delete(duplicate)
                    }
                } else if (targetQty > 0.0) {
                    val ingredient = Ingredient(
                        name = entry.item.name,
                        quantity = targetQty,
                        unit = entry.item.defaultUnit,
                        category = entry.item.category,
                        locationId = loc?.id,
                        stockPolicy = entry.item.stockPolicy
                    )
                    ingredientDao.insert(ingredient)
                }
            }
            _saveStatus.postValue(true)
        }
    }
}
