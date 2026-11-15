package com.chefinventory.app.utils

/**
 * Assistant de l'Étape 5 (Beautifier Source) dédié à la détection et au filtrage des bruits de lettres alpha
 * isolées dans les sources (ex: "A. O", "O B", "A.", "O.").
 */
object Ocr5BeautifierSourceNoiseHelper {

    /**
     * Détermine si une chaîne de source représente un bruit de lettres alpha isolées.
     *
     * @param sourceText Le texte de source à évaluer (ex: "A. O", "O B", "A.").
     * @return `true` s'il s'agit d'un bruit de lettres isolées, `false` sinon.
     */
    fun isIsolatedAlphaNoise(sourceText: String): Boolean {
        if (sourceText.isBlank()) return true
        val clean = sourceText.trim().removeSuffix(".").removeSuffix(",")
        val isSingleLetterNoise = Regex("(?i)^[a-z]\\s*[.,;:]?$").matches(clean)
        val isPairSingleLetterNoise = Regex("(?i)^[a-z]\\s*[.,;:]?\\s*[a-z]\\s*[.,;:]?$").matches(clean)
        return isSingleLetterNoise || isPairSingleLetterNoise
    }
}
