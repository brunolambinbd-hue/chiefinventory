package com.chefinventory.app.utils

/**
 * Étape 5 du pipeline OCR : Façade Orchestratrice de Beautification et Présentation Finale à l'Écran.
 *
 * Responsabilités :
 * - Déléguer la beautification des consignes d'instructions à [Ocr5BeautifierInstruction].
 * - Déléguer la beautification des ingrédients à [Ocr5BeautifierIngredient].
 * - Déléguer la beautification des recommandations de vins à [Ocr5BeautifierWine].
 * - Déléguer la beautification des sources et coordonnées d'établissements à [Ocr5BeautifierSource].
 */
object Ocr5Beautifier {

    /**
     * Effectue la beautification et le recollage sémantique final des consignes d'instructions.
     *
     * @param instructions La liste des lignes d'instructions fusionnées issues de [Ocr4Merger].
     * @param stepActionKeywords Liste des verbes d'action culinaires à l'impératif (`step_keywords.xml`).
     * @param hangingConnectors Liste des prépositions et connecteurs suspendus (`hanging_connectors.xml`).
     * @param culinaryUnits Liste des unités de mesure culinaires (`culinary_units.xml`).
     * @return La liste finale des consignes d'instructions nettoyées, majusculées et ponctuées.
     */
    fun beautifyInstructions(
        instructions: List<String>, 
        stepActionKeywords: List<String> = emptyList(),
        hangingConnectors: List<String> = emptyList(),
        culinaryUnits: List<String> = emptyList(),
        spellingRepairs: List<String> = emptyList()
    ): List<String> {
        return Ocr5BeautifierInstruction.beautifyInstructions(
            instructions,
            stepActionKeywords,
            hangingConnectors,
            culinaryUnits,
            spellingRepairs
        )
    }

    /**
     * Effectue la beautification et le regroupement des qualificatifs de variétés d'ingrédients orphelins.
     *
     * @param ingredients La liste des lignes d'ingrédients fusionnées issues de [Ocr4Merger].
     * @param baseIngredients Liste des ingrédients de base attendant un qualificatif (`ingredient_varieties.xml`).
     * @param varietyQualifiers Liste des qualificatifs de variétés (`ingredient_varieties.xml`).
     * @return La liste finale des ingrédients nettoyés, débutant par une majuscule et purgés des en-têtes résiduels.
     */
    fun beautifyIngredients(
        ingredients: List<String>,
        baseIngredients: List<String> = emptyList(),
        varietyQualifiers: List<String> = emptyList()
    ): List<String> {
        return Ocr5BeautifierIngredient.beautifyIngredients(
            ingredients,
            baseIngredients,
            varietyQualifiers
        )
    }

    /**
     * Effectue la beautification et le dé-doublonnage des recommandations de vins.
     *
     * @param wines La liste des recommandations de vins brutes.
     * @return La liste finale nettoyée, majusculée et sans doublons des vins recommandés.
     */
    fun beautifyWines(wines: List<String>): List<String> {
        return Ocr5BeautifierWine.beautifyWines(wines)
    }

    /**
     * Effectue la beautification et le dé-doublonnage des sources et coordonnées d'établissements.
     *
     * @param sources La liste des sources et coordonnées brutes.
     * @return La liste finale nettoyée, majusculée et sans doublons des sources d'origine.
     */
    fun beautifySources(sources: List<String>): List<String> {
        return Ocr5BeautifierSource.beautifySources(sources)
    }

    /**
     * Filtre les bruits de symboles et glyphes non-alphanumériques de l'Étape 5 pour tous les embellisseurs.
     *
     * @return La liste nettoyée des lignes exemptes de bruits de symboles purs.
     */
    internal fun List<String>.filterBeautifierNoise(): List<String> =
        filterNot { OcrTextUtils.isNonAlphanumericNoise(it) }

    /**
     * Assainit, dépollue et valide une ligne de texte individuelle pour la Phase 5 (Beautifier).
     *
     * @return La ligne nettoyée, ou `null` si la ligne est du bruit à éliminer.
     */
    internal fun sanitizeLine(
        line: String,
        spellingRepairs: List<String> = emptyList(),
        prefixPatterns: List<Regex> = emptyList(),
        vocab: OcrVocabulary = OcrVocabularyLoader.loadFromProjectResources(),
        standaloneTrashWords: Set<String> = emptySet(),
        headerTrashWords: Set<String> = emptySet(),
        hasActionVerb: Boolean = false
    ): String? {
        var cleaned = line.trim()
        if (spellingRepairs.isNotEmpty()) {
            cleaned = Ocr1Corrector.applySpellingRepairs(cleaned, spellingRepairs)
        }
        for (pattern in prefixPatterns) {
            cleaned = cleaned.replace(pattern, "")
        }
        cleaned = cleaned.trim()
        if (cleaned.isEmpty()) return null

        val lower = cleaned.lowercase().removeSuffix(".")

        val isOcrNoise = OcrTextUtils.isNonAlphanumericNoise(cleaned) ||
                vocab.ocrNoiseExclusions.any { noise -> noise.isNotBlank() && cleaned.contains(noise, ignoreCase = true) }

        val isRecipeHeaderTrash = if (!hasActionVerb && headerTrashWords.isNotEmpty()) {
            vocab.excludedOcrKeywords.any { kw ->
                val cleanKw = kw.trim()
                cleanKw.isNotEmpty() && Regex("(?i)(?<![\\p{L}\\p{N}])${Regex.escape(cleanKw)}(?![\\p{L}\\p{N}])").containsMatchIn(cleaned)
            } || isOcrNoise
        } else {
            false
        }

        val isShortNoise = OcrTextUtils.isShortNoise(cleaned, standaloneTrashWords, hasActionVerb)

        if (lower in headerTrashWords || lower in standaloneTrashWords || isRecipeHeaderTrash || isShortNoise) {
            return null
        }

        return cleaned
    }
}
