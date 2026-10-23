package com.example.chiefinventory.utils

import android.content.res.Resources
import com.example.chiefinventory.R

/**
 * Analyseur et extracteur des recommandations de vins et accords mets-vins.
 *
 * Responsabilités :
 * - Détecter si une ligne de texte représente une recommandation de vin (`isWineLine`).
 * - Disqualifier les lignes contenant des aliments liquides culinaires (ex: *"huile"*, *"vinaigre"*) ou des unités de recettes.
 * - Identifier les millésimes, volumes de bouteilles et appellations (`wine_arrays.xml`).
 * - Nettoyer et réparer les erreurs orthographiques et typographiques des lignes de vins (`cleanWineLine`).
 * - Consommer les expressions régulières pré-compilées de [OcrRegexes.Wines].
 */
object WineParser {

    /**
     * Conteneur des listes de ressources de vins (appellations, producteurs, mots-clés).
     *
     * @property appellations Liste des appellations et cépages (ex: "Bordeaux", "Chianti", "Rioja").
     * @property producers Liste des producteurs et domaines (ex: "Château", "Domaine", "Pfaffenheim").
     * @property keywords Liste des mots-clés généraux d'œnologie.
     * @property titleKeywords Liste des en-têtes de recommandation (ex: "vin conseillé", "accord").
     * @property removePattern Motif Regex d'effacement de préfixe de titre de section vin.
     */
    data class WineResources(
        val appellations: List<String>,
        val producers: List<String>,
        val keywords: List<String>,
        val titleKeywords: List<String>,
        val removePattern: String
    )

    /**
     * Charge les ressources de vins à partir des [Resources] Android (`wine_arrays.xml`).
     *
     * @param res Les ressources de l'application Android.
     * @return L'objet [WineResources] alimenté depuis XML.
     */
    fun loadResources(res: Resources): WineResources {
        return WineResources(
            appellations = res.getStringArray(R.array.wine_appellations).toList(),
            producers = res.getStringArray(R.array.wine_producers).toList(),
            keywords = res.getStringArray(R.array.wine_keywords).toList(),
            titleKeywords = res.getStringArray(R.array.wine_title_keywords).toList(),
            removePattern = try { res.getString(R.string.wine_remove_pattern) } catch (_: Exception) { "" }
        )
    }

    /**
     * Détermine si la ligne [line] représente une recommandation de vin en utilisant [vocabulary].
     *
     * @param line La ligne de texte à analyser.
     * @param vocabulary Le vocabulaire métier OCR.
     * @return `true` si la ligne est identifiée comme une recommandation de vin, `false` sinon.
     */
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

    /**
     * Détermine si la ligne [line] représente une recommandation de vin en utilisant [resources].
     *
     * @param line La ligne de texte à analyser.
     * @param resources Les ressources de vins contenant appellations et mots-clés.
     * @return `true` si la ligne correspond à un vin, `false` sinon.
     */
    fun isWineLine(line: String, resources: WineResources): Boolean {
        val lower = line.lowercase().trim()
        if (lower == "vin" || lower == "vin blanc" || lower == "vin rouge" || lower == "du vin blanc" || lower == "du vin rouge") return false

        // 1. Exclusion si contient un ingrédient culinaire ou une unité culinaire
        if (OcrRegexes.Wines.CULINARY_EXCLUSIONS.containsMatchIn(line)) return false
        if (OcrRegexes.Wines.CULINARY_UNITS.containsMatchIn(line)) return false

        // 2. Règle forte : Année + Volume (ex : 2015 75cl)
        if (OcrRegexes.Wines.YEAR.containsMatchIn(line) && OcrRegexes.Wines.VOLUME.containsMatchIn(line)) return true
        
        // 3. Règle spécifique : Format bouteille standard (75 cl) sans signal culinaire
        if (OcrRegexes.Wines.STANDARD_BOTTLE.containsMatchIn(line)) return true

        // 4. Match contre les listes de ressources
        val matchesResource = resources.appellations.any { containsWholeWord(line, it) } ||
                resources.producers.any { containsWholeWord(line, it) } ||
                resources.keywords.any { containsWholeWord(line, it) } ||
                resources.titleKeywords.any { containsWholeWord(line, it) }
        
        if (matchesResource) return true
        
        // 5. Millésime seul (si la ligne est courte et contient une année réaliste)
        if (line.length < 15 && OcrRegexes.Wines.YEAR.matches(line.trim())) return true

        return false
    }

    /**
     * Détermine si une ligne contient un volume explicite de bouteille de vin (ex: "75 cl", "1.5 l", "magnum").
     *
     * @param line La ligne à analyser.
     * @return `true` si un volume de bouteille est détecté, `false` sinon.
     */
    fun hasWineVolume(line: String): Boolean {
        return OcrRegexes.Wines.VOLUME.containsMatchIn(line) || OcrRegexes.Wines.STANDARD_BOTTLE.containsMatchIn(line)
    }

    /**
     * Nettoie une ligne de recommandation de vin en utilisant [vocabulary].
     *
     * @param line La ligne de vin brute.
     * @param vocabulary Le vocabulaire métier OCR.
     * @return La ligne de vin nettoyée et corrigée.
     */
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

    /**
     * Nettoie une ligne de recommandation de vin en supprimant les en-têtes parasites et en appliquant les corrections orthographiques.
     *
     * @param line La ligne de vin brute (ex: "Notre vin conseillé: Un Bordeau 1996 75 cl").
     * @param resources Les ressources de vins.
     * @return La ligne nettoyée (ex: "Un Bordeaux 1996 75 cl").
     */
    fun cleanWineLine(line: String, resources: WineResources): String {
        var cleaned = line
        cleaned = cleaned.replace(OcrRegexes.Wines.REMOVE_HEADER_PATTERN, "").trim()

        if (resources.removePattern.isNotBlank()) {
            try {
                cleaned = cleaned.replace(Regex(resources.removePattern, RegexOption.IGNORE_CASE), "").trim()
            } catch (_: Exception) {}
        }

        cleaned = cleaned.replace(OcrRegexes.Wines.HEADER_PREFIX, "").trim()
        cleaned = cleaned.replace(OcrRegexes.Wines.LEADING_PUNCTUATION, "").trim()

        return applySpellingCorrections(cleaned)
    }

    /**
     * Vérifie la présence d'un mot entier dans une chaîne de texte (délimiteur Unicode aware).
     *
     * @param text Le texte hôte dans lequel chercher.
     * @param keyword Le mot-clé à rechercher.
     * @return `true` si le mot-clé entier est trouvé, `false` sinon.
     */
    private fun containsWholeWord(text: String, keyword: String): Boolean {
        return OcrRegexes.getWholeWordPattern(keyword).containsMatchIn(text)
    }

    /**
     * Applique les corrections orthographiques et typographiques œnologiques de [OcrRegexes.Wines.SPELLING_CORRECTIONS].
     *
     * @param text Le texte de vin à corriger.
     * @return Le texte après corrections orthographiques.
     */
    private fun applySpellingCorrections(text: String): String {
        var corrected = text
        for ((regex, replacement) in OcrRegexes.Wines.SPELLING_CORRECTIONS) {
            corrected = corrected.replace(regex, replacement)
        }
        return corrected
    }
}
