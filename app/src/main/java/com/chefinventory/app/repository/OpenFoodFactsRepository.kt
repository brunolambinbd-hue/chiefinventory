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
 * - Fournir la recherche d'autocomplétion et la pagination dans la taxonomie française d'ingrédients.
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
     * Récupère un ingrédient de taxonomie par son identifiant unique [id].
     */
    suspend fun getTaxonomyById(id: Long): OpenFoodFactsTaxonomy? {
        if (id <= 0) return null
        return openFoodFactsDao.getTaxonomyById(id)
    }

    /**
     * Récupère un ingrédient de taxonomie par son nom exact [cleanName].
     */
    suspend fun getTaxonomyByExactName(cleanName: String): OpenFoodFactsTaxonomy? {
        val name = cleanName.trim()
        if (name.isBlank()) return null
        return openFoodFactsDao.getTaxonomyByExactName(name)
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
        val formattedQuery = if (cleanQuery.endsWith("%") || cleanQuery.startsWith("%")) cleanQuery else "%$cleanQuery%"
        return openFoodFactsDao.searchTaxonomy(formattedQuery)
    }

    /**
     * Récupère une page d'ingrédients de la taxonomie Open Food Facts ordonnée alphabétiquement.
     *
     * @param limit Le nombre maximum d'éléments par page (par défaut 50).
     * @param offset L'index d'origine de pagination.
     * @return La liste paginée d'ingrédients.
     */
    suspend fun getTaxonomyPaged(limit: Int = 50, offset: Int = 0): List<OpenFoodFactsTaxonomy> {
        val safeLimit = maxOf(1, limit)
        val safeOffset = maxOf(0, offset)
        return openFoodFactsDao.getTaxonomyPaged(safeLimit, safeOffset)
    }

    /**
     * Recherche de manière paginée dans la taxonomie par nom ou catégorie.
     *
     * @param query Le terme de recherche.
     * @param limit Le nombre maximum d'éléments par page.
     * @param offset L'index d'origine de pagination.
     * @return La liste paginée des ingrédients correspondants.
     */
    suspend fun searchTaxonomyPaged(query: String, limit: Int = 50, offset: Int = 0): List<OpenFoodFactsTaxonomy> {
        val cleanQuery = query.trim()
        if (cleanQuery.isBlank()) return getTaxonomyPaged(limit, offset)
        val safeLimit = maxOf(1, limit)
        val safeOffset = maxOf(0, offset)
        val formattedQuery = if (cleanQuery.endsWith("%") || cleanQuery.startsWith("%")) cleanQuery else "%$cleanQuery%"
        return openFoodFactsDao.searchTaxonomyPaged(formattedQuery, safeLimit, safeOffset)
    }

    /**
     * Recherche filtrée par code ID numérique de catégorie (0: Tous, 1: Légumes, 2: Crémerie, 3: Huiles...).
     */
    suspend fun getTaxonomyByCodePaged(
        categoryCode: Int = 0,
        query: String? = null,
        limit: Int = 200,
        offset: Int = 0
    ): List<OpenFoodFactsTaxonomy> {
        val cleanQuery = query?.trim()?.ifBlank { null }
        val formattedQuery = cleanQuery?.let { if (it.endsWith("%") || it.startsWith("%")) it else "%$cleanQuery%" }

        val safeLimit = maxOf(1, limit)
        val safeOffset = maxOf(0, offset)

        val results = openFoodFactsDao.getTaxonomyByCodePaged(categoryCode, formattedQuery, safeLimit, safeOffset)

        LogManager.d(TAG, "🔎 RECHERCHE SQL ROOM PAR CODE : categoryCode=$categoryCode, query='$cleanQuery' ➔ ${results.size} résultats trouvés")
        return results
    }

    /**
     * Recherche filtrée par catégorie et motif de recherche dans la base Room locale.
     *
     * @param category La catégorie optionnelle (ex: "Légumes", "Fruits", "Crémerie").
     * @param query Le motif optionnel de recherche ou de lettre initiale.
     * @param limit Le nombre maximum d'éléments retournés.
     * @param offset L'offset de pagination.
     * @return La liste filtrée des ingrédients de la taxonomie.
     */
    suspend fun getFilteredTaxonomyPaged(
        category: String? = null,
        query: String? = null,
        limit: Int = 200,
        offset: Int = 0
    ): List<OpenFoodFactsTaxonomy> {
        val cleanCat = category?.trim()?.ifBlank { null }
        val cleanQuery = query?.trim()?.ifBlank { null }

        val formattedCat = cleanCat?.let { "%$it%" }
        val formattedCatNoAccent = cleanCat?.let { "%${it.replace("é", "_").replace("É", "_").replace("è", "_").replace("ê", "_")}%" }
        val formattedQuery = cleanQuery?.let { if (it.endsWith("%") || it.startsWith("%")) it else "%$it%" }

        val safeLimit = maxOf(1, limit)
        val safeOffset = maxOf(0, offset)

        val results = openFoodFactsDao.getFilteredTaxonomyPaged(formattedCat, formattedCatNoAccent, formattedQuery, safeLimit, safeOffset)

        LogManager.d(TAG, "🔎 RECHERCHE SQL ROOM : category='$cleanCat', query='$cleanQuery' ➔ ${results.size} résultats trouvés dans la BDD")
        if (results.isNotEmpty()) {
            val preview = results.take(5).joinToString(" | ") { "'${it.cleanName}' [Cat:${it.category}]" }
            LogManager.d(TAG, "   └─ Aperçu des 5 premiers : $preview")
        }

        return results
    }

    /**
     * Récupère la liste distincte de toutes les catégories alimentaires présentes dans la taxonomie locale.
     *
     * @return La liste des catégories triées.
     */
    suspend fun getAllCategories(): List<String> {
        return openFoodFactsDao.getAllCategories()
    }
}
