package com.example.chiefinventory.utils

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Suite de tests unitaires de Type B vérifiant le comportement et les performances
 * du registre centralisé d'expressions régulières [OcrRegexes].
 */
class OcrRegexesTest {

    @Test
    fun `Quantities FRACTION should match fractional quantities`() {
        val match = OcrRegexes.Quantities.FRACTION.find("1/2 verre d'eau")

        assertThat(match).isNotNull()
        assertThat(match!!.groupValues[1]).isEqualTo("1")
        assertThat(match.groupValues[2]).isEqualTo("2")
    }

    @Test
    fun `Quantities RANGE should match quantity ranges`() {
        val match = OcrRegexes.Quantities.RANGE.find("10 à 15 minutes")

        assertThat(match).isNotNull()
        assertThat(match!!.groupValues[1]).isEqualTo("10")
        assertThat(match.groupValues[2]).isEqualTo("15")
    }

    @Test
    fun `Quantities STANDARD matcher should extract digit quantity and rest`() {
        val matcher = OcrRegexes.Quantities.STANDARD.matcher("250 g de farine")

        assertThat(matcher.find()).isTrue()
        assertThat(matcher.group(1)).isEqualTo("250")
        assertThat(matcher.group(2)).isEqualTo("g de farine")
    }

    @Test
    fun `Ingredients PARENTHESES should extract supplemental info in brackets`() {
        val match = OcrRegexes.Ingredients.PARENTHESES.find("4 petites tomates (ou 2 grosses)")

        assertThat(match).isNotNull()
        assertThat(match!!.groupValues[1]).isEqualTo("ou 2 grosses")
    }

    @Test
    fun `Ingredients VAGUE_QUANTITY should match vague quantity words`() {
        val match = OcrRegexes.Ingredients.VAGUE_QUANTITY.find("quelques branches de persil")

        assertThat(match).isNotNull()
        assertThat(match!!.groupValues[1]).isEqualTo("quelques")
    }

    @Test
    fun `Ingredients PREPARATION_PREFIX should match prep prefixes like le jus de`() {
        val match = OcrRegexes.Ingredients.PREPARATION_PREFIX.find("le jus de 1/2 citron")

        assertThat(match).isNotNull()
        assertThat(match!!.groupValues[1]).isEqualTo("jus")
    }

    @Test
    fun `Normalizer PROTECTED_KEYWORDS should match ingrédient and instruction headers`() {
        val match = OcrRegexes.Normalizer.PROTECTED_KEYWORDS.find("Ingrédients")

        assertThat(match).isNotNull()
        assertThat(match!!.groupValues[1]).isEqualTo("ngrédients")
    }

    @Test
    fun `Normalizer SPOON_NORMALIZATION should contain spoon correction rules`() {
        assertThat(OcrRegexes.Normalizer.SPOON_NORMALIZATION).isNotEmpty()
        val casRule = OcrRegexes.Normalizer.SPOON_NORMALIZATION.find { it.second == "c. à soupe" }
        assertThat(casRule).isNotNull()
    }

    @Test
    fun `Normalizer PREFIX_T_BEFORE_DIGIT should match t before digits`() {
        val replaced = "t 10 min".replace(OcrRegexes.Normalizer.PREFIX_T_BEFORE_DIGIT, "")
        assertThat(replaced).isEqualTo("10 min")
    }

    @Test
    fun `Normalizer ENVIRON_WORD should match environ word`() {
        val replaced = "environ 250 g".replace(OcrRegexes.Normalizer.ENVIRON_WORD, "").trim()
        assertThat(replaced).isEqualTo("250 g")
    }

    @Test
    fun `Normalizer SYMBOL_BEFORE_DIGIT should match bullet symbols before digits`() {
        val replaced = "! 100 g".replace(OcrRegexes.Normalizer.SYMBOL_BEFORE_DIGIT, "")
        assertThat(replaced).isEqualTo("100 g")
    }

    @Test
    fun `Cleaner TECHNICAL_DIMENSION should match mm and cm dimensions`() {
        assertThat(OcrRegexes.Cleaner.TECHNICAL_DIMENSION.containsMatchIn("5 mm sur 5 mm")).isTrue()
        assertThat(OcrRegexes.Cleaner.TECHNICAL_DIMENSION.containsMatchIn("1 cm x 2 cm")).isTrue()
    }

