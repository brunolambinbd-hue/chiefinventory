package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertWithMessage
import org.junit.Before
import org.junit.Test

/**
 * Tests unitaires isolés pour l'Étape 1 du pipeline OCR ([Ocr1Corrector]).
 */
class Ocr1CorrectorTest {

    private lateinit var vocabulary: OcrVocabulary

    @Before
    fun setUp() {
        vocabulary = OcrVocabularyLoader.loadFromProjectResources()
    }

    @Test
    fun testNormalize_StandardFlowsAndSpellingRepairs() {
        // Blank input
        assertWithMessage("Le texte d'entrée vide doit retourner une chaîne vide")
            .that(Ocr1Corrector.correct(""))
            .isEmpty()

        // Fraction rules and number corrections
        assertWithMessage("Les règles de fractions et unités doivent être conservées")
            .that(Ocr1Corrector.correct("1/2 l de lait"))
            .isEqualTo("1/2 l de lait")

        // Spelling repairs from dictionary
        val result1 = Ocr1Corrector.correct("poisson au goit de crabe", vocabulary.ocrSpellingRepairs)
        assertWithMessage("La réparation orthographique 'goit' -> 'goût' doit être appliquée")
            .that(result1)
            .isEqualTo("poisson au goût de crabe")

        val result2 = Ocr1Corrector.correct("FONDUE ÀLA BIERE", vocabulary.ocrSpellingRepairs)
        assertWithMessage("La séparation de contraction 'ÀLA' -> 'À LA' doit être appliquée")
            .that(result2)
            .isEqualTo("FONDUE À LA BIERE")

        // Test applySpellingRepairs with invalid formats and non-letter text
        assertWithMessage("Les lignes sans lettres ne doivent pas appliquer de réparation orthographique")
            .that(Ocr1Corrector.applySpellingRepairs("12345", listOf("wrong|correct")))
            .isEqualTo("12345")

        assertWithMessage("Les règles de réparations sans pipe '|' doivent être ignorées de manière sécurisée")
            .that(Ocr1Corrector.applySpellingRepairs("test", listOf("invalid_no_pipe", "wrong|correct")))
            .isEqualTo("test")
    }

    @Test
    fun testNormalize_DiacriticRepairsAndEmptyListAndInvalidEntries() {
        val diacritics = vocabulary.ocrDiacriticRepairs
        assertWithMessage("Le diacritique corrompu 'ế' doit être réparé en 'é'")
            .that(Ocr1Corrector.correct("ế", diacriticRepairs = diacritics))
            .isEqualTo("é")

        assertWithMessage("Le diacritique corrompu 'ề' doit être réparé en 'è'")
            .that(Ocr1Corrector.correct("ề", diacriticRepairs = diacritics))
            .isEqualTo("è")

        assertWithMessage("Le diacritique corrompu 'ấ' doit être réparé en 'â'")
            .that(Ocr1Corrector.correct("ấ", diacriticRepairs = diacritics))
            .isEqualTo("â")

        assertWithMessage("Le diacritique corrompu 'ầ' doit être réparé en 'à'")
            .that(Ocr1Corrector.correct("ầ", diacriticRepairs = diacritics))
            .isEqualTo("à")

        assertWithMessage("Le diacritique corrompu 'ặ' doit être réparé en 'a'")
            .that(Ocr1Corrector.correct("ặ", diacriticRepairs = diacritics))
            .isEqualTo("a")

        // Empty diacritic repairs fallback
        assertWithMessage("Une liste de diacritiques vide doit retourner le texte inchangé")
            .that(Ocr1Corrector.applyDiacriticRepairs("test", emptyList()))
            .isEqualTo("test")

        // Invalid diacritic entries without pipe
        val withInvalidDiacritic = Ocr1Corrector.correct("ế", diacriticRepairs = listOf("invalid_no_pipe"))
        assertWithMessage("Les entrées de diacritiques invalides doivent être ignorées sans planter")
            .that(withInvalidDiacritic)
            .isEqualTo("ế")
    }

    @Test
    fun testNormalize_TrailingPunctuationRemoval() {
        assertWithMessage("La virgule parasite de fin de ligne doit être retirée")
            .that(Ocr1Corrector.correct("500 g de farine,"))
            .isEqualTo("500 g de farine")

        assertWithMessage("Les deux-points parasites de fin de ligne doivent être retirés")
            .that(Ocr1Corrector.correct("200 ml d'eau:"))
            .isEqualTo("200 ml d'eau")

        assertWithMessage("Le point-virgule parasite de fin de ligne doit être retiré")
            .that(Ocr1Corrector.correct("1 pincée de sel;"))
            .isEqualTo("1 pincée de sel")

        // Trailing hyphen should be preserved for hyphenated multi-line instructions
        assertWithMessage("Le tiret de césure de fin de ligne doit être préservé pour l'Étape 4")
            .that(Ocr1Corrector.correct("Hachez finement le surimi ou-"))
            .isEqualTo("Hachez finement le surimi ou-")
    }

