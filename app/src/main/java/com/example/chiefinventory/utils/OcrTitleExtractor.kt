package com.example.chiefinventory.utils

import android.util.Log
import java.util.regex.Matcher

/**
 * Conteneur du résultat d'extraction du titre d'une recette.
 *
 * @property title Le titre extrait et nettoyé de la recette, ou null si aucun titre valide n'est trouvé.
 * @property cleanedLines Les lignes de texte restantes après extraction du titre.
 */
data class TitleExtractionResult(
    val title: String?,
    val cleanedLines: List<String>
)

/**
 * Conteneur interne représentant un candidat titre évalué avec son score et le détail du calcul.
 *
 * @property candidateText Le texte nettoyé du candidat titre.
 * @property originalLineIndex L'index de ligne d'origine.
 * @property originalLinesToSkip Le nombre de lignes adjacentes fusionnées à ignorer (ex: 1 pour un titre sur 2 lignes).
 * @property score Le score numérique attribué au candidat.
 * @property scoreDetails Le détail explicatif du calcul du score.
 */
private data class ScoredTitleCandidate(
    val candidateText: String,
    val originalLineIndex: Int,
    val originalLinesToSkip: Int,
    val score: Int,
    val scoreDetails: String
)

/**
 * Étape 2.5 du pipeline OCR : Extraction et Scoring du Titre de la Recette.
 *
 * Responsabilités :
 * - Évaluer et scorer tous les candidats titres possibles d'une fiche de recette.
 * - Supprimer les numéros de recette en début de titre (ex: "12 Salade grecque" -> "Salade grecque").
 * - Détecter les titres multi-lignes reliés par des connecteurs ("Büchettes de melon" \n "au jambon italien").
 * - Réparer les lettrines et majuscules déformées par l'OCR (`ocr_step25_title_repairs.xml` & corrélation avec les ingrédients).
 * - Sélectionner le candidat ayant le score le plus élevé et retourner la liste des lignes restantes.
 * - Consommer les expressions régulières pré-compilées de [OcrRegexes.Titles].
 */
object OcrTitleExtractor {
    private const val TAG = "RecipeOCR"

