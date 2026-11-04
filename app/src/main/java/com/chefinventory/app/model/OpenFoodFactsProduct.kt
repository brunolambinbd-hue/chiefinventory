package com.chefinventory.app.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entité Room représentant un produit alimentaire scanné ou récupéré d'Open Food Facts (Niveau 2).
 *
 * @property barcode Le code-barres EAN13 / UPC unique du produit (Clé Primaire).
 * @property productName Le nom commercial du produit.
 * @property brand La marque ou l'enseigne du produit.
 * @property quantityStr La contenance ou le grammage net.
 * @property nutriScore Le Nutri-Score (A, B, C, D, E).
 * @property imageUrl L'URL de l'image d'illustration du produit.
 * @property ingredientsText La liste brute des ingrédients.
 * @property timestampAdded La date de mise en cache locale en millisecondes.
 */
@Entity(tableName = "openfoodfacts_products")
data class OpenFoodFactsProduct(
    @PrimaryKey
    val barcode: String,
    val productName: String? = null,
    val brand: String? = null,
    val quantityStr: String? = null,
    val nutriScore: String? = null,
    val imageUrl: String? = null,
    val ingredientsText: String? = null,
    val timestampAdded: Long = System.currentTimeMillis()
)
