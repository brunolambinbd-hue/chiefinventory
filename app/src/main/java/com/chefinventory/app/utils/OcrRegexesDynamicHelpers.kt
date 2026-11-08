package com.chefinventory.app.utils

import java.util.regex.Pattern

/**
 * Registre assistant dédié aux expressions régulières dynamiques et mises en cache (memoization).
 *
 * Responsabilités :
 * - Isoler la gestion des caches mutables de Regexes dynamiques dépendantes du vocabulaire XML.
 * - Utiliser des ancrages de lettres Unicode (?<!\p{L}) et (?!\p{L}) pour une parité 100% stricte entre JVM et Android.
 * - Éliminer les ré-allocations CPU en réutilisant les motifs compilés.
 */
@Suppress("FoldInitializerAndIfNull", "kotlin:S6529")
object OcrRegexesDynamicHelpers {

    private val unitRegexCache = mutableMapOf<String, Regex>()
    private val adPrefixCache = mutableMapOf<String, Regex>()
    private val phonePrefixCache = mutableMapOf<String, Regex>()
    private val instructionPrefixCache = mutableMapOf<String, Regex>()
    private val spellingRepairCache = mutableMapOf<String, Pattern>()
    private val stemWordCache = mutableMapOf<String, Regex>()
    private val wholeWordCache = mutableMapOf<String, Regex>()

    /**
     * Retourne le motif Regex pré-compilé et mis en cache pour les unités de mesure culinaires ([IngredientParser]).
     *
     * @param unit L'unité de mesure culinaire (ex: "c. à soupe", "g", "cl").
     * @return L'expression régulière Regex compilée correspondante avec ancrages Unicode (?!\p{L}).
     */
    fun getUnitPattern(unit: String): Regex {
        return unitRegexCache.getOrPut(unit) {
            Regex("^${Regex.escape(unit)}(?:\\s+|de\\s+|d['’]\\s*|\\.|\\$|(?!\\p{L}))", RegexOption.IGNORE_CASE)
        }
    }

    /**
     * Retourne le motif Regex pré-compilé et mis en cache pour les préfixes téléphoniques de sources ([OcrSourceParser]).
     *
     * @param prefix Le préfixe téléphonique (ex: "Tel", "Fax", "GSM").
     * @return L'expression régulière Regex compilée correspondante avec ancrages Unicode.
     */
    fun getPhonePrefixPattern(prefix: String): Regex {
        return phonePrefixCache.getOrPut(prefix) {
            Regex("(?i)(?<!\\p{L})${Regex.escape(prefix)}(?!\\p{L})\\s*[:\\-.]?")
        }
    }

    /**
     * Retourne le motif Regex pré-compilé et mis en cache pour le filtrage des bannières publicitaires ([Ocr2Cleaner]).
     *
     * @param rawPattern Le motif publicitaire brut.
     * @return L'expression régulière Regex compilée correspondante.
     */
    fun getAdPrefixPattern(rawPattern: String): Regex {
        return adPrefixCache.getOrPut(rawPattern) {
            Regex("(?i)^.*?" + rawPattern + "\\s*")
        }
    }

    /**
     * Retourne le motif Regex pré-compilé et mis en cache pour les préfixes de consignes basés sur les verbes d'action.
     *
     * @param stepActionKeywords La liste des verbes d'action culinaires issue de `step_keywords.xml`.
     * @return L'expression régulière Regex compilée correspondante.
     */
    fun getInstructionPrefixPattern(stepActionKeywords: List<String>): Regex {
        val cacheKey = stepActionKeywords.joinToString(",")
        return instructionPrefixCache.getOrPut(cacheKey) {
            val timeExclusions = setOf("minutes", "heures", "secondes", "environ")
            val actionVerbsRegex = if (stepActionKeywords.isNotEmpty()) {
                val distinctKeywords = stepActionKeywords
                    .map { it.replace(Regex("\\[.*?\\]"), "").trim() }
                    .filter { it.length >= 3 && it.lowercase() !in timeExclusions }
                distinctKeywords.joinToString("|") { Regex.escape(it) }
            } else {
                "(?!minutes\\b|heures\\b)[\\p{Lu}][\\p{Ll}]+"
            }
            Regex("^(?:[\\p{L}]{1,3}\\s+)?(?:\\d{1,2}[.)\\-]?\\s+)*(?=(?:$actionVerbsRegex)(?!\\p{L}))", RegexOption.IGNORE_CASE)
        }
    }

    /**
     * Retourne le motif Pattern pré-compilé et mis en cache pour les réparations orthographiques d'Étape 1 ([Ocr1Normalizer]).
     *
     * @param wrong Le terme fautif à rechercher.
     * @return L'expression régulière Pattern compilée correspondante.
     */
    fun getSpellingRepairPattern(wrong: String): Pattern {
        return spellingRepairCache.getOrPut(wrong) {
            Pattern.compile("(?<!\\p{L})" + Regex.escape(wrong) + "(?!\\p{L})", Pattern.CASE_INSENSITIVE or Pattern.UNICODE_CASE)
        }
    }

    /**
     * Retourne le motif Regex pré-compilé et mis en cache pour la recherche de racine de mots d'ingrédients ([OcrTitleExtractor]).
     *
     * @param stem La racine du mot d'ingrédient (ex: "mangue", "crabe").
     * @return L'expression régulière compilée correspondante avec ancrages Unicode (?<!\p{L}) et (?!\p{L}).
     */
    fun getStemWordPattern(stem: String): Regex {
        return stemWordCache.getOrPut(stem) {
            Regex("(?i)(?<!\\p{L})${Regex.escape(stem)}(?:s|x)?(?!\\p{L})")
        }
    }

    /**
     * Retourne le motif Regex pré-compilé et mis en cache pour la vérification de mots entiers ([WineParser]).
     *
     * @param keyword Le mot-clé à rechercher.
     * @return L'expression régulière compilée correspondante avec ancrages Unicode.
     */
    fun getWholeWordPattern(keyword: String): Regex {
        return wholeWordCache.getOrPut(keyword) {
            Regex("(?iu)(?<![\\p{L}\\p{N}])${Regex.escape(keyword)}(?![\\p{L}\\p{N}])")
        }
    }

    /**
     * Réinitialise l'ensemble des caches mutables de Regexes dynamiques (utilisé pour les tests unitaires TDD).
     */
    fun clearCache() {
        unitRegexCache.clear()
        adPrefixCache.clear()
        phonePrefixCache.clear()
        instructionPrefixCache.clear()
        spellingRepairCache.clear()
        stemWordCache.clear()
        wholeWordCache.clear()
    }
}
