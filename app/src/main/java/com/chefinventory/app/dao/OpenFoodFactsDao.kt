package com.chefinventory.app.dao

import androidx.lifecycle.LiveData
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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: OpenFoodFactsProduct)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllProducts(products: List<OpenFoodFactsProduct>)

    @Query("SELECT * FROM openfoodfacts_taxonomy WHERE cleanName LIKE :query ORDER BY cleanName LIMIT 50")
    suspend fun searchTaxonomy(query: String): List<OpenFoodFactsTaxonomy>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTaxonomy(taxonomy: OpenFoodFactsTaxonomy): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllTaxonomy(taxonomies: List<OpenFoodFactsTaxonomy>)

    @Query("SELECT COUNT(*) FROM openfoodfacts_taxonomy")
    suspend fun getTaxonomyCount(): Int

    @Query("SELECT COUNT(*) FROM openfoodfacts_products")
    suspend fun getProductsCount(): Int
}
