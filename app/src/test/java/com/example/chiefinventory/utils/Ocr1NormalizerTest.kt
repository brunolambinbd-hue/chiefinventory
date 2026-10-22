package com.example.chiefinventory.utils

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Suite de tests unitaires de Type B vérifiant le comportement et l'exhaustivité de [Ocr1Normalizer].
 */
class Ocr1NormalizerTest {

    @Test
    fun `Blank and empty input handling`() {
        assertThat(Ocr1Normalizer.normalize("")).isEmpty()
        assertThat(Ocr1Normalizer.normalize("   ")).isEmpty()
        assertThat(Ocr1Normalizer.normalize("\n\t")).isEmpty()
    }

    @Test
    fun `spelling repairs from vocabulary should fix goit to goût`() {
        val vocabulary = OcrVocabularyLoader.loadFromProjectResources()
        val result = Ocr1Normalizer.normalize("poisson au goit de crabe", vocabulary.ocrSpellingRepairs)
        assertThat(result).isEqualTo("poisson au goût de crabe")
    }

    @Test
    fun `Vietnamese to French accent mapping`() {
        assertThat(Ocr1Normalizer.normalize("ế")).isEqualTo("é")
        assertThat(Ocr1Normalizer.normalize("ề")).isEqualTo("è")
        assertThat(Ocr1Normalizer.normalize("ấ")).isEqualTo("â")
        assertThat(Ocr1Normalizer.normalize("ầ")).isEqualTo("à")
        assertThat(Ocr1Normalizer.normalize("ặ")).isEqualTo("a")
    }

    @Test
    fun `Tolerance symbol and environ removal`() {
        assertThat(Ocr1Normalizer.normalize("± 100 g")).isEqualTo("100 g")
        assertThat(Ocr1Normalizer.normalize("200 g +/-")).isEqualTo("200 g")
        assertThat(Ocr1Normalizer.normalize("+- 50 g")).isEqualTo("50 g")
        assertThat(Ocr1Normalizer.normalize("environ 250 g")).isEqualTo("250 g")
        assertThat(Ocr1Normalizer.normalize("250 g ENVIRON")).isEqualTo("250 g")
    }

    @Test
    fun `OCR t prefix removal before digits`() {
        assertThat(Ocr1Normalizer.normalize("t 10 min")).isEqualTo("10 min")
        assertThat(Ocr1Normalizer.normalize("t15 min")).isEqualTo("15 min")
        assertThat(Ocr1Normalizer.normalize("6 aubergines moyennes (t 1 kg)")).isEqualTo("6 aubergines moyennes (1 kg)")
        assertThat(Ocr1Normalizer.normalize("6 aubergines moyennes (t1 kg)")).isEqualTo("6 aubergines moyennes (1 kg)")
    }

    @Test
    fun `Misread digit 1 as symbol or letter`() {
        assertThat(Ocr1Normalizer.normalize("I orange")).isEqualTo("1 orange")
        assertThat(Ocr1Normalizer.normalize("| citron")).isEqualTo("1 citron")
        assertThat(Ocr1Normalizer.normalize("l citron")).isEqualTo("1 citron")
        assertThat(Ocr1Normalizer.normalize("L citron")).isEqualTo("1 citron")
        assertThat(Ocr1Normalizer.normalize("! gousse")).isEqualTo("1 gousse")
        assertThat(Ocr1Normalizer.normalize("l kg")).isEqualTo("1 kg")
    }

    @Test
    fun `Fraction normalization for 1 2`() {
        assertThat(Ocr1Normalizer.normalize("ll2")).isEqualTo("1/2")
        assertThat(Ocr1Normalizer.normalize("lI2")).isEqualTo("1/2")
        assertThat(Ocr1Normalizer.normalize("!!2")).isEqualTo("1/2")
        assertThat(Ocr1Normalizer.normalize("U2")).isEqualTo("1/2")
        assertThat(Ocr1Normalizer.normalize("W2")).isEqualTo("1/2")
        assertThat(Ocr1Normalizer.normalize("1 12")).isEqualTo("1/2")
    }

    @Test
    fun `Fraction normalization for 1 4`() {
        assertThat(Ocr1Normalizer.normalize("ll4")).isEqualTo("1/4")
        assertThat(Ocr1Normalizer.normalize("!!4")).isEqualTo("1/4")
        assertThat(Ocr1Normalizer.normalize("1 14")).isEqualTo("1/4")
    }

    @Test
    fun `Fraction context preservation for mid sentence 1 2`() {
        assertThat(Ocr1Normalizer.normalize("112 citron")).isEqualTo("1/2 citron")
        assertThat(Ocr1Normalizer.normalize("112 oignon")).isEqualTo("1/2 oignon")
        assertThat(Ocr1Normalizer.normalize("12 oignon")).isEqualTo("1/2 oignon")
    }

    @Test
    fun `Number correction for del and ld eau`() {
        assertThat(Ocr1Normalizer.normalize("del")).isEqualTo("de 1")
        assertThat(Ocr1Normalizer.normalize("ld'eau")).isEqualTo("1 l d'eau")
        assertThat(Ocr1Normalizer.normalize("ld’eau")).isEqualTo("1 l d'eau")
    }

    @Test
    fun `Article normalization for leading 1 e and T`() {
        assertThat(Ocr1Normalizer.normalize("1 e citron")).isEqualTo("le citron")
        assertThat(Ocr1Normalizer.normalize("T'oignon")).isEqualTo("l'oignon")
        assertThat(Ocr1Normalizer.normalize("1'ail")).isEqualTo("l'ail")
    }

