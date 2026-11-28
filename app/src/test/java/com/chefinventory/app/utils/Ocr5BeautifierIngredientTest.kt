package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertThat
import com.google.common.truth.Truth.assertWithMessage
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.Test

/**
 * Suite de tests unitaires de Type B vérifiant le comportement et les règles de beautification d'ingrédients de [Ocr5BeautifierIngredient].
 */
class Ocr5BeautifierIngredientTest {

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testBeautifyIngredients_CapitalizesFirstLetterAndHandlesUppercaseDigitsAndEmpty() {
        val input = listOf("sel", "poivre du moulin", "2 carottes", "Farine", "", "   ")
        val expected = listOf("Sel", "Poivre du moulin", "2 carottes", "Farine")

        val result = Ocr5BeautifierIngredient.beautifyIngredients(input)
        assertWithMessage("Les ingrédients doivent être majusculés en premier caractère et les lignes vides purgées")
            .that(result)
            .containsExactlyElementsIn(expected)
            .inOrder()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testBeautifyIngredients_FiltersOutResidualHeadersAndTrashWords() {
        val input = listOf("INGRÉDIENTS:", "ingredients:", "composition:", "ngrédients:", "les", "du", "200 g de farine")
        val expected = listOf("200 g de farine")

        val result = Ocr5BeautifierIngredient.beautifyIngredients(input)
        assertWithMessage("Les en-têtes d'ingrédients résiduels et articles isolés doivent être éliminés")
            .that(result)
            .containsExactlyElementsIn(expected)
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testBeautifyIngredients_MergesOrphanVarietyQualifiers() {
        val vocabulary = OcrVocabularyLoader.loadFromProjectResources()
        val input = listOf("200 g de haricots", "princesses")

        val result = Ocr5BeautifierIngredient.beautifyIngredients(
            input,
            vocabulary.baseIngredientsExpectingVariety,
            vocabulary.ingredientVarietyQualifiers
        )

        assertWithMessage("Le qualificatif de variété orphelin 'princesses' doit être regroupé avec '200 g de haricots'")
            .that(result)
            .containsExactly("200 g de haricots princesses")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testApplyFrenchPluralIfQuantityGreaterThanOne_AllBranches() {
        val vocabulary = OcrVocabularyLoader.loadFromProjectResources()

        // 1. Quantity null or <= 1 -> unchanged
        assertThat(Ocr5BeautifierIngredient.applyFrenchPluralIfQuantityGreaterThanOne("sel", null, vocabulary)).isEqualTo("sel")
        assertThat(Ocr5BeautifierIngredient.applyFrenchPluralIfQuantityGreaterThanOne("1 gousse d'ail", 1.0, vocabulary)).isEqualTo("1 gousse d'ail")

        // 2. Unit present in container units -> pluralized unit
        assertThat(Ocr5BeautifierIngredient.applyFrenchPluralIfQuantityGreaterThanOne("2 gousse d'ail", 2.0, vocabulary)).isEqualTo("2 gousses d'ail")

        // 3. Unit present but not container unit (e.g. g, cl) -> unchanged
        assertThat(Ocr5BeautifierIngredient.applyFrenchPluralIfQuantityGreaterThanOne("200 g de farine", 200.0, vocabulary)).isEqualTo("200 g de farine")

        // 4. No unit, target word ends with s or x -> unchanged
        assertThat(Ocr5BeautifierIngredient.applyFrenchPluralIfQuantityGreaterThanOne("2 radis", 2.0, vocabulary)).isEqualTo("2 radis")

        // 5. No unit, target word ends with eau/eu -> pluralized with x
        assertThat(Ocr5BeautifierIngredient.applyFrenchPluralIfQuantityGreaterThanOne("2 nouveau poireau", 2.0, vocabulary)).isEqualTo("2 nouveau poireaux")

        // 6. No unit, target word regular -> pluralized with s
        assertThat(Ocr5BeautifierIngredient.applyFrenchPluralIfQuantityGreaterThanOne("2 petite tomate", 2.0, vocabulary)).isEqualTo("2 petite tomates")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testBeautifyIngredients_MusselContextAndTypoElisionAndGeneratedMethods() {
        assertWithMessage("Ocr5BeautifierIngredient doit générer une représentation textuelle valide")
            .that(Ocr5BeautifierIngredient.toString())
            .contains("Ocr5BeautifierIngredient")

        // Mussel context: "boules" -> "moules"
        val inputMussels = listOf("1 douzaine de boules de Zeeland")
        val resultMussels = Ocr5BeautifierIngredient.beautifyIngredients(inputMussels)
        assertThat(resultMussels).containsExactly("1 douzaine de moules de Zeeland")

        // Dessert context: "boules" preserved
        val inputDessert = listOf("1 boule de glace vanille sur du melon")
        val resultDessert = Ocr5BeautifierIngredient.beautifyIngredients(inputDessert)
        assertThat(resultDessert).containsExactly("1 boule de glace vanille sur du melon")

        // Typo correction with elision prefix: "d'hoile" -> "d'huile", "d’hoile" -> "d’huile"
        val inputElision = listOf("2 c. à soupe d'hoile d'olive", "1 c. à soupe d’hoile d'olive")
        val resultElision = Ocr5BeautifierIngredient.beautifyIngredients(inputElision)
        assertThat(resultElision).containsExactly("2 c. à soupe d'huile d'olive", "1 c. à soupe d’huile d'olive")

        // Mozzarella / Glace / Pain / Pate context: preserves 'boule'
        val inputMozza = listOf("1 boule de mozzarella", "1 boule de glace", "1 boule de pain", "1 boule de pate")
        val resultMozza = Ocr5BeautifierIngredient.beautifyIngredients(inputMozza)
        assertThat(resultMozza).containsExactly("1 boule de mozzarella", "1 boule de glace", "1 boule de pain", "1 boule de pate")

        // Mussel context: "bouchot", "mariniere", "marinière", "zelande", "zélande", "parc", "boule" (singular)
        val inputMussel2 = listOf("1 kg de boule de bouchot", "1 kg de boules mariniere", "1 kg de boules de zelande")
        val resultMussel2 = Ocr5BeautifierIngredient.beautifyIngredients(inputMussel2)
        assertThat(resultMussel2).containsExactly("1 kg de moules de bouchot", "1 kg de moules mariniere", "1 kg de moules de zelande")

        // Plural rules for -eau and -eu
        val vocabulary = OcrVocabularyLoader.loadFromProjectResources()
        assertThat(Ocr5BeautifierIngredient.applyFrenchPluralIfQuantityGreaterThanOne("2 couteau", 2.0, vocabulary)).isEqualTo("2 couteaux")
        assertThat(Ocr5BeautifierIngredient.applyFrenchPluralIfQuantityGreaterThanOne("2 pneu", 2.0, vocabulary)).isEqualTo("2 pneux")
        assertThat(Ocr5BeautifierIngredient.applyFrenchPluralIfQuantityGreaterThanOne("   ", 2.0, vocabulary)).isEqualTo("   ")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testBeautifyIngredients_BranchCoverageExhaustive() {
        // Non-elision prefix with apostrophe "s'il" and hyphen "d-hoile"
        val inputNonElision = listOf("2 c. à soupe s'il vous plaît", "2 c. à soupe d-hoile", "d'", "farine")
        val resNonElision = Ocr5BeautifierIngredient.beautifyIngredients(inputNonElision)
        assertThat(resNonElision).containsAtLeast("2 c. à soupe s'il vous plaît", "2 c. à soupe d-hoile", "Farine")

        // Mussel context with "douzaine"
        val inputDouzaine = listOf("1 douzaine de boules")
        val resDouzaine = Ocr5BeautifierIngredient.beautifyIngredients(inputDouzaine)
        assertThat(resDouzaine).containsExactly("1 douzaine de moules")
    }
}
