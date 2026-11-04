package com.chefinventory.app.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entité Room représentant un ingrédient de la taxonomie française d'Open Food Facts (Niveau 2).
 *
 * @property id L'identifiant unique de l'ingrédient (Clé Primaire Auto-générée).
 * @property cleanName Le nom d'ingrédient nettoyé en français (ex: "haricots verts").
 * @property category La famille alimentaire de l'ingrédient (ex: "Fruits & Légumes").
 * @property categoryCode Le code ID numérique de catégorie (1: Légumes, 2: Crémerie, 3: Huiles, 4: Viandes, 5: Volailles, 6: Poissons, 7: Épices, 8: Céréales, 9: Boissons, 10: Épicerie).
 * @property synonyms Les variantes et synonymes de l'ingrédient séparés par une virgule.
 * @property averageWeightStr Le poids unitaire moyen (ex: "250 g / unité").
 * @property seasonStr Les mois de saisonnalité en France (ex: "De Mai à Octobre").
 * @property carbonFootprintStr L'empreinte carbone environnementale (ex: "0.7 kg CO2 / kg").
 * @property ciqualName La dénomination nutritionnelle officielle de l'Anses / Ciqual.
 * @property ecobalyseCategory La catégorie Écobalyse brute Open Food Facts.
 * @property parentsStr Le parent ontologique mondial dans Open Food Facts (ex: "en:fruit-vegetable").
 * @property childrenStr Les sous-déclinaisons dérivées d'ingrédients (ex: "en:dried-courgette, fr:courgettes-en-rondelles").
 */
@Entity(tableName = "openfoodfacts_taxonomy")
data class OpenFoodFactsTaxonomy(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val cleanName: String,
    val category: String? = null,
    val categoryCode: Int = 10,
    val synonyms: String? = null,
    val averageWeightStr: String? = null,
    val seasonStr: String? = null,
    val carbonFootprintStr: String? = null,
    val ciqualName: String? = null,
    val ecobalyseCategory: String? = null,
    val parentsStr: String? = null,
    val childrenStr: String? = null
)
