package com.example.chiefinventory.utils

import android.content.res.Resources
import com.example.chiefinventory.R
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations

class Ocr3CategorizerTest {

    @Mock
    private lateinit var res: Resources

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        val emptyArr = arrayOf<String>()

        // Mocks de base pour les ressources utilisées par le catégoriseur
        `when`(res.getStringArray(R.array.step_action_keywords)).thenReturn(arrayOf("faites", "mélangez", "coupez", "ajoutez", "creusez", "haché", "hachés", "pressé", "mixez"))
        `when`(res.getStringArray(R.array.common_ingredients_no_qty)).thenReturn(arrayOf("sel", "poivre", "beurre", "salade"))
        `when`(res.getStringArray(R.array.excluded_ocr_keywords)).thenReturn(arrayOf("SAVEUR", "POUR", "PAGE"))
        `when`(res.getStringArray(R.array.ingredient_preparation_modifiers)).thenReturn(arrayOf("haché", "hachés", "frais hachés", "émincé", "salade"))

        // 2. Mocks pour SourceParser et WineParser
        `when`(res.getStringArray(R.array.source_keywords)).thenReturn(arrayOf("hôtel", "rue"))
        `when`(res.getStringArray(R.array.phone_prefixes)).thenReturn(arrayOf("tél"))
        `when`(res.getStringArray(R.array.wine_keywords)).thenReturn(arrayOf("merlot", "chardonnay"))
        `when`(res.getStringArray(R.array.wine_appellations)).thenReturn(emptyArr)
        `when`(res.getStringArray(R.array.wine_producers)).thenReturn(emptyArr)
        `when`(res.getStringArray(R.array.wine_title_keywords)).thenReturn(arrayOf("vin conseillé"))
        `when`(res.getString(R.string.wine_remove_pattern)).thenReturn("DUMMY")
    }

    @Test
    fun `Empty input list handling`() {
        // Verify that passing an empty list of strings returns an empty RawSections object without errors.
        val result = Ocr3Categorizer.categorize(emptyList(), res)
        assertTrue(result.rawIngredientsList.isEmpty())
        assertTrue(result.rawInstructionsList.isEmpty())
    }

    @Test
    fun `Blank and whitespace only lines`() {
        // Ensure that lines containing only whitespace are ignored and do not affect the currentSection state.
        val input = listOf("   ", "\t", "\n")
        val result = Ocr3Categorizer.categorize(input, res)
        assertTrue(result.rawIngredientsList.isEmpty())
        assertTrue(result.rawInstructionsList.isEmpty())
    }

    @Test
    fun `Technical dimensions protection`() {
        // Verify that lines matching Ocr2Cleaner.isTechnicalDimension are always added to rawInstructionsList and
        // bypass further categorization logic.
        val input = listOf("5 mm sur 5 mm")
        val result = Ocr3Categorizer.categorize(input, res)
        assertTrue(result.rawInstructionsList.contains("5 mm sur 5 mm"))
        assertTrue(result.rawIngredientsList.isEmpty())
    }

    @Test
    fun `Servings extraction primary regex`() {
        // Test 'Pour 4 personnes' or 'Serves: 6' patterns to ensure the digit is extracted into detectedServings
        // and the text is removed from the working line.
        val input = listOf("Pour 4 personnes")
        val result = Ocr3Categorizer.categorize(input, res)
        assertEquals("4", result.detectedServings)
    }

    @Test
    fun `Servings extraction alternate regex`() {
        // Test '4 pers.' or '2 servings' patterns where the digit precedes the keyword.
        val input = listOf("6 portions")
        val result = Ocr3Categorizer.categorize(input, res)
        assertEquals("6", result.detectedServings)
    }

    @Test
    fun `Servings extraction non duplicate locking`() {
        // Ensure only the first detected servings value is stored if multiple lines contain serving information.
        val input = listOf("Pour 4 personnes", "Serves 6")
        val result = Ocr3Categorizer.categorize(input, res)
        assertEquals("4", result.detectedServings)
    }

    @Test
    fun `Time extraction preparation time`() {
        // Verify that 'Prép: 15 min' correctly calculates 15 minutes and assigns it to detectedPrepTime.
        val input = listOf("Prép: 15 min")
        val result = Ocr3Categorizer.categorize(input, res)
        assertEquals("15", result.detectedPrepTime)
    }

    @Test
    fun `Time extraction cooking time`() {
        // Test 'Cuisson : 1h 20' to ensure extractMinutes correctly converts the value to '80'.
        val input = listOf("Cuisson : 1h 20")
        val result = Ocr3Categorizer.categorize(input, res)
        assertEquals("80", result.detectedCookTime)
    }

    @Test
    fun `Time extraction resting time`() {
        // Test 'Repos: 2h' to ensure detectedRestingTime is correctly set to '120'.
        val input = listOf("Repos: 2h")
        val result = Ocr3Categorizer.categorize(input, res)
        assertEquals("120", result.detectedRestingTime)
    }

    @Test
    fun `Time extraction multiple times on one line`() {
        // Ensure the while loop correctly processes and removes multiple time patterns (e.g., Prep and Cook) from a single line.
        val input = listOf("Prép: 10min, Cuisson: 20min")
        val result = Ocr3Categorizer.categorize(input, res)
        assertEquals("10", result.detectedPrepTime)
        assertEquals("20", result.detectedCookTime)
    }

    @Test
    fun `Excluded keywords removal`() {
        // Verify that words defined in R.array.excluded_ocr_keywords are stripped from the line without
        // leaving orphaned characters or affecting word boundaries.
        val input = listOf("200 g SAVEUR beurre")
        val result = Ocr3Categorizer.categorize(input, res)
        assertEquals("200 g beurre", result.rawIngredientsList[0])
    }

    @Test
    fun `Empty line and whitespace normalization`() {
        // Ensure extra spaces after cleaning are normalized to a single space.
        val input = listOf("   ", "200 g  sucre")
        val result = Ocr3Categorizer.categorize(input, res)
        assertEquals(1, result.rawIngredientsList.size)
        assertEquals("200 g sucre", result.rawIngredientsList[0])
    }

    @Test
    fun `Action verbs detection with extra verbs`() {
        // Verify that 'Creusez' triggers section change to instructions.
        val input = listOf("Creusez le concombre") // Creusez est dans extraVerbs
        val result = Ocr3Categorizer.categorize(input, res)
        assertTrue(result.rawInstructionsList.isNotEmpty())
    }

    @Test
    fun `Ingredient weight detection via parenthesis`() {
        // Check if weight patterns like '(50g)' help identify ingredients.
        val input = listOf("Truite (50g)")
        val result = Ocr3Categorizer.categorize(input, res)
        assertTrue(result.rawIngredientsList.contains("Truite (50g)"))
    }

    @Test
    fun `Quantity regex indefinite quantifiers`() {
        // Test 'Quelques' as a quantity trigger.
        val input = listOf("Quelques feuilles de menthe")
        val result = Ocr3Categorizer.categorize(input, res)
        assertTrue(result.rawIngredientsList.contains("Quelques feuilles de menthe"))
    }

    @Test
    fun `Quantity regex OCR artifacts`() {
        // Test pipe and exclamation mark as digit 1 indicators.
        val input = listOf("| 2 citrons", "! 5 g de sel")
        val result = Ocr3Categorizer.categorize(input, res)
        assertEquals(2, result.rawIngredientsList.size)
    }

    @Test
    fun `Preparation modifiers as ingredient signals`() {
        // Check if 'Haché' at start triggers ingredient classification.
        val input = listOf("Haché de boeuf")
        val result = Ocr3Categorizer.categorize(input, res)
        assertTrue(result.rawIngredientsList.contains("Haché de boeuf"))
    }

    @Test
    fun `Wine line identification and cleaning`() {
        val input = listOf("Accord : Merlot 2015")
        val result = Ocr3Categorizer.categorize(input, res)
        assertTrue(result.detectedWineList.isNotEmpty())
    }

    @Test
    fun `Source Proper name identification`() {
        val input = listOf("Chef Nahit YILMAZ")
        val result = Ocr3Categorizer.categorize(input, res)
        assertTrue(result.detectedSourceList.isNotEmpty())
    }

    @Test
    fun `Instruction header section switch`() {
        val input = listOf("PRÉPARATION", "Faites cuire")
        val result = Ocr3Categorizer.categorize(input, res)
        assertTrue(result.rawInstructionsList.contains("Faites cuire"))
    }

    @Test
    fun `Ingredient header section switch`() {
        val input = listOf("Ingrédients :", "Sel")
        val result = Ocr3Categorizer.categorize(input, res)
        assertTrue(result.rawIngredientsList.contains("Sel"))
    }

    @Test
    fun `Header length threshold check`() {
        val input = listOf("Cette phrase est beaucoup trop longue pour être un header Ingrédients")
        val result = Ocr3Categorizer.categorize(input, res)
        assertTrue(result.rawInstructionsList.isNotEmpty())
    }

    @Test
    fun `Noise cleaning No letters`() {
        val input = listOf("--- 123 ---", "200 g")
        val result = Ocr3Categorizer.categorize(input, res)
        // La ligne 123 est ignorée car pas de lettres
        assertEquals(1, result.rawIngredientsList.size)
    }

    @Test
    fun `Final classification Action verb logic`() {
        val input = listOf("Mélangez bien tous les ingrédients ensemble")
        val result = Ocr3Categorizer.categorize(input, res)
        assertTrue(result.rawInstructionsList.contains("Mélangez bien tous les ingrédients ensemble"))
    }

    @Test
    fun `Early ingredient detection No section defined`() {
        val input = listOf("200 g de sucre")
        val result = Ocr3Categorizer.categorize(input, res)
        assertTrue(result.rawIngredientsList.contains("200 g de sucre"))
    }

    @Test
    fun `Strict ingredient check inside instruction section`() {
        val input = listOf("PRÉPARATION", "200 g de beurre", "Faites fondre")
        val result = Ocr3Categorizer.categorize(input, res)
        assertTrue(result.rawIngredientsList.contains("200 g de beurre"))
        assertTrue(result.rawInstructionsList.contains("Faites fondre"))
    }

    @Test
    fun `Bullet point handling`() {
        val input = listOf("• Ajoutez le sel")
        val result = Ocr3Categorizer.categorize(input, res)
        assertTrue(result.rawInstructionsList.contains("• Ajoutez le sel"))
    }

    @Test
    fun `Ingredient connector detection`() {
        val input = listOf("De la ciboulette")
        val result = Ocr3Categorizer.categorize(input, res)
        assertTrue(result.rawIngredientsList.contains("De la ciboulette"))
    }

    @Test
    fun `ExtractMinutes Hour and minutes format`() {
        val input = listOf("Repos: 1 h 05")
        val result = Ocr3Categorizer.categorize(input, res)
        assertEquals("65", result.detectedRestingTime)
    }

    @Test
    fun `ExtractMinutes Only minutes format`() {
        val input = listOf("Cuisson: 45 mn")
        val result = Ocr3Categorizer.categorize(input, res)
        assertEquals("45", result.detectedCookTime)
    }

    @Test
    fun `Vague quantity detection at start`() {
        // Verify that quantifiers like 'quelques' or 'un peu' trigger SECTION_INGREDIENTS
        val input = listOf("quelques feuilles de menthe")
        val result = Ocr3Categorizer.categorize(input, res)
        assertTrue("Devrait être dans ingrédients", result.rawIngredientsList.contains("quelques feuilles de menthe"))
    }

    @Test
    fun `Source security - action verb forbids source classification`() {
        // Fragment often misread by ML Kit. The verb 'placez' must force it to instructions.
        val input = listOf("cée de sel et placez-y le fromage")
        val result = Ocr3Categorizer.categorize(input, res)

        assertTrue("Ne doit pas être une source", result.detectedSourceList.isEmpty())
        assertTrue("Doit être une instruction", result.rawInstructionsList.contains("cée de sel et placez-y le fromage"))
    }

    @Test
    fun `Wine security - culinary ingredient protects from wine theft`() {
        // 'cl' usually triggers wine, but 'huile' is a culinary exclusion
        val input = listOf("5 cl d'huile")
        val result = Ocr3Categorizer.categorize(input, res)

        assertTrue("L'huile doit rester dans ingrédients", result.rawIngredientsList.contains("5 cl d'huile"))
        assertTrue("Ne doit pas être dans le vin", result.detectedWineList.isEmpty())
    }

    @Test
    fun `Source preservation of uppercase names`() {
        // Since we removed global lowercase(), names like YILMAZ should stay uppercase
        val input = listOf("Chef Nahit YILMAZ")
        val result = Ocr3Categorizer.categorize(input, res)

        assertTrue(result.detectedSourceList.any { it.contains("YILMAZ") })
    }

    @Test
    fun `Instruction recovery lever 2 - mid-block ingredient`() {
        // Verify that an ingredient lost in instructions is pulled back
        val input = listOf("Réalisation", "Faites fondre le chocolat", "200 g de sucre", "Mélangez")
        val result = Ocr3Categorizer.categorize(input, res)

        assertTrue("Le sucre doit être récupéré", result.rawIngredientsList.contains("200 g de sucre"))
        assertEquals("Faites fondre le chocolat", result.rawInstructionsList[0])
        assertEquals("Mélangez", result.rawInstructionsList[1])
    }

    @Test
    fun `Multiple headers support`() {
        // Test that both Preparation and Cuisson act as instruction headers
        val input1 = listOf("Préparation", "Coupez tout")
        val result1 = Ocr3Categorizer.categorize(input1, res)
        assertTrue(result1.rawInstructionsList.contains("Coupez tout"))

        val input2 = listOf("Cuisson", "Faites dorer")
        val result2 = Ocr3Categorizer.categorize(input2, res)
        assertTrue(result2.rawInstructionsList.contains("Faites dorer"))
    }

    @Test
    fun `Ingredient with mid-line quantity detection`() {
        // Line like 'Truite (50 g)' should be recognized as ingredient because of weight pattern
        val input = listOf("Truite (50 g)")
        val result = Ocr3Categorizer.categorize(input, res)
        assertTrue("La truite avec poids devrait être un ingrédient", result.rawIngredientsList.contains("Truite (50 g)"))
    }

    @Test
    fun `Short action verb should not trigger instructions without signals`() {
        // '1 citron pressé' contains 'pressé' (action verb) but is short and no bullet
        val input = listOf("1 citron pressé")
        val result = Ocr3Categorizer.categorize(input, res)
        assertTrue("Doit rester un ingrédient", result.rawIngredientsList.contains("1 citron pressé"))
    }

    @Test
    fun `Bullet point forces instruction even if short`() {
        val input = listOf("• Mixez")
        val result = Ocr3Categorizer.categorize(input, res)
        assertTrue("Doit être une instruction grâce à la puce", result.rawInstructionsList.contains("• Mixez"))
    }

    @Test
    fun `Problem Case 1 - Mixed ingredient and instruction line`() {
        // ML Kit fusionne souvent les colonnes : "nom (poids) + action"
        // La présence de (50 g) doit forcer la classification en ingrédient
        val input = listOf("truite (50 g) frais hachés salade")
        val result = Ocr3Categorizer.categorize(input, res)

        assertTrue("Le bloc mixte contenant un poids doit être classé en ingrédient",
            result.rawIngredientsList.contains("truite (50 g) frais hachés salade"))
    }

    @Test
    fun `Problem Case 2 - Isolated food name recovery after connector`() {
        // Dans une section ingrédient, un mot seul comme 'salade' doit rester un ingrédient
        // surtout s'il suit un connecteur
        val input = listOf("Ingrédients", "quelques feuilles de", "salade")
        val result = Ocr3Categorizer.categorize(input, res)

        assertTrue("La ligne 'salade' doit être dans les ingrédients", result.rawIngredientsList.contains("salade"))
    }
}