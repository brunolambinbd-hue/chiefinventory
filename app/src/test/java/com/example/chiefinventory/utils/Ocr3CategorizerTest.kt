package com.example.chiefinventory.utils

import com.google.common.truth.Truth.assertThat
import com.google.common.truth.Truth.assertWithMessage
import org.junit.Before
import org.junit.Test

class Ocr3CategorizerTest {

    private lateinit var vocabulary: OcrVocabulary

    @Before
    fun setup() {
        // Charge le VRAI vocabulaire de production depuis les fichiers XML (app/src/main/res/values/*.xml)
        vocabulary = OcrVocabularyLoader.loadFromProjectResources()
    }

    @Test
    fun `Empty input list handling`() {
        val result = Ocr3Categorizer.categorize(emptyList(), vocabulary)
        assertThat(result.rawIngredientsList).isEmpty()
        assertThat(result.rawInstructionsList).isEmpty()
    }

    @Test
    fun `Blank and whitespace only lines`() {
        val input = listOf("   ", "\t", "\n")
        val result = Ocr3Categorizer.categorize(input, vocabulary)
        assertThat(result.rawIngredientsList).isEmpty()
        assertThat(result.rawInstructionsList).isEmpty()
    }

    @Test
    fun `Technical dimensions protection`() {
        val input = listOf("5 mm sur 5 mm")
        val result = Ocr3Categorizer.categorize(input, vocabulary)
        assertThat(result.rawInstructionsList).contains("5 mm sur 5 mm")
        assertThat(result.rawIngredientsList).isEmpty()
    }

    @Test
    fun `Servings extraction primary regex`() {
        val input = listOf("Pour 4 personnes")
        val result = Ocr3Categorizer.categorize(input, vocabulary)
        assertThat(result.detectedServings).isEqualTo("4")
    }

    @Test
    fun `Servings extraction alternate regex`() {
        val input = listOf("6 portions")
        val result = Ocr3Categorizer.categorize(input, vocabulary)
        assertThat(result.detectedServings).isEqualTo("6")
    }

    @Test
    fun `Servings extraction non duplicate locking`() {
        val input = listOf("Pour 4 personnes", "Serves 6")
        val result = Ocr3Categorizer.categorize(input, vocabulary)
        assertThat(result.detectedServings).isEqualTo("4")
    }

    @Test
    fun `Time extraction preparation time`() {
        val input = listOf("Prép: 15 min")
        val result = Ocr3Categorizer.categorize(input, vocabulary)
        assertThat(result.detectedPrepTime).isEqualTo("15")
    }

    @Test
    fun `Time extraction cooking time`() {
        val input = listOf("Cuisson : 1h 20")
        val result = Ocr3Categorizer.categorize(input, vocabulary)
        assertThat(result.detectedCookTime).isEqualTo("80")
    }

    @Test
    fun `Time extraction resting time`() {
        val input = listOf("Repos: 2h")
        val result = Ocr3Categorizer.categorize(input, vocabulary)
        assertThat(result.detectedRestingTime).isEqualTo("120")
    }

    @Test
    fun `Time extraction multiple times on one line`() {
        val input = listOf("Prép: 10min, Cuisson: 20min")
        val result = Ocr3Categorizer.categorize(input, vocabulary)
        assertThat(result.detectedPrepTime).isEqualTo("10")
        assertThat(result.detectedCookTime).isEqualTo("20")
    }

    @Test
    fun `Excluded keywords removal`() {
        val input = listOf("200 g SAVEUR beurre")
        val result = Ocr3Categorizer.categorize(input, vocabulary)
        assertThat(result.rawIngredientsList).containsExactly("200 g beurre")
    }

    @Test
    fun `Empty line and whitespace normalization`() {
        val input = listOf("   ", "200 g  sucre")
        val result = Ocr3Categorizer.categorize(input, vocabulary)
        assertThat(result.rawIngredientsList).containsExactly("200 g sucre")
    }

    @Test
    fun `Action verbs detection with extra verbs`() {
        val input = listOf("Creusez le concombre")
        val result = Ocr3Categorizer.categorize(input, vocabulary)
        assertThat(result.rawInstructionsList).isNotEmpty()
    }

    @Test
    fun `Ingredient weight detection via parenthesis`() {
        val input = listOf("Truite (50g)")
        val result = Ocr3Categorizer.categorize(input, vocabulary)
        assertThat(result.rawIngredientsList).contains("Truite (50g)")
    }

    @Test
    fun `Quantity regex indefinite quantifiers`() {
        val input = listOf("Quelques feuilles de menthe")
        val result = Ocr3Categorizer.categorize(input, vocabulary)
        assertThat(result.rawIngredientsList).contains("Quelques feuilles de menthe")
    }

    @Test
    fun `Quantity regex OCR artifacts`() {
        val input = listOf("| 2 citrons", "! 5 g de sel")
        val result = Ocr3Categorizer.categorize(input, vocabulary)
        assertThat(result.rawIngredientsList).hasSize(2)
    }

