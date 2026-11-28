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
                val cleanName = optCleanString(obj, "cleanName")
                if (cleanName.isNotBlank()) {
                    val rawCategory = optCleanString(obj, "category")
                    val rawCategoryCode = optInt(obj, "categoryCode", 10)
                    val rawSynonyms = optCleanString(obj, "synonyms")
                    val rawWeight = optCleanString(obj, "averageWeightStr")
                    val rawSeason = optCleanString(obj, "seasonStr")
                    val rawCarbon = optCleanString(obj, "carbonFootprintStr")
                    val rawCiqual = optCleanString(obj, "ciqualName")
                    val rawEcobalyse = optCleanString(obj, "ecobalyseCategory")
                    val rawParents = optCleanString(obj, "parentsStr")
                    val rawChildren = optCleanString(obj, "childrenStr")

                    list.add(
                        OpenFoodFactsTaxonomy(
                            cleanName = cleanName,
                            category = rawCategory.ifBlank { null },
                            categoryCode = rawCategoryCode,
                            synonyms = rawSynonyms.ifBlank { null },
                            averageWeightStr = rawWeight.ifBlank { null },
                            seasonStr = rawSeason.ifBlank { null },
                            carbonFootprintStr = rawCarbon.ifBlank { null },
                            ciqualName = rawCiqual.ifBlank { null },
                            ecobalyseCategory = rawEcobalyse.ifBlank { null },
                            parentsStr = rawParents.ifBlank { null },
                            childrenStr = rawChildren.ifBlank { null }
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

    private fun optCleanString(obj: org.json.JSONObject, key: String): String {
        if (!obj.has(key)) {
            return ""
        } else if (obj.isNull(key)) {
            return ""
        } else {
            return obj.optString(key, "").trim()
        }
    }

    private fun optInt(obj: org.json.JSONObject, key: String, defaultVal: Int): Int {
        if (!obj.has(key)) {
            return defaultVal
        } else if (obj.isNull(key)) {
            return defaultVal
        } else {
            return obj.optInt(key, defaultVal)
        }
    }

    /**
     * Alimente la table `openfoodfacts_taxonomy` à partir de l'asset `off_french_taxonomy.json` si la table est vide ou contient des données non mises à jour.
     *
     * @param context Le contexte Android.
     * @param database L'instance de la base de données Room [AppDatabase].
     * @return Le nombre d'ingrédients insérés dans la base de données.
     */
    suspend fun seedTaxonomyIfEmpty(context: Context, database: AppDatabase): Int {
        val dao = database.openFoodFactsDao()
        val currentCount = dao.getTaxonomyCount()
        val synonymsCount = dao.getSynonymsCount()

        if (currentCount > 0 && synonymsCount >= 100) {
            LogManager.d(TAG, "Taxonomie Open Food Facts déjà alimentée avec synonymes (count=$currentCount, synonymsCount=$synonymsCount). Seeding ignoré.")
            return 0
        } else {
            LogManager.d(TAG, "Ré-initialisation de la taxonomie pour chargement des synonymes/pluriels (count=$currentCount, synonymsCount=$synonymsCount)")
            dao.clearTaxonomy()
        }

        return try {
            val items = context.assets.open(ASSET_FILE_NAME).use { inputStream ->
                val jsonString = inputStream.bufferedReader().readText()
                parseTaxonomyJson(jsonString)
            }
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
