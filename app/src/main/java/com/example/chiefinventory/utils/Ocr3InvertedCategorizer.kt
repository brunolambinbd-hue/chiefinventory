package com.example.chiefinventory.utils

import android.util.Log

/**
 * Catégoriseur spécialisé pour les recettes inversées (Type 3) dont les consignes d'instructions
 * apparaissent en premier (haut/gauche) et dont la liste d'ingrédients apparaît en second (bas/droite).
 *
 * Responsabilités :
 * - Détecter si un scan de recette commence par des consignes d'instructions et comporte une liste d'ingrédients du bas.
 * - Passe 1 : Isoler les consignes du haut dans [RawSections.rawInstructionsList] et les ingrédients du bas dans [RawSections.rawIngredientsList].
 * - Passe 2 : Évaluer et réparer le titre du haut en utilisant les mots-clés d'ingrédients extraits du bas.
 * - Extraire les métadonnées, le vin et les sources.
 */
object Ocr3InvertedCategorizer {
    private const val TAG = "Ocr3InvertedCat"

    /**
     * Détermine si le scan de recette correspond à une recette inversée (Type 3) où les instructions précèdent les ingrédients.
     *
     * @param lines La liste des lignes de texte scannées nettoyées (sans le titre).
     * @param vocabulary Le vocabulaire métier OCR.
     * @return `true` s'il s'agit d'une recette inversée, `false` sinon (Redirection Type 1 ou Type 2).
     */
    fun isInvertedRecipe(lines: List<String>, vocabulary: OcrVocabulary): Boolean {
        if (lines.isEmpty()) return false

        // 1. Si la fiche contient un en-tête d'ingrédients explicite ("INGRÉDIENTS", "COMPOSITION"), ce n'est PAS du Type 3 !
        val hasExplicitIngrHeader = lines.any { line ->
            val lower = line.lowercase().trim()
            lower.contains("ingr") || lower.contains("composition")
        }
        if (hasExplicitIngrHeader) return false

        // 2. Détection du démarrage par des instructions (numéro d'étape 1), 2), ou verbe d'action)
        val startsWithInstruction = lines.take(5).any { line ->
            OcrInstructionUtils.hasActionVerb(line, vocabulary.stepActionKeywords) ||
                    Regex("^\\s*\\d{1,2}\\s*[.)\\-]").containsMatchIn(line) ||
                    line.trim().startsWith("•")
        }

        if (!startsWithInstruction) return false

        // 3. Vérification de la présence d'un bloc d'ingrédients du bas (ex: "POUR 4 PERSONNES" ou liste quantifiée plus bas)
        val hasBottomIngredientBlock = lines.drop(2).any { line ->
            val lower = line.lowercase().trim()
            val hasServingsHeader = lower.contains("pour") && lower.contains("personnes")
            val parsed = IngredientParser.parse(line, vocabulary.culinaryMeasurementUnits)
            val hasHardIngr = parsed.quantity != null && parsed.name.isNotBlank() && !OcrInstructionUtils.hasActionVerb(line, vocabulary.stepActionKeywords)
            hasServingsHeader || hasHardIngr
        }

        val result = startsWithInstruction && hasBottomIngredientBlock
        Log.d(TAG, "isInvertedRecipe = $result")
        return result
    }

