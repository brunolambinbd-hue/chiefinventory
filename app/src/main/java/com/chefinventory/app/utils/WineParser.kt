package com.chefinventory.app.utils

import android.content.res.Resources
import com.chefinventory.app.R

/**
 * Analyseur et extracteur spécialisé pour la détection et le nettoyage des lignes de recommandations de vins.
 *
 * Responsabilités :
 * - Détecter si une ligne correspond à une recommandation d'accord met-vin (`isWineLine`).
 * - Valider les appellations, châteaux et producteurs de vins contre les dictionnaires métier XML (`wine_arrays.xml`).
 * - Exclure rigoureusement les ingrédients culinaires (ex: "vinaigre de vin", "huile") pour éviter les fausses détections.
 * - Supprimer les préfixes d'en-tête et corriger les fautes d'orthographe OCR (`cleanWineLine`).
 * - Consommer les expressions régulières pré-compilées de [OcrRegexes.Wines].
 */
@Suppress("FoldInitializerAndIfNull", "kotlin:S6529")
object WineParser {

    /**
     * Conteneur des listes de ressources de vins (appellations, producteurs, mots-clés, en-têtes et motif de suppression).
     *
     * @property appellations Liste des appellations et cépages de vins (ex: "Bordeaux", "Chardonnay").
     * @property producers Liste des châteaux, domaines et maisons de vins (ex: "Château", "Domaine").
     * @property keywords Liste des mots-clés d'œnologie (ex: "cuvée", "millesime", "grand cru").
     * @property titleKeywords Liste des expressions d'en-têtes de section vin (ex: "conseil vin", "accord").
     * @property removePattern Expression régulière de suppression de l'en-tête de section vin.
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
     * Détermine si la ligne [line] correspond à une recommandation de vin en utilisant [vocabulary].
     *
     * @param line La ligne de texte à analyser.
     * @param vocabulary Le vocabulaire métier OCR.
     * @return `true` si la ligne correspond à un vin, `false` sinon.
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
     * Détermine si la ligne [line] correspond à une recommandation de vin en utilisant [resources].
     *
     * @param line La ligne de texte à analyser.
     * @param resources Les ressources de vins contenant appellations et mots-clés.
     * @return `true` si la ligne correspond à un vin, `false` sinon.
     */
    fun isWineLine(line: String, resources: WineResources): Boolean {
        val lower = line.lowercase().trim()
        val genericWineWords = setOf("vin", "vin blanc", "vin rouge", "du vin blanc", "du vin rouge")
        if (lower in genericWineWords) return false

        // 1. Exclusion si contient un ingrédient culinaire ou une unité culinaire
        if (OcrRegexes.Wines.CULINARY_EXCLUSIONS.containsMatchIn(line)) return false
        if (OcrRegexes.Wines.CULINARY_UNITS.containsMatchIn(line)) return false

        // 2. Règle forte : Année + Volume (ex : 2015 75cl)
        if (OcrRegexes.Wines.YEAR.containsMatchIn(line) && OcrRegexes.Wines.VOLUME.containsMatchIn(line)) return true
        
        // 3. Règle spécifique : Format bouteille standard (75 cl) sans signal culinaire
        if (OcrRegexes.Wines.STANDARD_BOTTLE.containsMatchIn(line)) return true

        // 4. Match contre les listes de ressources
        val matchesResource = if (resources.appellations.any { containsWholeWord(line, it) }) {
            true
        } else if (resources.producers.any { containsWholeWord(line, it) }) {
            true
        } else if (resources.keywords.any { containsWholeWord(line, it) }) {
            true
        } else {
            resources.titleKeywords.any { containsWholeWord(line, it) }
        }
        
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
        val hasVolume = OcrRegexes.Wines.VOLUME.containsMatchIn(line)
        if (hasVolume) {
            return true
        } else {
            return OcrRegexes.Wines.STANDARD_BOTTLE.containsMatchIn(line)
        }
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
        } else {
            // Aucun motif d'effacement spécifique configuré
        }

        cleaned = cleaned.replace(OcrRegexes.Wines.HEADER_PREFIX, "").trim()
        cleaned = cleaned.replace(OcrRegexes.Wines.LEADING_PUNCTUATION, "").trim()

        return applySpellingCorrections(cleaned)
    }

    /**
     * Vérifie la présence d'un mot entier dans une chaîne de texte (délimiteur Unicode aware).
     *
     * @param text Le texte source à analyser.
     * @param word Le mot à rechercher.
     * @return `true` si le mot entier est présent dans le texte, `false` sinon.
     */
    fun containsWholeWord(text: String, word: String): Boolean {
        if (text.isBlank() || word.isBlank()) return false
        val pattern = OcrRegexes.getWholeWordPattern(word)
        return pattern.containsMatchIn(text)
    }

    /**
     * Applique la liste des règles de correction orthographique sur les appellations et châteaux de vins.
     *
     * @param text Le texte brut à corriger.
     * @return Le texte après remplacement des fautes de frappe récurrentes.
     */
    fun applySpellingCorrections(text: String): String {
        var result = text
        for ((pattern, replacement) in OcrRegexes.Wines.SPELLING_CORRECTIONS) {
            result = pattern.replace(result, replacement)
        }
        return result
    }
}
