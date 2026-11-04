package com.chefinventory.app.data

import android.content.Context
import com.chefinventory.app.model.OpenFoodFactsTaxonomy
import com.parabd.lib.traceability.utils.LogManager
import org.json.JSONArray

/**
 * Chargeur et alimentateur de la taxonomie française d'Open Food Facts dans la base Room locale (Niveau 2).
 */
@Suppress("FoldInitializerAndIfNull", "kotlin:S6529")
object OpenFoodFactsTaxonomyLoader {

    private const val TAG = "OFFTaxonomyLoader"
    private const val ASSET_FILE_NAME = "off_french_taxonomy.json"

    /**
     * Parse une chaîne JSON représentant une liste d'ingrédients de taxonomie Open Food Facts.
     *
     * @param jsonString La chaîne de caractères au format JSON.
     * @return La liste des objets [OpenFoodFactsTaxonomy] analysés.
     */
    internal fun parseTaxonomyJson(jsonString: String): List<OpenFoodFactsTaxonomy> {
        if (jsonString.isBlank()) return emptyList()
        return try {
            val list = mutableListOf<OpenFoodFactsTaxonomy>()
            val jsonArray = JSONArray(jsonString)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val cleanName = obj.optString("cleanName", "").trim()
                if (cleanName.isNotBlank()) {
                    val rawCategory = if (obj.has("category") && !obj.isNull("category")) obj.getString("category").trim() else ""
                    val rawSynonyms = if (obj.has("synonyms") && !obj.isNull("synonyms")) obj.getString("synonyms").trim() else ""
                    list.add(
                        OpenFoodFactsTaxonomy(
                            cleanName = cleanName,
                            category = rawCategory.ifBlank { null },
                            synonyms = rawSynonyms.ifBlank { null }
                        )
                    )
                }
            }
            list
        } catch (e: Exception) {
            LogManager.e(TAG, "Erreur d'analyse JSON de la taxonomie", e)
            emptyList()
        }
    }

    /**
     * Alimente la table `openfoodfacts_taxonomy` à partir de l'asset `off_french_taxonomy.json` si la table est vide.
     *
     * @param context Le contexte Android.
     * @param database L'instance de la base de données Room [AppDatabase].
     * @return Le nombre d'ingrédients insérés dans la base de données.
     */
    suspend fun seedTaxonomyIfEmpty(context: Context, database: AppDatabase): Int {
        val dao = database.openFoodFactsDao()
        val currentCount = dao.getTaxonomyCount()
        if (currentCount > 0) {
            LogManager.d(TAG, "Taxonomie Open Food Facts déjà alimentée (count=$currentCount). Seeding ignoré.")
            return 0
        }

        return try {
            val inputStream = context.assets.open(ASSET_FILE_NAME)
            val jsonString = inputStream.bufferedReader().readText()
            try { inputStream.close() } catch (_: Exception) {}

            val items = parseTaxonomyJson(jsonString)
            if (items.isNotEmpty()) {
                dao.insertAllTaxonomy(items)
                LogManager.d(TAG, "Seeding de la taxonomie Open Food Facts réussi (${items.size} entrées insérées).")
                items.size
            } else {
                0
            }
        } catch (e: Exception) {
            LogManager.e(TAG, "Erreur lors du seeding de la taxonomie Open Food Facts", e)
            0
        }
    }
}
