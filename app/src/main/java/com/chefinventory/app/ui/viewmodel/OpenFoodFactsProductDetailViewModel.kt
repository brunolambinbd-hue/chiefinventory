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
            val cleanBarcode = barcode?.trim()
            if (!cleanBarcode.isNullOrBlank()) {
                val foundProduct = offRepository.getProductByBarcode(cleanBarcode)
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

            val cleanNameTrimmed = cleanName?.trim()
            if (!cleanNameTrimmed.isNullOrBlank()) {
                val exactMatch = offRepository.getTaxonomyByExactName(cleanNameTrimmed)
                if (exactMatch != null) {
                    _taxonomy.value = exactMatch
                    return@launch
                }

                val matches = offRepository.searchTaxonomy(cleanNameTrimmed)
                if (matches.isNotEmpty()) {
                    _taxonomy.value = matches.first()
                } else {
                    _taxonomy.value = OpenFoodFactsTaxonomy(cleanName = cleanNameTrimmed, category = category)
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

        val rawName = currentProd?.productName?.trim().takeUnless { it.isNullOrBlank() }
            ?: currentTax?.cleanName?.trim().takeUnless { it.isNullOrBlank() }
            ?: return@launch

        val ingredientName = rawName.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        val ingredientCat = currentProd?.brand?.trim().takeUnless { it.isNullOrBlank() }
            ?: currentTax?.category?.trim().takeUnless { it.isNullOrBlank() }
            ?: "Épicerie"

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
