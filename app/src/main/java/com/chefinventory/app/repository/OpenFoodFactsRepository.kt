package com.chefinventory.app.repository

import com.chefinventory.app.dao.OpenFoodFactsDao
import com.chefinventory.app.model.OpenFoodFactsProduct
import com.chefinventory.app.model.OpenFoodFactsTaxonomy
import com.chefinventory.app.network.OpenFoodFactsApiClient
import com.parabd.lib.traceability.utils.LogManager

/**
 * Dépôt unifié de gestion des produits et de la taxonomie Open Food Facts (Niveau 2).
 *
 * Responsabilités :
 * - Gérer la stratégie de cache Offline-First (Recherche locale Room DB -> Fallback API OFF v2 -> Cache local).
 * - Fournir la recherche d'autocomplétion dans la taxonomie française d'ingrédients.
 */
@Suppress("FoldInitializerAndIfNull", "kotlin:S6529")
class OpenFoodFactsRepository(
    private val openFoodFactsDao: OpenFoodFactsDao,
    private val apiClient: OpenFoodFactsApiClient = OpenFoodFactsApiClient
) {

    private companion object {
        private const val TAG = "OFFRepository"
    }

    /**
     * Récupère un produit par son code-barres [barcode] en appliquant la stratégie Offline-First :
     * 1. Cherche d'abord dans la base de données Room locale.
     * 2. Si non trouvé localement, interroge l'API REST Open Food Facts v2.
     * 3. Si le produit est trouvé en ligne, l'enregistre automatiquement dans la base locale pour les futurs scans.
     *
     * @param barcode Le code-barres EAN13 / UPC du produit.
     * @return L'objet [OpenFoodFactsProduct] si trouvé localement ou en ligne, `null` sinon.
     */
    suspend fun getProductByBarcode(barcode: String): OpenFoodFactsProduct? {
        val cleanBarcode = barcode.trim()
        if (cleanBarcode.isBlank()) return null

        // 1. Recherche dans la base de données Room locale (Hit Cache)
        val localProduct = openFoodFactsDao.getProductByBarcode(cleanBarcode)
        if (localProduct != null) {
            LogManager.d(TAG, "Produit trouvé dans le cache Room local pour barcode '$cleanBarcode'")
            return localProduct
        }

        // 2. Interrogation de l'API REST Open Food Facts v2 (Miss Cache)
        LogManager.d(TAG, "Produit non trouvé en local. Interrogation de l'API Open Food Facts pour barcode '$cleanBarcode'")
        val remoteProduct = apiClient.fetchProductByBarcode(cleanBarcode)

        // 3. Mise en cache locale si le produit est trouvé en ligne
        if (remoteProduct != null) {
            try {
                openFoodFactsDao.insertProduct(remoteProduct)
                LogManager.d(TAG, "Produit en ligne enregistré avec succès dans Room local pour barcode '$cleanBarcode'")
            } catch (e: Exception) {
                LogManager.e(TAG, "Erreur lors de la mise en cache locale du produit '$cleanBarcode'", e)
            }
        }

        return remoteProduct
    }

    /**
     * Enregistre un produit manuellement dans la base de données Room locale.
     *
     * @param product Le produit à sauvegarder.
     */
    suspend fun saveProductLocally(product: OpenFoodFactsProduct) {
        if (product.barcode.isNotBlank()) {
            openFoodFactsDao.insertProduct(product)
            LogManager.d(TAG, "Produit enregistré manuellement dans Room local pour barcode '${product.barcode}'")
        }
    }

    /**
     * Effectue une recherche d'autocomplétion d'ingrédients dans la taxonomie française d'Open Food Facts.
     *
     * @param query Le terme de recherche saisi par l'utilisateur.
     * @return La liste des ingrédients correspondants.
     */
    suspend fun searchTaxonomy(query: String): List<OpenFoodFactsTaxonomy> {
        val cleanQuery = query.trim()
        if (cleanQuery.isBlank()) return emptyList()
        return openFoodFactsDao.searchTaxonomy("%$cleanQuery%")
    }
}