    /**
     * Extrait le titre principal de la recette d'après la liste des lignes nettoyées [lines] et le vocabulaire [vocabulary].
     *
     * @param lines La liste des lignes de texte nettoyées issues de [Ocr2Cleaner].
     * @param vocabulary Le vocabulaire métier data-driven contenant les dictionnaires XML du projet.
     * @return L'objet [TitleExtractionResult] contenant le titre retenu et la liste des lignes restantes.
     */
    fun extractTitle(lines: List<String>, vocabulary: OcrVocabulary): TitleExtractionResult {
        val candidates = mutableListOf<ScoredTitleCandidate>()
        val actionVerbs = vocabulary.stepActionKeywords.distinct()
        val containsActionRegex = if (actionVerbs.isEmpty()) null else OcrRegexes.getInstructionPrefixPattern(actionVerbs)

        val recipeIngredientWords = extractRecipeIngredientWords(lines)
        val firstIngredientWord = findFirstIngredientWord(lines, vocabulary)
        if (recipeIngredientWords.isNotEmpty()) {
            Log.d(TAG, "TITLE EXTRACTOR: Ingrédients détectés sur la fiche pour scoring -> $recipeIngredientWords")
        }

        Log.d(TAG, "=== DEBUT EXTRACTION DU TITRE PAR SCORING ===")

        for (i in lines.indices) {
            val workingLine = lines[i].trim()
            if (workingLine.isEmpty()) continue

            // 1. Détection de titre collé après "sel et poivre" (ex: "sel et poivre alade d'avocat et princesses")
            if (workingLine.lowercase().contains("sel et poivre") && workingLine.length > 12) {
                val parts = workingLine.split(OcrRegexes.Titles.SPLIT_SEL_POIVRE, 2)
                if (parts.size == 2 && parts[1].isNotBlank()) {
                    var potentialTitle = stripRecipeNumber(parts[1].trim())
                    val lowerPotential = potentialTitle.lowercase()
                    var skipCount = 0
                    if (i + 1 < lines.size && (lowerPotential.endsWith(" et") || lowerPotential.endsWith(" d'") || lowerPotential.endsWith(" de") || lowerPotential.endsWith(" aux") || isMultiLineTitleConnector(lines[i + 1], vocabulary))) {
                        potentialTitle = "$potentialTitle ${lines[i + 1].trim()}"
                        skipCount = 1
                    }
                    potentialTitle = repairFirstWordLettrine(potentialTitle, recipeIngredientWords, vocabulary)
                    if (isBaseCandidate(potentialTitle, vocabulary, containsActionRegex)) {
                        val (score, details) = evaluateScore(potentialTitle, vocabulary, recipeIngredientWords, firstIngredientWord)
                        candidates.add(ScoredTitleCandidate(potentialTitle, i, skipCount, score, details))
                        continue
                    }
                }
            }

            // 2. Détection de titre sur 2 lignes (ex: "Büchettes de melon" \n "au jambon italien et au porto")
            if (i + 1 < lines.size) {
                val nextLine = lines[i + 1].trim()
                if (isMultiLineTitleConnector(nextLine, vocabulary) && !OcrInstructionUtils.hasActionVerb(nextLine, vocabulary.stepActionKeywords)) {
                    val combined = "$workingLine $nextLine"
                    val cleanCombined = repairFirstWordLettrine(stripRecipeNumber(combined), recipeIngredientWords, vocabulary)
                    val baseCand = isBaseCandidate(cleanCombined, vocabulary, containsActionRegex)
                    if (baseCand) {
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
                val cleanLine = postProcessTitle(stripRecipeNumber(workingLine), vocabulary).replace(Regex("\\s+[a-zA-Z]$"), "").trim()
                val repairedCandidate = repairFirstWordLettrine(cleanLine, recipeIngredientWords, vocabulary)
                if (isBaseCandidate(repairedCandidate, vocabulary, containsActionRegex)) {
                    val (score, details) = evaluateScore(repairedCandidate, vocabulary, recipeIngredientWords, firstIngredientWord)
                    candidates.add(ScoredTitleCandidate(repairedCandidate, i, 0, score, details))
                }
            }
        }

        // Logcat de tous les candidats évalués avec leurs détails
        candidates.forEachIndexed { index, candidate ->
            System.err.println("TITLE CANDIDATE [#$index]: '${candidate.candidateText}' -> SCORE: ${candidate.score} (${candidate.scoreDetails})")
            Log.d(TAG, "TITLE CANDIDATE [#$index]: '${candidate.candidateText}' -> SCORE: ${candidate.score} (${candidate.scoreDetails})")
        }

        val bestCandidate = candidates.maxByOrNull { it.score }?.takeIf { it.score > 0 }
        var extractedTitle: String? = null
        val remainingLines = mutableListOf<String>()

        if (bestCandidate != null) {
            extractedTitle = postProcessTitle(bestCandidate.candidateText, vocabulary)
            Log.d(TAG, ">>> TITRE RETENU INITIALEMENT: '$extractedTitle' avec SCORE MAX: ${bestCandidate.score}")
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
                    val parts = line.split(OcrRegexes.Titles.SPLIT_SEL_POIVRE, 2)
                    remainingLines.add(parts[0].trim() + " sel et poivre")
                }
                skipCounter = bestCandidate.originalLinesToSkip
                continue
            }
            remainingLines.add(line)
        }

        // Complétion grammaticale du titre si le titre se termine par un adjectif/déterminant suspendu
        if (extractedTitle != null) {
            val lowerTitle = extractedTitle.lowercase().trim()
            val trailingAdjRegex = Regex("(?i)\\b(?:petite|petites|grand|grande|grands|grandes|beau|beaux|belle|belles|façon|facon|fa\u00e7ons|facons|sa|son|ses|aux|au|avec)\\s*[a-z]?$")
            if (trailingAdjRegex.containsMatchIn(lowerTitle)) {
                val isFeminineAdj = lowerTitle.endsWith("petite") || lowerTitle.endsWith("petite s") || lowerTitle.endsWith("grande") || lowerTitle.endsWith("belle")
                val numberWords = setOf("un", "une", "deux", "trois", "quatre", "cinq", "six", "sept", "huit", "neuf", "dix")
                val orphanNounIdx = remainingLines.indexOfFirst { l ->
                    val cleanL = l.lowercase().trim().removeSuffix(".")
                    val matchesGender = if (isFeminineAdj) (cleanL.endsWith("e") || cleanL == "salade") else true
                    cleanL.length in 3..20 && !cleanL.contains(" ") && matchesGender && cleanL !in numberWords &&
                    !OcrInstructionUtils.hasActionVerb(cleanL, vocabulary.stepActionKeywords) &&
                    cleanL !in listOf("les", "des", "pour", "dans", "sur", "avec", "par", "tout", "melon")
                }
                if (orphanNounIdx != -1) {
                    val orphanNoun = remainingLines[orphanNounIdx].trim().removeSuffix(".")
                    val baseTitle = extractedTitle.replace(Regex("\\s+[a-zA-Z]$"), "").trim()
                    extractedTitle = "$baseTitle $orphanNoun"
                    remainingLines.removeAt(orphanNounIdx)
                    Log.d(TAG, "TITLE EXTRACTOR: Title completed with orphan noun -> '$extractedTitle' (consumed line: '$orphanNoun')")
                }
            }
        }

        Log.d(TAG, "=== FIN EXTRACTION DU TITRE PAR SCORING ===")
        return TitleExtractionResult(extractedTitle, remainingLines)
    }

