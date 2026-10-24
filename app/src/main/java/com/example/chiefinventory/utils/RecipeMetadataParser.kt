package com.example.chiefinventory.utils

/**
 * Conteneur du résultat d'extraction de métadonnées.
 *
 * @property isPureMetadata `true` si la ligne contenait uniquement des métadonnées et doit être sautée.
 * @property remainingLine Le texte de la ligne restant après suppression des métadonnées extraites.
 */
data class MetadataResult(
    val isPureMetadata: Boolean,
    val remainingLine: String
)

/**
 * Analyseur et extracteur des méta-données de recettes (Portions, Temps Prép/Cuisson/Repos, Kcal, Difficulté, Valeurs Nutritionnelles).
 *
 * Responsabilités :
 * - Détecter et extraire les blocs de valeurs nutritionnelles (Kcal, Lipides, Protéines, Glucides, Sel, Fibres) même fortement corrompus par l'OCR.
 * - Normaliser les nombres nutritionnels corrompus (*"880"* -> *"8.8g"*, *"97 0"* -> *"9.7g"*, *"LO0"* -> *"1.0g"*).
 * - Détecter et extraire le nombre de portions ou personnes (ex: "pour 4 personnes", "A4PERS").
 * - Détecter et convertir les durées en minutes (ex: "1 h 30 min" -> "90").
 * - Extraire l'apport calorique (ex: "250 kcal") et le niveau de difficulté (ex: "Facile").
 * - Consommer les expressions régulières pré-compilées de [OcrRegexes.Metadata].
 */
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
        val isNutritionHeader = OcrRegexes.Metadata.NUTRITION_HEADER.containsMatchIn(workingLine)
        val fatMatch = OcrRegexes.Metadata.NUTRITION_FAT.find(workingLine)
        val satFatMatch = OcrRegexes.Metadata.NUTRITION_SAT_FAT.find(workingLine)
        val carbsMatch = OcrRegexes.Metadata.NUTRITION_CARBS.find(workingLine)
        val sugarMatch = OcrRegexes.Metadata.NUTRITION_SUGAR.find(workingLine)
        val proteinMatch = OcrRegexes.Metadata.NUTRITION_PROTEIN.find(workingLine)
        val saltMatch = OcrRegexes.Metadata.NUTRITION_SALT.find(workingLine)
        val fiberMatch = OcrRegexes.Metadata.NUTRITION_FIBER.find(workingLine)

        if (isNutritionHeader || fatMatch != null || satFatMatch != null || carbsMatch != null || sugarMatch != null || proteinMatch != null || fiberMatch != null) {
            val kcalMatchInNut = OcrRegexes.Metadata.NUTRITION_KCAL.find(workingLine)
            if (kcalMatchInNut != null && results.detectedKcal == null) {
                results.detectedKcal = kcalMatchInNut.groupValues[1]
            }

            val nutParts = mutableListOf<String>()
            if (results.detectedKcal != null && results.detectedNutritionInfo?.contains("kcal") != true) {
                nutParts.add("${results.detectedKcal} kcal")
            }
            
            fatMatch?.let {
                val fatVal = normalizeNoisyNutritionalValue(it.groupValues[1])
                val satVal = satFatMatch?.let { sat -> " (dont saturés: ${normalizeNoisyNutritionalValue(sat.groupValues[1])}g)" } ?: ""
                nutParts.add("Lipides: ${fatVal}g$satVal")
            }

            if (fatMatch == null && satFatMatch != null) {
                val satVal = normalizeNoisyNutritionalValue(satFatMatch.groupValues[1])
                val existing = results.detectedNutritionInfo
                if (existing != null && existing.contains("Lipides:") && !existing.contains("dont saturés")) {
                    results.detectedNutritionInfo = existing.replace(Regex("Lipides:\\s*(\\d+(?:[.,]\\d+)?g)")) { m ->
                        "Lipides: ${m.groupValues[1]} (dont saturés: ${satVal}g)"
                    }
                } else {
                    nutParts.add("Acides gras saturés: ${satVal}g")
                }
            }

            carbsMatch?.let {
                val carbVal = normalizeNoisyNutritionalValue(it.groupValues[1])
                val sugarVal = sugarMatch?.let { sug -> " (dont sucres: ${normalizeNoisyNutritionalValue(sug.groupValues[1])}g)" } ?: ""
                nutParts.add("Glucides: ${carbVal}g$sugarVal")
            }

            if (carbsMatch == null && sugarMatch != null) {
                val sugVal = normalizeNoisyNutritionalValue(sugarMatch.groupValues[1])
                val existing = results.detectedNutritionInfo
                if (existing != null && existing.contains("Glucides:") && !existing.contains("dont sucres")) {
                    results.detectedNutritionInfo = existing.replace(Regex("Glucides:\\s*(\\d+(?:[.,]\\d+)?g)")) { m ->
                        "Glucides: ${m.groupValues[1]} (dont sucres: ${sugVal}g)"
                    }
                } else {
                    nutParts.add("Sucres: ${sugVal}g")
                }
            }

            proteinMatch?.let { nutParts.add("Protéines: ${normalizeNoisyNutritionalValue(it.groupValues[1])}g") }
            fiberMatch?.let { nutParts.add("Fibres: ${normalizeNoisyNutritionalValue(it.groupValues[1])}g") }
            saltMatch?.let { nutParts.add("Sel: ${normalizeNoisyNutritionalValue(it.groupValues[1])}g") }

            if (nutParts.isNotEmpty()) {
                val refStr = if (lower.contains("100g") || lower.contains("100 g") || lower.contains("1000")) "/100g" else ""
                val newSummary = nutParts.joinToString(", ")
                val existing = results.detectedNutritionInfo
                results.detectedNutritionInfo = when {
                    existing == null -> "Valeurs$refStr : $newSummary"
                    else -> "$existing, $newSummary"
                }
            }
            return MetadataResult(true, "")
        }

        // 2. EXTRACTION KCAL, DIFFICULTÉ, PORTIONS
        val kcalMatch = OcrRegexes.Metadata.KCAL.find(workingLine)
        if (kcalMatch != null) {
            if (results.detectedKcal == null) results.detectedKcal = kcalMatch.groupValues[1]
            workingLine = workingLine.replace(kcalMatch.value, "").trim()
        }

        val diffMatch = OcrRegexes.Metadata.DIFFICULTY.find(workingLine)
        if (diffMatch != null) {
            if (results.detectedDifficulty == null) {
                results.detectedDifficulty = diffMatch.groupValues[1].lowercase().replace("diffcile", "difficile")
            }
            workingLine = workingLine.replace(diffMatch.value, "").trim()
        }

        val sMatch = OcrRegexes.Metadata.SERVINGS.find(workingLine)
        if (sMatch != null) {
            if (results.detectedServings == null) {
                results.detectedServings = sMatch.groupValues.drop(1).firstOrNull { it.isNotEmpty() }
            }
            workingLine = workingLine.replace(sMatch.value, "").trim()
            val hasIngrHeader = line.lowercase().contains("ingrédients") || line.lowercase().contains("ingredients")
            val hasInstrHeader = line.lowercase().contains("préparation") || line.lowercase().contains("instructions")
            if (hasIngrHeader && workingLine.isBlank()) workingLine = "ingrédients"
            if (hasInstrHeader && workingLine.isBlank()) workingLine = "préparation"
        }

        // 2.5 EXTRACTION DU PRIX / BUDGET (ex: "Prix : bon marché", "Bon marché", "Coût moyen")
        val priceCategoriesLow = vocabulary?.priceCategoriesLow?.takeIf { it.isNotEmpty() }
            ?: listOf("très bon marché", "tres bon marche", "bon marché", "bon marche", "économique", "economique", "petit prix", "pas cher")
        val priceCategoriesMedium = vocabulary?.priceCategoriesMedium?.takeIf { it.isNotEmpty() }
            ?: listOf("coût moyen", "cout moyen", "prix moyen", "budget moyen", "moyen", "moyennement cher")
        val priceCategoriesHigh = vocabulary?.priceCategoriesHigh?.takeIf { it.isNotEmpty() }
            ?: listOf("très cher", "tres cher", "assez cher", "cher", "élevé", "eleve", "gastronomique", "coûteux", "couteux")

        val priceTitles = vocabulary?.priceTitleKeywords?.takeIf { it.isNotEmpty() }
            ?: listOf("prix :", "prix", "coût :", "cout :", "coût", "cout", "budget :", "budget")

        val normLine = java.text.Normalizer.normalize(workingLine.lowercase(), java.text.Normalizer.Form.NFC)

        val matchedLow = priceCategoriesLow.firstOrNull { cat ->
            val normCat = java.text.Normalizer.normalize(cat.lowercase(), java.text.Normalizer.Form.NFC)
            OcrRegexes.getSpellingRepairPattern(normCat).matcher(normLine).find() || normLine.contains(normCat)
        }
        val matchedMedium = if (matchedLow == null) priceCategoriesMedium.firstOrNull { cat ->
            val normCat = java.text.Normalizer.normalize(cat.lowercase(), java.text.Normalizer.Form.NFC)
            OcrRegexes.getSpellingRepairPattern(normCat).matcher(normLine).find() || normLine.contains(normCat)
        } else null

        val matchedHigh = if (matchedLow == null && matchedMedium == null) priceCategoriesHigh.firstOrNull { cat ->
            val normCat = java.text.Normalizer.normalize(cat.lowercase(), java.text.Normalizer.Form.NFC)
            OcrRegexes.getSpellingRepairPattern(normCat).matcher(normLine).find() || normLine.contains(normCat)
        } else null

        val matchedCategory = matchedLow ?: matchedMedium ?: matchedHigh

        if (matchedCategory != null) {
            if (results.detectedPriceRating == null) {
                val formattedPrice = when {
                    matchedLow != null -> "Bon marché"
                    matchedMedium != null -> "Coût moyen"
                    else -> "Assez cher"
                }
                results.detectedPriceRating = formattedPrice
            }
            workingLine = OcrRegexes.getSpellingRepairPattern(matchedCategory).matcher(workingLine).replaceAll("").trim()
            priceTitles.forEach { pTitle ->
                workingLine = OcrRegexes.getSpellingRepairPattern(pTitle).matcher(workingLine).replaceAll("").trim()
            }
        }

        // 3. EXTRACTION DES TEMPS (PRÉPARATION, CUISSON, REPOS)
        val actionKeywords = vocabulary?.stepActionKeywords ?: emptyList()
        val isInstructionSentence = OcrInstructionUtils.hasActionVerb(line, actionKeywords) || 
                line.trim().startsWith("•") || 
                Regex("^\\s*\\d{1,2}\\s*[.)\\-]").containsMatchIn(line)

        var foundTime = true
        while (foundTime) {
            val matchComb = OcrRegexes.Metadata.TIME_COMBINED.find(workingLine)
            val matchPre = OcrRegexes.Metadata.TIME_PREFIX.find(workingLine)
            val matchSuf = OcrRegexes.Metadata.TIME_SUFFIX.find(workingLine)
            when {
                matchComb != null -> {
                    results.detectedPrepTime = extractMinutes(matchComb.groupValues[1])
                    results.detectedCookTime = extractMinutes(matchComb.groupValues[2])
                    if (!isInstructionSentence) workingLine = workingLine.replace(matchComb.value, "").trim()
                    else foundTime = false
                }
                matchPre != null -> {
                    val mins = extractMinutes(matchPre.groupValues[2])
                    updateTimeResult(results, matchPre.groupValues[1].lowercase(), mins)
                    if (!isInstructionSentence) workingLine = workingLine.replace(matchPre.value, "").trim()
                    else foundTime = false
                }
                matchSuf != null -> {
                    val mins = extractMinutes(matchSuf.groupValues[1])
                    updateTimeResult(results, matchSuf.groupValues[2].lowercase(), mins)
                    if (!isInstructionSentence) workingLine = workingLine.replace(matchSuf.value, "").trim()
                    else foundTime = false
                }
                else -> {
                    val standaloneTimeMatch = Regex("(?i)^(\\d+\\s*(?:mn|min|minute|h|heure|heures))\\b").find(workingLine)
                    if (standaloneTimeMatch != null && workingLine.length < 12) {
                        val mins = extractMinutes(standaloneTimeMatch.groupValues[1])
                        if (results.detectedPrepTime == null) {
                            results.detectedPrepTime = mins
                        } else if (results.detectedCookTime == null) {
                            results.detectedCookTime = mins
                        }
                        if (!isInstructionSentence) workingLine = workingLine.replace(standaloneTimeMatch.value, "").trim()
                    }
                    foundTime = false
                }
            }
        }

        val isOrphanTimeHeader = OcrRegexes.Metadata.ORPHAN_TIME_HEADER.containsMatchIn(workingLine) || lower == "temps de" || lower == "durée de" || lower == "temps" || lower == "durée" || lower == "temps de préparation" || lower == "préparation" || lower == "preparation" || lower == "préparation :" || lower == "preparation :"
        if (isOrphanTimeHeader) {
            workingLine = workingLine.replace(OcrRegexes.Metadata.ORPHAN_TIME_HEADER, "").trim()
        }

        val isMetadataHeader = isOrphanTimeHeader || lower.startsWith("préparation :") || lower.startsWith("cuisson :") || lower.startsWith("repos :") || lower == "pour"
        val isSectionHeader = workingLine.lowercase() in listOf("ingrédients", "ingredients", "préparation", "preparation", "instructions", "consignes", "réalisation", "realisation")
        val isPure = (isMetadataHeader || workingLine.isBlank()) && !isSectionHeader
        val finalRemainingLine = if (isPure) "" else workingLine

        return MetadataResult(isPure, finalRemainingLine)

        return MetadataResult(isPure, finalRemainingLine)
    }

    /**
     * Normalise les valeurs numériques nutritionnelles corrompues par l'OCR.
     *
     * @param rawValue La chaîne numérique brute extraite.
     * @return La valeur numérique nettoyée sous forme décimale (ex: "8.8", "9.7", "1.0").
     */
    private fun normalizeNoisyNutritionalValue(rawValue: String): String {
        var clean = rawValue.trim().replace(" ", "")
        // Remplace les confusions de chiffres LO0 / L00 / I0 -> 1.0
        clean = clean.replace(Regex("(?i)^[lI]00?$"), "1.0")
            .replace(Regex("(?i)^[lI][oO]0$"), "1.0")
            .replace(Regex("(?i)^0,(\\d{2})$"), "0.$1")
            .replace(",", ".")

        // Si 3 chiffres (ex: "880" -> "8.8", "107" -> "10.7")
        if (clean.matches(Regex("^\\d{3}$"))) {
            return if (clean.startsWith("10") || clean.startsWith("11") || clean.startsWith("12")) {
                "${clean.substring(0, 2)}.${clean.substring(2)}"
            } else {
                "${clean[0]}.${clean[1]}"
            }
        }
        // Si 2 chiffres suivis de 0 (ex: "970" -> "9.7")
        if (clean.matches(Regex("^\\d{2}0$"))) {
            return "${clean[0]}.${clean[1]}"
        }
        // Si 2 chiffres isolés pour grammage (ex: "97" -> "9.7")
        if (clean.matches(Regex("^\\d{2}$"))) {
            val valInt = clean.toInt()
            if (valInt in 15..99) {
                return "${clean[0]}.${clean[1]}"
            }
        }
        return clean
    }

    /**
     * Mis à jour le résultat de temps extrait dans l'objet [results].
     *
     * @param results L'objet [RawSections] mis à jour.
     * @param type Le type de temps ("prép", "cuisson", "repos").
     * @param mins Le nombre de minutes sous forme de texte.
     */
    private fun updateTimeResult(results: RawSections, type: String, mins: String?) {
        when {
            type.startsWith("prép") || type.startsWith("prep") -> results.detectedPrepTime = mins
            type.startsWith("cuis") || type.startsWith("cuisson") -> results.detectedCookTime = mins
            type.startsWith("re") -> results.detectedRestingTime = mins
        }
    }

    /**
     * Convertit une chaîne de durée sous forme de texte (ex: "1 h 30", "45 min") en nombre de minutes sous forme de texte.
     *
     * @param timeStr La chaîne de durée brute.
     * @return Le nombre de minutes calculé sous forme de texte (ex: "90", "45"), ou null si non convertible.
     */
    fun extractMinutes(timeStr: String): String? {
        OcrRegexes.Metadata.HOURS_PATTERN.find(timeStr)?.let {
            val h = it.groupValues[1].toIntOrNull() ?: 0
            val m = it.groupValues[2].toIntOrNull() ?: 0
            return (h * 60 + m).toString()
        }
        OcrRegexes.Metadata.MINUTES_PATTERN.find(timeStr)?.let { return it.groupValues[1] }
        return Regex("(\\d+)").find(timeStr)?.groupValues?.get(1)
    }
}