    @Test
    fun testCorrect_NfcUnicodeNormalization() {
        // Forme NFD décomposée ('a' + U+0300)
        val nfdInput = "1 mangue a\u0300 point"
        val expectedNfc = "1 mangue à point"

        val result = Ocr1Corrector.correct(nfdInput, vocabulary.ocrSpellingRepairs, vocabulary.ocrDiacriticRepairs)

        assertWithMessage("Ocr1Corrector doit normaliser la forme NFD décomposée vers la forme NFC canonique")
            .that(result)
            .isEqualTo(expectedNfc)

        assertWithMessage("La longueur de la chaîne NFC normalisée doit correspondre exactement au texte canonique")
            .that(result.length)
            .isEqualTo(expectedNfc.length)
    }

    @Test
    fun testExhaustiveOcr1CorrectorCoverage_WithoutModifyingSourceFile() {
        assertWithMessage("Ocr1Corrector doit générer une représentation textuelle valide")
            .that(Ocr1Corrector.toString())
            .contains("Ocr1Corrector")

        // 1. loadDefaultDiacriticRepairs with error fallback (Line 39, 40, 41)
        val tempFile = java.io.File.createTempFile("invalid_dir", ".tmp")
        val emptyDiacritics = Ocr1Corrector.loadDefaultDiacriticRepairs(tempFile)
        tempFile.delete()
        assertWithMessage("Un fichier invalide doit déclencher le catch et retourner une liste vide")
            .that(emptyDiacritics)
            .isEmpty()

        // 2. defaultDiacriticRepairs getter (Line 57)
        val defaultDiacritics = Ocr1Corrector.defaultDiacriticRepairs
        assertWithMessage("Les diacritiques par défaut doivent être chargés")
            .that(defaultDiacritics)
            .isNotEmpty()

        // 3. correctLine with 2D OcrTextLine (Line 58)
        val blank2DLine = OcrTextLine("   ", top = 100, left = 50)
        val correctedBlank2D = Ocr1Corrector.correctLine(blank2DLine)
        assertWithMessage("Une ligne 2D blanche doit être retournée nettoyée").that(correctedBlank2D.text).isEmpty()

        val valid2DLine = OcrTextLine("500g de farine", top = 100, left = 50)
        val correctedValid2D = Ocr1Corrector.correctLine(valid2DLine)
        assertWithMessage("Une ligne 2D valide doit être corrigée").that(correctedValid2D.text).isEqualTo("500 g de farine")

        // 4. Prefix 't' before digit removal (Line 108)
        val resultPrefixT = Ocr1Corrector.correct("t 10 min")
        assertWithMessage("Le préfixe 't' parasite devant un chiffre doit être retiré").that(resultPrefixT).contains("10 min")

        // 5. Number-unit separation (Line 129)
        val resultNumUnit = Ocr1Corrector.correct("500g 1l")
        assertWithMessage("Les nombres collés aux unités '500g 1l' doivent être séparés en '500 g 1 l'")
            .that(resultNumUnit)
            .isEqualTo("500 g 1 l")

        // 6. applyCaseSensitiveSpellingRepairs (Line 240)
        assertWithMessage("Texte sans lettres ne doit pas appliquer de réparation sensible à la casse")
            .that(Ocr1Corrector.applyCaseSensitiveSpellingRepairs("12345", listOf("wrong|correct")))
            .isEqualTo("12345")

        assertWithMessage("Règles vides doivent retourner le texte inchangé")
            .that(Ocr1Corrector.applyCaseSensitiveSpellingRepairs("RecouVrez", emptyList()))
            .isEqualTo("RecouVrez")

        val resultCaseSensitive = Ocr1Corrector.applyCaseSensitiveSpellingRepairs("RecouVrez", listOf("RecouVrez | Recouvrez", "invalid_rule_no_pipe"))
        assertWithMessage("La réparation sensible à la casse 'RecouVrez' -> 'Recouvrez' doit être appliquée")
            .that(resultCaseSensitive)
            .isEqualTo("Recouvrez")

        // 7. Deprecated normalize function
        @Suppress("DEPRECATION")
        val resultNormalize = Ocr1Corrector.normalize("500g de farine")
        assertWithMessage("La méthode normalize doit agir comme un alias de correct").that(resultNormalize).isEqualTo("500 g de farine")
    }
}
