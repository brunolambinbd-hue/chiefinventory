package com.chefinventory.app.utils

import com.parabd.lib.traceability.utils.LogManager

/**
 * Assistant dédié à la détection et à la fusion des lignes de quantités d'ingrédients inachevées lors de l'Étape 3 (Catégorisation).
 *
 * Exemple : "1 cuillère à soupe" (Ligne N) + "de moutarde" (Ligne N+1) -> "1 cuillère à soupe de moutarde".
 *
 * Ce sous-composant permet de maintenir la charge cognitive minimale dans [Ocr3Categorizer].
 */
@Suppress("FoldInitializerAndIfNull", "kotlin:S6529")
object Ocr3CategorizerUnfinishedIngredient {
    private const val TAG = "RecipeOCR"

    /**
     * Tente de fusionner la ligne courante [workingLine] avec la dernière ligne enregistrée dans [rawIngredientsList]
     * si celle-ci ne comporte qu'une quantité + unité sans nom d'ingrédient fini (ex: "1 cuillère à soupe", "250 g").
     *
     * @param workingLine La ligne de texte courante analysée.
     * @param currentSection La section actuelle du catégoriseur principal (doit être [Ocr3Categorizer.SECTION_INGREDIENTS]).
     * @param containsAction `true` si la ligne contient un verbe d'action de cuisine.
     * @param startsWithBullet `true` si la ligne commence par une puce d'instruction.
     * @param vocabulary Le vocabulaire métier OCR contenant les unités culinaires.
     * @param rawIngredientsList La liste mutable des lignes d'ingrédients bruts.
     * @param seenIngredientKeywords L'ensemble mutable des mots-clés d'ingrédients vus.
     * @return `true` si la ligne a été fusionnée et doit être sautée dans le catégoriseur principal, `false` sinon.
     */
    fun tryMergeUnfinishedQuantityLine(
        workingLine: String,
        currentSection: Int,
        containsAction: Boolean,
        startsWithBullet: Boolean,
        vocabulary: OcrVocabulary,
        rawIngredientsList: MutableList<String>,
        seenIngredientKeywords: MutableSet<String>
    ): Boolean {
        if (currentSection != Ocr3Categorizer.SECTION_INGREDIENTS) return false
        if (containsAction || startsWithBullet) return false
        if (rawIngredientsList.isEmpty()) return false

        val lastRawIng = rawIngredientsList.last().trim()
        val lastLower = lastRawIng.lowercase()

        // 1. Analyse du nom de la dernière ligne
        val parsedLast = IngredientParser.parse(lastRawIng, vocabulary.culinaryMeasurementUnits)

        // S'il existe déjà un véritable nom d'ingrédient (ex: "persil" dans "2 c. à soupe de persil haché"), la ligne est complète !
        val units = vocabulary.culinaryMeasurementUnits
        val hasIngredientNoun = parsedLast.name.isNotBlank() &&
            parsedLast.name.split(Regex("[\\s,.'’()\\-*•/0-9]+")).any { word ->
                val cleanWord = word.lowercase().replace(Regex("[^\\p{L}]"), "")
                cleanWord.length >= 3 &&
                units.none { unit -> unit.equals(cleanWord, ignoreCase = true) } &&
                vocabulary.ingredientPreparationModifiers.none { mod -> mod.equals(cleanWord, ignoreCase = true) }
            }

        if (hasIngredientNoun) {
            return false // Ingrédient déjà complet, ne pas fusionner !
        }

        // Test 1 : Nom vide ou ne contenant qu'une unité (ex: "1 cuillère à soupe", "2 c. à soupe")
        val isBlankName = parsedLast.quantity != null && (parsedLast.name.isBlank() || parsedLast.name.matches(Regex("(?i)^(?:soupe|café|cafe|c\\.?\\s*[àa]\\s*(?:soupe|café)|cuillère|cuillère\\s+à\\s+soupe|cuillère\\s+à\\s+café|g|kg|cl|ml|l)$")))

        // Test 2 : Adjectif de taille/modificateur seul avec quantité (ex: "1 petite")
        val isModifierName = if (parsedLast.quantity != null) {
            vocabulary.ingredientPreparationModifiers.any { mod ->
                if (parsedLast.name.equals(mod, ignoreCase = true)) {
                    true
                } else {
                    lastLower.endsWith(" $mod")
                }
            }
        } else {
            false
        }

        // Test 3 : Se termine par une unité culinaire isolée
        val allUnits = vocabulary.culinaryMeasurementUnits.map { it.lowercase() }.distinct()

        val endsWithUnit = allUnits.any { unit ->
            val cleanUnit = unit.trim().lowercase()
            lastLower.endsWith(" $cleanUnit") || lastLower == cleanUnit
        }

        if (isBlankName || endsWithUnit || isModifierName) {
            val lastIdx = rawIngredientsList.lastIndex
            rawIngredientsList[lastIdx] = "$lastRawIng $workingLine"
            val parsedNew = IngredientParser.parse(rawIngredientsList[lastIdx], vocabulary.culinaryMeasurementUnits)
            if (parsedNew.name.isNotBlank()) {
                val rawWords = parsedNew.name.split(Regex("[\\s,.'’()\\-*•/0-9]+"))
                val keywords = rawWords.filter { word ->
                    if (word.length < 3) return@filter false
                    val isModifier = vocabulary.ingredientPreparationModifiers.any { mod ->
                        mod.equals(word, ignoreCase = true)
                    }
                    !isModifier
                }
                seenIngredientKeywords.addAll(keywords)
            } else {
                if (vocabulary.logOcr3UnfinishedIngrStillEmpty.isNotBlank()) {
                    LogManager.d(TAG, String.format(vocabulary.logOcr3UnfinishedIngrStillEmpty, rawIngredientsList[lastIdx]))
                }
            }
            if (vocabulary.logOcr3UnfinishedIngrFused.isNotBlank()) {
                LogManager.d(TAG, String.format(vocabulary.logOcr3UnfinishedIngrFused, workingLine, rawIngredientsList[lastIdx]))
            }
            return true
        }

        return false
    }
}
