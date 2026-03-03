package com.example.chiefinventory.utils

import android.content.res.Resources
import com.example.chiefinventory.R

/**
 * Data class representing the structured result of an OCR recipe scan.
 */
data class RecipeOcrResult(
    val title: String? = null,
    val ingredients: String? = null,
    val instructions: String? = null,
    val wine: String? = null,
    val source: String? = null,
    val servings: String? = null
)

/**
 * Main orchestrator for Recipe OCR parsing.
 * It uses specialized parsers for each field to ensure modularity.
 */
object RecipeOcrParser {

    fun parse(fullText: String, res: Resources): RecipeOcrResult {
        // Traitement spécial : le caractère '|' est souvent un séparateur de ligne mal lu.
        val processedText = fullText.replace("|", "\n|")
        val lines = processedText.lines().map { it.trim() }.filter { it.isNotBlank() }
        if (lines.isEmpty()) return RecipeOcrResult()

        // Chargement des ressources pour les sous-parseurs
        val wineRes = WineParser.loadResources(res)
        val sourceRes = SourceParser.loadResources(res)
        val menuCategoryKeywords = res.getStringArray(R.array.menu_category_keywords).toList()
        val stepActionKeywords = res.getStringArray(R.array.step_action_keywords).toList()

        val ingredientHeaderKeywords = listOf("ingrédients", "ingredients", "composition")
        val instructionHeaderKeywords = listOf("préparation", "instructions", "étapes", "réalisation", "méthode", "progression")
        
        val stepStartRegex = Regex("\\b(?:${stepActionKeywords.joinToString("|")})\\b", RegexOption.IGNORE_CASE)
        val qtyRegex = Regex("^[|Il!\\d\\-*]")

        var currentSection = 0 // 0: None, 1: Ingredients, 2: Instructions
        val rawIngredientsList = mutableListOf<String>()
        val instructionsList = mutableListOf<String>()
        val detectedWineList = mutableListOf<String>()
        val detectedSourceList = mutableListOf<String>()
        var detectedServings: String? = null
        var detectedTitle: String? = lines[0]

        for (line in lines) {
            val lowerLine = line.lowercase()

            // 1. Champ SOURCE (Coordonnées, Hôtels, etc.)
            if (SourceParser.isSourceLine(line, sourceRes)) {
                detectedSourceList.add(SourceParser.cleanSourceLine(line, sourceRes))
                continue
            }

            // 2. Champ VIN (Appellations, Cépages, etc.)
            if (WineParser.isWineLine(line, wineRes)) {
                detectedWineList.add(WineParser.cleanWineLine(line, wineRes))
                continue
            }

            // 3. Champ PORTIONS
            val servingsRegex = Regex("(?:pour|serves|portions?|servings?|pers\\.?|personnes?)\\s*:?\\s*(\\d+)", RegexOption.IGNORE_CASE)
            val alternateServingsRegex = Regex("(\\d+)\\s*(?:pers\\.?|personnes?|portions?|servings?)", RegexOption.IGNORE_CASE)
            val servingsMatch = servingsRegex.find(line) ?: alternateServingsRegex.find(line)
            if (servingsMatch != null) {
                if (detectedServings == null) detectedServings = servingsMatch.groupValues[1]
                continue
            }

            // 4. EXCLUSIONS (Titres de menu vides de sens pour les stocks)
            if (menuCategoryKeywords.any { lowerLine.contains(it) } && !lowerLine.contains(Regex("\\d"))) {
                continue
            }

            // 5. LOGIQUE DE BASCULE (Ingrédients vs Instructions)
            val looksLikeIngredient = qtyRegex.containsMatchIn(line.take(5)) && !line.contains(Regex("^\\d+\\."))
            val isActionVerb = stepStartRegex.containsMatchIn(line)

            if (isActionVerb || instructionHeaderKeywords.any { lowerLine.contains(it) }) {
                currentSection = 2
                if (instructionHeaderKeywords.any { lowerLine.contains(it) }) continue
            } else if (looksLikeIngredient || ingredientHeaderKeywords.any { lowerLine.contains(it) }) {
                currentSection = 1
                if (ingredientHeaderKeywords.any { lowerLine.contains(it) }) continue
            }

            // 6. REMPLISSAGE DES CHAMPS
            when (currentSection) {
                1 -> rawIngredientsList.add(line)
                2 -> instructionsList.add(line)
                else -> {
                    if (line != lines[0]) {
                        instructionsList.add(line)
                        if (line.length > 30) currentSection = 2
                    }
                }
            }
        }

        // FUSION ET NETTOYAGE DES INGRÉDIENTS (via IngredientParser)
        val finalIngredients = mutableListOf<String>()
        if (rawIngredientsList.isNotEmpty()) {
            var currentIng = IngredientParser.preClean(rawIngredientsList[0])
            for (i in 1 until rawIngredientsList.size) {
                val nextLine = IngredientParser.preClean(rawIngredientsList[i])
                val nextIsNew = qtyRegex.containsMatchIn(nextLine.take(5))
                if (!nextIsNew && nextLine.length > 2) {
                    currentIng += " $nextLine"
                } else {
                    finalIngredients.add(cleanIngredientSemantics(currentIng))
                    currentIng = nextLine
                }
            }
            finalIngredients.add(cleanIngredientSemantics(currentIng))
        }

        return RecipeOcrResult(
            title = detectedTitle,
            ingredients = finalIngredients.joinToString("\n"),
            instructions = instructionsList.joinToString("\n"),
            wine = if (detectedWineList.isNotEmpty()) detectedWineList.joinToString(" ") else null,
            source = if (detectedSourceList.isNotEmpty()) detectedSourceList.joinToString(", ") else null,
            servings = detectedServings
        )
    }

    private fun cleanIngredientSemantics(text: String): String {
        return text.replace(Regex("\\(.*?\\)"), "")
            .replace(Regex("(?i)\\b(ingrédients|ingredients|personnes|portions|pers\\.?)\\b"), "")
            .replace(Regex("\\s{2,}"), " ")
            .trim()
    }
}
