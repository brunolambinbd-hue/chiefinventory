package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertWithMessage
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.Before
import org.junit.Test

/**
 * Suite de tests unitaires dédiée 1-à-1 pour [Ocr3CategorizerProperNameDetector].
 */
class Ocr3CategorizerProperNameDetectorTest {

    private lateinit var vocabulary: OcrVocabulary

    @Before
    fun setUp() {
        vocabulary = OcrVocabularyLoader.loadFromProjectResources()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testObjectGeneratedMethodsCoverage() {
        assertWithMessage("Le composant Ocr3CategorizerProperNameDetector doit générer une représentation textuelle valide")
            .that(Ocr3CategorizerProperNameDetector.toString())
            .contains("Ocr3CategorizerProperNameDetector")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testIsLikelyProperNameOrSource_AllBranches() {
        // 1. Valid uppercase proper names
        assertWithMessage("Le nom propre en majuscules 'MICHEL LAROCHE' doit être reconnu comme nom propre/source")
            .that(Ocr3CategorizerProperNameDetector.isLikelyProperNameOrSource("MICHEL LAROCHE", vocabulary))
            .isTrue()

        assertWithMessage("Le nom d'établissement 'HÔTEL HILTON' doit être reconnu comme nom propre/source")
            .that(Ocr3CategorizerProperNameDetector.isLikelyProperNameOrSource("HÔTEL HILTON", vocabulary))
            .isTrue()

        // 2. Default argument overload (vocabulary = null)
        assertWithMessage("Le surchargement de vocabulaire par défaut (null) doit valider 'MICHEL LAROCHE'")
            .that(Ocr3CategorizerProperNameDetector.isLikelyProperNameOrSource("MICHEL LAROCHE"))
            .isTrue()

        // 3. Lowercase name -> false
        assertWithMessage("Un nom en minuscules 'michel laroche' ne doit pas être qualifié de nom propre")
            .that(Ocr3CategorizerProperNameDetector.isLikelyProperNameOrSource("michel laroche", vocabulary))
            .isFalse()

        // 4. Word count not in 2..4 -> false
        assertWithMessage("Un seul mot 'MICHEL' ne doit pas être qualifié de nom propre")
            .that(Ocr3CategorizerProperNameDetector.isLikelyProperNameOrSource("MICHEL", vocabulary))
            .isFalse()

        assertWithMessage("Un titre de 5 mots 'UN DEUX TROIS QUATRE CINQ' ne doit pas être qualifié de nom propre")
            .that(Ocr3CategorizerProperNameDetector.isLikelyProperNameOrSource("UN DEUX TROIS QUATRE CINQ", vocabulary))
            .isFalse()

        // 5. Contains digits -> false
        assertWithMessage("Une ligne contenant des chiffres 'HÔTEL HILTON 123' ne doit pas être qualifiée de nom propre")
            .that(Ocr3CategorizerProperNameDetector.isLikelyProperNameOrSource("HÔTEL HILTON 123", vocabulary))
            .isFalse()

        // 6. Contains food connector or food word -> false
        assertWithMessage("La ligne 'RICOTTA ET ÉPINARDS' contenant un aliment/connecteur ne doit pas être qualifiée de nom propre")
            .that(Ocr3CategorizerProperNameDetector.isLikelyProperNameOrSource("RICOTTA ET ÉPINARDS", vocabulary))
            .isFalse()

        assertWithMessage("La ligne 'CANAPÉS GRECS' contenant un plat ne doit pas être qualifiée de nom propre")
            .that(Ocr3CategorizerProperNameDetector.isLikelyProperNameOrSource("CANAPÉS GRECS", vocabulary))
            .isFalse()

        // 7. With null vocabulary
        assertWithMessage("L'évaluation avec vocabulaire null doit reconnaître 'MICHEL LAROCHE'")
            .that(Ocr3CategorizerProperNameDetector.isLikelyProperNameOrSource("MICHEL LAROCHE", null))
            .isTrue()
    }
}
