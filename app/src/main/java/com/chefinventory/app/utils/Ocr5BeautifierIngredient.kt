package com.chefinventory.app.utils

/**
 * Assistant dédié à la beautification et au nettoyage final des lignes d'ingrédients lors de l'Étape 5.
 *
 * Responsabilités :
 * - Regrouper les qualificatifs de variétés d'ingrédients orphelines réparties sur 2 lignes ([Ocr5BeautifierIngredientVarietyMerger]).
 * - Forcer la majuscule initiale sur chaque nom d'ingrédient.
 * - Supprimer les en-têtes de section résiduels ("INGRÉDIENTS:", "Composition:").
 */
@Suppress("FoldInitializerAndIfNull", "kotlin:S6529")
object Ocr5BeautifierIngredient {

    /**
     * Effectue la beautification et le regroupement des qualificatifs de variétés d'ingrédients orphelins.
     *
     * @param ingredients La liste des lignes d'ingrédients issues de [Ocr4Merger].
     * @param baseIngredients Liste des ingrédients de base durant un qualificatif (`ingredient_varieties.xml`).
     * @param varietyQualifiers Liste des qualificatifs de variétés (`ingredient_varieties.xml`).
     * @return La liste nettoyée des ingrédients avec majuscule initiale et sans en-têtes résiduels.
     */
    fun beautifyIngredients(
        ingredients: List<String>,
        baseIngredients: List<String> = emptyList(),
        varietyQualifiers: List<String> = emptyList(),
        offTaxonomyList: List<String> = emptyList()
    ): List<String> {
        val vocab = OcrVocabularyLoader.loadFromProjectResources()
        val fullDictionary = (vocab.commonIngredientsNoQty + offTaxonomyList).distinct()
        val dictHashSet = fullDictionary.map { OcrLevenshteinUtils.stripAccents(it) }.toSet()
        val standaloneTrashWords = (vocab.standaloneArticles + vocab.hangingConnectors + vocab.priceTitleKeywords + vocab.wineTitleKeywords)
            .map { it.lowercase().trim() }
            .toSet()
        val headerTrashWords = vocab.ocr1ProtectedIngredientsHeaders.map { it.lowercase().trim() }.toSet()

        val containerAndMeasurementUnits = listOf(
            "boîte", "boite", "boîtes", "boites", "sachet", "sachets", "paquet", "paquets", 
            "verre", "verres", "tranche", "tranches", "gousse", "gousses", "branche", "branches", 
            "pincée", "pincées", "bocal", "bocaux", "botte", "bottes", "barquette", "barquettes", 
            "ravier", "raviers", "pot", "pots", "pointe", "pointes", "filet", "filets", 
            "cuisse", "cuisses", "aile", "ailes", "pilon", "pilons", "patte", "pattes", 
            "pavé", "pavés", "pave", "paves", "escalope", "escalopes", "côte", "côtes", 
            "côtelette", "côtelettes", "magret", "magrets", "aiguillette", "aiguillettes", 
            "paupiette", "paupiettes", "darne", "darnes", "noix", "boule", "boules", "bille", "billes"
        )

        val articlesAndDeterminers = (
            vocab.standaloneArticles + 
            listOf("un", "une", "des", "du", "de", "d'", "le", "la", "l'", "quelques", "plusieurs", "certains", "certaines")
        ).map { OcrLevenshteinUtils.stripAccents(it.trim().lowercase()) }

        val allAdjectivesAndModifiers = (
            vocab.ingredientPreparationModifiers + 
            vocab.ingredientPreparationSuffixModifiers + 
            vocab.ingredientVarietyQualifiers + 
            vocab.culinaryMeasurementUnits + 
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
                "demi-écrémé", "écrémé", "dur", "durs", "dure", "dures", "gris", "grise", "grises", "mûr", "mûre", "mûrs", "mûres", "mur", "mure", "murs", "mures", "nouvelle", "nouvelles", "nouveau", "nouveaux", "parme", "bayonne", "serrano", "iberique", "ibérique", "jus", "jus de", "jus d'", "jus du", "zeste", "zeste de", "zeste d'", "chair", "chair de", "quartier", "quartiers"
            )
        ).map { OcrLevenshteinUtils.stripAccents(it.trim().lowercase()) }.toSet()

