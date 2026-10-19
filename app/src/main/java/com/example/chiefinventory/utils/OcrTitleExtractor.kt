package com.example.chiefinventory.utils

import android.util.Log

/**
 * Data class representing the result of title extraction.
 */
data class TitleExtractionResult(
    val title: String?,
    val cleanedLines: List<String>
)

private data class ScoredTitleCandidate(
    val candidateText: String,
    val originalLineIndex: Int,
    val originalLinesToSkip: Int,
    val score: Int,
    val scoreDetails: String
)

/**
 * Dedicated step for Title Extraction with Logcat Candidate Scoring and Ingredient Matching.
 */
object OcrTitleExtractor {
    private const val TAG = "RecipeOCR"

    /**
     * Extracts the recipe title from lines by scoring all candidates and logging details in Logcat.
     */
    fun extractTitle(lines: List<String>, vocabulary: OcrVocabulary): TitleExtractionResult {
        val candidates = mutableListOf<ScoredTitleCandidate>()
        val actionVerbs = vocabulary.stepActionKeywords.distinct()
        val containsActionRegex = if (actionVerbs.isEmpty()) null else Regex("(?i)\\b(?:${actionVerbs.joinToString("|")})\\b")
        val hasQuantityRegex = Regex("(?i)^\\d+\\s*(?:g|kg|cl|ml|l|c\\.|cuillè|pincée)")

        val recipeIngredientWords = extractRecipeIngredientWords(lines)
        val firstIngredientWord = findFirstIngredientWord(lines)
        if (recipeIngredientWords.isNotEmpty()) {
            Log.d(TAG, "TITLE EXTRACTOR: Ingrédients détectés sur la fiche pour scoring -> $recipeIngredientWords")
        }

        Log.d(TAG, "=== DEBUT EXTRACTION DU TITRE PAR SCORING ===")

        for (i in lines.indices) {
            val workingLine = lines[i].trim()
            if (workingLine.isEmpty()) continue

            // 1. Détection de titre collé après "sel et poivre" (ex: "sel et poivre alade d'avocat et princesses")
            if (workingLine.lowercase().contains("sel et poivre") && workingLine.length > 12) {
                val parts = workingLine.split(Regex("(?i)sel et poivre\\s*"), 2)
                if (parts.size == 2 && parts[1].isNotBlank()) {
                    var potentialTitle = stripRecipeNumber(parts[1].trim())
                    val lowerPotential = potentialTitle.lowercase()
                    var skipCount = 0
                    if (i + 1 < lines.size && (lowerPotential.endsWith(" et") || lowerPotential.endsWith(" d'") || lowerPotential.endsWith(" de") || lowerPotential.endsWith(" aux"))) {
                        potentialTitle = "$potentialTitle ${lines[i + 1].trim()}"
                        skipCount = 1
                    }
                    potentialTitle = repairFirstWordLettrine(potentialTitle, recipeIngredientWords, vocabulary)
                    if (isBaseCandidate(potentialTitle, vocabulary, containsActionRegex, hasQuantityRegex)) {
                        val (score, details) = evaluateScore(potentialTitle, vocabulary, recipeIngredientWords, firstIngredientWord)
                        candidates.add(ScoredTitleCandidate(potentialTitle, i, skipCount, score, details))
                        continue
                    }
                }
            }

            // 2. Détection de titre sur 2 lignes
            if (i + 1 < lines.size) {
                val nextLine = lines[i + 1].trim()
                val lowerNext = nextLine.lowercase()
                if (lowerNext.startsWith("aux ") || lowerNext.startsWith("et ") || lowerNext.startsWith("à la ") || lowerNext.startsWith("au ")) {
                    val combined = "$workingLine $nextLine"
                    val cleanCombined = repairFirstWordLettrine(stripRecipeNumber(combined), recipeIngredientWords, vocabulary)
                    if (isBaseCandidate(cleanCombined, vocabulary, containsActionRegex, hasQuantityRegex)) {
                        val (score, details) = evaluateScore(cleanCombined, vocabulary, recipeIngredientWords, firstIngredientWord)
                        candidates.add(ScoredTitleCandidate(cleanCombined, i, 1, score, details))
                    }
                }
            }

            // 3. Traitement de la ligne courante (Exclut les lignes ayant des quantités d'ingrédients)
            val parsedRawLine = IngredientParser.parse(workingLine)
            val isIngredientQuantityLine = parsedRawLine.quantity != null && 
                    (parsedRawLine.unit != null || parsedRawLine.quantity!! < 50.0)

            if (!isIngredientQuantityLine) {
                val cleanLine = stripRecipeNumber(workingLine)
                val repairedCandidate = repairFirstWordLettrine(cleanLine, recipeIngredientWords, vocabulary)
                if (isBaseCandidate(repairedCandidate, vocabulary, containsActionRegex, hasQuantityRegex)) {
                    val (score, details) = evaluateScore(repairedCandidate, vocabulary, recipeIngredientWords, firstIngredientWord)
                    candidates.add(ScoredTitleCandidate(repairedCandidate, i, 0, score, details))
                }
            }
        }

        // Logcat de tous les candidats évalués avec leurs détails
        candidates.forEachIndexed { index, candidate ->
            Log.d(TAG, "TITLE CANDIDATE [#$index]: '${candidate.candidateText}' -> SCORE: ${candidate.score} (${candidate.scoreDetails})")
        }

        val bestCandidate = candidates.maxByOrNull { it.score }?.takeIf { it.score > 0 }
        var extractedTitle: String? = null
        val remainingLines = mutableListOf<String>()

        if (bestCandidate != null) {
            extractedTitle = bestCandidate.candidateText
            Log.d(TAG, ">>> TITRE RETENU FINALEMENT: '$extractedTitle' avec SCORE MAX: ${bestCandidate.score}")
        } else {
            Log.d(TAG, ">>> AUCUN TITRE VALIDE RETENU")
        }

        // Reconstruction des lignes restantes sans le titre retenu
        var skipCounter = 0
        for (i in lines.indices) {
            if (skipCounter > 0) {
                skipCounter--
                continue
            }
            val line = lines[i].trim()
            if (line.isEmpty()) continue

            if (bestCandidate != null && i == bestCandidate.originalLineIndex) {
                if (line.lowercase().contains("sel et poivre")) {
                    val parts = line.split(Regex("(?i)sel et poivre\\s*"), 2)
                    remainingLines.add(parts[0].trim() + " sel et poivre")
                }
                skipCounter = bestCandidate.originalLinesToSkip
                continue
            }
            remainingLines.add(line)
        }

        Log.d(TAG, "=== FIN EXTRACTION DU TITRE PAR SCORING ===")
        return TitleExtractionResult(extractedTitle, remainingLines)
    }

