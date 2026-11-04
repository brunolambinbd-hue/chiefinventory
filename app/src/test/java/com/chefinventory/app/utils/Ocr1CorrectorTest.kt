package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertThat
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.Test
import java.io.File

/**
 * Suite de tests unitaires de Type B vérifiant le comportement et l'exhaustivité de [Ocr1Corrector].
 */
class Ocr1CorrectorTest {

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testNormalize_BlankAndEmptyInput() {
        assertThat(Ocr1Corrector.normalize("")).isEmpty()
        assertThat(Ocr1Corrector.normalize("   ")).isEmpty()
        assertThat(Ocr1Corrector.normalize("\n\t")).isEmpty()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testNormalize_SpellingRepairsFromVocabulary() {
        val vocabulary = OcrVocabularyLoader.loadFromProjectResources()
        val result1 = Ocr1Corrector.normalize("poisson au goit de crabe", vocabulary.ocrSpellingRepairs)
        assertThat(result1).isEqualTo("poisson au goût de crabe")

        val result2 = Ocr1Corrector.normalize("FONDUE ÀLA BIERE", vocabulary.ocrSpellingRepairs)
        assertThat(result2).isEqualTo("FONDUE À LA BIERE")

        // Test applySpellingRepairs with invalid formats and non-letter text
        assertThat(Ocr1Corrector.applySpellingRepairs("12345", listOf("wrong|correct"))).isEqualTo("12345")
        assertThat(Ocr1Corrector.applySpellingRepairs("test", listOf("invalid_no_pipe", "wrong|correct"))).isEqualTo("test")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testNormalize_DefaultDiacriticRepairsAndEmptyListAndInvalidEntries() {
        assertThat(Ocr1Corrector.defaultDiacriticRepairs).isNotEmpty()
        assertThat(Ocr1Corrector.normalize("ế")).isEqualTo("é")
        assertThat(Ocr1Corrector.normalize("ề")).isEqualTo("è")
        assertThat(Ocr1Corrector.normalize("ấ")).isEqualTo("â")
        assertThat(Ocr1Corrector.normalize("ầ")).isEqualTo("à")
        assertThat(Ocr1Corrector.normalize("ặ")).isEqualTo("a")

        // Empty diacritic repairs fallback
        assertThat(Ocr1Corrector.applyDiacriticRepairs("test", emptyList())).isEqualTo("test")

        // Invalid diacritic entries without pipe
        val withInvalidDiacritic = Ocr1Corrector.normalize("ế", diacriticRepairs = listOf("invalid_no_pipe"))
        assertThat(withInvalidDiacritic).isEqualTo("ế")
    }

    @Test
    @CoveredRequirement("6.3.2.9")
    fun testLoadDefaultDiacriticRepairs_InvalidDirectory_CatchesExceptionAndReturnsEmptyList() {
        val isolatedDir = File(System.getProperty("java.io.tmpdir"), "isolated_test_dir_normalizer_999")
        isolatedDir.mkdirs()

        val result = Ocr1Corrector.loadDefaultDiacriticRepairs(isolatedDir)
        assertThat(result).isEmpty()

        isolatedDir.delete()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testNormalize_ProtectedKeywords() {
        assertThat(Ocr1Corrector.normalize("Ingrédients")).isEqualTo("ingrédients")
        assertThat(Ocr1Corrector.normalize("I nstructions")).isEqualTo("instructions")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testNormalize_ToleranceSymbolAndEnvironRemoval() {
        assertThat(Ocr1Corrector.normalize("± 100 g")).isEqualTo("100 g")
        assertThat(Ocr1Corrector.normalize("200 g +/-")).isEqualTo("200 g")
        assertThat(Ocr1Corrector.normalize("+- 50 g")).isEqualTo("50 g")
        assertThat(Ocr1Corrector.normalize("environ 250 g")).isEqualTo("250 g")
        assertThat(Ocr1Corrector.normalize("250 g ENVIRON")).isEqualTo("250 g")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testNormalize_OcrTPrefixRemovalBeforeDigits() {
        assertThat(Ocr1Corrector.normalize("t 10 min")).isEqualTo("10 min")
        assertThat(Ocr1Corrector.normalize("t15 min")).isEqualTo("15 min")
        assertThat(Ocr1Corrector.normalize("6 aubergines moyennes (t 1 kg)")).isEqualTo("6 aubergines moyennes (1 kg)")
    }
}
