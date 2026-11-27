package com.chefinventory.app.utils

import java.text.Normalizer

/**
 * Composant utilitaire léger en Kotlin pur (0 dépendance externe) calculant la distance d'édition
 * de Levenshtein et le taux de similarité textuelle pour les ingrédients et mots du garde-manger.
 *
 * Responsabilités :
 * - Calculer le nombre minimum d'opérations d'édition (insertion, suppression, substitution).
 * - Calculer le taux de similarité normalisé entre 0.0 (totalement différent) et 1.0 (identique).
 * - Rechercher la meilleure correspondance dans un dictionnaire avec un seuil de tolérance configurable.
 */
object OcrLevenshteinUtils {

    /**
     * Calcule la distance d'édition de Levenshtein entre deux chaînes de caractères.
     *
     * @param s1 La première chaîne de texte.
     * @param s2 La seconde chaîne de texte.
     * @return Le nombre minimum d'éditions nécessaires.
     */
    fun distance(s1: String, s2: String): Int {
        if (s1 == s2) return 0
        val len1 = s1.length
        val len2 = s2.length
        if (len1 == 0) return len2
        if (len2 == 0) return len1

        var prev = IntArray(len2 + 1) { it }
        var curr = IntArray(len2 + 1)

        for (i in 1..len1) {
            curr[0] = i
            val c1 = s1[i - 1]
            for (j in 1..len2) {
                val cost = if (c1 == s2[j - 1]) 0 else 1
                curr[j] = minOf(
                    prev[j] + 1,
                    curr[j - 1] + 1,
                    prev[j - 1] + cost
                )
            }
            val temp = prev
            prev = curr
            curr = temp
        }
        return prev[len2]
    }

    /**
     * Normalise une chaîne en majuscules sans accents pour la comparaison d'ingrédients.
     */
    fun stripAccents(input: String): String {
        if (input.isBlank()) return ""
        return Normalizer.normalize(input, Normalizer.Form.NFD)
            .replace(Regex("\\p{InCombiningDiacriticalMarks}+"), "")
            .replace("\\", "")
            .uppercase()
            .trim()
    }

    /**
     * Calcule le pourcentage de similarité normalisé (entre 0.0 et 1.0) entre deux mots.
     *
     * @param s1 Le premier mot à évaluer.
     * @param s2 Le second mot à évaluer.
     * @param ignoreAccents Si true, retire les accents avant d'évaluer la similarité (par défaut true).
     * @return Le taux de similarité entre 0.0 (totalement différent) et 1.0 (identique).
     */
    fun similarity(s1: String, s2: String, ignoreAccents: Boolean = true): Double {
        val str1 = if (ignoreAccents) stripAccents(s1) else s1.trim().uppercase()
        val str2 = if (ignoreAccents) stripAccents(s2) else s2.trim().uppercase()

        val maxLen = maxOf(str1.length, str2.length)
        if (maxLen == 0) return 1.0

        val dist = distance(str1, str2)
        return 1.0 - (dist.toDouble() / maxLen)
    }

    /**
     * Détermine le nombre maximum d'erreurs d'édition tolérées selon la longueur du mot.
     * Protège les mots courts (Sel, Ail, Riz) contre les faux positifs.
     *
     * @param wordLength La longueur du mot de référence.
     * @return Le nombre maximum d'erreurs d'édition tolérées.
     */
    fun getMaxAllowedErrors(wordLength: Int): Int = when {
        wordLength <= 3 -> 0  // "Sel", "Ail", "Riz" -> Match exact (0 erreur)
        wordLength <= 5 -> 1  // "Thym", "Sucre" -> 1 erreur max
        wordLength <= 9 -> 2  // "Parmesan" -> 2 erreurs max ("parmnesan" -> "parmesan")
        else -> 3             // "Champignon", "Mozzarella" -> 3 erreurs max
    }

    /**
     * Vérifie si un mot scanné correspond à un mot du dictionnaire sous le seuil d'erreurs progressif.
     *
     * @param ocrWord Le mot scanné par l'OCR.
     * @param dictWord Le mot du dictionnaire d'ingrédients.
     * @return `true` si la distance est inférieure ou égale au seuil autorisé, `false` sinon.
     */
    fun isFuzzyMatch(ocrWord: String, dictWord: String): Boolean {
        val cleanOcr = stripAccents(ocrWord)
        val cleanDict = stripAccents(dictWord)
        val dist = distance(cleanOcr, cleanDict)
        val maxAllowed = getMaxAllowedErrors(cleanDict.length)
        return dist <= maxAllowed
    }

    /**
     * Trouve le mot du dictionnaire ayant la plus forte similarité avec le terme recherché.
     *
     * @param query Le terme recherché (ex: mot scanné par l'OCR ou saisi par l'utilisateur).
     * @param dictionary La liste des mots connus (ex: liste d'ingrédients).
     * @param minSimilarityThreshold Le seuil minimal de similarité toléré (entre 0.0 et 1.0, par défaut 0.75).
     * @return Le mot original du dictionnaire le plus proche, ou null si aucun mot ne dépasse le seuil.
     */
    fun findBestMatch(
        query: String,
        dictionary: List<String>,
        minSimilarityThreshold: Double = 0.75
    ): String? {
        if (query.isBlank()) return null
        if (dictionary.isEmpty()) return null

        val cleanQuery = stripAccents(query)
        val qLen = cleanQuery.length
        if (qLen == 0) return null

        val maxAllowed = getMaxAllowedErrors(qLen)

        var bestMatch: String? = null
        var maxSimilarity = 0.0

        for (item in dictionary) {
            val cleanItem = stripAccents(item)
            val iLen = cleanItem.length
            val lenDiff = kotlin.math.abs(qLen - iLen)
            if (lenDiff > maxAllowed) continue

            if (cleanQuery == cleanItem) {
                return item
            }

            val maxLen = maxOf(qLen, iLen)
            val dist = distance(cleanQuery, cleanItem)
            if (dist <= maxAllowed) {
                val sim = 1.0 - (dist.toDouble() / maxLen)
                if (sim >= minSimilarityThreshold && sim > maxSimilarity) {
                    maxSimilarity = sim
                    bestMatch = item
                }
            }
        }
        return bestMatch
    }
}
