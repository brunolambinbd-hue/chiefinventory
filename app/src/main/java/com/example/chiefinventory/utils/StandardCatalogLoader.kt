package com.example.chiefinventory.utils

import android.content.res.Resources
import com.example.chiefinventory.R
import com.example.chiefinventory.model.StandardCatalogItem

/**
 * Chargeur du catalogue d'ingrédients types du garde-manger depuis les ressources XML Android.
 * Provenance : Open Food Facts & ANSES CIQUAL (Adaptation Franco-Belge).
 *
 * @see REQ-4.1.1.1 Garde-manger optimiste 1-tap.
 * @see REQ-4.3.1.1 Catalogue XML au format à 6 champs (SeuilMin non nul).
 */
object StandardCatalogLoader {

    /**
     * Charge l'ensemble des 150 à 200 ingrédients types depuis [R.array] (`standard_catalog_ingredients.xml`).
     *
     * @param res Les ressources de l'application Android.
     * @return La liste complète des objets [StandardCatalogItem] prêts pour la sélection 1-Tap.
     */
    fun loadFromResources(res: Resources): List<StandardCatalogItem> {
        val items = mutableListOf<StandardCatalogItem>()

        val arrayMap = mapOf(
            R.array.catalog_cremerie to "Frigo",
            R.array.catalog_epicerie to "Placard",
            R.array.catalog_epices_condiments to "Placard",
            R.array.catalog_legumes_fruits to "Placard",
            R.array.catalog_surgeles to "Surgélateur",
            R.array.catalog_boissons_cave to "Cave"
        )

        for ((arrayId, defaultLocation) in arrayMap) {
            val array = try { res.getStringArray(arrayId) } catch (_: Exception) { emptyArray() }
            for (raw in array) {
                val parsed = parseRawCatalogLine(raw, defaultLocation)
                if (parsed != null) {
                    items.add(parsed)
                }
            }
        }

        return items
    }

    /**
     * Parse une ligne brute du XML au format `Nom|QuantitéDéfaut|UnitéDéfaut|SousCatégorie`.
     *
     * @param raw Ligne brute du XML.
     * @param defaultLocation Emplacement par défaut selon l'array XML.
     * @return L'objet [StandardCatalogItem] ou null si ligne invalide.
     */
    fun parseRawCatalogLine(raw: String, defaultLocation: String = "Placard"): StandardCatalogItem? {
        val trimmed = raw.trim()
        if (trimmed.isBlank()) return null

        val parts = trimmed.split("|")
        if (parts.isEmpty()) return null

        val name = parts[0].trim()
        if (name.isBlank()) return null

        val qty = if (parts.size > 1) parts[1].trim().toDoubleOrNull() ?: 1.0 else 1.0
        val unit = if (parts.size > 2) parts[2].trim().ifBlank { null } else null
        val category = if (parts.size > 3) parts[3].trim().ifBlank { "Épicerie" } else "Épicerie"
        val policyRaw = if (parts.size > 4) parts[4].trim().uppercase() else "PERMANENT"
        val stockPolicy = if (policyRaw == "ON_DEMAND") "ON_DEMAND" else "PERMANENT"
        val thresholdVal = if (parts.size > 5) parts[5].trim().toDoubleOrNull() else null
        val minThreshold = thresholdVal ?: (qty / 2.0)
        val packVal = if (parts.size > 6) parts[6].trim().toDoubleOrNull() else null
        val packSize = packVal ?: 1.0

        return StandardCatalogItem(
            name = name,
            defaultQuantity = qty,
            defaultUnit = unit,
            category = category,
            defaultLocationName = defaultLocation,
            stockPolicy = stockPolicy,
            minThreshold = minThreshold,
            packSize = packSize
        )
    }
}
