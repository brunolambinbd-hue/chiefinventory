package com.chefinventory.app.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entité Room représentant un ingrédient de la taxonomie française d'Open Food Facts (Niveau 2).
 *
 * @property id L'identifiant unique de l'ingrédient (Clé Primaire Auto-générée).
 * @property cleanName Le nom d'ingrédient nettoyé en français (ex: "haricots verts").
 * @property category La famille alimentaire de l'ingrédient (ex: "Légumes").
 * @property synonyms Les variantes et synonymes de l'ingrédient séparés par une virgule.
 */
@Entity(tableName = "openfoodfacts_taxonomy")
data class OpenFoodFactsTaxonomy(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val cleanName: String,
    val category: String? = null,
    val synonyms: String? = null
)
