package com.chefinventory.app.utils

import android.content.Context
import com.chefinventory.app.R

/**
 * Modèle représentant un problème d'intégrité détecté sur une ressource.
 *
 * @property domain Le domaine fonctionnel concerné (ex: "OCR", "Garde-Manger").
 * @property resourceName Le nom de la ressource XML (ex: "step_start_verbs").
 * @property description La description détaillée de l'anomalie.
 * @property isCritical Vrai si l'anomalie bloque le fonctionnement de l'OCR/Scanner.
 */
data class ResourceIssue(
    val domain: String,
    val resourceName: String,
    val description: String,
    val isCritical: Boolean = true
)

/**
 * Rapport complet du bilan de santé des ressources de l'application.
 *
 * @property isHealthy Vrai si aucune anomalie critique n'a été détectée.
 * @property issues La liste de toutes les anomalies relevées.
 * @property checkedCount Le nombre total de contrôles effectués.
 */
data class DiagnosticReport(
    val isHealthy: Boolean,
    val issues: List<ResourceIssue>,
    val checkedCount: Int
)

/**
 * Détecteur d'intégrité et bilan de santé des ressources XML au démarrage de l'application.
 */
object ResourceHealthChecker {

    /**
     * Effectue un contrôle complet de présence et de non-vacuité des dictionnaires XML nécessaires au scan OCR.
     *
     * @param context Le contexte Android de l'application.
     * @return Le [DiagnosticReport] complet.
     */
    fun check(context: Context): DiagnosticReport {
        val res = context.resources
        val issues = mutableListOf<ResourceIssue>()
        var checked = 0

        fun checkArray(domain: String, name: String, id: Int, minExpected: Int = 1) {
            checked++
            try {
                val array = res.getStringArray(id)
                if (array.isEmpty() || array.size < minExpected) {
                    issues.add(
                        ResourceIssue(
                            domain = domain,
                            resourceName = name,
                            description = "Tableau XML vide ou incomplet (${array.size}/$minExpected items attendus)"
                        )
                    )
                }
            } catch (_: Exception) {
                issues.add(
                    ResourceIssue(
                        domain = domain,
                        resourceName = name,
                        description = "Ressource R.array.$name introuvable ou inaccessible"
                    )
                )
            }
        }

        fun checkString(domain: String, name: String, id: Int) {
            checked++
            try {
                val str = res.getString(id)
                if (str.isBlank()) {
                    issues.add(
                        ResourceIssue(
                            domain = domain,
                            resourceName = name,
                            description = "Chaîne R.string.$name vide"
                        )
                    )
                }
            } catch (_: Exception) {
                issues.add(
                    ResourceIssue(
                        domain = domain,
                        resourceName = name,
                        description = "Ressource R.string.$name introuvable"
                    )
                )
            }
        }

        // 1. Contrôles du Domaine OCR (Dictionnaires & Verbes)
        val commonIngrs = try { res.getStringArray(R.array.common_ingredients_no_qty) } catch (_: Exception) { null }
        val containsThymRomarin = commonIngrs?.contains("du thym et du romarin frais") == true
        com.parabd.lib.traceability.utils.LogManager.d("ResourceHealthChecker", "RESOURCE DIAGNOSTIC: common_ingredients_no_qty taille = ${commonIngrs?.size ?: 0}, contient 'du thym et du romarin frais': $containsThymRomarin")

        checkArray("OCR - Verbes", "step_start_verbs", R.array.step_start_verbs, minExpected = 5)
        checkArray("OCR - Verbes", "step_secondary_verbs", R.array.step_secondary_verbs, minExpected = 10)
        checkArray("OCR - Unités", "culinary_measurement_units", R.array.culinary_measurement_units, minExpected = 10)
        checkArray("OCR - Ingrédients", "common_ingredients_no_qty", R.array.common_ingredients_no_qty, minExpected = 10)
        checkArray("OCR - Connecteurs", "ingredient_connectors", R.array.ingredient_connectors, minExpected = 5)
        checkArray("OCR - Réparations", "ocr_spelling_repairs", R.array.ocr_spelling_repairs, minExpected = 5)
        checkArray("OCR - Diacritiques", "ocr_diacritic_repairs", R.array.ocr_diacritic_repairs, minExpected = 5)
        checkArray("OCR - Durées", "duration_units", R.array.duration_units, minExpected = 3)
        checkArray("OCR - Titres", "title_keywords", R.array.title_keywords, minExpected = 5)
        checkArray("OCR - Ponctuation", "sentence_ending_punctuation", R.array.sentence_ending_punctuation, minExpected = 3)

        // 2. Contrôles des Cibles Canoniques
        checkString("OCR - En-têtes", "ocr1_canonical_ingredients_header", R.string.ocr1_canonical_ingredients_header)
        checkString("OCR - En-têtes", "ocr1_canonical_instructions_header", R.string.ocr1_canonical_instructions_header)

        val isHealthy = issues.none { it.isCritical }
        return DiagnosticReport(
            isHealthy = isHealthy,
            issues = issues,
            checkedCount = checked
        )
    }
}