    @Test
    fun `NUMBER_CORRECTIONS list validation`() {
        assertThat(Ocr1Normalizer.normalize("ld'eau")).isEqualTo("1 l d'eau")
        assertThat(Ocr1Normalizer.normalize("1 ajout")).isEqualTo("l'ajout")
        assertThat(Ocr1Normalizer.normalize("| citron")).isEqualTo("1 citron")
        assertThat(Ocr1Normalizer.normalize("I/2")).isEqualTo("1/2")
        assertThat(Ocr1Normalizer.normalize("dose | dose")).isEqualTo("dose 1 dose")
        assertThat(Ocr1Normalizer.normalize("del citron")).isEqualTo("de 1 citron")
        assertThat(Ocr1Normalizer.normalize("l kg")).isEqualTo("1 kg")
        assertThat(Ocr1Normalizer.normalize("I ml")).isEqualTo("1 ml")
    }

    @Test
    fun `Linguistic spacing for apostrophes`() {
        assertThat(Ocr1Normalizer.normalize("jus d' orange")).isEqualTo("jus d'orange")
        assertThat(Ocr1Normalizer.normalize("jauned'oeuf")).isEqualTo("jaune d'oeuf")
    }

    @Test
    fun `Spelling correction for oeuf and l ajout`() {
        assertThat(Ocr1Normalizer.normalize("1 euf")).isEqualTo("1 oeuf")
        assertThat(Ocr1Normalizer.normalize("2 eufs")).isEqualTo("2 oeufs")
        assertThat(Ocr1Normalizer.normalize("lajout")).isEqualTo("l'ajout")
    }

    @Test
    fun `OCR correction for longueur les and gris`() {
        assertThat(Ocr1Normalizer.normalize("1ongueur")).isEqualTo("longueur")
        assertThat(Ocr1Normalizer.normalize("1 es")).isEqualTo("les")
        assertThat(Ocr1Normalizer.normalize("grib")).isEqualTo("gris")
        assertThat(Ocr1Normalizer.normalize("gri6")).isEqualTo("gris")
        assertThat(Ocr1Normalizer.normalize("4 1 oasts")).isEqualTo("4 toasts")
        assertThat(Ocr1Normalizer.normalize("4 1oasts")).isEqualTo("4 toasts")
    }

    @Test
    fun `Spoon unit normalization Soup`() {
        assertThat(Ocr1Normalizer.normalize("cas")).isEqualTo("c. à soupe")
        assertThat(Ocr1Normalizer.normalize("c. à supe")).isEqualTo("c. à soupe")
        assertThat(Ocr1Normalizer.normalize("c. à sope")).isEqualTo("c. à soupe")
        assertThat(Ocr1Normalizer.normalize("c à soupe")).isEqualTo("c. à soupe")
    }

    @Test
    fun `Spoon unit normalization Coffee`() {
        assertThat(Ocr1Normalizer.normalize("cac")).isEqualTo("c. à café")
        assertThat(Ocr1Normalizer.normalize("c. à café")).isEqualTo("c. à café")
        assertThat(Ocr1Normalizer.normalize("c à fé")).isEqualTo("c. à café")
    }

    @Test
    fun `Digit and unit separation`() {
        assertThat(Ocr1Normalizer.normalize("500g")).isEqualTo("500 g")
        assertThat(Ocr1Normalizer.normalize("1l")).isEqualTo("1 l")
        assertThat(Ocr1Normalizer.normalize("200G")).isEqualTo("200 g")
    }

    @Test
    fun `Whitespace collapse and trimming`() {
        assertThat(Ocr1Normalizer.normalize("  100   g   de   sucre  ")).isEqualTo("100 g de sucre")
    }

    @Test
    fun `Proper name preservation case`() {
        val result = Ocr1Normalizer.normalize("Nahit YILMAZ de l'Hôtel CONRAD")
        assertThat(result).contains("Nahit")
        assertThat(result).contains("YILMAZ")
        assertThat(result).contains("Hôtel")
        assertThat(result).contains("CONRAD")
    }

    @Test
    fun `Misread digit 1 should NOT touch protected headers`() {
        val result1 = Ocr1Normalizer.normalize("INGRÉDIENTS")
        assertThat(result1).ignoringCase().contains("ingrédients")
        assertThat(result1.startsWith("1")).isFalse()

        val result2 = Ocr1Normalizer.normalize("INSTRUCTIONS")
        assertThat(result2).ignoringCase().contains("instructions")
        assertThat(result2.startsWith("1")).isFalse()
    }

    @Test
    fun `Historical noise removal`() {
        assertThat(Ocr1Normalizer.normalize("Michel Laroche, 14/12/94: 209 F")).isEqualTo("Michel Laroche")
        assertThat(Ocr1Normalizer.normalize("Prix: 15.50 EUR")).isEqualTo("Prix")
        assertThat(Ocr1Normalizer.normalize("Menu 20 €")).isEqualTo("Menu")
    }

    @Test
    fun `Trailing punctuation and whitespace cleanup`() {
        assertThat(Ocr1Normalizer.normalize("200 g de sucre, : ;")).isEqualTo("200 g de sucre")
    }

    @Test
    fun `Fused spoon OCR typos should be repaired via vocabulary`() {
        val vocabulary = OcrVocabularyLoader.loadFromProjectResources()
        assertThat(Ocr1Normalizer.normalize("lcaàs de sucre", vocabulary.ocrSpellingRepairs))
            .isEqualTo("1 c. à soupe de sucre")
        assertThat(Ocr1Normalizer.normalize("cdsdhuile", vocabulary.ocrSpellingRepairs))
            .isEqualTo("c. à soupe d'huile")
        assertThat(Ocr1Normalizer.normalize("lc. À soupe d'hulle d'olive", vocabulary.ocrSpellingRepairs))
            .isEqualTo("1 c. à soupe d'huile d'olive")
    }
}
