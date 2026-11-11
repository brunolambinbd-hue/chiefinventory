package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertWithMessage
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.Before
import org.junit.Test

/**
 * Suite de tests unitaires dédiée 1-à-1 pour [Ocr2CleanerAdSourceExtractor].
 */
class Ocr2CleanerAdSourceExtractorTest {

    private lateinit var vocabulary: OcrVocabulary

    @Before
    fun setUp() {
        vocabulary = OcrVocabularyLoader.loadFromProjectResources()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testObjectGeneratedMethodsCoverage() {
        assertWithMessage("Le composant Ocr2CleanerAdSourceExtractor doit générer un nom d'objet valide")
            .that(Ocr2CleanerAdSourceExtractor.toString())
            .contains("Ocr2CleanerAdSourceExtractor")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testExtractSourceFromAdLine_AllBranches() {
        // 1. Valid brand in ad line
        val result = Ocr2CleanerAdSourceExtractor.extractSourceFromAdLine("Assiette disponible dans les Maxi GB", vocabulary)
        assertWithMessage("L'enseigne source 'Maxi GB' présente dans la ligne publicitaire doit être extraite")
            .that(result)
            .isEqualTo("Maxi GB")

        // 2. Empty or blank input -> null
        assertWithMessage("Une ligne d'entrée vide ou blanche doit retourner null pour l'extraction de source")
            .that(Ocr2CleanerAdSourceExtractor.extractSourceFromAdLine("", vocabulary))
            .isNull()

        assertWithMessage("Une ligne d'entrée contenant uniquement des espaces doit retourner null")
            .that(Ocr2CleanerAdSourceExtractor.extractSourceFromAdLine("   ", vocabulary))
            .isNull()

        // 3. Vocabulary with empty knownSourceBrands -> null
        val emptyBrandsVocab = vocabulary.copy(knownSourceBrands = emptyList())
        assertWithMessage("Un dictionnaire d'enseignes sources vide doit retourner null")
            .that(Ocr2CleanerAdSourceExtractor.extractSourceFromAdLine("Assiette Carrefour", emptyBrandsVocab))
            .isNull()

        // 4. No matching brand in text -> null
        assertWithMessage("Une ligne publicitaire sans enseigne connue doit retourner null")
            .that(Ocr2CleanerAdSourceExtractor.extractSourceFromAdLine("Assiette disponible dans les supermarchés", vocabulary))
            .isNull()

        // 5. Blank brand entry in vocabulary list -> skipped safely
        val vocabWithBlankBrand = vocabulary.copy(knownSourceBrands = listOf("   ", "Delhaize"))
        val resultDelhaize = Ocr2CleanerAdSourceExtractor.extractSourceFromAdLine("Recette proposée par Delhaize", vocabWithBlankBrand)
        assertWithMessage("Les entrées d'enseignes blanches doivent être ignorées pour extraire 'Delhaize'")
            .that(resultDelhaize)
            .isEqualTo("Delhaize")
    }
}
