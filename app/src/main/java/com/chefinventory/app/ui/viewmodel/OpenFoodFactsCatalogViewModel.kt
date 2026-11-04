package com.chefinventory.app.ui.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chefinventory.app.data.AppDatabase
import com.chefinventory.app.data.OpenFoodFactsTaxonomyLoader
import com.chefinventory.app.model.Ingredient
import com.chefinventory.app.model.OpenFoodFactsTaxonomy
import com.chefinventory.app.repo.IngredientRepository
import com.chefinventory.app.repository.OpenFoodFactsRepository
import kotlinx.coroutines.launch

/**
 * ViewModel pour l'écran de consultation et recherche du Catalogue Open Food Facts (Niveau 2).
 *
 * Gère la recherche dans la taxonomie des 5 003 ingrédients et l'importation optionnelle vers le Garde-Manger.
 */
class OpenFoodFactsCatalogViewModel(
    private val offRepository: OpenFoodFactsRepository,
    private val ingredientRepository: IngredientRepository? = null
) : ViewModel() {

    private val _taxonomyList = MutableLiveData<List<OpenFoodFactsTaxonomy>>()
    val taxonomyList: LiveData<List<OpenFoodFactsTaxonomy>> = _taxonomyList

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _importStatus = MutableLiveData<String?>()
    val importStatus: LiveData<String?> = _importStatus

    private var currentQuery: String = ""
    private var selectedCategoryCode: Int = 0
    private var selectedLetter: String? = null

    fun loadInitialData(context: Context? = null) = viewModelScope.launch {
        _isLoading.value = true
        try {
            if (context != null) {
                val database = AppDatabase.getDatabase(context)
                OpenFoodFactsTaxonomyLoader.seedTaxonomyIfEmpty(context, database)
            }
            applyFilters()
        } catch (_: Exception) {
            _taxonomyList.value = emptyList()
        } finally {
            _isLoading.value = false
        }
    }

    fun search(query: String) {
        currentQuery = query.trim()
        applyFilters()
    }

    fun filterByCategoryCode(code: Int) {
        selectedCategoryCode = maxOf(0, code)
        applyFilters()
    }

    fun filterByCategory(category: String?) {
        val cleanCat = category?.trim()
        selectedCategoryCode = when (cleanCat) {
            "Fruits & Légumes" -> 1
            "Produits Laitiers & Crémerie" -> 2
            "Huiles & Graisses" -> 3
            "Viandes Rouges" -> 4
            "Volailles" -> 5
            "Poissons & Crustacés" -> 6
            "Épices & Condiments" -> 7
            "Céréales & Farines" -> 8
            "Boissons" -> 9
            "Épicerie & Divers" -> 10
            else -> 0
        }
        applyFilters()
    }

    fun filterByLetter(letter: String?) {
        selectedLetter = if (letter.isNullOrBlank() || letter == "TOUS") null else letter.trim().uppercase()
        applyFilters()
    }

    private fun applyFilters() = viewModelScope.launch {
        _isLoading.value = true
        try {
            val searchPattern = when {
                !selectedLetter.isNullOrBlank() && currentQuery.isNotBlank() -> "$selectedLetter%$currentQuery"
                !selectedLetter.isNullOrBlank() -> "$selectedLetter%"
                else -> currentQuery
            }

            _taxonomyList.value = offRepository.getTaxonomyByCodePaged(
                categoryCode = selectedCategoryCode,
                query = searchPattern,
                limit = 200,
                offset = 0
            )
        } catch (_: Exception) {
            _taxonomyList.value = emptyList()
        } finally {
            _isLoading.value = false
        }
    }

    fun importToPantry(taxonomy: OpenFoodFactsTaxonomy, defaultLocationId: Long = 2L) = viewModelScope.launch {
        if (ingredientRepository == null) return@launch
        try {
            val newIngredient = Ingredient(
                name = taxonomy.cleanName.replaceFirstChar { it.titlecase() },
                category = taxonomy.category ?: "Épicerie",
                locationId = defaultLocationId,
                quantity = 0.0,
                unit = "unité"
            )
            ingredientRepository.insert(newIngredient)
            _importStatus.value = "Ingrédient '${newIngredient.name}' ajouté à votre Garde-Manger !"
        } catch (_: Exception) {
            _importStatus.value = "Erreur lors de l'ajout de l'ingrédient"
        }
    }

    fun clearImportStatus() {
        _importStatus.value = null
    }
}
