package com.example.chiefinventory.utils

/**
 * Analyseur d'ingrédients culinaires data-driven.
 *
 * Responsabilités :
 * - Décomposer une chaîne d'ingrédient brute (ex: "250 g de farine (tamisée)") en ses 4 composants structurés :
 *   nom (`"farine"`), quantité numérique (`250.0`), unité culinaire (`"g"`), information complémentaire (`"tamisée"`).
 * - Prendre en charge les fractions ("1/2"), les plages ("10 à 15"), et les quantifieurs vagues ("quelques").
 * - Consommer les expressions régulières pré-compilées et le cache d'unités de [OcrRegexes].
 */
object IngredientParser {

    /**
     * Représente le résultat structuré du parsing d'une ligne d'ingrédient.
     *
     * @property name Le nom principal nettoyé de l'aliment (ex: "farine", "beurre", "tomates").
     * @property quantity La quantité numérique extraite (ex: 250.0, 0.5, 1.0). Null si aucune quantité explicite.
     * @property unit L'unité de mesure culinaire identifiée (ex: "g", "c. à soupe", "cl"). Null si sans unité.
     * @property supplementalInfo Les informations de préparation ou précisions secondaires (ex: "haché", "frais", "ou 2 grosses").
     */
    data class ParsedIngredient(
        val name: String,
        val quantity: Double? = null,
        val unit: String? = null,
        val supplementalInfo: String? = null
    )

