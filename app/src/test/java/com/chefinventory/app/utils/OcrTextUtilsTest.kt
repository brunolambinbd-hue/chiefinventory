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

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testOcrTextUtils_AllFunctionsExhaustiveCoverage() {
        assertWithMessage("OcrTextUtils doit générer une représentation textuelle valide")
            .that(OcrTextUtils.toString())
            .contains("OcrTextUtils")

        // 1. ensureSentenceEndingPunctuation
        assertWithMessage("Chaîne vide").that(OcrTextUtils.ensureSentenceEndingPunctuation("   ")).isEmpty()
        assertWithMessage("Se termine déjà par un point").that(OcrTextUtils.ensureSentenceEndingPunctuation("Mélanger.", listOf(".", "!", "?"))).isEqualTo("Mélanger.")
        assertWithMessage("Se termine déjà par un point d'exclamation").that(OcrTextUtils.ensureSentenceEndingPunctuation("Mélanger!", listOf(".", "!", "?"))).isEqualTo("Mélanger!")
        assertWithMessage("Ajoute un point par défaut").that(OcrTextUtils.ensureSentenceEndingPunctuation("Mélanger")).isEqualTo("Mélanger.")

        // 2. isFrenchLetterOrDigit
        assertWithMessage("a").that(OcrTextUtils.isFrenchLetterOrDigit('a')).isTrue()
        assertWithMessage("A").that(OcrTextUtils.isFrenchLetterOrDigit('A')).isTrue()
        assertWithMessage("5").that(OcrTextUtils.isFrenchLetterOrDigit('5')).isTrue()
        assertWithMessage("é").that(OcrTextUtils.isFrenchLetterOrDigit('é')).isTrue()
        assertWithMessage("É").that(OcrTextUtils.isFrenchLetterOrDigit('É')).isTrue()
        assertWithMessage("#").that(OcrTextUtils.isFrenchLetterOrDigit('#')).isFalse()

        // 3. isNonAlphanumericNoise
        assertWithMessage("Chaîne vide").that(OcrTextUtils.isNonAlphanumericNoise("   ")).isFalse()
        assertWithMessage("Symboles seuls").that(OcrTextUtils.isNonAlphanumericNoise("• - *")).isTrue()
        assertWithMessage("Texte valide").that(OcrTextUtils.isNonAlphanumericNoise("200 g de farine")).isFalse()

        // 4. isShortNoise
        assertWithMessage("Avec verbe d'action").that(OcrTextUtils.isShortNoise("Mélanger", hasActionVerb = true)).isFalse()
        assertWithMessage("Ligne vide").that(OcrTextUtils.isShortNoise("   ")).isFalse()
        assertWithMessage("Avec chiffres").that(OcrTextUtils.isShortNoise("200 g")).isFalse()
        assertWithMessage("Avec point").that(OcrTextUtils.isShortNoise("a.b")).isFalse()
        assertWithMessage("Bruit <= 3 chars").that(OcrTextUtils.isShortNoise("abc")).isTrue()
        assertWithMessage("Mot poubelle").that(OcrTextUtils.isShortNoise("pour", standaloneTrashWords = setOf("pour"))).isTrue()
        assertWithMessage("Mot long hors poubelle").that(OcrTextUtils.isShortNoise("beaucoup", standaloneTrashWords = setOf("pour"))).isFalse()

        // 5. isLikelyProperNameOrSource sans vocabulaire
        assertWithMessage("Nom propre sans vocabulaire").that(OcrTextUtils.isLikelyProperNameOrSource("MICHEL LAROCHE")).isTrue()
        assertWithMessage("Titre plat sans vocabulaire").that(OcrTextUtils.isLikelyProperNameOrSource("Salade de tomates")).isFalse()
    }
}
