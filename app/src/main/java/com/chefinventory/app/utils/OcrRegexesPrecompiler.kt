package com.chefinventory.app.utils

import com.parabd.lib.traceability.utils.LogManager

/**
 * Assistant de pré-compilation en amont des expressions régulières dynamiques ([OcrRegexesDynamicHelpers]).
 *
 * Responsabilités :
 * - Préchauffer l'ensemble des caches mutables de Regexes dynamiques au démarrage de l'application.
 * - Éliminer le surcoût de compilation JIT lors du premier scan OCR de l'utilisateur.
 */
object OcrRegexesPrecompiler {

    private const val TAG = "OcrRegexesPrecompiler"

    /**
     * Pré-compile en amont l'ensemble des motifs Regexes dynamiques issus du vocabulaire métier [OcrVocabulary].
     *
     * @param vocab L'instance de vocabulaire métier [OcrVocabulary].
     * @return Le nombre total de motifs Regex pré-compilés et mis en cache.
     */
    fun precompile(vocab: OcrVocabulary): Int {
        val startTime = System.currentTimeMillis()
        var compiledCount = 0

        // 1. Pré-compiler les motifs d'unités culinaires
        vocab.culinaryMeasurementUnits.forEach { unit ->
            if (unit.isNotBlank()) {
                OcrRegexesDynamicHelpers.getUnitPattern(unit)
                compiledCount++
            }
        }

        // 2. Pré-compiler les motifs de réparations orthographiques
        vocab.ocrSpellingRepairs.forEach { repairLine ->
            val wrong = repairLine.split("|").firstOrNull()?.trim().orEmpty()
            if (wrong.isNotBlank()) {
                OcrRegexesDynamicHelpers.getSpellingRepairPattern(wrong)
                compiledCount++
            }
        }

        // 3. Pré-compiler les préfixes téléphoniques
        vocab.phonePrefixes.forEach { prefix ->
            if (prefix.isNotBlank()) {
                OcrRegexesDynamicHelpers.getPhonePrefixPattern(prefix)
                compiledCount++
            }
        }

        // 4. Pré-compiler les verbes d'action d'instructions
        if (vocab.stepActionKeywords.isNotEmpty()) {
            OcrRegexesDynamicHelpers.getInstructionPrefixPattern(vocab.stepActionKeywords)
            compiledCount++
        }

        val duration = System.currentTimeMillis() - startTime
        LogManager.d(TAG, "⚡ [PRECOMPILER REGEX] $compiledCount motifs Regex dynamiques pré-compilés en $duration ms")
        return compiledCount
    }
}