    @Test
    fun `Preparation modifiers as ingredient signals`() {
        val input = listOf("Haché de boeuf")
        val result = Ocr3Categorizer.categorize(input, vocabulary)
        assertThat(result.rawIngredientsList).contains("Haché de boeuf")
    }

    @Test
    fun `Wine line identification and cleaning`() {
        val input = listOf("Accord : Merlot 2015")
        val result = Ocr3Categorizer.categorize(input, vocabulary)
        assertThat(result.detectedWineList).isNotEmpty()
    }

    @Test
    fun `Source Proper name identification`() {
        val input = listOf("Chef Nahit YILMAZ")
        val result = Ocr3Categorizer.categorize(input, vocabulary)
        assertThat(result.detectedSourceList).isNotEmpty()
    }

    @Test
    fun `Instruction header section switch`() {
        val input = listOf("PRÉPARATION", "Faites cuire")
        val result = Ocr3Categorizer.categorize(input, vocabulary)
        assertThat(result.rawInstructionsList).contains("Faites cuire")
    }

    @Test
    fun `Ingredient header section switch`() {
        val input = listOf("Ingrédients :", "Sel")
        val result = Ocr3Categorizer.categorize(input, vocabulary)
        assertThat(result.rawIngredientsList).contains("Sel")
    }

    @Test
    fun `Header length threshold check`() {
        val input = listOf("Cette phrase est beaucoup trop longue pour être un header Ingrédients")
        val result = Ocr3Categorizer.categorize(input, vocabulary)
        assertThat(result.rawInstructionsList).isNotEmpty()
    }

    @Test
    fun `Noise cleaning No letters`() {
        val input = listOf("--- 123 ---", "200 g")
        val result = Ocr3Categorizer.categorize(input, vocabulary)
        assertThat(result.rawIngredientsList).hasSize(1)
    }

    @Test
    fun `Final classification Action verb logic`() {
        val input = listOf("Mélangez bien tous les ingrédients ensemble")
        val result = Ocr3Categorizer.categorize(input, vocabulary)
        assertThat(result.rawInstructionsList).contains("Mélangez bien tous les ingrédients ensemble")
    }

    @Test
    fun `Early ingredient detection No section defined`() {
        val input = listOf("200 g de sucre")
        val result = Ocr3Categorizer.categorize(input, vocabulary)
        assertThat(result.rawIngredientsList).contains("200 g de sucre")
    }

    @Test
    fun `Strict ingredient check inside instruction section`() {
        val input = listOf("PRÉPARATION", "200 g de beurre", "Faites fondre")
        val result = Ocr3Categorizer.categorize(input, vocabulary)
        assertThat(result.rawIngredientsList).contains("200 g de beurre")
        assertThat(result.rawInstructionsList).contains("Faites fondre")
    }

    @Test
    fun `Bullet point handling`() {
        val input = listOf("• Ajoutez le sel")
        val result = Ocr3Categorizer.categorize(input, vocabulary)
        assertWithMessage("Doit être une instruction nettoyée de sa puce")
            .that(result.rawInstructionsList).contains("Ajoutez le sel")
    }

    @Test
    fun `Ingredient connector detection`() {
        val input = listOf("De la ciboulette")
        val result = Ocr3Categorizer.categorize(input, vocabulary)
        assertThat(result.rawIngredientsList).contains("De la ciboulette")
    }

    @Test
    fun `ExtractMinutes Hour and minutes format`() {
        val input = listOf("Repos: 1 h 05")
        val result = Ocr3Categorizer.categorize(input, vocabulary)
        assertThat(result.detectedRestingTime).isEqualTo("65")
    }

    @Test
    fun `ExtractMinutes Only minutes format`() {
        val input = listOf("Cuisson: 45 mn")
        val result = Ocr3Categorizer.categorize(input, vocabulary)
        assertThat(result.detectedCookTime).isEqualTo("45")
    }

    @Test
    fun `Vague quantity detection at start`() {
        val input = listOf("quelques feuilles de menthe")
        val result = Ocr3Categorizer.categorize(input, vocabulary)
        assertWithMessage("Devrait être dans ingrédients")
            .that(result.rawIngredientsList).contains("quelques feuilles de menthe")
    }

    @Test
    fun `Source security - action verb forbids source classification`() {
        val input = listOf("cée de sel et placez-y le fromage")
        val result = Ocr3Categorizer.categorize(input, vocabulary)

        assertWithMessage("Ne doit pas être une source").that(result.detectedSourceList).isEmpty()
        assertWithMessage("Doit être une instruction").that(result.rawInstructionsList).contains("cée de sel et placez-y le fromage")
    }

    @Test
    fun `Wine security - culinary ingredient protects from wine theft`() {
        val input = listOf("5 cl d'huile")
        val result = Ocr3Categorizer.categorize(input, vocabulary)

        assertWithMessage("L'huile doit rester dans ingrédients").that(result.rawIngredientsList).contains("5 cl d'huile")
        assertWithMessage("Ne doit pas être dans le vin").that(result.detectedWineList).isEmpty()
    }

