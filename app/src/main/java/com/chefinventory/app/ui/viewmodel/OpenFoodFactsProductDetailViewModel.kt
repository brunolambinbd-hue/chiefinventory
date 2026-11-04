package com.chefinventory.app.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chefinventory.app.model.Ingredient
import com.chefinventory.app.model.OpenFoodFactsProduct
import com.chefinventory.app.model.OpenFoodFactsTaxonomy
import com.chefinventory.app.repo.IngredientRepository
import com.chefinventory.app.repository.OpenFoodFactsRepository
import kotlinx.coroutines.launch

/**
 * ViewModel pour l'écran de visualisation et d'édition détaillée des fiches produits Open Food Facts (Niveau 2).
 */
class OpenFoodFactsProductDetailViewModel(
    private val offRepository: OpenFoodFactsRepository,
    private val ingredientRepository: IngredientRepository? = null
) : ViewModel() {

    private val _product = MutableLiveData<OpenFoodFactsProduct?>()
    val product: LiveData<OpenFoodFactsProduct?> = _product

    private val _taxonomy = MutableLiveData<OpenFoodFactsTaxonomy?>()
    val taxonomy: LiveData<OpenFoodFactsTaxonomy?> = _taxonomy

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _importStatus = MutableLiveData<String?>()
    val importStatus: LiveData<String?> = _importStatus

    fun loadProduct(barcode: String?, cleanName: String?, taxonomyId: Long = 0L, category: String? = null) = viewModelScope.launch {
        _isLoading.value = true
        try {
            if (!barcode.isNullOrBlank()) {
                val foundProduct = offRepository.getProductByBarcode(barcode)
                if (foundProduct != null) {
                    _product.value = foundProduct
                    return@launch
                }
            }

            if (taxonomyId > 0L) {
                val foundTaxonomy = offRepository.getTaxonomyById(taxonomyId)
                if (foundTaxonomy != null) {
                    _taxonomy.value = foundTaxonomy
                    return@launch
                }
            }

            if (!cleanName.isNullOrBlank()) {
                val exactMatch = offRepository.getTaxonomyByExactName(cleanName)
                if (exactMatch != null) {
                    _taxonomy.value = exactMatch
                    return@launch
                }

                val matches = offRepository.searchTaxonomy(cleanName)
                if (matches.isNotEmpty()) {
                    _taxonomy.value = matches.first()
                } else {
                    _taxonomy.value = OpenFoodFactsTaxonomy(cleanName = cleanName, category = category)
                }
            }
        } catch (_: Exception) {
            _product.value = null
            _taxonomy.value = null
        } finally {
            _isLoading.value = false
        }
    }

    fun importToPantry(defaultLocationId: Long = 2L) = viewModelScope.launch {
        if (ingredientRepository == null) return@launch
        val currentProd = _product.value
        val currentTax = _taxonomy.value

        val ingredientName = currentProd?.productName
            ?: currentTax?.cleanName?.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
            ?: return@launch

        val ingredientCat = currentProd?.brand ?: currentTax?.category ?: "Épicerie"

        try {
            val newIngredient = Ingredient(
                name = ingredientName,
                category = ingredientCat,
                locationId = defaultLocationId,
                quantity = 0.0,
                unit = "unité",
                description = currentProd?.ingredientsText ?: currentTax?.synonyms
            )
            ingredientRepository.insert(newIngredient)
            _importStatus.value = "Ingrédient '$ingredientName' ajouté à votre Garde-Manger !"
        } catch (_: Exception) {
            _importStatus.value = "Erreur lors de l'ajout de l'ingrédient"
        }
    }

    fun clearImportStatus() {
        _importStatus.value = null
    }
}