    /**
     * Extraie les mots des NOMS d'ingrédients (parsed.name) présents sur la fiche scannée de la recette.
     */
    private fun extractRecipeIngredientWords(lines: List<String>): Set<String> {
        val ingredientNameWords = mutableSetOf<String>()
        val ignoreWords = setOf("pour", "personnes", "portions", "cuillere", "soupe", "cafe", "pincee", "gousses", "grammes", "kilo")
        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.isEmpty()) continue
            if (Regex("^\\d").containsMatchIn(trimmed) || Regex("(?i)\\b(?:g|kg|cl|ml|l|c\\.|cuillè|pincée|gousses?)\\b").containsMatchIn(trimmed)) {
                val parsed = IngredientParser.parse(trimmed)
                if (parsed.name.isNotBlank()) {
                    val words = parsed.name.split(Regex("[\\s,;.:'’\\-()]"))
                        .map { it.lowercase().trim() }
                        .filter { it.length >= 3 && !it.any { c -> c.isDigit() } && it !in ignoreWords }
                    ingredientNameWords.addAll(words)
                    words.forEach { w ->
                        if (w.endsWith("s") || w.endsWith("x")) {
                            ingredientNameWords.add(w.dropLast(1))
                        }
                    }
                }
            }
        }
        return ingredientNameWords
    }

    /**
     * Répaire la lettrine / première lettre du premier mot du titre si le suffixe matche un ingrédient de la fiche.
     * Exemple : "Drabe mangue" -> suffixe "rabe" matche "crabe" -> "Crabe mangue".
     */
    fun repairFirstWordLettrine(titleCandidate: String, candidateIngredients: Set<String>, vocabulary: OcrVocabulary): String {
        val trimmed = titleCandidate.trim()
        val words = trimmed.split(Regex("\\s+"))
        if (words.isEmpty()) return trimmed

        val firstWord = words[0]
        val cleanFirstWord = firstWord.replace(Regex("^[^\\p{L}]+"), "")
        if (cleanFirstWord.length < 3) return trimmed

        val lowerFirst = cleanFirstWord.lowercase()
        val allIngredients = (candidateIngredients + vocabulary.titleKeywords + vocabulary.commonIngredientsNoQty).map { it.lowercase() }.distinct()

        if (allIngredients.any { it == lowerFirst }) {
            return trimmed
        }

        var matchedIngredient = allIngredients.firstOrNull { ing ->
            ing.length > lowerFirst.length && ing.endsWith(lowerFirst)
        }

        if (matchedIngredient == null && lowerFirst.length >= 3) {
            val suffix = lowerFirst.drop(1)
            matchedIngredient = allIngredients.firstOrNull { ing ->
                ing.length > suffix.length && ing.endsWith(suffix)
            }
        }

        if (matchedIngredient != null) {
            val repairedFirstWord = matchedIngredient.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
            val repairedText = (listOf(repairedFirstWord) + words.drop(1)).joinToString(" ")
            Log.d(TAG, "TITLE EXTRACTOR: Lettrine réparée -> '$titleCandidate' devenant '$repairedText' (via '$matchedIngredient')")
            return repairedText
        }

        return trimmed
    }

    /**
     * Recherche le nom du premier ingrédient de la recette pour le départage.
     */
    private fun findFirstIngredientWord(lines: List<String>): String? {
        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.isEmpty()) continue
            if (Regex("^\\d").containsMatchIn(trimmed) || Regex("(?i)\\b(?:g|kg|cl|ml|l|c\\.|cuillè|pincée)\\b").containsMatchIn(trimmed)) {
                val parsed = IngredientParser.parse(trimmed)
                if (parsed.name.isNotBlank()) {
                    val words = parsed.name.split(Regex("[\\s,;.:'’\\-()]"))
                        .map { it.lowercase().trim() }
                        .filter { it.length >= 3 && it !in listOf("de", "du", "des", "d'", "la", "le", "les", "pour") }
                    if (words.isNotEmpty()) {
                        return words.first()
                    }
                }
            }
        }
        return null
    }

    /**
     * Évalue et calcule le score d'une ébauche de titre d'après les critères XML et linguistiques.
     */
    private fun evaluateScore(
        candidate: String, 
        vocabulary: OcrVocabulary, 
        recipeIngredientWords: Set<String>, 
        firstIngredientWord: String?
    ): Pair<Int, String> {
        var score = 0
        val details = mutableListOf<String>()

        val trimmed = candidate.trim()
        val lower = trimmed.lowercase()

        // Critère 1 : Ponctuation de fin (Un titre ne finit pas par un point)
        if (trimmed.endsWith(".") || trimmed.endsWith("!") || trimmed.endsWith("?")) {
            score -= 20
            details.add("ponctuation_fin:-20")
        } else {
            score += 10
            details.add("sans_point_fin:+10")
        }

        // Critère 2 : Mots-clés de titre (fichiers XML : title_keywords.xml)
        val matchedTitleKeywords = vocabulary.titleKeywords.filter { kw ->
            Regex("(?i)(?<!\\p{L})${Regex.escape(kw)}(?!\\p{L})").containsMatchIn(trimmed)
        }
        if (matchedTitleKeywords.isNotEmpty()) {
            val pts = matchedTitleKeywords.size * 30
            score += pts
            details.add("mots_clés_titre($matchedTitleKeywords):+$pts")
        }

        // Critère 3 : Connecteurs de titre (et, aux, à la, au)
        val connectors = listOf(" et ", " aux ", " à la ", " au ", " d'", " de ")
        val matchedConnectors = connectors.filter { lower.contains(it) }
        if (matchedConnectors.isNotEmpty()) {
            val pts = matchedConnectors.size * 10
            score += pts
            details.add("connecteurs_titre($matchedConnectors):+$pts")
        }

        // Critère 4 : Longueur en mots (Un titre comporte au moins 2 mots)
        val wordCount = trimmed.split(Regex("\\s+")).size
        if (wordCount >= 2) {
            score += 10
            details.add("plus_de_2_mots:+10")
        }

        // Critère 5 : Préposition d'ingrédient au début (ex: "d'huile", "de farine")
        if (lower.startsWith("d'") || lower.startsWith("de ") || lower.startsWith("du ") || lower.startsWith("des ")) {
            score -= 25
            details.add("debut_preposition_ingr:-25")
        }

        // Critère 6 : Mots d'ingrédients présents sur la fiche scannée de la recette (+15 pts par ingrédient)
        val matchedRecipeIngredients = recipeIngredientWords.filter { ingWord ->
            val stem = ingWord.removeSuffix("s").removeSuffix("x")
            Regex("(?i)\\b${Regex.escape(stem)}(?:s|x)?\\b").containsMatchIn(trimmed)
        }
        if (matchedRecipeIngredients.isNotEmpty()) {
            val pts = matchedRecipeIngredients.size * 15
            score += pts
            details.add("ingrédient_fiche_recette($matchedRecipeIngredients):+$pts")
        }

        // Critère 7 : Départage par le premier ingrédient de la recette (+30 pts)
        if (firstIngredientWord != null) {
            val stem = firstIngredientWord.removeSuffix("s").removeSuffix("x")
            if (Regex("(?i)\\b${Regex.escape(stem)}(?:s|x)?\\b").containsMatchIn(trimmed)) {
                score += 30
                details.add("premier_ingrédient($firstIngredientWord):+30")
            }
        }

        return Pair(score, details.joinToString(", "))
    }

    private fun isBaseCandidate(
        line: String,
        vocabulary: OcrVocabulary,
        containsActionRegex: Regex?,
        hasQuantityRegex: Regex
    ): Boolean {
        val trimmed = line.trim()
        if (trimmed.length < 5 || trimmed.length > 60) return false
        val wordList = trimmed.split(Regex("\\s+"))
        if (wordList.size < 2 && vocabulary.titleKeywords.none { it.equals(trimmed, ignoreCase = true) }) return false
        if (trimmed.endsWith(".") || trimmed.endsWith("!") || trimmed.endsWith("?")) return false
        if (Regex("(?i)^(?:[1-9]|1[0-9]|20)\\s+[a-zA-Z]").containsMatchIn(line.trim())) return false
        if (trimmed.contains("(") || trimmed.contains(")") || trimmed.contains("[")) return false
        if (hasQuantityRegex.containsMatchIn(trimmed)) return false
        if (Regex("(?i)\\b\\d/\\d\\b").containsMatchIn(trimmed)) return false
        if (Regex("(?i)\\bc\\.?\\s*[àa]\\s*(?:soupe|café|dessert)").containsMatchIn(trimmed)) return false
        if (containsActionRegex?.containsMatchIn(trimmed) == true) return false
        if (trimmed.startsWith("•") || trimmed.startsWith("-") || trimmed.startsWith("*")) return false

        if (vocabulary.excludedOcrKeywords.any { it.equals(trimmed, ignoreCase = true) }) return false

        val cleanIngName = trimmed.replace(Regex("(?i)^(?:d'|de\\s+|du\\s+|des\\s+|de\\s+la\\s+|de\\s+l')"), "").trim()
        if (vocabulary.commonIngredientsNoQty.any { it.equals(trimmed, ignoreCase = true) || it.equals(cleanIngName, ignoreCase = true) }) return false

        if (vocabulary.ingredientPreparationModifiers.any { Regex("(?i)(?<!\\p{L})${Regex.escape(it)}(?!\\p{L})").containsMatchIn(trimmed) }) return false

        if (OcrHelperUtils.isLikelyProperNameOrSource(trimmed) || SourceParser.isSourceLine(trimmed, vocabulary)) return false
        if (WineParser.isWineLine(trimmed, vocabulary)) return false

        val lower = trimmed.lowercase()
        if (lower.startsWith("d'") || lower.startsWith("de ") || lower.startsWith("du ") || lower.startsWith("des ") || lower.startsWith("de la ") || lower.startsWith("de l'")) return false
        if (lower.startsWith("ingrédients") || lower.startsWith("ingredients") || lower.startsWith("préparation") || lower.startsWith("pour ") || lower.startsWith("le jus") || lower.startsWith("jus de")) return false
        if (lower.contains("personnes") || lower.contains("portions")) return false

        val lastWord = lower.split(Regex("\\s+")).lastOrNull() ?: ""
        if (lastWord in listOf("de", "d'", "pour", "du", "des", "avec", "en", "le", "la", "les", "un", "une", "et", "ou", "ce", "que")) return false

        val letterCount = trimmed.count { it.isLetter() }
        val digitCount = trimmed.count { it.isDigit() }
        return letterCount >= 4 && digitCount <= 2
    }

    private fun stripRecipeNumber(text: String): String {
        var cleaned = text.trim()
        cleaned = cleaned.replace(Regex("(?i)^\\d{1,3}\\s+(?!(?:g|kg|cl|ml|l|c\\.|cuillè|pincée|soupe|café|gousse|gousses|botte|bocal|tranche|tranches)\\b)"), "")
        if (cleaned.isNotEmpty()) {
            cleaned = cleaned.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        }
        return cleaned
    }
}
