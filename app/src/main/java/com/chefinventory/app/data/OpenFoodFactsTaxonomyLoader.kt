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
                    val rawCategoryCode = if (obj.has("categoryCode") && !obj.isNull("categoryCode")) obj.getInt("categoryCode") else 10
                    val rawSynonyms = if (obj.has("synonyms") && !obj.isNull("synonyms")) obj.getString("synonyms").trim() else ""
                    val rawWeight = if (obj.has("averageWeightStr") && !obj.isNull("averageWeightStr")) obj.getString("averageWeightStr").trim() else ""
                    val rawSeason = if (obj.has("seasonStr") && !obj.isNull("seasonStr")) obj.getString("seasonStr").trim() else ""
                    val rawCarbon = if (obj.has("carbonFootprintStr") && !obj.isNull("carbonFootprintStr")) obj.getString("carbonFootprintStr").trim() else ""
                    val rawCiqual = if (obj.has("ciqualName") && !obj.isNull("ciqualName")) obj.getString("ciqualName").trim() else ""
                    val rawEcobalyse = if (obj.has("ecobalyseCategory") && !obj.isNull("ecobalyseCategory")) obj.getString("ecobalyseCategory").trim() else ""
                    val rawParents = if (obj.has("parentsStr") && !obj.isNull("parentsStr")) obj.getString("parentsStr").trim() else ""
                    val rawChildren = if (obj.has("childrenStr") && !obj.isNull("childrenStr")) obj.getString("childrenStr").trim() else ""

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
        }

        if (synonymsCount < 100 || currentCount == 0) {
            LogManager.d(TAG, "Ré-initialisation de la taxonomie pour chargement des synonymes/pluriels (count=$currentCount, synonymsCount=$synonymsCount)")
            dao.clearTaxonomy()
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
