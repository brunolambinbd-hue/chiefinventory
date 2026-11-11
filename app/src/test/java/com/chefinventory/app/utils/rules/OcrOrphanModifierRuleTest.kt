package com.chefinventory.app.utils.rules

import com.chefinventory.app.utils.OcrVocabulary
import com.chefinventory.app.utils.OcrVocabularyLoader
import com.chefinventory.app.utils.RecipeOcrParser
import com.google.common.truth.Truth.assertWithMessage
import org.junit.Before
import org.junit.Test

class OcrOrphanModifierRuleTest {

    private lateinit var vocabulary: OcrVocabulary

    @Before
    fun setUp() {
        vocabulary = OcrVocabularyLoader.loadFromProjectResources()
    }

    @Test
    fun `parse recipe with orphan modifier fume alone on line should merge with previous ingredient`() {
        val ocrText = """
            SALADE DE SAUMON
            INGRÉDIENTS
            200 g de saumon
            fumé
            1 oignon
            haché
            2 gousses d'ail
            hachées
            persil
            hachés
            INSTRUCTIONS
            Disposer le saumon et servir.
        """.trimIndent()

        val result = RecipeOcrParser.parse(ocrText, vocabulary)

        val ing = result.ingredients ?: ""

        assertWithMessage("Le modificateur orphelin 'fumé' doit être fusionné avec '200 g de saumon'")
            .that(ing)
            .contains("200 g de saumon fumé")

        assertWithMessage("Le modificateur orphelin 'haché' doit être fusionné avec '1 oignon'")
            .that(ing)
            .contains("1 oignon haché")

        assertWithMessage("Le modificateur orphelin 'hachées' doit être fusionné avec '2 gousses d'ail'")
            .that(ing)
            .contains("2 gousses d'ail hachées")

        assertWithMessage("Le modificateur orphelin 'hachés' doit être fusionné avec 'Persil'")
            .that(ing)
            .contains("Persil hachés")
    }

    @Test
    fun `parse recipe with feminine and plural orphan modifiers should merge correctly with previous line`() {
        val ocrText = """
            SALADE COMPOSÉE
            INGRÉDIENTS
            1 truite
            fumée
            4 tranches de truite
            fumées
            100 g de poissons
            fumés
            2 pommes
            émincées
            1 gousse d'ail
            pelée
            10 olives
            dénoyautées
            INSTRUCTIONS
            Disposer et servir.
        """.trimIndent()

        val result = RecipeOcrParser.parse(ocrText, vocabulary)

        val ing = result.ingredients ?: ""

        assertWithMessage("Le modificateur féminin 'fumée' doit être fusionné avec '1 truite'")
            .that(ing)
            .contains("1 truite fumée")

        assertWithMessage("Le modificateur 'fumées' doit être fusionné avec '4 tranches de truite'")
            .that(ing)
            .contains("4 tranches de truite fumées")

        assertWithMessage("Le modificateur masculin pluriel 'fumés' doit être fusionné avec '100 g de poissons'")
            .that(ing)
            .contains("100 g de poissons fumés")

        assertWithMessage("Le modificateur 'émincées' doit être fusionné avec '2 pommes'")
            .that(ing)
            .contains("2 pommes émincées")

        assertWithMessage("Le modificateur 'pelée' doit être fusionné avec '1 gousse d'ail'")
            .that(ing)
            .contains("1 gousse d'ail pelée")

        assertWithMessage("Le modificateur 'dénoyautées' doit être fusionné avec '10 olives'")
            .that(ing)
            .contains("10 olives dénoyautées")
    }
}
