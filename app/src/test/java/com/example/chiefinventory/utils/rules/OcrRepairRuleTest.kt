package com.example.chiefinventory.utils.rules

import com.example.chiefinventory.utils.Ocr1Normalizer
import com.example.chiefinventory.utils.OcrVocabulary
import com.example.chiefinventory.utils.OcrVocabularyLoader
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test

class OcrRepairRuleTest {

    private lateinit var baseVocabulary: OcrVocabulary

    @Before
    fun setUp() {
        baseVocabulary = OcrVocabularyLoader.loadFromProjectResources()
    }

    @Test
    fun `normalize should apply all production ocr_spelling_repairs XML`() {
        baseVocabulary.ocrSpellingRepairs.forEach { repair ->
            val parts = repair.split("|")
            if (parts.size == 2) {
                val wrong = parts[0].trim().replace("\\'", "'")
                val expected = parts[1].trim().replace("\\'", "'")

                val normalized = Ocr1Normalizer.normalize(wrong, baseVocabulary.ocrSpellingRepairs)
                assertThat(normalized).ignoringCase().contains(expected.lowercase())
            }
        }
    }

    @Test
    fun `normalize should apply custom synthetic repair complement`() {
        val customRepairs = baseVocabulary.ocrSpellingRepairs + "mot_invalide_test|mot_corrige_test"
        val result = Ocr1Normalizer.normalize("Ceci est un mot_invalide_test", customRepairs)

        assertThat(result).contains("mot_corrige_test")
    }
}
