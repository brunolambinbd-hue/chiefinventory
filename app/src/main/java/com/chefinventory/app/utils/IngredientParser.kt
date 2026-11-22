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
     * Nettoie et extrait le texte restant après la quantité numérique.
     */
    internal fun extractRestText(rawGroup2: String?): String {
        if (rawGroup2 == null) {
            return ""
        }
        val trimmed = rawGroup2.trim()
        if (trimmed.isEmpty()) {
            return ""
        }
        return trimmed
    }

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
            val isPrefixEmpty = prefixBeforeFraction.isEmpty()
            val isVaguePrefix = OcrRegexes.Ingredients.VAGUE_QUANTITY.containsMatchIn(prefixBeforeFraction)
            val isJusPrefix = prefixBeforeFraction.lowercase().startsWith("le jus")
            val isCleanPrefix = if (isPrefixEmpty) {
                true
            } else if (isVaguePrefix) {
                true
            } else {
                isJusPrefix
            }

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
        if (rangeMatch != null) {
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
        if (vagueMatch != null) {
            val vagueWord = vagueMatch.groupValues[1].trim()
            var restText = workingText.substring(vagueMatch.range.last + 1).trim()

            val extraInfo: String?
            val adjMatch = OcrRegexes.Ingredients.ADJECTIVE_PREFIX.find(restText)
            if (adjMatch != null) {
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
        var cleanName = name.replace(Regex("\\s+"), " ").trim()
        cleanName = cleanName.replace(Regex("^\\s*[.\\-,•*!;:?\\s]+"), "").trim()
        cleanName = cleanName.replace(Regex("\\s+[.\\-,•*!:;?]$"), "").trim()
        cleanName = cleanName.replace(Regex("\\s+[b-fB-Fh-kH-Km-zM-Z]$"), "").trim()
        cleanName = cleanName.replace(Regex("[,:;\\s.]+$"), "").trim()
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
            val qtyStr = matcher.group(1)
            val qty = qtyStr!!.replace(',', '.').toDoubleOrNull()

            val rawGroup2 = matcher.group(2)
            var restText = extractRestText(rawGroup2)

            val extraInfo: String?
            val adjMatch = OcrRegexes.Ingredients.ADJECTIVE_PREFIX.find(restText)
            if (adjMatch != null) {
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

    /**
     * Localise les intervalles de caractères [IntRange] des mots noyaux d'ingrédients au sein d'une ligne d'ingrédient brute.
     *
     * @param line La ligne d'ingrédient brute (ex: "200 g d'épinards frais", "Sel et poivre").
     * @param vocabulary Le vocabulaire métier OCR.
     * @param offTaxonomyList La liste optionnelle des aliments d'Open Food Facts.
     * @return La liste des intervalles [IntRange] (start..end) des mots noyaux dans la ligne.
     */
    fun findCoreIngredientSpans(
        line: String,
        vocabulary: OcrVocabulary,
        offTaxonomyList: List<String> = emptyList()
    ): List<IntRange> {
        if (line.isBlank()) return emptyList()

        val cleanLineLower = OcrLevenshteinUtils.stripAccents(line.trim().lowercase()).removeSuffix(":").removeSuffix(".").trim()
        val allSubHeaders = (vocabulary.ingredientSubHeaders + listOf("pour la sauce", "pour la marinade", "pour la garniture", "pour la vinaigrette", "pour la farce", "pour la pâte", "pour le glaçage")).map { OcrLevenshteinUtils.stripAccents(it.trim().lowercase()) }
        val isSubHeaderLine = allSubHeaders.any { subHeader ->
            cleanLineLower == subHeader || cleanLineLower.startsWith("pour ") || cleanLineLower.startsWith("pour la ") || cleanLineLower.startsWith("pour le ") || cleanLineLower.startsWith("pour les ")
        }
        if (isSubHeaderLine) return emptyList()

        val containerAndMeasurementUnits = listOf(
            "boîte", "boite", "boîtes", "boites", "sachet", "sachets", "paquet", "paquets", 
            "verre", "verres", "tranche", "tranches", "gousse", "gousses", "branche", "branches", 
            "pincée", "pincées", "bocal", "bocaux", "botte", "bottes", "barquette", "barquettes", 
            "ravier", "raviers", "pot", "pots", "pointe", "pointes", "filet", "filets", 
            "cuisse", "cuisses", "aile", "ailes", "pilon", "pilons", "patte", "pattes", 
            "pavé", "pavés", "pave", "paves", "escalope", "escalopes", "côte", "côtes", 
            "côtelette", "côtelettes", "magret", "magrets", "aiguillette", "aiguillettes", 
            "paupiette", "paupiettes", "darne", "darnes"
        )

        val articlesAndDeterminers = (
            vocabulary.standaloneArticles + 
            listOf("un", "une", "des", "du", "de", "d'", "le", "la", "l'", "quelques", "plusieurs", "certains", "certaines")
        ).map { OcrLevenshteinUtils.stripAccents(it.trim().lowercase()) }

        val ignoreModifiers = (
            vocabulary.ingredientPreparationModifiers + 
            vocabulary.ingredientPreparationSuffixModifiers + 
            vocabulary.ingredientVarietyQualifiers + 
            vocabulary.culinaryMeasurementUnits + 
            containerAndMeasurementUnits + 
            articlesAndDeterminers + 
            listOf(
                "frais", "fraîche", "fraiche", "haché", "hachée", "hachés", "hachées", 
                "émincé", "émincée", "émincés", "émincées", "râpé", "râpée", "râpés", "râpées", 
                "moulu", "moulue", "moulus", "moulues", "dénoyauté", "dénoyautée", "dénoyautés", "dénoyautées", 
                "concassé", "concassée", "concassés", "concassées", "décortiqué", "décortiquée", "décortiqués", "décortiquées", 
                "cuit", "cuite", "cuits", "cuites", "cru", "crue", "crus", "crues", 
                "séché", "séchée", "séchés", "séchées", "fondant", "fondante", "fondants", "fondantes", 
                "grillé", "grillée", "grillés", "grillées", "salé", "salée", "salés", "salées", 
                "sucré", "sucrée", "sucrés", "sucrées", "jaune", "jaunes", "rouge", "rouges", 
                "blanc", "blanche", "blancs", "blanches", "vert", "verte", "verts", "vertes", 
                "noir", "noire", "noirs", "noires", "doux", "douce", "fort", "forte", "forts", "fortes", 
                "demi-écrémé", "écrémé", "crémé", "creme", "créme", "morts", "propres", "sauce", "sauces",
                "dur", "durs", "dure", "dures", "jus", "jus de", "jus d'", "jus du", "zeste", "zeste de", "zeste d'", "chair", "chair de", "quartier", "quartiers"
            )
        ).map { OcrLevenshteinUtils.stripAccents(it.trim().lowercase()) }.toSet()

        val parsed = parse(line, vocabulary.culinaryMeasurementUnits)
        val name = parsed.name.trim()
        if (name.isEmpty()) return emptyList()

        val fullDict = (vocabulary.commonIngredientsNoQty + offTaxonomyList)
            .filter { it.isNotBlank() }
            .distinct()
            .sortedByDescending { it.length }

        val spans = mutableListOf<IntRange>()
        val lineAccent = OcrLevenshteinUtils.stripAccents(line.lowercase())

        val nameStartInLine = if (name.isNotEmpty()) {
            val cleanNameWords = name.split(Regex("\\s+")).filter { 
                val w = OcrLevenshteinUtils.stripAccents(it.lowercase())
                w.length >= 3 && w !in ignoreModifiers && w !in vocabulary.standaloneArticles
            }
            val firstCoreWord = cleanNameWords.firstOrNull()
            if (firstCoreWord != null) {
                val idx = lineAccent.indexOf(OcrLevenshteinUtils.stripAccents(firstCoreWord.lowercase()))
                if (idx >= 0) idx else 0
            } else 0
        } else 0

        // 1. Chercher d'abord les expressions multi-mots composées les plus longues (ex: "vinaigre de vin blanc", "huile d'olive")
        for (dictItem in fullDict) {
            val cleanItem = OcrLevenshteinUtils.stripAccents(dictItem.lowercase())
            val cleanItemNoPrefix = cleanItem.replace(Regex("(?i)^(?:DU|DE|D'|DES|LA|LE|UN|UNE)\\s+"), "").trim()
            if (cleanItemNoPrefix.length >= 4 && cleanItemNoPrefix !in ignoreModifiers) {
                var start = lineAccent.indexOf(cleanItemNoPrefix, nameStartInLine)
                var end = if (start >= 0) start + cleanItemNoPrefix.length else -1

                if (start < 0) {
                    // Recherche tolérante au pluriel (-S / -X) (ex: Open Food Facts "graine de sésame" vs Recette "graines de sésame")
                    val lineWords = lineAccent.split(Regex("\\s+"))
                    val itemWords = cleanItemNoPrefix.split(Regex("\\s+"))
                    if (itemWords.isNotEmpty() && lineWords.size >= itemWords.size) {
                        for (wIdx in 0..(lineWords.size - itemWords.size)) {
                            var matchesAllWords = true
                            for (k in itemWords.indices) {
                                val lw = lineWords[wIdx + k].removeSuffix("S").removeSuffix("X")
                                val iw = itemWords[k].removeSuffix("S").removeSuffix("X")
                                if (lw != iw) {
                                    matchesAllWords = false
                                    break
                                }
                            }
                            if (matchesAllWords) {
                                val firstWordInLine = lineWords[wIdx]
                                val lastWordInLine = lineWords[wIdx + itemWords.size - 1]
                                var currentOffset = 0
                                for (p in 0 until wIdx) {
                                    currentOffset += lineWords[p].length + 1
                                }
                                start = lineAccent.indexOf(firstWordInLine, currentOffset)
                                if (start >= 0) {
                                    val endWordStart = lineAccent.indexOf(lastWordInLine, start)
                                    if (endWordStart >= start) {
                                        end = endWordStart + lastWordInLine.length
                                    }
                                }
                                break
                            }
                        }
                    }
                }

                if (start >= 0 && end > start) {
                    val isOverlapping = spans.any { existing ->
                        (start in existing.first..existing.last) || (end in existing.first..existing.last) || (existing.first in start..end)
                    }
                    if (!isOverlapping) {
                        val originalTarget = line.substring(start, minOf(end, line.length))
                        spans.add(IntRange(start, start + originalTarget.length))
                    }
                }
            }
        }

        if (spans.isNotEmpty()) {
            return spans.sortedBy { it.first }
        }

        // 2. Si aucune expression multi-mots n'a été trouvée, évaluer mot par mot (uniquement dans le nom d'ingrédient)
        val words = line.split(Regex("\\s+"))
        var searchOffset = nameStartInLine
        words.forEach { word ->
            var cleanW = word.trim().trim('.', ',', ';', ':', '!', '?', '•', '-', '*')
            if (cleanW.lowercase().startsWith("d'") || cleanW.lowercase().startsWith("l'")) {
                cleanW = cleanW.substring(2)
            } else if (cleanW.lowercase().startsWith("de ") || cleanW.lowercase().startsWith("du ")) {
                cleanW = cleanW.substring(3)
            }
            val cleanWAccent = OcrLevenshteinUtils.stripAccents(cleanW.lowercase())

            if (cleanWAccent.length >= 3 && cleanWAccent !in ignoreModifiers) {
                val matchesDict = fullDict.any { dictItem ->
                    val cleanItem = OcrLevenshteinUtils.stripAccents(dictItem.lowercase())
                    cleanItem == cleanWAccent || cleanItem.contains(cleanWAccent) || cleanWAccent.contains(cleanItem)
                }
                if (matchesDict) {
                    val start = line.indexOf(cleanW, searchOffset, ignoreCase = true)
                    if (start >= nameStartInLine) {
                        val end = start + cleanW.length
                        val isOverlapping = spans.any { existing ->
                            (start in existing.first..existing.last) || (end in existing.first..existing.last)
                        }
                        if (!isOverlapping) {
                            spans.add(IntRange(start, end))
                            searchOffset = end
                        }
                    }
                }
            }
        }

        if (spans.isEmpty() && name.isNotEmpty()) {
            val fallbackWord = name.split(Regex("\\s+")).firstOrNull { w ->
                var cw = w.trim().trim('.', ',', ';', ':', '!', '?', '•', '-', '*')
                if (cw.lowercase().startsWith("d'") || cw.lowercase().startsWith("l'")) cw = cw.substring(2)
                val cleanCw = OcrLevenshteinUtils.stripAccents(cw.lowercase())
                cleanCw.length >= 3 && cleanCw !in ignoreModifiers
            } ?: name
            var cleanTarget = fallbackWord.trim().trim('.', ',', ';', ':', '!', '?', '•', '-', '*')
            if (cleanTarget.lowercase().startsWith("d'") || cleanTarget.lowercase().startsWith("l'")) cleanTarget = cleanTarget.substring(2)
            val start = line.indexOf(cleanTarget, nameStartInLine, ignoreCase = true)
            if (start >= nameStartInLine) {
                spans.add(IntRange(start, start + cleanTarget.length))
            }
        }

        return spans.sortedBy { it.first }
    }

    /**
     * Alias de rétrocompatibilité pour [findCoreIngredientSpans].
     */
    fun findCoreIngredientSpan(
        line: String,
        vocabulary: OcrVocabulary,
        offTaxonomyList: List<String> = emptyList()
    ): IntRange? {
        return findCoreIngredientSpans(line, vocabulary, offTaxonomyList).firstOrNull()
    }
}