        val mergedVarieties = Ocr5BeautifierIngredientVarietyMerger.beautifyIngredientVarieties(ingredients, baseIngredients, varietyQualifiers)
        return mergedVarieties.map { line ->
            var cleaned = line.trim()
            cleaned = cleaned.replace(Regex("^\\s*[.\\-,•*!;:?\\s]+"), "").trim()
            cleaned = cleaned.replace(Regex("(?i)(?<=\\b\\p{L}{2,20}\\s)[a-zA-Z]$"), "").trim()
            cleaned = cleaned.replace(Regex("[,:;\\s.]+$"), "").trim()

            val parsed = IngredientParser.parse(cleaned, vocab.culinaryMeasurementUnits)
            val name = parsed.name.trim()
            if (name.isNotEmpty()) {
                val nameWords = name.split(Regex("\\s+"))
                nameWords.forEach { word ->
                    val cleanWord = word.trim().trim('.', ',', ';', ':', '!', '?', '•', '-', '*')
                    val cleanWordAccent = OcrLevenshteinUtils.stripAccents(cleanWord)
                    val cleanWordSingular = cleanWordAccent.removeSuffix("S").removeSuffix("X")
                    val isAlreadyInDict = dictHashSet.contains(cleanWordAccent) || dictHashSet.contains(cleanWordSingular)

                    val cleanLower = cleanWord.lowercase()
                    var wordToMatch = cleanWord
                    var elisionPrefix = ""
                    if (cleanLower.length > 2 && (cleanLower[1] == '\'' || cleanLower[1] == '’')) {
                        val firstChar = cleanLower[0]
                        if (firstChar == 'd' || firstChar == 'l') {
                            elisionPrefix = cleanWord.substring(0, 2)
                            wordToMatch = cleanWord.substring(2)
                        }
                    }

                    if (!isAlreadyInDict && wordToMatch.length > 3 && wordToMatch.lowercase() !in standaloneTrashWords && cleanWordAccent !in allAdjectivesAndModifiers) {
                        val bestMatch = OcrLevenshteinUtils.findBestMatch(wordToMatch, fullDictionary)
                        if (bestMatch != null) {
                            val dist = OcrLevenshteinUtils.distance(OcrLevenshteinUtils.stripAccents(wordToMatch), OcrLevenshteinUtils.stripAccents(bestMatch))
                            val maxAllowed = OcrLevenshteinUtils.getMaxAllowedErrors(bestMatch.length)
                            val isPluralDiffOnly = wordToMatch.lowercase().trim().removeSuffix("s").removeSuffix("x") == bestMatch.lowercase().trim().removeSuffix("s").removeSuffix("x")
                            if (dist in 1..maxAllowed && !isPluralDiffOnly) {
                                val cleanBest = bestMatch.replace(Regex("(?i)^(?:du|de|d'|des|la|le|un|une)\\s+"), "").trim()
                                val finalReplacement = if (elisionPrefix.isNotEmpty()) "$elisionPrefix$cleanBest" else cleanBest
                                val pattern = Regex("(?iu)(?:^|(?<=\\s|\\b))${Regex.escape(cleanWord)}(?:$|(?=\\s|\\b))")
                                if (pattern.containsMatchIn(cleaned)) {
                                    cleaned = pattern.replace(cleaned, java.util.regex.Matcher.quoteReplacement(finalReplacement))
                                }
                            }
                        }
                    }
                }

                val lowerLine = cleaned.lowercase()
                val isFruitOrDessertContext = lowerLine.contains("melon") || lowerLine.contains("glace") || lowerLine.contains("mozzarella") || lowerLine.contains("pain") || lowerLine.contains("pate") || lowerLine.contains("pâte")
                val isMusselContext = !isFruitOrDessertContext && (lowerLine.contains("zelande") || lowerLine.contains("zélande") || lowerLine.contains("mariniere") || lowerLine.contains("marinière") || lowerLine.contains("bouchot") || lowerLine.contains("parc") || lowerLine.contains("douzaine"))

                if (isMusselContext && Regex("(?iu)\\bboules?\\b").containsMatchIn(cleaned)) {
                    cleaned = cleaned.replace(Regex("(?iu)\\bboules?\\b"), "moules")
                }
            }

            cleaned = applyFrenchPluralIfQuantityGreaterThanOne(cleaned, parsed.quantity, vocab)

            if (cleaned.isNotEmpty()) {
                val first = cleaned[0]
                if (first.isLowerCase()) {
                    cleaned = first.titlecase() + cleaned.substring(1)
                }
            }
            cleaned
        }.filter { line ->
            val lower = line.lowercase().trim().removeSuffix(":").removeSuffix(".")
            val isHeaderTrash = headerTrashWords.any { header -> lower.startsWith(header) }
            line.isNotBlank() && !isHeaderTrash && lower !in standaloneTrashWords
        }
    }

    /**
     * Applique la règle d'accord du pluriel français (-s ou -x) lorsque la quantité numérique de l'ingrédient est supérieure à 1.0.
     *
     * @param cleanedLine La ligne d'ingrédient nettoyée.
     * @param quantity La quantité numérique extraite (null si aucune).
     * @param vocab Le vocabulaire métier OCR.
     * @return La ligne d'ingrédient accordée au pluriel si nécessaire.
     */
    fun applyFrenchPluralIfQuantityGreaterThanOne(cleanedLine: String, quantity: Double?, vocab: OcrVocabulary): String {
        if (quantity == null || quantity <= 1.0 || cleanedLine.isBlank()) return cleanedLine

        val parsed = IngredientParser.parse(cleanedLine, vocab.culinaryMeasurementUnits)
        val unit = parsed.unit?.trim()?.lowercase()

        // 1. Si une unité est présente : pluraliser uniquement si l'unité est un conteneur décomptable (gousse, tranche, branche, pincée...)
        if (!unit.isNullOrEmpty()) {
            val containerUnits = setOf("gousse", "tranche", "branche", "pincée", "pincee", "feuille", "goutte", "ravier", "paquet", "sachet", "boîte", "boite", "verre")
            val cleanUnitWord = unit.split(Regex("\\s+"))[0].trim()
            if (cleanUnitWord in containerUnits) {
                val pluralUnit = cleanUnitWord + "s"
                val pattern = Regex("(?iu)\\b${Regex.escape(cleanUnitWord)}\\b")
                if (pattern.containsMatchIn(cleanedLine)) {
                    return pattern.replace(cleanedLine, java.util.regex.Matcher.quoteReplacement(pluralUnit))
                }
            }
            return cleanedLine // Ne pas modifier les unités abrégées (g, kg, cl, dl, l) ni les ingrédients massiques (farine, beurre)
        }

        // 2. Si aucune unité n'est présente (ex: "4 petites tomate"), pluraliser le nom d'ingrédient décomptable
        val name = parsed.name.trim()
        if (name.isEmpty()) return cleanedLine

        val parts = name.split(Regex("(?i)\\s+(?:de|d'|du|des)\\s+"))
        val basePart = parts[0].trim()
        val words = basePart.split(Regex("\\s+"))
        val targetWord = words.last()

        if (targetWord.isEmpty() || targetWord.endsWith("s", ignoreCase = true) || targetWord.endsWith("x", ignoreCase = true)) {
            return cleanedLine
        }

        val pluralWord = if (targetWord.endsWith("eau", ignoreCase = true) || targetWord.endsWith("eu", ignoreCase = true)) {
            targetWord + "x"
        } else {
            targetWord + "s"
        }

        val pattern = Regex("(?iu)\\b${Regex.escape(targetWord)}\\b")
        return if (pattern.containsMatchIn(cleanedLine)) {
            pattern.replace(cleanedLine, java.util.regex.Matcher.quoteReplacement(pluralWord))
        } else {
            cleanedLine
        }
    }
}