    /**
     * Catégorise une recette inversée (Type 3) :
     * - Passe 1 : Sépare les consignes du haut et la liste d'ingrédients du bas.
     * - Passe 2 : Évalue et répare le titre d'après la liste d'ingrédients du bas.
     *
     * @param lines La liste des lignes de texte scannées.
     * @param vocabulary Le vocabulaire métier OCR.
     * @return L'objet [RawSections] alimenté avec le titre réparé, les instructions, ingrédients et métadonnées.
     */
    fun categorizeInvertedRecipe(
        lines: List<String>, 
        vocabulary: OcrVocabulary, 
        originalCleanedLines: List<String> = emptyList()
    ): RawSections {
        Log.d(TAG, "--- DÉBUT CATÉGORISATION INVERSÉE (TYPE 3) ---")
        val results = RawSections()

        var currentSection = Ocr3Categorizer.SECTION_INSTRUCTIONS
        val wineState = Ocr3CategorizerWine.WineState()
        val seenIngredientKeywords = mutableSetOf<String>()

        // PASSE 1 : Extraction des consignes d'instructions du haut et des ingrédients du bas
        for (line in lines) {
            var workingLine = line.trim()
            if (workingLine.isBlank()) continue

            if (wineState.isWineSectionClosed) continue

            // 1. Détection du basculement en SECTION_INGREDIENTS pour le bloc d'ingrédients du bas
            val lowerLine = workingLine.lowercase()
            val containsAction = OcrInstructionUtils.hasActionVerb(workingLine, vocabulary.stepActionKeywords)
            val isStepNumber = Regex("^\\s*\\d{1,2}\\s*[.)\\-]").containsMatchIn(workingLine)

            val isServingsHeader = !containsAction && !isStepNumber && ((lowerLine.contains("pour") && lowerLine.contains("personnes")) || lowerLine.contains("personnes") || lowerLine.contains("portions"))
            val isIngrHeader = !containsAction && (lowerLine.contains("ingrédients") || lowerLine.contains("ingredients") || lowerLine.contains("composition"))
            val isWineHeader = !containsAction && (lowerLine.contains("conseil vin") || lowerLine.contains("vin conseillé") || lowerLine == "notre" || (Regex("(?i)\\bvin(?:s)?\\b").containsMatchIn(workingLine) && !lowerLine.contains("vinaigre") && !lowerLine.contains("vinaigrette")))

            if (isWineHeader) {
                System.err.println("DEBUG IS_WINE_HEADER MATCHED ON: '$workingLine'")
                currentSection = Ocr3Categorizer.SECTION_NONE
                continue
            }

            if (currentSection == Ocr3Categorizer.SECTION_INSTRUCTIONS) {
                if (isServingsHeader || isIngrHeader) {
                    currentSection = Ocr3Categorizer.SECTION_INGREDIENTS
                    Log.d(TAG, "  [!] BASCULE SECTION INGRÉDIENTS DU BAS DÉTECTÉE (Ligne: '$line')")
                }
            }

            // 2. Extraction des méta-données (Portions, Temps, Kcal, Prix)
            val metaResult = RecipeMetadataParser.extractMetadata(workingLine, results, vocabulary)
            workingLine = metaResult.remainingLine.trim()
            if (metaResult.isPureMetadata || workingLine.isBlank()) continue

            // 3. Traitement des recommandations de vins (uniquement si la ligne ne contient aucun verbe d'action culinaire)
            if (!containsAction && Ocr3CategorizerWine.processWineLine(workingLine, currentSection, false, false, false, vocabulary, wineState)) {
                if (currentSection == Ocr3Categorizer.SECTION_INGREDIENTS) {
                    currentSection = Ocr3Categorizer.SECTION_NONE
                }
                continue
            }

            // 4. Traitement des sources/établissements
            if (!OcrInstructionUtils.hasActionVerb(workingLine, vocabulary.stepActionKeywords) && SourceParser.isSourceLine(workingLine, vocabulary)) {
                val cleanedSource = SourceParser.cleanSourceLine(workingLine, vocabulary)
                if (cleanedSource.isNotBlank() && cleanedSource !in results.detectedSourceList) {
                    results.detectedSourceList.add(cleanedSource)
                }
                continue
            }

            // 5. Classification selon la section active
            when (currentSection) {
                Ocr3Categorizer.SECTION_INGREDIENTS -> {
                    if (!Ocr2Cleaner.isTechnicalDimension(workingLine)) {
                        results.rawIngredientsList.add(workingLine)
                        val parsed = IngredientParser.parse(workingLine, vocabulary.culinaryMeasurementUnits)
                        val keywords = extractKeywordsFromText(parsed.name, parsed.supplementalInfo)
                            .filter { kw -> vocabulary.ingredientPreparationModifiers.none { mod -> mod.equals(kw, ignoreCase = true) } }
                        seenIngredientKeywords.addAll(keywords)
                    }
                }
                else -> {
                    val cleanInstruction = workingLine.replace(OcrRegexes.Beautifier.BULLET_PREFIX, "").trim()
                    if (cleanInstruction.isNotBlank()) {
                        results.rawInstructionsList.add(cleanInstruction)
                    }
                }
            }
        }

        results.detectedWineList.addAll(wineState.detectedWineList)
        results.ingredientKeywords = seenIngredientKeywords

        // PASSE 2 : Extraction et réparation du Titre en utilisant les mots-clés d'ingrédients du bas
        val titleLines = if (originalCleanedLines.isNotEmpty()) originalCleanedLines else lines
        val titleResult = OcrTitleExtractor.extractTitle(titleLines, vocabulary)
        if (titleResult.title != null) {
            results.detectedTitle = titleResult.title
        }

        Log.d(TAG, "--- FIN CATÉGORISATION INVERSÉE (TYPE 3) ---")
        return results
    }

    private fun extractKeywordsFromText(name: String, info: String?): List<String> {
        val textToSplit = if (info.isNullOrBlank()) name else "$name $info"
        return textToSplit.split(Regex("[\\s,.'’()\\-*•/0-9]+"))
            .map { it.trim().lowercase() }
            .filter { it.length >= 3 }
            .distinct()
    }
}