    @Test
    fun `Metadata regexes should match servings prep time and kcal`() {
        assertThat(OcrRegexes.Metadata.SERVINGS.containsMatchIn("pour 4 personnes")).isTrue()
        assertThat(OcrRegexes.Metadata.TIME_PREFIX.containsMatchIn("préparation : 15 min")).isTrue()
        assertThat(OcrRegexes.Metadata.KCAL.containsMatchIn("250 kcal")).isTrue()
    }

    @Test
    fun `getAdPrefixPattern should return memoized regex pattern for ad exclusions`() {
        val pattern1 = OcrRegexes.getAdPrefixPattern("Assiette de présentation")
        val pattern2 = OcrRegexes.getAdPrefixPattern("Assiette de présentation")

        assertThat(pattern1).isSameInstanceAs(pattern2)
        assertThat(pattern1.containsMatchIn("Assiette de présentation disponible")).isTrue()
    }

    @Test
    fun `getUnitPattern should return memoized regex pattern for culinary units`() {
        val pattern1 = OcrRegexes.getUnitPattern("c. à soupe")
        val pattern2 = OcrRegexes.getUnitPattern("c. à soupe")

        assertThat(pattern1).isSameInstanceAs(pattern2)
        assertThat(pattern1.containsMatchIn("c. à soupe de farine")).isTrue()
    }

    @Test
    fun `Beautifier regexes should match step numbers and bullets`() {
        assertThat(OcrRegexes.Beautifier.STEP_DIGIT_PREFIX.containsMatchIn("1. Couper")).isTrue()
        assertThat(OcrRegexes.Beautifier.STEP_NUMBER_BEFORE_CAPITAL.containsMatchIn("1 Plongez")).isTrue()
        assertThat(OcrRegexes.Beautifier.BULLET_PREFIX.containsMatchIn("• Faire cuire")).isTrue()
    }

    @Test
    fun `getInstructionPrefixPattern should return memoized regex pattern for instruction prefixes`() {
        val keywords = listOf("Coupez", "Plongez", "Mélangez")
        val pattern1 = OcrRegexes.getInstructionPrefixPattern(keywords)
        val pattern2 = OcrRegexes.getInstructionPrefixPattern(keywords)

        assertThat(pattern1).isSameInstanceAs(pattern2)
        assertThat(pattern1.containsMatchIn("1. Coupez les tomates")).isTrue()
    }

    @Test
    fun `getSpellingRepairPattern should return memoized pattern for spelling repairs`() {
        val pattern1 = OcrRegexes.getSpellingRepairPattern("lhuile")
        val pattern2 = OcrRegexes.getSpellingRepairPattern("lhuile")

        assertThat(pattern1).isSameInstanceAs(pattern2)
        assertThat(pattern1.matcher("lhuile").find()).isTrue()
    }

    @Test
    fun `getStemWordPattern should return memoized regex pattern for ingredient stem`() {
        val pattern1 = OcrRegexes.getStemWordPattern("mangue")
        val pattern2 = OcrRegexes.getStemWordPattern("mangue")

        assertThat(pattern1).isSameInstanceAs(pattern2)
        assertThat(pattern1.containsMatchIn("Salade de mangues")).isTrue()
    }

    @Test
    fun `getWholeWordPattern should return memoized regex pattern for whole words`() {
        val pattern1 = OcrRegexes.getWholeWordPattern("Bordeaux")
        val pattern2 = OcrRegexes.getWholeWordPattern("Bordeaux")

        assertThat(pattern1).isSameInstanceAs(pattern2)
        assertThat(pattern1.containsMatchIn("Un vin de Bordeaux")).isTrue()
    }

    @Test
    fun `DIFFICULTY regex should match recipe difficulty but ignore feu moyen and chaleur moyenne`() {
        assertThat(OcrRegexes.Metadata.DIFFICULTY.containsMatchIn("Difficulté : Moyen")).isTrue()
        assertThat(OcrRegexes.Metadata.DIFFICULTY.containsMatchIn("Facile")).isTrue()

        assertThat(OcrRegexes.Metadata.DIFFICULTY.containsMatchIn("à feu moyen")).isFalse()
        assertThat(OcrRegexes.Metadata.DIFFICULTY.containsMatchIn("a feu moyen")).isFalse()
    }
}
