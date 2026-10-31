package com.chefinventory.app.utils

import com.parabd.lib.traceability.utils.LogManager

/**
 * Analyseur d'ingrédients culinaires data-driven.
 *
 * Responsabilités :
 * - Décomposer une chaîne d'ingrédient brute (ex: "250 g de farine (tamisée)") en ses 4 composants structurés :
 *   nom (`"farine"`), quantité numérique (`250.0`), unité culinaire (`"g"`), information complémentaire (`"tamisée"`).
 * - Prendre en charge les fractions ("1/2"), les plages ("10 à 15"), et les quantificateurs vagues ("quelques").
 * - Consommer les expressions régulières pré-compilées et le cache d'unités de [OcrRegexes].
 */
@Suppress("FoldInitializerAndIfNull", "kotlin:S6529")
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
        val supplementalInfo: String? = null,
    )

    /**
     * Charge la liste des unités culinaires autorisées depuis les ressources XML du projet.
     *
     * @param projectDir Répertoire racine optionnel pour la résolution des ressources (par défaut : dossier courant `"."`).
     * @return La liste des unités culinaires ou une liste vide en cas d'erreur I/O.
     */
    internal fun loadDefaultUnits(projectDir: java.io.File = java.io.File(".")): List<String> {
        return try {
            OcrVocabularyLoader.loadFromProjectResources(projectDir).culinaryMeasurementUnits
        } catch (e: Exception) {
            LogManager.e("IngredientParser", "Failed to load default culinary measurement units", e)
            emptyList()
        }
    }

    /**
     * Unités de mesure culinaires par défaut chargées dynamiquement depuis `R.array.culinary_measurement_units` (`culinary_units.xml`).
     */
    internal val defaultUnits: List<String>
        get() = loadDefaultUnits()

    /**
     * Décompose une chaîne de texte d'ingrédient brute en un objet structuré [ParsedIngredient].
     *
     * @param input La ligne de texte d'ingrédient brute (ex: "2 c. à soupe de mayonnaise").
     * @param customUnits Liste facultative d'unités de mesure personnalisées (ex: issues de [OcrVocabulary.culinaryMeasurementUnits]).
     * @return L'ingrédient décomposé et structuré.
     */
    fun parse(input: String, customUnits: List<String> = emptyList()): ParsedIngredient {
        val activeUnits = customUnits.ifEmpty { defaultUnits }
        val text = input.trim()
        if (text.isEmpty()) {
            return ParsedIngredient("")
        }

        // 1. Isoler et extraire les informations complémentaires entre parenthèses
        var workingText = text
        var supplementalInfo: String? = null
        val parentheticalMatch = OcrRegexes.Ingredients.PARENTHESES.find(workingText)
        if (parentheticalMatch != null) {
            supplementalInfo = parentheticalMatch.groupValues[1].trim()
            workingText = workingText.replace(parentheticalMatch.value, " ").trim()
        }

        // 2. Traiter le cas spécial des fractions pures (ex: "1/2 verre d'eau", "3/4 l", "le jus de 1/2 citron")
        val fractionMatch = OcrRegexes.Quantities.FRACTION.find(workingText)
        if (fractionMatch != null) {
            val prefixBeforeFraction = workingText.substring(0, fractionMatch.range.first).trim()
            val isCleanPrefix = prefixBeforeFraction.isEmpty() || OcrRegexes.Ingredients.VAGUE_QUANTITY.containsMatchIn(prefixBeforeFraction) || prefixBeforeFraction.lowercase().startsWith("le jus")
            if (isCleanPrefix) {
                val denominator = fractionMatch.groupValues[2].toDouble()
                if (denominator == 0.0) {
                    return createParsedIngredient(name = workingText, quantity = null, unit = null, supplementalInfo = null)
                } else {
                    val numerator = fractionMatch.groupValues[1].toDouble()
                    val value = numerator / denominator

                    val restText = workingText.substring(fractionMatch.range.last + 1).trim()
                    val unitInfo = findUnit(restText, activeUnits)

                    val combinedInfo = when {
                        prefixBeforeFraction.isEmpty() -> supplementalInfo
                        supplementalInfo == null -> prefixBeforeFraction
                        else -> "$prefixBeforeFraction $supplementalInfo"
                    }

                    return createParsedIngredient(
                        name = unitInfo.remainingText,
                        quantity = value,
                        unit = unitInfo.unit,
                        supplementalInfo = combinedInfo,
                    )
                }
            }
        }

        // 3. Traiter le cas des plages numériques (ex: "10 à 15 min", "1 ou 2 gousses")
        val rangeMatch = OcrRegexes.Quantities.RANGE.find(workingText)
        if (rangeMatch != null && rangeMatch.range.first == 0) {
            val high = rangeMatch.groupValues[2].toDouble()

            val restText = workingText.substring(rangeMatch.range.last + 1).trim()
            val unitInfo = findUnit(restText, activeUnits)

            return createParsedIngredient(
                name = unitInfo.remainingText,
                quantity = high,
                unit = unitInfo.unit,
                supplementalInfo = supplementalInfo,
            )
        }

        // 3.5. Traiter les quantificateurs vagues (ex: "quelques branches d'aneth", "des petites feuilles de chicon")
        val vagueMatch = OcrRegexes.Ingredients.VAGUE_QUANTITY.find(workingText)
        if (vagueMatch != null && vagueMatch.range.first == 0) {
            val vagueWord = vagueMatch.groupValues[1].trim()
            var restText = workingText.substring(vagueMatch.range.last + 1).trim()

            val extraInfo: String?
            val adjMatch = OcrRegexes.Ingredients.ADJECTIVE_PREFIX.find(restText)
            if (adjMatch != null && adjMatch.range.first == 0) {
                extraInfo = adjMatch.groupValues[1].trim()
                restText = restText.substring(adjMatch.range.last + 1).trim()
            } else {
                extraInfo = null
            }

            val unitInfo = findUnit(restText, activeUnits)

            val combinedInfoList = listOfNotNull(extraInfo, vagueWord, supplementalInfo)
            val finalInfo = combinedInfoList.joinToString(" ")

            return createParsedIngredient(
                name = unitInfo.remainingText,
                quantity = null,
                unit = unitInfo.unit,
                supplementalInfo = finalInfo,
            )
        }

        // 4. Traitement standard des quantités numériques (ex: "250 g de farine", "2 grosses tomates")
        val standardParsed = parseStandardQuantity(workingText, activeUnits)

        // Réassembler supplementalInfo si des qualificatifs supplémentaires étaient en parenthèses
        val finalInfo = when {
            supplementalInfo == null -> standardParsed.supplementalInfo
            standardParsed.supplementalInfo == null -> supplementalInfo
            else -> "$supplementalInfo ${standardParsed.supplementalInfo}"
        }

        return createParsedIngredient(
            name = standardParsed.name,
            quantity = standardParsed.quantity,
            unit = standardParsed.unit,
            supplementalInfo = finalInfo,
        )
    }

    /**
     * Fabrique un objet [ParsedIngredient] en nettoyant et normalisant les espaces superflus du nom de l'aliment.
     *
     * @param name Le nom brut ou intermédiaire de l'aliment (ex: "  surimi   ou de crabe  ").
     * @param quantity La quantité numérique extraite (ou null si non précisée).
     * @param unit L'unité culinaire identifiée (ou null si sans unité).
     * @param supplementalInfo Les précisions de préparation ou qualificatifs secondaires (ou null si absents).
     * @return L'objet [ParsedIngredient] instancié avec le nom d'aliment purgé des espaces multiples.
     */
    private fun createParsedIngredient(
        name: String,
        quantity: Double?,
        unit: String?,
        supplementalInfo: String?,
    ): ParsedIngredient {
        val cleanName = name.replace(Regex("\\s+"), " ").trim()
        return ParsedIngredient(cleanName, quantity, unit, supplementalInfo)
    }

    /**
     * Conteneur interne représentant le résultat de la recherche d'unité culinaire.
     *
     * @property unit L'unité identifiée (ou null si aucune unité ne correspond).
     * @property remainingText Le texte restant de l'ingrédient purgé du préfixe d'unité et des articles.
     */
    internal data class UnitInfo(val unit: String?, val remainingText: String)

    /**
     * Recherche la plus longue unité de mesure correspondant au début du texte transmis.
     *
     * @param text Le texte d'ingrédient purgé de sa quantité numérique.
     * @param activeUnits La liste ordonnée des unités culinaires autorisées.
     * @return Un conteneur [UnitInfo] avec l'unité trouvée et le texte du nom restant.
     */
    internal fun findUnit(text: String, activeUnits: List<String> = defaultUnits): UnitInfo {
        for (unit in activeUnits) {
            val unitPattern = OcrRegexes.getUnitPattern(unit)
            val match = unitPattern.find(text)
            if (match != null) {
                val namePart = text.substring(match.range.last + 1).trim()
                val finalName = namePart.replace(OcrRegexes.Ingredients.DE_PREFIX, "").trim()
                return UnitInfo(unit, finalName.ifEmpty { text })
            }
        }
        return UnitInfo(null, text)
    }

    /**
     * Effectue le parsing d'une ligne d'ingrédient à quantité numérique standard (ex: "250 g de farine").
     *
     * @param input La ligne d'ingrédient brute sans parenthèses.
     * @param activeUnits La liste ordonnée des unités culinaires autorisées.
     * @return L'ingrédient décomposé.
     */
    private fun parseStandardQuantity(input: String, activeUnits: List<String>): ParsedIngredient {
        val matcher = OcrRegexes.Quantities.STANDARD.matcher(input)
        if (matcher.find()) {
            val rawGroup1 = matcher.group(1)
            val qtyStr = if (rawGroup1 != null) rawGroup1 else ""
            val qty = qtyStr.replace(',', '.').toDoubleOrNull()

            val rawGroup2 = matcher.group(2)
            var restText = if (rawGroup2 != null) rawGroup2.trim() else ""

            val extraInfo: String?
            val adjMatch = OcrRegexes.Ingredients.ADJECTIVE_PREFIX.find(restText)
            if (adjMatch != null && adjMatch.range.first == 0) {
                extraInfo = adjMatch.groupValues[1].trim()
                restText = restText.substring(adjMatch.range.last + 1).trim()
            } else {
                extraInfo = null
            }

            val unitInfo = findUnit(restText, activeUnits)

            return createParsedIngredient(
                name = unitInfo.remainingText,
                quantity = qty,
                unit = unitInfo.unit,
                supplementalInfo = extraInfo,
            )
        }

        val unitInfo = findUnit(input, activeUnits)
        if (unitInfo.unit != null) {
            return createParsedIngredient(
                name = unitInfo.remainingText,
                quantity = null,
                unit = unitInfo.unit,
                supplementalInfo = null,
            )
        }

        return createParsedIngredient(
            name = input,
            quantity = null,
            unit = null,
            supplementalInfo = null,
        )
    }
}