    @Test
    fun `Multi line ingredient inside active ingredient section should not steal wine`() {
        val input = listOf(
            "INGRÉDIENTS",
            "Pour la vinaigrette :",
            "4 c. à soupe de vinaigre de",
            "vin blanc"
        )
        val result = Ocr3Categorizer.categorize(input, vocabulary)

        assertWithMessage("Le vin ne doit pas capturer 'vin blanc' dans la section Ingrédients active")
            .that(result.detectedWineList).isEmpty()
        assertThat(result.rawIngredientsList).contains("4 c. à soupe de vinaigre de vin blanc")
    }

    @Test
    fun `Source preservation of uppercase names`() {
        val input = listOf("Chef Nahit YILMAZ")
        val result = Ocr3Categorizer.categorize(input, vocabulary)

        assertThat(result.detectedSourceList.any { it.contains("YILMAZ") }).isTrue()
    }

    @Test
    fun `Instruction recovery lever 2 - mid-block ingredient`() {
        val input = listOf("Réalisation", "Faites fondre le chocolat", "200 g de sucre", "Mélangez")
        val result = Ocr3Categorizer.categorize(input, vocabulary)

        assertWithMessage("Le sucre doit être récupéré").that(result.rawIngredientsList).contains("200 g de sucre")
        assertThat(result.rawInstructionsList).containsExactly("Réalisation", "Faites fondre le chocolat", "Mélangez").inOrder()
    }

    @Test
    fun `Multiple headers support`() {
        val input1 = listOf("Préparation", "Coupez tout")
        val result1 = Ocr3Categorizer.categorize(input1, vocabulary)
        assertThat(result1.rawInstructionsList).contains("Coupez tout")

        val input2 = listOf("Cuisson", "Faites dorer")
        val result2 = Ocr3Categorizer.categorize(input2, vocabulary)
        assertThat(result2.rawInstructionsList).contains("Faites dorer")
    }

    @Test
    fun `Ingredient with mid-line quantity detection`() {
        val input = listOf("Truite (50 g)")
        val result = Ocr3Categorizer.categorize(input, vocabulary)
        assertWithMessage("La truite avec poids devrait être un ingrédient")
            .that(result.rawIngredientsList).contains("Truite (50 g)")
    }

    @Test
    fun `Short action verb should not trigger instructions without signals`() {
        val input = listOf("1 citron pressé")
        val result = Ocr3Categorizer.categorize(input, vocabulary)
        assertWithMessage("Doit rester un ingrédient")
            .that(result.rawIngredientsList).contains("1 citron pressé")
    }

    @Test
    fun `Bullet point forces instruction even if short`() {
        val input = listOf("• Mixez")
        val result = Ocr3Categorizer.categorize(input, vocabulary)
        assertWithMessage("Doit être une instruction grâce à la puce")
            .that(result.rawInstructionsList).contains("Mixez")
    }

    @Test
    fun `Problem Case 1 - Mixed ingredient and instruction line`() {
        val input = listOf("truite (50 g) frais hachés salade")
        val result = Ocr3Categorizer.categorize(input, vocabulary)

        assertWithMessage("Le bloc mixte contenant un poids doit être classé en ingrédient")
            .that(result.rawIngredientsList).contains("truite (50 g) frais hachés salade")
    }

    @Test
    fun `Problem Case 2 - Isolated food name recovery after connector`() {
        val input = listOf("Ingrédients", "quelques feuilles de", "salade")
        val result = Ocr3Categorizer.categorize(input, vocabulary)

        assertWithMessage("La ligne 'salade' doit être dans les ingrédients")
            .that(result.rawIngredientsList).contains("quelques feuilles de salade")
    }

    @Test
    fun `Time extraction combined preparation and cooking`() {
        val input = listOf("15 min + 30 min de cuisson")
        val result = Ocr3Categorizer.categorize(input, vocabulary)
        assertThat(result.detectedPrepTime).isEqualTo("15")
        assertThat(result.detectedCookTime).isEqualTo("30")
    }

    @Test
    fun `Time extraction suffix format`() {
        val input = listOf("30 min de cuisson")
        val result = Ocr3Categorizer.categorize(input, vocabulary)
        assertThat(result.detectedCookTime).isEqualTo("30")
    }

    @Test
    fun `Disqualification via instruction trigger`() {
        val input = listOf("Cuire pendant 10 min")
        val result = Ocr3Categorizer.categorize(input, vocabulary)
        assertWithMessage("Doit être une instruction à cause du déclencheur 'pendant'")
            .that(result.rawInstructionsList).contains("Cuire pendant 10 min")
        assertWithMessage("Ne doit pas être un ingrédient")
            .that(result.rawIngredientsList).isEmpty()
    }
}
