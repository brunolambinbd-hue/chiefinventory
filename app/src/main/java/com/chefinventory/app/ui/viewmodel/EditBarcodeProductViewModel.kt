package com.chefinventory.app.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chefinventory.app.model.OpenFoodFactsProduct
import com.chefinventory.app.repository.OpenFoodFactsRepository
import kotlinx.coroutines.launch

/**
 * ViewModel dédié à la création, au chargement et à la sauvegarde d'un Produit Code-Barres Open Food Facts (Niveau 2).
 */
class EditBarcodeProductViewModel(
    private val offRepository: OpenFoodFactsRepository
) : ViewModel() {

    private val _product = MutableLiveData<OpenFoodFactsProduct?>()
    val product: LiveData<OpenFoodFactsProduct?> = _product

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _saveStatus = MutableLiveData<String?>()
    val saveStatus: LiveData<String?> = _saveStatus

    fun loadProduct(barcode: String?) = viewModelScope.launch {
        if (barcode.isNullOrBlank()) return@launch
        _isLoading.value = true
        try {
            _product.value = offRepository.getProductByBarcode(barcode)
        } catch (_: Exception) {
            _product.value = null
        } finally {
            _isLoading.value = false
        }
    }

    fun saveProduct(
        barcode: String,
        productName: String?,
        brand: String?,
        quantityStr: String?,
        nutriScore: String?,
        imageUrl: String?,
        ingredientsText: String?
    ) = viewModelScope.launch {
        val cleanBarcode = barcode.trim()
        if (cleanBarcode.isBlank()) {
            _saveStatus.value = "Erreur : Le code-barres EAN13 est obligatoire"
            return@launch
        }

        _isLoading.value = true
        try {
            val newProduct = OpenFoodFactsProduct(
                barcode = cleanBarcode,
                productName = productName?.trim()?.ifBlank { null },
                brand = brand?.trim()?.ifBlank { null },
                quantityStr = quantityStr?.trim()?.ifBlank { null },
                nutriScore = nutriScore?.trim()?.uppercase()?.ifBlank { null },
                imageUrl = imageUrl?.trim()?.ifBlank { null },
                ingredientsText = ingredientsText?.trim()?.ifBlank { null },
                timestampAdded = System.currentTimeMillis()
            )
            offRepository.saveProductLocally(newProduct)
            _saveStatus.value = "Produit '$cleanBarcode' enregistré avec succès !"
        } catch (_: Exception) {
            _saveStatus.value = "Erreur lors de l'enregistrement du produit code-barres"
        } finally {
            _isLoading.value = false
        }
    }

    fun clearSaveStatus() {
        _saveStatus.value = null
    }
}
