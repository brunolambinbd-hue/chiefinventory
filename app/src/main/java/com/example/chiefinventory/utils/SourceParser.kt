package com.example.chiefinventory.utils

import android.content.res.Resources
import com.example.chiefinventory.R

/**
 * Analyseur et extracteur des informations de provenance et de sources de recettes (hôtels, restaurants, téléphones, adresses, codes postaux).
 *
 * Responsabilités :
 * - Détecter si une ligne contient des coordonnées ou références de provenance (`isSourceLine`).
 * - Nettoyer les lignes de sources en supprimant les préfixes téléphoniques ("Tel:", "Fax:") et ponctuations parasites (`cleanSourceLine`).
 * - Consommer les expressions régulières pré-compilées de [OcrRegexes.Sources] et le cache memoized de préfixes [OcrRegexes.getPhonePrefixPattern].
 */
object SourceParser {

    /**
     * Conteneur des listes de ressources de sources (mots-clés et préfixes de téléphone).
     *
     * @property sourceKeywords Liste des mots-clés d'adresses et d'établissements (ex: "restaurant", "hôtel", "rue").
     * @property phonePrefixes Liste des préfixes de téléphone à nettoyer (ex: "Tel", "Fax", "GSM").
     */
    data class SourceResources(
        val sourceKeywords: List<String>,
        val phonePrefixes: List<String>
    )

    /**
     * Charge les ressources de sources à partir des [Resources] Android (`source_keywords` et `phone_prefixes`).
     *
     * @param res Les ressources de l'application Android.
     * @return L'objet [SourceResources] alimenté depuis XML.
     */
    fun loadResources(res: Resources): SourceResources {
        return SourceResources(
            sourceKeywords = res.getStringArray(R.array.source_keywords).toList(),
            phonePrefixes = res.getStringArray(R.array.phone_prefixes).toList()
        )
    }

    /**
     * Détermine si la ligne [line] constitue une source d'information de recette en utilisant [vocabulary].
     *
     * @param line La ligne de texte à analyser.
     * @param vocabulary Le vocabulaire métier OCR.
     * @return `true` si la ligne contient une information de source/adresse/téléphone, `false` sinon.
     */
    fun isSourceLine(line: String, vocabulary: OcrVocabulary): Boolean {
        return isSourceLine(line, SourceResources(vocabulary.sourceKeywords, vocabulary.phonePrefixes))
    }

    /**
     * Détermine si la ligne [line] constitue une source d'information de recette en utilisant [resources].
     *
     * @param line La ligne de texte à analyser.
     * @param resources Les ressources de sources contenant mots-clés et préfixes.
     * @return `true` si la ligne contient un téléphone, un mot-clé de source ou un code postal, `false` sinon.
     */
    fun isSourceLine(line: String, resources: SourceResources): Boolean {
        val lowerLine = line.lowercase()
        return OcrRegexes.Sources.PHONE_NUMBER.containsMatchIn(line) || 
               resources.sourceKeywords.any { lowerLine.contains(it) } || 
               (OcrRegexes.Sources.ZIP_CODE.containsMatchIn(line) && line.length < 50)
    }

    /**
     * Nettoie une ligne de source en supprimant les préfixes téléphoniques parasites en utilisant [vocabulary].
     *
     * @param line La ligne de source brute.
     * @param vocabulary Le vocabulaire métier OCR.
     * @return La ligne de source nettoyée.
     */
    fun cleanSourceLine(line: String, vocabulary: OcrVocabulary): String {
        return cleanSourceLine(line, SourceResources(vocabulary.sourceKeywords, vocabulary.phonePrefixes))
    }

    /**
     * Nettoie une ligne de source en supprimant les préfixes téléphoniques parasites en utilisant [resources].
     *
     * @param line La ligne de source brute (ex: "Tel: 01 23 45 67 89").
     * @param resources Les ressources de sources contenant les préfixes.
     * @return La ligne nettoyée (ex: "01 23 45 67 89").
     */
    fun cleanSourceLine(line: String, resources: SourceResources): String {
        var cleaned = line
        resources.phonePrefixes.forEach { prefix ->
            val prefixRegex = OcrRegexes.getPhonePrefixPattern(prefix)
            cleaned = cleaned.replace(prefixRegex, "").trim()
        }
        // Supprime toute ponctuation résiduelle en début de ligne (ex: ": 02/542.42.42" -> "02/542.42.42")
        return cleaned.replace(OcrRegexes.Sources.LEADING_PUNCTUATION, "").trim()
    }

    /**
     * Inspecte une ligne publicitaire ou marketing et en extrait la marque ou l'enseigne culinaire connue ([OcrVocabulary.knownSourceBrands]).
     *
     * @param line La ligne publicitaire à analyser (ex: "Assiette de présentation disponible dans les Maxi GB").
     * @param vocabulary Le vocabulaire métier OCR contenant la liste blanche des enseignes.
     * @return Le nom exact de l'enseigne/marque trouvée (ex: "Maxi GB"), ou `null` si aucune marque de la liste n'est présente.
     */
    fun extractSourceFromAdLine(line: String, vocabulary: OcrVocabulary): String? {
        if (line.isBlank() || vocabulary.knownSourceBrands.isEmpty()) return null
        return vocabulary.knownSourceBrands.firstOrNull { brand ->
            brand.isNotBlank() && Regex("(?i)(?<![\\p{L}\\p{N}])${Regex.escape(brand)}(?![\\p{L}\\p{N}])").containsMatchIn(line)
        }
    }
}
