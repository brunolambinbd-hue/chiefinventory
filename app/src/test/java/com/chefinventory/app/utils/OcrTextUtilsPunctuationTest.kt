package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertWithMessage
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Test unitaire TDD dédié spécifiquement à la fonction [OcrTextUtils.ensureSentenceEndingPunctuation].
 */
class OcrTextUtilsPunctuationTest {

    @Before
    fun setUp() {
        OcrVocabularyLoader.clearCache()
    }

    @After
    fun tearDown() {
        OcrVocabularyLoader.clearCache()
    }

    @Test
    fun testEnsureSentenceEndingPunctuation_appendsPeriodWhenMissing() {
        val terminators = listOf(".", "!", "?")

        assertWithMessage("Un point doit être ajouté en fin de phrase lorsqu'il manque")
            .that(OcrTextUtils.ensureSentenceEndingPunctuation("Mélanger la farine", terminators))
            .isEqualTo("Mélanger la farine.")
    }

    @Test
    fun testEnsureSentenceEndingPunctuation_preservesExistingPeriod() {
        val terminators = listOf(".", "!", "?")

        assertWithMessage("Un point existant doit être préservé sans en ajouter un second")
            .that(OcrTextUtils.ensureSentenceEndingPunctuation("Mélanger la farine.", terminators))
            .isEqualTo("Mélanger la farine.")
    }

    @Test
    fun testEnsureSentenceEndingPunctuation_preservesExclamationMark() {
        val terminators = listOf(".", "!", "?")

        assertWithMessage("Un point d'exclamation existant doit être préservé")
            .that(OcrTextUtils.ensureSentenceEndingPunctuation("Servir chaud !", terminators))
            .isEqualTo("Servir chaud !")
    }

    @Test
    fun testEnsureSentenceEndingPunctuation_preservesQuestionMark() {
        val terminators = listOf(".", "!", "?")

        assertWithMessage("Un point d'interrogation existant doit être préservé")
            .that(OcrTextUtils.ensureSentenceEndingPunctuation("Déguster chaud ?", terminators))
            .isEqualTo("Déguster chaud ?")
    }

    @Test
    fun testEnsureSentenceEndingPunctuation_handlesEmptyString() {
        val terminators = listOf(".", "!", "?")

        assertWithMessage("Une chaîne d'espaces doit retourner une chaîne vide")
            .that(OcrTextUtils.ensureSentenceEndingPunctuation("   ", terminators))
            .isEmpty()
    }

    @Test
    fun testIsFrenchLetterOrDigit_validatesAllFrenchAccentedLettersAndDigits() {
        assertWithMessage("Le caractère 'a' doit être reconnu comme lettre française")
            .that(OcrTextUtils.isFrenchLetterOrDigit('a'))
            .isTrue()

        assertWithMessage("Le caractère 'Z' doit être reconnu comme lettre française")
            .that(OcrTextUtils.isFrenchLetterOrDigit('Z'))
            .isTrue()

        assertWithMessage("Le caractère '5' doit être reconnu comme chiffre")
            .that(OcrTextUtils.isFrenchLetterOrDigit('5'))
            .isTrue()

        val frenchAccents = "àâäéèêëîïôöùûüÿçæœÀÂÄÉÈÊËÎÏÔÖÙÛÜŸÇÆŒ"
        frenchAccents.forEach { char ->
            assertWithMessage("Le caractère accentué français '$char' doit être validé")
                .that(OcrTextUtils.isFrenchLetterOrDigit(char))
                .isTrue()
        }
    }

    @Test
    fun testIsFrenchLetterOrDigit_rejectsForeignGlyphsAndSymbols() {
        val foreignGlyphsAndSymbols = "Ə©§∂∆∑¶®™|!*•-+="
        foreignGlyphsAndSymbols.forEach { char ->
            assertWithMessage("Le glyphe/symbole parasite '$char' doit être rejeté")
                .that(OcrTextUtils.isFrenchLetterOrDigit(char))
                .isFalse()
        }
    }

