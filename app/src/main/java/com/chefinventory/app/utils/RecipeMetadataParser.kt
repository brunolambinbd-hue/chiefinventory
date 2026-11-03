package com.chefinventory.app.utils

/**
 * Analyseur et extracteur des méta-données de recettes (Portions, Temps Prép/Cuisson/Repos, Kcal, Difficulté, Valeurs Nutritionnelles).
 *
 * Responsabilités :
 * - Détecter et extraire les blocs de valeurs nutritionnelles (Kcal, Lipides, Protéines, Glucides, Sel, Fibres).
 * - Normaliser les nombres nutritionnels corrompus (*"880"* -> *"8.8g"*, *"97 0"* -> *"9.7g"*, *"LO0"* -> *"1.0g"*).
 * - Déléguer l'analyse séquentielle aux processeurs assistants [RecipeMetadataParser0_Nutrition] à [RecipeMetadataParser3_BlockC_Times].
 */
@Suppress("FoldInitializerAndIfNull", "kotlin:S6529")
object RecipeMetadataParser {

    /**
     * Tente d'extraire les méta-données de nutrition, temps, portions, calories et difficulté de la ligne [line]
     * et alimente l'objet [results].
     *
     * @param line La ligne de texte en cours d'analyse.
     * @param results L'objet mutable [RawSections] alimenté si des méta-données sont trouvées.
     * @return L'objet [MetadataResult] indiquant si la ligne est purement de la métadonnée et la ligne restante.
     */
    fun extractMetadata(line: String, results: RawSections, vocabulary: OcrVocabulary? = null): MetadataResult {
        var workingLine = line.trim()
        val lower = workingLine.lowercase()

        // 1. DÉTECTION ET EXTRACTION DES VALEURS NUTRITIONNELLES
        val nutResult = RecipeMetadataParser0_Nutrition.processNutritionBlock(workingLine, lower, results)
        if (nutResult != null) {
            return nutResult
        }

        // 2. EXTRACTION KCAL, DIFFICULTÉ, PORTIONS
        workingLine = RecipeMetadataParser1_BlockA_ServingsDiff.processServingsAndDifficulty(workingLine, line, results)

        // 2.5 EXTRACTION DU PRIX / BUDGET
        workingLine = RecipeMetadataParser2_BlockB_Price.processPriceBlock(workingLine, vocabulary, results)

        // 3. EXTRACTION DES TEMPS (PRÉPARATION, CUISSON, REPOS)
        workingLine = RecipeMetadataParser3_BlockC_Times.processTimesBlock(workingLine, line, vocabulary, results)

        // 4. NETTOYAGE DES EN-TÊTES ORPHELINS ET DÉTERMINATION DU STATUT PURE METADATA
        val orphanExacts = setOf("temps de", "durée de", "temps", "durée", "temps de préparation", "préparation", "preparation", "préparation :", "preparation :")
        val orphanRegex = OcrRegexes.Metadata.ORPHAN_TIME_HEADER.containsMatchIn(workingLine)
        val orphanExact = lower in orphanExacts
        val isOrphanTimeHeader = orphanRegex || orphanExact

        if (isOrphanTimeHeader) {
            workingLine = workingLine.replace(OcrRegexes.Metadata.ORPHAN_TIME_HEADER, "").trim()
        }

        val isPour = lower == "pour"
        val isMetadataHeader = isOrphanTimeHeader || isPour

        val sectionHeaders = setOf("ingrédients", "ingredients", "préparation", "preparation", "instructions", "consignes", "réalisation", "realisation")
        val isSectionHeader = lower in sectionHeaders
        val isHeaderOrBlank = isMetadataHeader || workingLine.isBlank()
        val isPure = isHeaderOrBlank && !isSectionHeader
        val finalRemainingLine = if (isPure) "" else workingLine

        return MetadataResult(isPure, finalRemainingLine)
    }

    /**
     * Convertit une chaîne de durée sous forme de texte (ex: "1 h 30", "45 min") en nombre de minutes sous forme de texte.
     */
    fun extractMinutes(timeStr: String): String? {
        return RecipeMetadataParser3_BlockC_Times.extractMinutes(timeStr)
    }
}
