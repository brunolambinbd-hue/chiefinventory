package com.example.chiefinventory.utils

import android.content.res.Resources
import com.example.chiefinventory.R

/**
 * Utility to parse and identify wine-related information from OCR text.
 */
object WineParser {
    private val wineYearRegex = Regex("\\b(?:19|20)\\d{2}\\b")
    private val wineVolRegex = Regex("\\b\\d+(?:[\\s.,]\\d+)?\\s*(?:cl|ml|l|vol)\\b", RegexOption.IGNORE_CASE)
    // Regex spécifique pour les bouteilles standards (75 cl, 750 ml)
    private val standardBottleRegex = Regex("(?i)\\b(75\\s*cl|750\\s*ml|1\\s*5\\s*l|magnum)\\b")
    
    private val extraWineCleanRegex = Regex("(?i)^(?:accord|boisson|boire|servir avec|suggestion)\\s*:?", RegexOption.IGNORE_CASE)

    // Mots-clés qui indiquent qu'il s'agit d'un ingrédient de cuisine et NON de vin
    private val WINE_EXCLUSION_REGEX = Regex("(?i)\\b(huile|vinaigre|beurre|crème|creme|lait|bouillon|eau|jus|sirop)\\b")
    
    // Unités purement culinaires qui disqualifient une recommandation de vin
    private val CULINARY_UNITS_REGEX = Regex("(?i)\\b(\\d+\\s*(?:dl|g|mg|kg)|c\\.?\\s*[àa]\\s*(?:soupe|caf[eé]|dessert))\\b")

    data class WineResources(
        val appellations: List<String>,
        val producers: List<String>,
        val keywords: List<String>,
        val titleKeywords: List<String>,
        val removePattern: String
    )

    fun loadResources(res: Resources): WineResources {
        return WineResources(
            appellations = res.getStringArray(R.array.wine_appellations).toList(),
            producers = res.getStringArray(R.array.wine_producers).toList(),
            keywords = res.getStringArray(R.array.wine_keywords).toList(),
            titleKeywords = res.getStringArray(R.array.wine_title_keywords).toList(),
            removePattern = try { res.getString(R.string.wine_remove_pattern) } catch (_: Exception) { "" }
        )
    }

    fun isWineLine(line: String, vocabulary: OcrVocabulary): Boolean {
        return isWineLine(
            line,
            WineResources(
                appellations = vocabulary.wineAppellations,
                producers = vocabulary.wineProducers,
                keywords = vocabulary.wineKeywords,
                titleKeywords = vocabulary.wineTitleKeywords,
                removePattern = vocabulary.wineRemovePattern
            )
        )
    }

    fun isWineLine(line: String, resources: WineResources): Boolean {
        val lower = line.lowercase().trim()
        if (lower == "vin" || lower == "vin blanc" || lower == "vin rouge" || lower == "du vin blanc" || lower == "du vin rouge") return false

        // 1. Exclusion si contient un ingrédient culinaire ou une unité culinaire
        if (WINE_EXCLUSION_REGEX.containsMatchIn(line)) return false
        if (CULINARY_UNITS_REGEX.containsMatchIn(line)) return false

        // 2. Règle forte : Année + Volume (ex : 2015 75cl)
        if (wineYearRegex.containsMatchIn(line) && wineVolRegex.containsMatchIn(line)) return true
        
        // 3. Règle spécifique : Format bouteille standard (75 cl) sans signal culinaire
        if (standardBottleRegex.containsMatchIn(line)) return true

        // Helper pour détecter les mots entiers (Unicode aware)
        fun containsWholeWord(text: String, keyword: String): Boolean {
            val pattern = Regex("(?<![\\p{L}\\p{N}])${Regex.escape(keyword)}(?![\\p{L}\\p{N}])", RegexOption.IGNORE_CASE)
            return pattern.containsMatchIn(text)
        }

        // 4. Match contre les listes de ressources
        val matchesResource = resources.appellations.any { containsWholeWord(line, it) } ||
                resources.producers.any { containsWholeWord(line, it) } ||
                resources.keywords.any { containsWholeWord(line, it) } ||
                resources.titleKeywords.any { containsWholeWord(line, it) }
        
        if (matchesResource) return true
        
        // 5. Millésime seul (si la ligne est courte et contient une année réaliste)
        if (line.length < 15 && wineYearRegex.matches(line.trim())) return true

        return false
    }

    fun hasWineVolume(line: String): Boolean {
        return wineVolRegex.containsMatchIn(line) || standardBottleRegex.containsMatchIn(line)
    }

    fun cleanWineLine(line: String, vocabulary: OcrVocabulary): String {
        return cleanWineLine(
            line,
            WineResources(
                appellations = vocabulary.wineAppellations,
                producers = vocabulary.wineProducers,
                keywords = vocabulary.wineKeywords,
                titleKeywords = vocabulary.wineTitleKeywords,
                removePattern = vocabulary.wineRemovePattern
            )
        )
    }

    fun cleanWineLine(line: String, resources: WineResources): String {
        val wineRemoveRegex = if (resources.removePattern.isNotBlank()) {
            Regex(resources.removePattern, RegexOption.IGNORE_CASE)
        } else null

        var cleaned = line
        wineRemoveRegex?.let { cleaned = cleaned.replace(it, "") }
        cleaned = cleaned.replace(extraWineCleanRegex, "").trim()
        cleaned = cleaned.replace(Regex("^[:\\-\\s\\.]+"), "").trim()

        return applySpellingCorrections(cleaned)
    }

    private fun applySpellingCorrections(text: String): String {
        var corrected = text
        corrected = corrected.replace(Regex("(?i)\\bBordeau\\b"), "Bordeaux")
        corrected = corrected.replace(Regex("(?i)\\bAtinum\\b"), "Atinium")
        corrected = corrected.replace(Regex("(?i)\\bChậteau\\b"), "Château")

        // Post-processing des fautes OCR sur le vin : millésime (I996 -> 1996), volumes (75 d -> 75 cl) et termes (vìn -> vin)
        corrected = corrected.replace(Regex("(?i)\\b[I!l|](?=[12]?9\\d{2}\\b)"), "1")
        corrected = corrected.replace(Regex("(?i)\\bI(\\d{3})\\b")) { match -> "1${match.groupValues[1]}" }
        corrected = corrected.replace(Regex("(?i)\\b(\\d{2})\\s*d\\b"), "$1 cl")
        corrected = corrected.replace(Regex("(?i)\\bvìn\\b"), "vin")
        corrected = corrected.replace(Regex("(?i)\\bM\\.0\\.\\b"), "M.O.")
        return corrected
    }
}