    /**
     * Extraie les mots des noms d'ingrédients présents sur la fiche scannée de la recette.
     *
     * @param lines La liste des lignes brutes de la recette.
     * @return L'ensemble des mots significatifs d'ingrédients de longueur >= 3.
     */
    private fun extractRecipeIngredientWords(lines: List<String>): Set<String> {
        val ingredientNameWords = mutableSetOf<String>()
        val ignoreWords = setOf("pour", "personnes", "portions", "cuillere", "soupe", "cafe", "pincee", "gousses", "grammes", "kilo")
        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.isEmpty()) continue
            if (OcrRegexes.Titles.STARTS_WITH_DIGIT.containsMatchIn(trimmed) || OcrRegexes.Titles.INGREDIENT_UNIT_WORD.containsMatchIn(trimmed)) {
                val parsed = IngredientParser.parse(trimmed)
                if (parsed.name.isNotBlank()) {
                    val words = parsed.name.split(OcrRegexes.Titles.PUNCTUATION_SPLIT)
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
     * Post-traite et applique les réparations orthographiques du dictionnaire XML [vocabulary.titleSpellingRepairs] au titre retenu.
     *
     * @param title Le titre à réparer.
     * @param vocabulary Le vocabulaire métier contenant les réparations de titres (`ocr_step25_title_repairs.xml`).
     * @return Le titre réparé.
     */
    fun postProcessTitle(title: String, vocabulary: OcrVocabulary): String {
        var result = title.trim()
        for (repair in vocabulary.titleSpellingRepairs) {
            val parts = repair.split("|")
            if (parts.size == 2) {
                val wrong = parts[0].trim().replace("\\'", "'")
                val correct = parts[1].trim().replace("\\'", "'")
                val pattern = OcrRegexes.getSpellingRepairPattern(wrong)
                result = pattern.matcher(result).replaceAll(Matcher.quoteReplacement(correct))
            }
        }
        return result
    }

    /**
     * Vérifie si une ligne secondaire commence par un connecteur ou une préposition de suite de titre multi-lignes.
     *
     * @param line La ligne secondaire à analyser.
     * @param vocabulary Le vocabulaire métier data-driven (`ocr_step25_title_repairs.xml`).
     * @return `true` si la ligne commence par un connecteur de titre (ex: "au jambon", "aux crevettes"), `false` sinon.
     */
    fun isMultiLineTitleConnector(line: String, vocabulary: OcrVocabulary? = null): Boolean {
        val lower = line.lowercase().trim()
        val defaultConnectors = listOf("au ", "aux ", "et ", "à la ", "a la ", "à ", "de ", "d'", "du ", "des ", "avec ", "sur ")
        val connectors = vocabulary?.titleMultilineConnectors?.map { " ${it.trim().lowercase()} " }
            ?: defaultConnectors
        return connectors.any { lower.startsWith(it.trimStart()) || lower.startsWith(it.trim()) }
    }

    /**
     * Répare la lettrine / première lettre du premier mot du titre si le suffixe matche un ingrédient de la fiche scannée.
     *
     * @param titleCandidate Le candidat titre à réparer (ex: "Drabe mangue").
     * @param candidateIngredients Les mots d'ingrédients inventoriés sur la fiche (ex: "crabe").
     * @param vocabulary Le vocabulaire métier OCR.
     * @return Le titre réparé (ex: "Crabe mangue").
     */
    fun repairFirstWordLettrine(titleCandidate: String, candidateIngredients: Set<String>, vocabulary: OcrVocabulary): String {
        val trimmed = titleCandidate.trim()
        val words = trimmed.split(Regex("\\s+"))
        if (words.isEmpty()) return trimmed

        val firstWord = words[0]
        val cleanFirstWord = firstWord.replace(OcrRegexes.Titles.NON_LETTER_PREFIX, "").replace(OcrRegexes.Titles.SLASH_PIPE_SYMBOLS, "")
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
     * Recherche le nom du premier ingrédient de la recette pour le départage des scores.
     *
     * @param lines La liste des lignes brutes de la recette.
     * @return Le premier mot d'ingrédient significatif trouvé, ou null.
     */
    private fun findFirstIngredientWord(lines: List<String>, vocabulary: OcrVocabulary? = null): String? {
        val stopWords = (vocabulary?.standaloneArticles ?: listOf("de", "du", "des", "d'", "la", "le", "les", "pour")).map { it.trim().lowercase() }
        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.isEmpty()) continue
            if (OcrRegexes.Titles.STARTS_WITH_DIGIT.containsMatchIn(trimmed) || OcrRegexes.Titles.INGREDIENT_UNIT_WORD.containsMatchIn(trimmed)) {
                val parsed = IngredientParser.parse(trimmed)
                if (parsed.name.isNotBlank()) {
                    val words = parsed.name.split(OcrRegexes.Titles.PUNCTUATION_SPLIT)
                        .map { it.lowercase().trim() }
                        .filter { it.length >= 3 && it !in stopWords }
                    if (words.isNotEmpty()) {
                        return words.first()
                    }
                }
            }
        }
        return null
    }

    /**
     * Évalue et calcule le score numérique d'un candidat titre d'après un barème de 7 critères pondérés.
     *
     * **Barème de Scoring des Titres :**
     * - **Critère 1 : Ponctuation de fin (+10 pts / -20 pts)**  
     *   Un titre légitime ne se termine presque jamais par un point. (+10 si sans point final, -20 si point/exclamation/interrogation).
     * - **Critère 2 : Mots-clés du dictionnaire `ocr_shared_title_keywords.xml` (+30 pts par mot-clé)**  
     *   Présence de mots récurrents dans les titres de recettes (ex: *"Salade"*, *"Rôti"*, *"Gratin"*, *"Tarte"*, *"Potage"*, *"Gâteau"*).
     * - **Critère 3 : Connecteurs de titre (+10 pts par connecteur)**  
     *   Présence de prépositions de liaison culinaire (*" et "*, *" aux "*, *" à la "*, *" au "*, *" d'"*, *" de "*).
     * - **Critère 4 : Longueur minimale en mots (+10 pts)**  
     *   Bonus si le candidat comporte au moins 2 mots (évite les mots isolés).
     * - **Critère 5 : Pénalité de préposition d'ingrédient initiale (-25 pts)**  
     *   Pénalise un titre commençant par *"d'"*, *"de "*, *"du "* ou *"des "* (caractéristique d'une ligne d'ingrédient tronquée).
     * - **Critère 6 : Mots d'ingrédients de la fiche scannée (+15 pts par ingrédient)**  
     *   Bonus si le titre contient des mots d'ingrédients inventoriés dans la recette (ex: *"mangue"*, *"crabe"*, *"aubergines"*).
     * - **Critère 7 : Départage par le premier ingrédient (+30 pts)**  
     *   Bonus si le titre contient le nom du tout premier ingrédient de la liste.
     *
     * @param candidate Le texte du candidat titre à évaluer.
     * @param vocabulary Le vocabulaire métier data-driven (`ocr_shared_title_keywords.xml`).
     * @param recipeIngredientWords Les mots d'ingrédients inventoriés sur la fiche scannée.
     * @param firstIngredientWord Le nom du premier ingrédient de la recette pour le départage.
     * @return Une paire `(scoreTotal, detailReglesCalculees)` permettant d'expliquer le score dans les logs Logcat.
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

        // Critère 2 : Mots-clés de titre (fichiers XML : ocr_shared_title_keywords.xml)
        val matchedTitleKeywords = vocabulary.titleKeywords.filter { kw ->
            OcrRegexes.getSpellingRepairPattern(kw).matcher(trimmed).find()
        }
        if (matchedTitleKeywords.isNotEmpty()) {
            val pts = matchedTitleKeywords.size * 30
            score += pts
            details.add("mots_clés_titre($matchedTitleKeywords):+$pts")
        }

        // Critère 3 : Connecteurs de titre (et, aux, à la, au)
        val defaultConnectors = listOf(" et ", " aux ", " à la ", " au ", " d'", " de ")
        val connectors = if (vocabulary.titleMultilineConnectors.isNotEmpty()) {
            vocabulary.titleMultilineConnectors.map { " ${it.trim()} " }
        } else {
            defaultConnectors
        }
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
            OcrRegexes.getStemWordPattern(stem).containsMatchIn(trimmed)
        }
        if (matchedRecipeIngredients.isNotEmpty()) {
            val pts = matchedRecipeIngredients.size * 15
            score += pts
            details.add("ingrédient_fiche_recette($matchedRecipeIngredients):+$pts")
        }

        // Critère 7 : Départage par le premier ingrédient de la recette (+30 pts)
        if (firstIngredientWord != null) {
            val stem = firstIngredientWord.removeSuffix("s").removeSuffix("x")
            if (OcrRegexes.getStemWordPattern(stem).containsMatchIn(trimmed)) {
                score += 30
                details.add("premier_ingrédient($firstIngredientWord):+30")
            }
        }

        return Pair(score, details.joinToString(", "))
    }

    /**
     * Détermine si la ligne constitue une ébauche de candidat titre valide d'après les critères fondamentaux.
     *
     * @param line La ligne de texte à évaluer.
     * @param vocabulary Le vocabulaire métier OCR.
     * @param containsActionRegex Expression régulière pré-compilée des verbes d'action culinaires.
     * @return `true` si la ligne est un candidat titre valide, `false` si elle est disqualifiée.
     */
    private fun isBaseCandidate(
        line: String,
        vocabulary: OcrVocabulary,
        containsActionRegex: Regex?
    ): Boolean {
        val trimmed = line.trim()
        val lower = trimmed.lowercase()

        if (trimmed.length < 5 || trimmed.length > 80) return false
        val wordList = trimmed.split(Regex("\\s+"))
        if (wordList.size < 2 && vocabulary.titleKeywords.none { it.equals(trimmed, ignoreCase = true) }) return false
        if (trimmed.endsWith(".") || trimmed.endsWith("!") || trimmed.endsWith("?")) return false
        if (OcrRegexes.Titles.DISQUALIFYING_NUMBER_START.containsMatchIn(line.trim())) return false
        if (trimmed.contains("(") || trimmed.contains(")") || trimmed.contains("[")) return false
        if (OcrRegexes.Titles.HAS_QUANTITY.containsMatchIn(trimmed)) return false
        if (OcrRegexes.Titles.DISQUALIFYING_FRACTION.containsMatchIn(trimmed)) return false
        if (OcrRegexes.Titles.DISQUALIFYING_SPOON.containsMatchIn(trimmed)) return false
        if (OcrInstructionUtils.hasActionVerb(trimmed, vocabulary.stepActionKeywords)) return false
        if (trimmed.startsWith("•") || trimmed.startsWith("-") || trimmed.startsWith("*")) return false

        if (vocabulary.excludedOcrKeywords.any { it.equals(trimmed, ignoreCase = true) }) return false

        val cleanIngName = trimmed.replace(OcrRegexes.Titles.DE_PREFIX_FULL, "").trim()
        if (vocabulary.commonIngredientsNoQty.any { it.equals(trimmed, ignoreCase = true) || it.equals(cleanIngName, ignoreCase = true) }) return false

        val hasTitleKeyword = vocabulary.titleKeywords.any { OcrRegexes.getSpellingRepairPattern(it).matcher(trimmed).find() }
        if (!hasTitleKeyword && vocabulary.ingredientPreparationModifiers.any { OcrRegexes.getSpellingRepairPattern(it).matcher(trimmed).find() }) return false

        if (OcrHelperUtils.isLikelyProperNameOrSource(trimmed, vocabulary) || SourceParser.isSourceLine(trimmed, vocabulary)) return false
        if (WineParser.isWineLine(trimmed, vocabulary)) return false

        if (lower.startsWith("d'") || lower.startsWith("l'") || lower.startsWith("de ") || lower.startsWith("du ") || lower.startsWith("des ") || lower.startsWith("de la ") || lower.startsWith("de l'") || lower.startsWith("ou ") || lower.startsWith("à ") || lower.startsWith("a ") || lower.startsWith("tranches de ") || lower.startsWith("tranche de ")) return false
        if (lower.startsWith("quelques ") || lower.startsWith("plusieurs ") || lower.startsWith("ingrédients") || lower.startsWith("ingredients") || lower.startsWith("préparation") || lower.startsWith("cuisson") || lower.startsWith("repos") || lower.startsWith("pour ") || lower.startsWith("le jus") || lower.startsWith("jus de")) return false
        if (lower.contains("personnes") || lower.contains("portions")) return false

        val lastWord = lower.split(Regex("\\s+")).lastOrNull() ?: ""
        val stopLastWords = (vocabulary.standaloneArticles + vocabulary.hangingConnectors + listOf("ce", "que")).map { it.trim().lowercase() }.distinct()
        if (lastWord in stopLastWords) return false

        val letterCount = trimmed.count { it.isLetter() }
        val digitCount = trimmed.count { it.isDigit() }
        return letterCount >= 4 && digitCount <= 2
    }

    /**
     * Supprime le numéro de fiche ou de recette en début de titre (ex: "12 Salade grecque" -> "Salade grecque").
     *
     * @param text Le texte brut du candidat titre.
     * @return Le titre sans son numéro de fiche initial.
     */
    private fun stripRecipeNumber(text: String): String {
        var cleaned = text.trim()
        cleaned = cleaned.replace(OcrRegexes.Titles.RECIPE_NUMBER_PREFIX, "")
        if (cleaned.isNotEmpty()) {
            cleaned = cleaned.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        }
        return cleaned
    }
}
