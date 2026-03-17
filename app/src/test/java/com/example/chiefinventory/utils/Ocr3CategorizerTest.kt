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
        `when`(res.getStringArray(R.array.step_action_keywords)).thenReturn(arrayOf("faites", "mélangez", "coupez", "ajoutez"))
        `when`(res.getStringArray(R.array.common_ingredients_no_qty)).thenReturn(arrayOf("sel", "poivre", "beurre"))
        `when`(res.getStringArray(R.array.excluded_ocr_keywords)).thenReturn(arrayOf("SAVEUR", "POUR"))
        `when`(res.getStringArray(R.array.source_keywords)).thenReturn(arrayOf("hôtel", "rue"))
        `when`(res.getStringArray(R.array.phone_prefixes)).thenReturn(arrayOf("tél"))

        // Mocks pour WineParser et SourceParser
        `when`(res.getStringArray(R.array.wine_keywords)).thenReturn(arrayOf("merlot"))
        `when`(res.getStringArray(R.array.wine_appellations)).thenReturn(emptyArr)
        `when`(res.getStringArray(R.array.wine_producers)).thenReturn(emptyArr)
        `when`(res.getStringArray(R.array.wine_title_keywords)).thenReturn(arrayOf("vin conseillé"))
        `when`(res.getString(R.string.wine_remove_pattern)).thenReturn("DUMMY")
    }

    @Test
    fun `Empty input lines list`() {
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
    fun `Technical dimension preservation`() {
        // Verify that lines matching Ocr2Cleaner.isTechnicalDimension are always added to rawInstructionsList and
        // bypass further categorization logic.
        val input = listOf("5 mm x 5 mm")
        val result = Ocr3Categorizer.categorize(input, res)
        assertTrue(result.rawInstructionsList.contains("5 mm x 5 mm"))
        assertTrue(result.rawIngredientsList.isEmpty())
    }

    @Test
    fun `Servings extraction standard format`() {
        // Test 'Pour 4 personnes' or 'Serves: 6' patterns to ensure the digit is extracted into detectedServings
        // and the text is removed from the working line.
        val input = listOf("Pour 4 personnes")
        val result = Ocr3Categorizer.categorize(input, res)
        assertEquals("4", result.detectedServings)
        // La ligne est consommée, donc rien ne doit rester dans les listes brutes
        assertTrue(result.rawIngredientsList.isEmpty())
    }

    @Test
    fun `Servings extraction alternate format`() {
        // Test '4 pers.' or '2 servings' patterns where the digit precedes the keyword.
        val input = listOf("6 servings")
        val result = Ocr3Categorizer.categorize(input, res)
        assertEquals("6", result.detectedServings)
    }

    @Test
    fun `Servings extraction priority`() {
        // Ensure only the first detected servings value is stored if multiple lines contain serving information.
        val input = listOf("Pour 4 personnes", "Serves 6")
        val result = Ocr3Categorizer.categorize(input, res)
        assertEquals("4", result.detectedServings)
    }

    @Test
    fun `Time extraction preparation time`() {
        // Verify that 'Préparation : 15 min' correctly calculates 15 minutes and assigns it to detectedPrepTime.
        val input = listOf("Préparation : 15 min")
        val result = Ocr3Categorizer.categorize(input, res)
        assertEquals("15", result.detectedPrepTime)
    }

    @Test
    fun `Time extraction cooking time with hours`() {
        // Test 'Cuisson: 1h 20' to ensure extractMinutes correctly converts the value to '80'.
        val input = listOf("Cuisson: 1h 20")
        val result = Ocr3Categorizer.categorize(input, res)
        assertEquals("80", result.detectedCookTime)
    }

    @Test
    fun `Time extraction resting time`() {
        // Test 'Repos: 2 h' to ensure detectedRestingTime is correctly set to '120'.
        val input = listOf("Repos: 2 h")
        val result = Ocr3Categorizer.categorize(input, res)
        assertEquals("120", result.detectedRestingTime)
    }

    @Test
    fun `Time extraction multiple times on one line`() {
        // Ensure the while loop correctly processes and removes multiple time patterns (e.g., Prep and Cook) from a single line.
        val input = listOf("Prép: 10 min, Cuisson: 30 min")
        val result = Ocr3Categorizer.categorize(input, res)
        assertEquals("10", result.detectedPrepTime)
        assertEquals("30", result.detectedCookTime)
    }

    @Test
    fun `Excluded keywords removal`() {
        // Verify that words defined in R.array.excluded_ocr_keywords are stripped from the line without
        // leaving orphaned characters or affecting word boundaries.
        val input = listOf("200 g SAVEUR beurre")
        val result = Ocr3Categorizer.categorize(input, res)
        // La ligne résultante doit être dans les ingrédients (car 200g) mais sans SAVEUR
        assertEquals("200 g beurre", result.rawIngredientsList[0])
    }

    @Test
    fun `Wine detection exclusion for ingredients`() {
        // Ensure a line like '5 cl d'huile' is NOT categorized as wine even if it matches wine patterns,
        // because it looks like a strict ingredient.
        val input = listOf("5 cl d'huile")
        val result = Ocr3Categorizer.categorize(input, res)
        assertTrue(result.rawIngredientsList.contains("5 cl d'huile"))
        assertTrue(result.detectedWineList.isEmpty())
    }

    @Test
    fun `Wine detection and cleaning`() {
        // Verify that valid wine lines are cleaned via WineParser and added to detectedWineList,
        // stopping further processing for that line.
        val input = listOf("Suggestion : Merlot 2015")
        val result = Ocr3Categorizer.categorize(input, res)
        // Le wineRes simulé a 'merlot' en mot-clé
        assertTrue(result.detectedWineList.isNotEmpty())
        assertTrue(result.rawIngredientsList.isEmpty())
    }

    @Test
    fun `Source and proper name detection`() {
        // Verify that lines identifying the recipe source are added to detectedSourceList and skip further categorization.
        val input = listOf("Hôtel Ritz")
        val result = Ocr3Categorizer.categorize(input, res)
        assertTrue(result.detectedSourceList.contains("Hôtel Ritz"))
    }

    @Test
    fun `Ingredient header state transition`() {
        // Test that lines like 'Ingrédients' (length < 35) switch currentSection to SECTION_INGREDIENTS
        // and the header text itself is stripped.
        val input = listOf("Ingrédients", "200 g de beurre")
        val result = Ocr3Categorizer.categorize(input, res)
        assertEquals(1, result.rawIngredientsList.size)
        assertEquals("200 g de beurre", result.rawIngredientsList[0])
    }

    @Test
    fun `Instruction header state transition`() {
        // Test that lines like 'Préparation' switch currentSection to SECTION_INSTRUCTIONS and the header text is stripped.
        val input = listOf("Réalisation", "Faites fondre le beurre")
        val result = Ocr3Categorizer.categorize(input, res)
        assertEquals(1, result.rawInstructionsList.size)
        assertEquals("Faites fondre le beurre", result.rawInstructionsList[0])
    }

    @Test
    fun `Header length constraint`() {
        // Verify that a very long line (> 35 chars) containing a header word is NOT treated as a section
        // header switch.
        val input = listOf("Ceci est une phrase très longue qui contient le mot ingrédients mais qui n'est pas un header")
        val result = Ocr3Categorizer.categorize(input, res)
        // Elle sera classée comme instruction par défaut car pas de qty
        assertEquals(1, result.rawInstructionsList.size)
    }

    @Test
    fun `Noise removal for non letter lines`() {
        // Ensure lines consisting only of symbols or numbers (no letters) after cleaning are discarded.
        val input = listOf("123", "---", "1")
        val result = Ocr3Categorizer.categorize(input, res)
        assertTrue(result.rawIngredientsList.isEmpty())
        assertTrue(result.rawInstructionsList.isEmpty())
    }

    @Test
    fun `Instruction classification via action verbs`() {
        // Verify that a line containing an action verb (e.g., 'Mélangez') with length > 25 is categorized as an instruction.
        val input = listOf("Mélangez bien tous les éléments dans un grand saladier")
        val result = Ocr3Categorizer.categorize(input, res)
        assertTrue(result.rawInstructionsList.contains("Mélangez bien tous les éléments dans un grand saladier"))
    }

    @Test
    fun `Ingredient classification via quantity regex`() {
        // Test lines starting with '1/', 'Une', or symbols like '|' followed by digits to ensure they are
        // categorized as ingredients.
        val input = listOf("1/2 oignon", "| 2 citrons")
        val result = Ocr3Categorizer.categorize(input, res)
        assertEquals(2, result.rawIngredientsList.size)
    }

    @Test
    fun `Ingredient classification via connectors`() {
        // Verify lines starting with 'de ', 'du ', or 'aux ' are categorized as ingredients when no section is set.
        val input = listOf("de la ciboulette")
        val result = Ocr3Categorizer.categorize(input, res)
        assertTrue(result.rawIngredientsList.contains("de la ciboulette"))
    }

    @Test
    fun `Bullet point instruction classification`() {
        // Test that lines starting with '•' or '-' containing an action verb are categorized as instructions
        // regardless of length.
        val input = listOf("• Ajoutez le sel")
        val result = Ocr3Categorizer.categorize(input, res)
        assertTrue(result.rawInstructionsList.contains("• Ajoutez le sel"))
    }

    @Test
    fun `Ingredient detection within instruction section`() {
        // Verify that short lines (< 45 chars) without action verbs found inside an instruction section
        // are 'pulled back' into the rawIngredientsList.
        val input = listOf("Réalisation", "200 g de beurre", "Faites fondre")
        val result = Ocr3Categorizer.categorize(input, res)
        assertEquals("200 g de beurre", result.rawIngredientsList[0])
        assertEquals("Faites fondre", result.rawInstructionsList[0])
    }

    @Test
    fun `Default classification fallback`() {
        // Test how lines are handled when currentSection is SECTION_NONE and no clear ingredient/instruction
        // indicators are present.
        val input = listOf("Phrase lambda sans signal")
        val result = Ocr3Categorizer.categorize(input, res)
        // Par défaut, sans signal, on met dans les instructions
        assertTrue(result.rawInstructionsList.contains("Phrase lambda sans signal"))
    }

    @Test
    fun `ExtractMinutes edge case missing minutes`() {
        // Verify that '1h' without specified minutes is correctly parsed as '60' minutes.
        val input = listOf("Cuisson: 1h")
        val result = Ocr3Categorizer.categorize(input, res)
        assertEquals("60", result.detectedCookTime)
    }

    @Test
    fun `ExtractMinutes edge case  u  unit`() {
        // Verify that the 'u' (common OCR error for 'mn') is correctly caught by the mPattern.
        val input = listOf("Cuisson: 30 u")
        val result = Ocr3Categorizer.categorize(input, res)
        assertEquals("30", result.detectedCookTime)
    }

    @Test
    fun `Case insensitivity of headers and verbs`() {
        // Verify that 'INGRÉDIENTS' or 'mélangez' (lowercase) are correctly identified using case-insensitive regex/logic.
        val input = listOf("INGRÉDIENTS", "mélangez doucement tous les ingrédients dans un grand bol")
        val result = Ocr3Categorizer.categorize(input, res)
        // mélangez est dans les instructions (action verb)
        assertEquals(1, result.rawInstructionsList.size)
        assertTrue(result.rawInstructionsList[0].contains("mélangez"))
    }

    @Test
    fun `Common ingredient list matching`() {
        // Verify that lines starting with words from common_ingredients_no_qty are categorized as ingredients
        // even without leading quantities.
        val input = listOf("sel et poivre")
        val result = Ocr3Categorizer.categorize(input, res)
        assertTrue(result.rawIngredientsList.contains("sel et poivre"))
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
}