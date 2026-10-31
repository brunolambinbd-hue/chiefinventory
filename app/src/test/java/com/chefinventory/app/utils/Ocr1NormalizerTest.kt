package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertThat
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.Test
import java.io.File

/**
 * Suite de tests unitaires de Type B vérifiant le comportement et l'exhaustivité de [Ocr1Normalizer].
 */
class Ocr1NormalizerTest {

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testNormalize_BlankAndEmptyInput() {
        assertThat(Ocr1Normalizer.normalize("")).isEmpty()
        assertThat(Ocr1Normalizer.normalize("   ")).isEmpty()
        assertThat(Ocr1Normalizer.normalize("\n\t")).isEmpty()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testNormalize_SpellingRepairsFromVocabulary() {
        val vocabulary = OcrVocabularyLoader.loadFromProjectResources()
        val result1 = Ocr1Normalizer.normalize("poisson au goit de crabe", vocabulary.ocrSpellingRepairs)
        assertThat(result1).isEqualTo("poisson au goût de crabe")

        val result2 = Ocr1Normalizer.normalize("FONDUE ÀLA BIERE", vocabulary.ocrSpellingRepairs)
        assertThat(result2).isEqualTo("FONDUE À LA BIERE")

        // Test applySpellingRepairs with invalid formats and non-letter text
        assertThat(Ocr1Normalizer.applySpellingRepairs("12345", listOf("wrong|correct"))).isEqualTo("12345")
        assertThat(Ocr1Normalizer.applySpellingRepairs("test", listOf("invalid_no_pipe", "wrong|correct"))).isEqualTo("test")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testNormalize_DefaultDiacriticRepairsAndEmptyListAndInvalidEntries() {
        assertThat(Ocr1Normalizer.defaultDiacriticRepairs).isNotEmpty()
        assertThat(Ocr1Normalizer.normalize("ế")).isEqualTo("é")
        assertThat(Ocr1Normalizer.normalize("ề")).isEqualTo("è")
        assertThat(Ocr1Normalizer.normalize("ấ")).isEqualTo("â")
        assertThat(Ocr1Normalizer.normalize("ầ")).isEqualTo("à")
        assertThat(Ocr1Normalizer.normalize("ặ")).isEqualTo("a")

        // Empty diacritic repairs fallback
        assertThat(Ocr1Normalizer.applyDiacriticRepairs("test", emptyList())).isEqualTo("test")

        // Invalid diacritic entries without pipe
        val withInvalidDiacritic = Ocr1Normalizer.normalize("ế", diacriticRepairs = listOf("invalid_no_pipe"))
        assertThat(withInvalidDiacritic).isEqualTo("ế")
    }

    @Test
    @CoveredRequirement("6.3.2.9")
    fun testLoadDefaultDiacriticRepairs_InvalidDirectory_CatchesExceptionAndReturnsEmptyList() {
        val isolatedDir = File(System.getProperty("java.io.tmpdir"), "isolated_test_dir_normalizer_999")
        isolatedDir.mkdirs()

        val result = Ocr1Normalizer.loadDefaultDiacriticRepairs(isolatedDir)
        assertThat(result).isEmpty()

        isolatedDir.delete()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testNormalize_ProtectedKeywords() {
        assertThat(Ocr1Normalizer.normalize("Ingrédients")).isEqualTo("ingrédients")
        assertThat(Ocr1Normalizer.normalize("I nstructions")).isEqualTo("instructions")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testNormalize_ToleranceSymbolAndEnvironRemoval() {
        assertThat(Ocr1Normalizer.normalize("± 100 g")).isEqualTo("100 g")
        assertThat(Ocr1Normalizer.normalize("200 g +/-")).isEqualTo("200 g")
        assertThat(Ocr1Normalizer.normalize("+- 50 g")).isEqualTo("50 g")
        assertThat(Ocr1Normalizer.normalize("environ 250 g")).isEqualTo("250 g")
        assertThat(Ocr1Normalizer.normalize("250 g ENVIRON")).isEqualTo("250 g")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testNormalize_OcrTPrefixRemovalBeforeDigits() {
        assertThat(Ocr1Normalizer.normalize("t 10 min")).isEqualTo("10 min")
        assertThat(Ocr1Normalizer.normalize("t15 min")).isEqualTo("15 min")
        assertThat(Ocr1Normalizer.normalize("6 aubergines moyennes (t 1 kg)")).isEqualTo("6 aubergines moyennes (1 kg)")
    }
}
