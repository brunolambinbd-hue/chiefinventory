package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertWithMessage
import org.junit.Before
import org.junit.Test
import java.util.Locale

/**
 * Test unitaire d'isolation TDD démontrant l'indépendance de la conversion de casse du pipeline OCR
 * par rapport à la locale du système d'exploitation hôte JVM.
 */
class OcrLocaleIndependenceTest {

    @Before
    fun setUp() {
        OcrVocabularyLoader.clearCache()
    }

    @Test
    fun testOcr1Corrector_IsIndependentOfHostJvmLocale() {
        val originalLocale = Locale.getDefault()
        try {
            // Simulation d'une locale avec règles de conversion de casse particulières (ex: Turc 'I' -> 'ı')
            Locale.setDefault(Locale("tr", "TR"))

            val input = "INGRÉDIENTS"
            val vocab = OcrVocabularyLoader.loadFromProjectResources()
            val result = Ocr1Corrector.correct(input, vocab.ocrSpellingRepairs, vocab.ocrDiacriticRepairs)

            assertWithMessage("Sous n'importe quelle locale JVM hôte, 'INGRÉDIENTS' doit être corrigé en 'ingrédients' avec le 'i' standard")
                .that(result.lowercase(Locale.ROOT))
                .isEqualTo("ingrédients")

            assertWithMessage("Le résultat ne doit pas contenir la lettre turque 'ı'")
                .that(result.contains("ı"))
                .isFalse()
        } finally {
            // Restauration de la locale système d'origine
            Locale.setDefault(originalLocale)
        }
    }
}
