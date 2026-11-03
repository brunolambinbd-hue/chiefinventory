package com.chefinventory.app.utils

import android.content.res.Resources
import com.chefinventory.app.R

/**
 * Analyseur et extracteur des informations de provenance et de sources de recettes (hôtels, restaurants, téléphones, adresses, codes postaux).
 *
 * Responsabilités :
 * - Détecter si une ligne contient des coordonnées ou références de provenance (`isSourceLine`).
 * - Déléguer l'extraction de marque publicitaire à [Ocr2CleanerAdSourceExtractor].
 * - Déléguer le nettoyage de préfixe de source à [Ocr3CategorizerSourceCleaner].
 * - Consommer les expressions régulières pré-compilées de [OcrRegexes.Sources].
 */
@Suppress("FoldInitializerAndIfNull", "kotlin:S6529")
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
     */
    fun cleanSourceLine(line: String, vocabulary: OcrVocabulary): String {
        return Ocr3CategorizerSourceCleaner.cleanSourceLine(line, vocabulary)
    }

    /**
     * Nettoie une ligne de source en supprimant les préfixes téléphoniques parasites en utilisant [resources].
     */
    fun cleanSourceLine(line: String, resources: SourceResources): String {
        return Ocr3CategorizerSourceCleaner.cleanSourceLine(line, resources.phonePrefixes)
    }

    /**
     * Inspecte une ligne publicitaire ou marketing et en extrait la marque ou l'enseigne culinaire connue ([OcrVocabulary.knownSourceBrands]).
     */
    fun extractSourceFromAdLine(line: String, vocabulary: OcrVocabulary): String? {
        return Ocr2CleanerAdSourceExtractor.extractSourceFromAdLine(line, vocabulary)
    }
}
