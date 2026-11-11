package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertWithMessage
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Suite de tests unitaires dédiée 1-à-1 pour [Ocr3CategorizerTitleExtractor0_Context].
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class Ocr3CategorizerTitleExtractor0_ContextTest {

    private lateinit var vocabulary: OcrVocabulary

    @Before
    fun setUp() {
        vocabulary = OcrVocabularyLoader.loadFromProjectResources()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testExtractFirstIngredientNameWord_DirectCall() {
        val stop = listOf("de", "du", "des", "d'", "la", "le", "les", "pour")

        // 1. Empty or blank name -> null
        assertWithMessage("Un nom d'ingrédient vide doit retourner null")
            .that(Ocr3CategorizerTitleExtractor0_Context.extractFirstIngredientNameWord("", stop))
            .isNull()

        assertWithMessage("Un nom d'ingrédient composé uniquement d'espaces doit retourner null")
            .that(Ocr3CategorizerTitleExtractor0_Context.extractFirstIngredientNameWord("   ", stop))
            .isNull()

        // 2. Only stop words -> null
        assertWithMessage("Un nom composé uniquement de mots vides doit retourner null")
            .that(Ocr3CategorizerTitleExtractor0_Context.extractFirstIngredientNameWord("de pour", stop))
            .isNull()

        // 3. Valid name -> returns first word
        assertWithMessage("Le premier mot significatif 'farine' de 'farine fluide' doit être extrait")
            .that(Ocr3CategorizerTitleExtractor0_Context.extractFirstIngredientNameWord("farine fluide", stop))
            .isEqualTo("farine")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testExtractRecipeIngredientWords_FullCoverage() {
        val lines = listOf(
            "   ",
            "INGRÉDIENTS",
            "200 g de farine pommes choux gousses",
            "3 cuillères de sucre"
        )
        val words = Ocr3CategorizerTitleExtractor0_Context.extractRecipeIngredientWords(lines)
        assertWithMessage("Les mots des ingrédients de la fiche doivent contenir 'farine'")
            .that(words)
            .contains("farine")

        assertWithMessage("Les mots des ingrédients de la fiche doivent contenir 'pomme'")
            .that(words)
            .contains("pomme")

        assertWithMessage("Les mots des ingrédients de la fiche doivent contenir 'chou'")
            .that(words)
            .contains("chou")

        assertWithMessage("Les mots des ingrédients de la fiche doivent contenir 'sucre'")
            .that(words)
            .contains("sucre")

        assertWithMessage("Les unités de mesure 'gousses' doivent être ignorées des mots d'ingrédients")
            .that(words)
            .doesNotContain("gousses")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testFindFirstIngredientWord_WithNullAndNonNullVocabulary() {
        val lines = listOf("   ", "200 g de farine")

        // 1. With non-null vocabulary
        val word1 = Ocr3CategorizerTitleExtractor0_Context.findFirstIngredientWord(lines, vocabulary)
        assertWithMessage("Le premier mot d'ingrédient avec vocabulaire non null doit être 'farine'")
            .that(word1)
            .isEqualTo("farine")

        // 2. With null vocabulary
        val word2 = Ocr3CategorizerTitleExtractor0_Context.findFirstIngredientWord(lines, null)
        assertWithMessage("Le premier mot d'ingrédient avec vocabulaire null doit être 'farine'")
            .that(word2)
            .isEqualTo("farine")

        // 3. Overloaded method without second argument
        val word3 = Ocr3CategorizerTitleExtractor0_Context.findFirstIngredientWord(lines)
        assertWithMessage("La surcharge sans deuxième argument doit retourner 'farine'")
            .that(word3)
            .isEqualTo("farine")

        // 4. Vocabulary with empty standaloneArticles
        val emptyArticlesVocab = vocabulary.copy(standaloneArticles = emptyList())
        val word4 = Ocr3CategorizerTitleExtractor0_Context.findFirstIngredientWord(lines, emptyArticlesVocab)
        assertWithMessage("Le premier mot d'ingrédient avec standaloneArticles vide doit être 'farine'")
            .that(word4)
            .isEqualTo("farine")

        // 5. Lines with blank parsed name ("200 g") and stopWords only ("3 g de pour")
        val wordBlank = Ocr3CategorizerTitleExtractor0_Context.findFirstIngredientWord(listOf("200 g", "3 g de pour", "200 g de farine"), vocabulary)
        assertWithMessage("Le premier ingrédient valide 'farine' doit être extrait après les lignes vides/mots-morts")
            .that(wordBlank)
            .isEqualTo("farine")

        // 6. With empty lines
        val word5 = Ocr3CategorizerTitleExtractor0_Context.findFirstIngredientWord(listOf("   ", "Instructions"), vocabulary)
        assertWithMessage("Des lignes sans ingrédient valide doivent retourner null")
            .that(word5)
            .isNull()
    }
}
