package com.example.chiefinventory.utils

/**
 * Étape 1 du pipeline OCR : Normalisation des caractères, chiffres, fractions et symboles.
 *
 * Responsabilités :
 * - Appliquer le dictionnaire de réparations orthographiques `ocr_repairs.xml` au démarrage du pipeline.
 * - Corriger les confusions de chiffres et lettres générées par l'OCR (`I12` -> `1/2`, `I` -> `1`, `I'eau` -> `l'eau`).
 * - Normaliser les abréviations de cuillères (`c. à s.` -> `c. à soupe`, `cac` -> `c. à café`).
 * - Nettoyer les bruits de dates, devises et caractères diacritiques parasites (`diacritic_repairs.xml`).
 * - Séparer les nombres collés aux unités (`200g` -> `200 g`).
 * - Consommer les expressions régulières pré-compilées de [OcrRegexes.Normalizer] et [OcrRegexes.Quantities].
 */
object Ocr1Normalizer {

    /** Diacritiques et accents parasites par défaut chargés dynamiquement depuis [R.array.ocr_diacritic_repairs] (`diacritic_repairs.xml`). */
    internal val defaultDiacriticRepairs: List<String> by lazy {
        try {
            OcrVocabularyLoader.loadFromProjectResources().ocrDiacriticRepairs
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Effectue la normalisation complète d'une chaîne de texte brute issue de l'OCR.
     *
     * Détail ligne par ligne des 15 sous-traitements de normalisation :
     * 1. **Garde-fou d'entrée :** Vérifie que la ligne n'est ni vide ni constituée uniquement d'espaces.
     * 2. **Sélection des diacritiques :** Reçoit ou charge les paires de réparations de diacritiques depuis `diacritic_repairs.xml`.
     * 3. **Réparations orthographiques XML (`ocr_repairs.xml`) :** Applique les paires fautif|correct (`lajout` -> `l'ajout`, `càsde` -> `c. à soupe de`).
     * 4. **Protection des mots-clés de section :** Empêche la confusion du 'I' majuscule avec '1' dans `"INGRÉDIENTS"` et `"INSTRUCTIONS"`.
     * 5. **Nettoyage du préfixe 't' parasite :** Supprime le caractère 't' ou 't ' accidentellement inséré devant un chiffre (ex: `t10 min` -> `10 min`).
     * 6. **Suppression des symboles de tolérance & puces :** Retire `±`, `+/-`, `+-`, `environ` et les puces devant un chiffre (ex: `! 100 g` -> `100 g`).
     * 7. **Suppression du bruit publicitaire & devises :** Élimine les dates (`14/12/94`), prix (`209 F`, `15.50 EUR`, `20 €`).
     * 8. **Correction OCR du chiffre '1' :** Restaure le chiffre `1` lorsqu'il est lu comme `|`, `!`, `i` ou `l` devant une lettre.
     * 9. **Fractions & Erreurs de nombres (Garde-fou `isDigit()`) :** Si la ligne contient un chiffre, convertit `I12` -> `1/2`, `112` -> `1/2`, `del` -> `de 1`.
     * 10. **Conversion des diacritiques parasites (`diacritic_repairs.xml`) :** Remplace les accents Unicode étrangers/vietnamiens (`ế` -> `é`, `ệ` -> `è`, `š` -> `s`).
     * 11. **Corrections d'articles & liaisons d'apostrophes :** Normalise `1 e` -> `le`, `T'oignon` -> `l'oignon`, `jauned'oeuf` -> `jaune d'oeuf`.
     * 12. **Normalisation des unités de cuillères (Garde-fou `contains('c')`) :** Si la ligne contient 'c', convertit `cas` -> `c. à soupe`, `cac` -> `c. à café`.
     * 13. **Séparation Chiffre / Unité :** Sépare `500g` -> `500 g`, `1l` -> `1 l`.
     * 14. **Nettoyage de la ponctuation finale :** Nettoie les virgules, deux-points et points-virgules finaux parasites.
     * 15. **Normalisation des espaces :** Effondre les espaces multiples consécutifs en un espace unique.
     *
     * @param input La chaîne de texte brute.
     * @param repairs Liste facultative de paires de réparations orthographiques (ex: de [OcrVocabulary.ocrSpellingRepairs]).
     * @param diacriticRepairs Liste facultative de réparations diacritiques (ex: de [OcrVocabulary.ocrDiacriticRepairs]).
     * @return La chaîne nettoyée et normalisée.
     */
    fun normalize(input: String, repairs: List<String> = emptyList(), diacriticRepairs: List<String> = emptyList()): String {
        // 1. Garde-fou d'entrée : chaîne vide ou blanche
        if (input.isBlank()) return ""
        var text = input.trim()

        // 2. Sélection de la table de diacritiques (reçue ou par défaut depuis diacritic_repairs.xml)
        val activeDiacritics = if (diacriticRepairs.isNotEmpty()) diacriticRepairs else defaultDiacriticRepairs

        // 3. Application des réparations orthographiques issues de ocr_repairs.xml
        text = applySpellingRepairs(text, repairs)

        // 4. Protection des mots-clés d'en-tête de section ("INGRÉDIENTS", "INSTRUCTIONS") contre la confusion 'I' -> '1'
        text = text.replace(OcrRegexes.Normalizer.PROTECTED_KEYWORDS) { match ->
            val suffix = match.groupValues[1]
            if (suffix.lowercase().startsWith("ng")) "ingrédients" else "instructions"
        }

        // 5. Suppression du préfixe 't' parasite devant un chiffre (ex: "t 10 min" -> "10 min")
        text = text.replace(OcrRegexes.Normalizer.PREFIX_T_BEFORE_DIGIT, "")

        // 6. Suppression des symboles de tolérance (±, +/-) et des puces parasites devant un chiffre (ex: "! 100 g" -> "100 g")
        text = text.replace("±", "").replace("+/-", "").replace("+-", "")
            .replace(OcrRegexes.Normalizer.ENVIRON_WORD, "")
            .replace(OcrRegexes.Normalizer.SYMBOL_BEFORE_DIGIT, "")

        // 7. Suppression du bruit de dates, devises et prix (ex: "14/12/94", "15.50 EUR", "20 €")
        OcrRegexes.Normalizer.NOISE_REMOVAL.forEach { (regex, replacement) -> text = text.replace(regex, replacement) }

        // 8. Correction de la confusion du chiffre '1' lu comme '|', '!', 'i' ou 'l' devant un mot
        text = text.replace(OcrRegexes.Normalizer.OCR_ONE_CORRECTION) { match -> match.groupValues[1] + "1 " }

        // 9. Garde-fou de performance Chiffres : ne déclencher FRACTION_RULES et NUMBER_CORRECTIONS que si la ligne contient au moins un chiffre
        if (text.any { it.isDigit() }) {
            OcrRegexes.Quantities.FRACTION_RULES.forEach { (regex, replacement) -> text = text.replace(regex, replacement) }
        }
        OcrRegexes.Quantities.NUMBER_CORRECTIONS.forEach { (regex, replacement) -> text = text.replace(regex, replacement) }

        // 10. Conversion des diacritiques et accents parasites Unicode (diacritic_repairs.xml)
        text = applyDiacriticRepairs(text, activeDiacritics)
        
        // 11. Corrections des articles ("1 e" -> "le") et liaisons linguistiques ("d' orange" -> "d'orange")
        OcrRegexes.Normalizer.ARTICLE_CORRECTIONS.forEach { (regex, replacement) -> text = text.replace(regex, replacement) }
        OcrRegexes.Normalizer.LINGUISTIC_CORRECTIONS.forEach { (regex, replacement) -> text = text.replace(regex, replacement) }

        // 12. Garde-fou de performance Cuillères : ne déclencher SPOON_NORMALIZATION que si la ligne contient la lettre 'c'
        if (text.contains('c', ignoreCase = true)) {
            OcrRegexes.Normalizer.SPOON_NORMALIZATION.forEach { (regex, replacement) -> text = text.replace(regex, replacement) }
        }

        // 13. Séparation d'un nombre collé à son unité (ex: "500g" -> "500 g", "1l" -> "1 l")
        text = text.replace(Regex("(?i)(?<!\\p{L})(\\d+)([a-z]+)")) { match ->
            "${match.groupValues[1]} ${match.groupValues[2].lowercase()}"
        }

        // 14. Nettoyage de la ponctuation finale parasite (virgules, deux-points, points-virgules) tout en préservant le tiret de césure
        text = text.replace(Regex("[,:;\\s]+$"), "")

        // 15. Effondrement des espaces multiples consécutifs en un espace unique et retrait des espaces de bordure
        return text.replace(Regex("\\s+"), " ").trim()
    }

    /**
     * Applique la table de conversion des diacritiques et accents parasites issue de [diacriticRepairs].
     *
     * @param text Le texte sur lequel corriger les diacritiques.
     * @param diacriticRepairs La liste des paires "fautif|correct" (ex: "ế|é").
     * @return Le texte nettoyé de ses diacritiques parasites.
     */
    private fun applyDiacriticRepairs(text: String, diacriticRepairs: List<String>): String {
        if (diacriticRepairs.isEmpty()) return text
        var result = text
        for (repair in diacriticRepairs) {
            val parts = repair.split("|")
            if (parts.size == 2) {
                result = result.replace(parts[0], parts[1])
            }
        }
        return result
    }

    /**
     * Applique les réparations orthographiques du dictionnaire XML [repairs] sur la chaîne [text].
     *
     * @param text Le texte sur lequel appliquer les réparations.
     * @param repairs La liste des réparations au format "fautif|correct".
     * @return Le texte réparé.
     */
    private fun applySpellingRepairs(text: String, repairs: List<String>): String {
        var result = text
        for (repair in repairs) {
            val parts = repair.split("|")
            if (parts.size == 2) {
                val wrong = parts[0].trim().replace("\\'", "'")
                val correct = parts[1].trim().replace("\\'", "'")
                val pattern = Regex("(?i)(?<!\\p{L})${Regex.escape(wrong)}(?!\\p{L})")
                result = result.replace(pattern, correct)
            }
        }
        return result
    }
}
