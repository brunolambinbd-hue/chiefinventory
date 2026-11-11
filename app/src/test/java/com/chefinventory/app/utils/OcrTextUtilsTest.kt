package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertWithMessage
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.Before
import org.junit.Test

/**
 * Suite de tests unitaires de Type B vérifiant le comportement de [OcrTextUtils].
 */
class OcrTextUtilsTest {

    private lateinit var vocabulary: OcrVocabulary

    @Before
    fun setUp() {
        vocabulary = OcrVocabularyLoader.loadFromProjectResources()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testIsInsideParentheses_PositionBeyondLength() {
        val text = "1 Oignon (poids 200g)"
        assertWithMessage("Une position au-delà de la longueur de la chaîne ne doit pas être qualifiée de position entre parenthèses")
            .that(OcrTextUtils.isInsideParentheses(text, 99))
            .isFalse()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testIsExcluded_BlankText() {
        assertWithMessage("Une chaîne d'espaces ne doit pas être qualifiée de mot exclu")
            .that(OcrTextUtils.isExcluded("   ", listOf("pub")))
            .isFalse()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testIsLikelyProperNameOrSource_LowercaseAndUppercaseCases() {
        assertWithMessage("Un nom en minuscules 'michel laroche' ne doit pas être qualifié de nom propre/source")
            .that(OcrTextUtils.isLikelyProperNameOrSource("michel laroche", vocabulary))
            .isFalse()

        assertWithMessage("Un nom d'établissement en majuscules 'HÔTEL HILTON' doit être qualifié de nom propre/source")
            .that(OcrTextUtils.isLikelyProperNameOrSource("HÔTEL HILTON", vocabulary))
            .isTrue()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun `isInsideParentheses should return true when position is inside brackets`() {
        val text = "1 Oignon (poids 200g)"
        assertWithMessage("La position 12 située à l'intérieur de '(poids 200g)' doit retourner vrai")
            .that(OcrTextUtils.isInsideParentheses(text, 12))
            .isTrue()

        assertWithMessage("La position 2 située avant la parenthèse doit retourner faux")
            .that(OcrTextUtils.isInsideParentheses(text, 2))
            .isFalse()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun `isExcluded should match case-insensitive exact words`() {
        assertWithMessage("Le mot 'PAGE' doit matcher l'exclusion 'page' (insensible à la casse)")
            .that(OcrTextUtils.isExcluded("PAGE", listOf("page")))
            .isTrue()

        assertWithMessage("Le mot 'Ingrédient' ne doit pas matcher l'exclusion 'page'")
            .that(OcrTextUtils.isExcluded("Ingrédient", listOf("page")))
            .isFalse()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun `isLikelyProperNameOrSource should reject food titles in uppercase`() {
        assertWithMessage("Le titre de plat 'RICOTTA ET ÉPINARDS' ne doit pas être qualifié de nom propre")
            .that(OcrTextUtils.isLikelyProperNameOrSource("RICOTTA ET ÉPINARDS", vocabulary))
            .isFalse()

        assertWithMessage("Le titre de plat 'CANAPÉS GRECS' ne doit pas être qualifié de nom propre")
            .that(OcrTextUtils.isLikelyProperNameOrSource("CANAPÉS GRECS", vocabulary))
            .isFalse()

        assertWithMessage("Le nom propre d'auteur 'MICHEL LAROCHE' doit être qualifié de nom propre")
            .that(OcrTextUtils.isLikelyProperNameOrSource("MICHEL LAROCHE", vocabulary))
            .isTrue()
    }
}