    /**
     * Unités de mesure culinaires par défaut chargées dynamiquement depuis [R.array.culinary_measurement_units] (`culinary_units.xml`).
     */
    internal val defaultUnits: List<String> by lazy {
        try {
            OcrVocabularyLoader.loadFromProjectResources().culinaryMeasurementUnits
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Décompose une chaîne de texte d'ingrédient brute en un objet structuré [ParsedIngredient].
     *
     * @param input La ligne de texte d'ingrédient brute (ex: "2 c. à soupe de mayonnaise").
     * @param customUnits Liste facultative d'unités de mesure personnalisées (ex: issues de [OcrVocabulary.culinaryMeasurementUnits]).
     * @return L'ingrédient décomposé et structuré.
     */
    fun parse(input: String, customUnits: List<String> = emptyList()): ParsedIngredient {
        val activeUnits = if (customUnits.isNotEmpty()) {
            (customUnits + defaultUnits).distinct().sortedByDescending { it.length }
        } else {
            defaultUnits.sortedByDescending { it.length }
        }

        val parentheticalMatch = OcrRegexes.Ingredients.PARENTHESES.find(input)
        var supplementalInfo = parentheticalMatch?.groupValues?.get(1)
        var mainText = input.replace(OcrRegexes.Ingredients.PARENTHESES, "").trim()

        val (textAfterFirstExtraction, infoAfterFirstExtraction) = extractExtra(mainText, supplementalInfo)
        mainText = textAfterFirstExtraction
        supplementalInfo = infoAfterFirstExtraction

        val fractionMatch = OcrRegexes.Quantities.FRACTION.find(mainText)
        if (fractionMatch != null) {
            val numerator = fractionMatch.groupValues[1].toDoubleOrNull() ?: 0.0
            val denominator = fractionMatch.groupValues[2].toDoubleOrNull() ?: 1.0
            val rest = mainText.replace(fractionMatch.value, "").trim()
            val unitInfo = findUnit(rest, activeUnits)
            val (finalName, finalInfo) = extractExtra(unitInfo.remainingText, supplementalInfo)
            return ParsedIngredient(finalName, numerator / denominator, unitInfo.unit, finalInfo)
        }

        val rangeMatch = OcrRegexes.Quantities.RANGE.find(mainText)
        if (rangeMatch != null) {
            val secondQty = rangeMatch.groupValues[2].replace(",", ".").toDoubleOrNull()
            val rest = mainText.substring(rangeMatch.range.last + 1).trim()
            val standard = parseStandard(rest, supplementalInfo, activeUnits)
            return ParsedIngredient(standard.name, secondQty, standard.unit, standard.supplementalInfo)
        }

        return parseStandard(mainText, supplementalInfo, activeUnits)
    }

    /**
     * Extrait les préfixes de préparation (ex: "le jus de"), quantifieurs vagues ("quelques") ou adjectifs ("petites")
     * du nom principal pour les placer dans l'information supplémentaire.
     *
     * @param text Le texte principal d'ingrédient en cours d'analyse.
     * @param currentInfo L'information supplémentaire déjà accumulée.
     * @return Une paire contenant `(textePrincipalRestant, informationSecondaireFormatee)`.
     */
    private fun extractExtra(text: String, currentInfo: String?): Pair<String, String?> {
        var mainText = text.trim()
        var supplementalInfo = currentInfo
        var continueExtraction = true
        while (continueExtraction) {
            val prepMatch = OcrRegexes.Ingredients.PREPARATION_PREFIX.find(mainText)
            val vagueMatch = OcrRegexes.Ingredients.VAGUE_QUANTITY.find(mainText)
            val adjMatch = OcrRegexes.Ingredients.ADJECTIVE_PREFIX.find(mainText)

            if (prepMatch != null) {
                val word = prepMatch.value.trim()
                supplementalInfo = if (supplementalInfo.isNullOrBlank()) word else "$supplementalInfo $word"
                mainText = mainText.replaceFirst(prepMatch.value, "").trim()
            } else if (vagueMatch != null) {
                val word = vagueMatch.groupValues[1]
                supplementalInfo = if (supplementalInfo.isNullOrBlank()) word else "$word $supplementalInfo"
                mainText = mainText.replaceFirst(vagueMatch.value, "").trim()
            } else if (adjMatch != null) {
                val word = adjMatch.groupValues[1]
                supplementalInfo = if (supplementalInfo.isNullOrBlank()) word else "$word $supplementalInfo"
                mainText = mainText.replaceFirst(adjMatch.value, "").trim()
            } else {
                continueExtraction = false
            }
        }
        return mainText to supplementalInfo
    }

    /**
     * Encapsule le résultat de la détection d'unité de mesure et le texte restant d'ingrédient.
     *
     * @property unit L'unité de mesure culinaire identifiée (ex: "g", "c. à soupe"), ou null si non trouvée.
     * @property remainingText Le texte de l'ingrédient restant après extraction de l'unité.
     */
    private data class UnitInfo(val unit: String?, val remainingText: String)

    /**
     * Recherche la plus longue unité de mesure correspondant au début du texte transmis.
     *
     * @param text Le texte d'ingrédient purgé de sa quantité numérique.
     * @param activeUnits La liste ordonnée des unités culinaires autorisées.
     * @return Un conteneur [UnitInfo] avec l'unité trouvée et le texte du nom restant.
     */
    private fun findUnit(text: String, activeUnits: List<String> = defaultUnits): UnitInfo {
        for (unit in activeUnits) {
            val unitPattern = OcrRegexes.getUnitPattern(unit)
            val match = unitPattern.find(text)
            if (match != null) {
                val namePart = text.substring(match.range.last + 1).trim()
                val finalName = namePart.replace(OcrRegexes.Ingredients.DE_PREFIX, "").trim()
                return UnitInfo(unit, if (finalName.isEmpty()) text else finalName)
            }
        }
        return UnitInfo(null, text)
    }

    /**
     * Effectue le parsing d'une ligne d'ingrédient à quantité numérique standard (ex: "250 g de farine").
     *
     * @param input La ligne d'ingrédient brute sans parenthèses.
     * @param supplementalInfo L'information supplémentaire déjà extraite.
     * @param activeUnits La liste ordonnée des unités culinaires autorisées.
     * @return L'ingrédient décomposé et structuré.
     */
    private fun parseStandard(input: String, supplementalInfo: String?, activeUnits: List<String>): ParsedIngredient {
        val matcher = OcrRegexes.Quantities.STANDARD.matcher(input)
        var qty: Double? = null
        var rest = input.trim()

        if (matcher.find()) {
            val qtyStr = matcher.group(1)
            if (!qtyStr.isNullOrEmpty()) {
                qty = qtyStr.replace(",", ".").toDoubleOrNull()
                rest = matcher.group(2)?.trim() ?: ""
            }
        }

        val unitInfo = findUnit(rest, activeUnits)
        val (finalName, finalInfo) = extractExtra(unitInfo.remainingText, supplementalInfo)
        return ParsedIngredient(finalName, qty, unitInfo.unit, finalInfo)
    }
}