    @Test
    fun testIsNonAlphanumericNoise_identifiesSymbolOnlyAndForeignGlyphLines() {
        assertWithMessage("Le symbole '©' doit être reconnu comme bruit")
            .that(OcrTextUtils.isNonAlphanumericNoise("©"))
            .isTrue()

        assertWithMessage("Le symbole '§' doit être reconnu comme bruit")
            .that(OcrTextUtils.isNonAlphanumericNoise("§"))
            .isTrue()

        assertWithMessage("Le glyphe 'Ə' doit être reconnu comme bruit")
            .that(OcrTextUtils.isNonAlphanumericNoise("Ə"))
            .isTrue()

        assertWithMessage("Le glyphe 'ƏƏ' doit être reconnu comme bruit")
            .that(OcrTextUtils.isNonAlphanumericNoise("ƏƏ"))
            .isTrue()

        assertWithMessage("La suite de puces '• - *' doit être reconnue comme bruit")
            .that(OcrTextUtils.isNonAlphanumericNoise("• - *"))
            .isTrue()

        assertWithMessage("La suite de symboles '| ! -' doit être reconnue comme bruit")
            .that(OcrTextUtils.isNonAlphanumericNoise("| ! -"))
            .isTrue()

        assertWithMessage("La suite de symboles mathématiques '∂ ∆ ∑' doit être reconnue comme bruit")
            .that(OcrTextUtils.isNonAlphanumericNoise("∂ ∆ ∑"))
            .isTrue()
    }

    @Test
    fun testIsNonAlphanumericNoise_returnsFalseForValidFrenchTextAndDigits() {
        assertWithMessage("La lettrine 'E' ne doit pas être qualifiée de bruit de symbole")
            .that(OcrTextUtils.isNonAlphanumericNoise("E"))
            .isFalse()

        assertWithMessage("La durée '10 min' ne doit pas être qualifiée de bruit")
            .that(OcrTextUtils.isNonAlphanumericNoise("10 min"))
            .isFalse()

        assertWithMessage("Le mot 'Éplucher' ne doit pas être qualifié de bruit")
            .that(OcrTextUtils.isNonAlphanumericNoise("Éplucher"))
            .isFalse()

        assertWithMessage("Le mot 'Cœur' ne doit pas être qualifié de bruit")
            .that(OcrTextUtils.isNonAlphanumericNoise("Cœur"))
            .isFalse()

        assertWithMessage("Le mot 'Bûchettes' ne doit pas être qualifié de bruit")
            .that(OcrTextUtils.isNonAlphanumericNoise("Bûchettes"))
            .isFalse()

        assertWithMessage("Une chaîne d'espaces ne doit pas être qualifiée de bruit de symbole")
            .that(OcrTextUtils.isNonAlphanumericNoise("   "))
            .isFalse()
    }

    @Test
    fun testIsShortNoise_identifiesOrphanShortFragmentLines() {
        val trashWords = setOf("le", "la", "les", "du", "des", "pour")

        assertWithMessage("La lettrine seule 'E' doit être reconnue comme bruit court orphelin")
            .that(OcrTextUtils.isShortNoise("E", trashWords))
            .isTrue()

        assertWithMessage("Le fragment orphelin 'e t' doit être reconnu comme bruit court")
            .that(OcrTextUtils.isShortNoise("e t", trashWords))
            .isTrue()

        assertWithMessage("L'article isolé 'de' doit être reconnu comme bruit court")
            .that(OcrTextUtils.isShortNoise("de", trashWords))
            .isTrue()

        assertWithMessage("L'article isolé 'pour' doit être reconnu comme bruit court")
            .that(OcrTextUtils.isShortNoise("pour", trashWords))
            .isTrue()
    }

    @Test
    fun testIsShortNoise_returnsFalseForValidTextDigitsAndActions() {
        val trashWords = setOf("le", "la", "les", "du", "des", "pour")

        assertWithMessage("La durée '10 min' avec chiffres ne doit pas être qualifiée de bruit court")
            .that(OcrTextUtils.isShortNoise("10 min", trashWords))
            .isFalse()

        assertWithMessage("La quantité '200 g' avec chiffres ne doit pas être qualifiée de bruit court")
            .that(OcrTextUtils.isShortNoise("200 g", trashWords))
            .isFalse()

        assertWithMessage("Le verbe d'action 'Éplucher' ne doit pas être qualifié de bruit court")
            .that(OcrTextUtils.isShortNoise("Éplucher", trashWords, hasActionVerb = true))
            .isFalse()

        assertWithMessage("Le nom propre 'Nahit YILMAZ' ne doit pas être qualifié de bruit court")
            .that(OcrTextUtils.isShortNoise("Nahit YILMAZ", trashWords))
            .isFalse()

        assertWithMessage("Une chaîne d'espaces ne doit pas être qualifiée de bruit court")
            .that(OcrTextUtils.isShortNoise("   ", trashWords))
            .isFalse()
    }
}
