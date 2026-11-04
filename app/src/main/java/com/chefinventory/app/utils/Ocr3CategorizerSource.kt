package com.chefinventory.app.utils

import android.content.res.Resources
import com.chefinventory.app.R

/**
 * Moteur de l'Étape 3 (Catégorisation) dédié à la détection, au filtrage et au nettoyage des sources et coordonnées d'établissements.
 */
@Suppress("FoldInitializerAndIfNull", "kotlin:S6529")
object Ocr3CategorizerSource {

    /**
     * Conteneur des listes de ressources de sources (mots-clés et préfixes de téléphone).
     */
    data class SourceResources(
        val sourceKeywords: List<String>,
        val phonePrefixes: List<String>
    )

    /**
     * Charge les ressources de sources à partir des [Resources] Android (`source_keywords` et `phone_prefixes`).
     */
    fun loadResources(res: Resources): SourceResources {
        return SourceResources(
            sourceKeywords = res.getStringArray(R.array.source_keywords).toList(),
            phonePrefixes = res.getStringArray(R.array.phone_prefixes).toList()
        )
    }

    /**
     * Détermine si la ligne [line] constitue une source d'information de recette en utilisant [vocabulary].
     */
    fun isSourceLine(line: String, vocabulary: OcrVocabulary): Boolean {
        return isSourceLine(line, SourceResources(vocabulary.sourceKeywords, vocabulary.phonePrefixes))
    }

    /**
     * Détermine si la ligne [line] constitue une source d'information de recette en utilisant [resources].
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
        return cleanSourceLine(line, vocabulary.phonePrefixes)
    }

    /**
     * Nettoie une ligne de source en supprimant les préfixes téléphoniques parasites en utilisant [phonePrefixes].
     */
    fun cleanSourceLine(line: String, phonePrefixes: List<String>): String {
        var cleaned = line.trim()
        phonePrefixes.forEach { prefix ->
            val prefixRegex = OcrRegexes.getPhonePrefixPattern(prefix)
            cleaned = cleaned.replace(prefixRegex, "").trim()
        }
        return cleaned.replace(OcrRegexes.Sources.LEADING_PUNCTUATION, "").trim()
    }
}
