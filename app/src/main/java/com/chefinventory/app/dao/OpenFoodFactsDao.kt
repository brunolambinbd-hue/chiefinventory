package com.chefinventory.app.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.chefinventory.app.model.OpenFoodFactsProduct
import com.chefinventory.app.model.OpenFoodFactsTaxonomy

/**
 * Interface DAO pour la gestion des produits et de la taxonomie Open Food Facts (Niveau 2).
 */
@Dao
interface OpenFoodFactsDao {

    @Query("SELECT * FROM openfoodfacts_products WHERE barcode = :barcode LIMIT 1")
    suspend fun getProductByBarcode(barcode: String): OpenFoodFactsProduct?

    @Query("SELECT * FROM openfoodfacts_taxonomy WHERE id = :id LIMIT 1")
    suspend fun getTaxonomyById(id: Long): OpenFoodFactsTaxonomy?

    @Query("SELECT * FROM openfoodfacts_taxonomy WHERE LOWER(cleanName) = LOWER(:cleanName) LIMIT 1")
    suspend fun getTaxonomyByExactName(cleanName: String): OpenFoodFactsTaxonomy?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: OpenFoodFactsProduct)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllProducts(products: List<OpenFoodFactsProduct>)

    @Query("SELECT * FROM openfoodfacts_taxonomy WHERE LOWER(cleanName) LIKE LOWER(:query) ORDER BY cleanName LIMIT 50")
    suspend fun searchTaxonomy(query: String): List<OpenFoodFactsTaxonomy>

    @Query("SELECT * FROM openfoodfacts_taxonomy ORDER BY cleanName LIMIT :limit OFFSET :offset")
    suspend fun getTaxonomyPaged(limit: Int, offset: Int): List<OpenFoodFactsTaxonomy>

    @Query("SELECT * FROM openfoodfacts_taxonomy WHERE cleanName LIKE :query OR category LIKE :query ORDER BY cleanName LIMIT :limit OFFSET :offset")
    suspend fun searchTaxonomyPaged(query: String, limit: Int, offset: Int): List<OpenFoodFactsTaxonomy>

    @Query("""
        SELECT * FROM openfoodfacts_taxonomy 
        WHERE (:categoryCode = 0 OR categoryCode = :categoryCode) 
          AND (:query IS NULL OR :query = '' OR LOWER(cleanName) LIKE LOWER(:query) OR LOWER(synonyms) LIKE LOWER(:query)) 
        ORDER BY cleanName 
        LIMIT :limit OFFSET :offset
    """)
    suspend fun getTaxonomyByCodePaged(categoryCode: Int, query: String?, limit: Int, offset: Int): List<OpenFoodFactsTaxonomy>

    @Query("""
        SELECT * FROM openfoodfacts_taxonomy 
        WHERE (:category IS NULL OR :category = '' OR category = :category OR category LIKE :categoryPattern) 
          AND (:query IS NULL OR :query = '' OR LOWER(cleanName) LIKE LOWER(:query) OR LOWER(synonyms) LIKE LOWER(:query)) 
        ORDER BY cleanName 
        LIMIT :limit OFFSET :offset
    """)
    suspend fun getFilteredTaxonomyPaged(category: String?, categoryPattern: String?, query: String?, limit: Int, offset: Int): List<OpenFoodFactsTaxonomy>

    @Query("SELECT DISTINCT category FROM openfoodfacts_taxonomy WHERE category IS NOT NULL AND category != '' ORDER BY category")
    suspend fun getAllCategories(): List<String>

    @Query("SELECT COUNT(*) FROM openfoodfacts_taxonomy WHERE synonyms IS NOT NULL")
    suspend fun getSynonymsCount(): Int

    @Query("SELECT COUNT(*) FROM openfoodfacts_taxonomy WHERE categoryCode = 3 OR category LIKE '%Huiles%'")
    suspend fun getHuilesGraissesCount(): Int

    @Query("SELECT COUNT(*) FROM openfoodfacts_taxonomy WHERE averageWeightStr IS NOT NULL")
    suspend fun getAverageWeightCount(): Int

    @Query("SELECT COUNT(*) FROM openfoodfacts_taxonomy WHERE ciqualName IS NOT NULL")
    suspend fun getCiqualTaxonomyCount(): Int

    @Query("SELECT COUNT(*) FROM openfoodfacts_taxonomy WHERE (cleanName LIKE '%épinard%' OR cleanName LIKE '%epinard%') AND (category LIKE '%L_gumes%' OR category IS NOT NULL)")
    suspend fun getEpinardsLegumesCount(): Int

    @Query("SELECT COUNT(DISTINCT category) FROM openfoodfacts_taxonomy")
    suspend fun getDistinctCategoriesCount(): Int

    @Query("SELECT COUNT(*) FROM openfoodfacts_taxonomy WHERE category = 'Épicerie' OR category = 'Alimentation' OR category IS NULL")
    suspend fun getEpicerieTaxonomyCount(): Int

    @Query("SELECT COUNT(*) FROM openfoodfacts_taxonomy WHERE category = 'Alimentation' OR category IS NULL")
    suspend fun getUncategorizedTaxonomyCount(): Int

    @Query("DELETE FROM openfoodfacts_taxonomy")
    suspend fun clearTaxonomy()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTaxonomy(taxonomy: OpenFoodFactsTaxonomy): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllTaxonomy(taxonomies: List<OpenFoodFactsTaxonomy>)

    @Query("SELECT * FROM openfoodfacts_taxonomy")
    suspend fun getAllTaxonomyItems(): List<OpenFoodFactsTaxonomy>

    @Query("SELECT COUNT(*) FROM openfoodfacts_taxonomy")
    suspend fun getTaxonomyCount(): Int

    @Query("SELECT COUNT(*) FROM openfoodfacts_products")
    suspend fun getProductsCount(): Int
}
