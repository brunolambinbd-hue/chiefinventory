package com.chefinventory.app.utils

/**
 * Assistant de l'Étape 4 (Merger) dédié à la détection et qualification unifiée des tournures d'assaisonnement
 * "[assaisonnement] [de | du | d'] <item>".
 */
object Ocr4MergerSeasoningHelper {

    /**
     * Valide de manière générique et unifiée une tournure d'assaisonnement "[mot-clé] [de | du | d'] <item>".
     *
     * @param line La ligne de texte à analyser.
     * @param seasoningKeyword Le mot-clé de l'assaisonnement (ex: "sel", "poivre").
     * @param origins La liste des origines/appellations autorisées.
     * @return `true` s'il s'agit d'un assaisonnement d'origine valide, `false` sinon.
     */
    fun isSeasoningWithPrepositionOrigin(
        line: String,
        seasoningKeyword: String,
        origins: List<String>
    ): Boolean {
        if (line.isBlank() || origins.isEmpty()) return false
        val nfcLine = java.text.Normalizer.normalize(line.trim().lowercase(), java.text.Normalizer.Form.NFC)
        val keywordPattern = Regex.escape(seasoningKeyword.trim().lowercase())

        // Motif générique : "(sel|poivre)? (de|du|d') "
        val isPreposition = Regex("(?i)^(?:$keywordPattern\\s+)?(?:de|du|d['’])\\s+").containsMatchIn(nfcLine)
        if (!isPreposition) return false

        return origins.any { origin ->
            val nfcOrigin = java.text.Normalizer.normalize(origin.trim().lowercase(), java.text.Normalizer.Form.NFC)
            nfcLine.contains(nfcOrigin)
        }
    }

    /**
     * Détermine si une ligne de texte correspond à une tournure valide "sel [de | du | d'] <item>".
     *
     * @param line La ligne de texte à analyser (ex: "sel de Guérande", "sel de mer").
     * @param saltOrigins La liste des origines/appellations de sel autorisées (ex: de [OcrVocabulary.saltVarietyOrigins]).
     * @return `true` s'il s'agit d'un sel d'origine valide, `false` sinon.
     */
    fun isSaltWithPrepositionOrigin(line: String, saltOrigins: List<String>): Boolean {
        return isSeasoningWithPrepositionOrigin(line, "sel", saltOrigins)
    }

    /**
     * Détermine si une ligne de texte correspond à une tournure valide "poivre [de | du | d'] <item>".
     *
     * @param line La ligne de texte à analyser (ex: "poivre de Cayenne", "poivre du moulin").
     * @param pepperOrigins La liste des origines/appellations de poivre autorisées (ex: de [OcrVocabulary.pepperDeOriginItems]).
     * @return `true` s'il s'agit d'un poivre d'origine valide, `false` sinon.
     */
    fun isPepperWithPrepositionOrigin(line: String, pepperOrigins: List<String>): Boolean {
        return isSeasoningWithPrepositionOrigin(line, "poivre", pepperOrigins)
    }
}
