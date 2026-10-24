package com.example.chiefinventory.utils.rules

import com.example.chiefinventory.utils.OcrVocabulary
import com.example.chiefinventory.utils.OcrVocabularyLoader
import com.example.chiefinventory.utils.RecipeOcrParser
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test

class RecipeOcrRouladeJambonRuleTest {

    private lateinit var vocabulary: OcrVocabulary

    @Before
    fun setUp() {
        vocabulary = OcrVocabularyLoader.loadFromProjectResources()
    }

    @Test
    fun `parse roulade de jambon de parme recipe should extract title ingredients instructions wine and prepTime`() {
        val ocrText = """
            1
            Koulade de jambon de Parme el sa petite s
            1) Nettoyez la salade de blé,
            lavez-la, essorez-la.
            2) Egouttez les petites boules de
            melon.
            3) Préparez une vinaigrette avec
            I'huile, le vinaigre, le sel et le
            poivre. Ajoutez-la à la salade,
            joignez les boules de melon et
            les croûtons.
            4)
            Répartissez la salade sur
            assiettes, ajoutez
            quatre
            tranches de jambon de Parme
            roulées et servez sans attendre.
            salade
            les
            POUR
            4 PERSONNES:
            2 ou 3 paquets
            de jambon de Parnme
            en fines tranches
            (125 à 180 g)
            1/2 ravier de salade
            de blé
            1/2 sachet de croûtons
            à l'ail
            1/2 boîte de boules de
            melon au sirop
            3 c. à soupe d'huile
            d'olive
            1c.à soupe de vinaigre
            sel et poivre
            15 mn
            Notre
            conseil vin
            Château
            VERDIGNAN
            Haut-Médoc
            MDC,1989, 75 cl
            Prix indicatif au
            14/12/94: 353 F
            11
        """.trimIndent()

        val result = RecipeOcrParser.parse(ocrText, vocabulary)
        System.err.println("=== DEBUG RESULT TITLE = " + result.title)

        val ing = result.ingredients ?: ""
        val inst = result.instructions ?: ""

        // 1. Validation du Titre reconstitué et réparé
        assertThat(result.title).ignoringCase().contains("Roulade de jambon de Parme et sa petite salade")

        // 2. Validation des portions et temps
        assertThat(result.servings).isEqualTo("4")
        assertThat(result.prepTime).isEqualTo("15")

        // 3. Validation des ingrédients de la section du bas (sans pollution d'instructions ni de vin)
        System.err.println("=== REAL INGREDIENTS RESULT ===\n" + ing)
        assertThat(ing).ignoringCase().contains("2 ou 3 paquets")
        assertThat(ing).ignoringCase().contains("jambon de Parme en fines tranches")
        assertThat(ing).ignoringCase().contains("ravier de salade de blé")
        assertThat(ing).ignoringCase().contains("croûtons")
        assertThat(ing).ignoringCase().contains("3 c. à soupe")
        assertThat(ing).ignoringCase().doesNotContain("Répartissez")
        assertThat(ing).ignoringCase().doesNotContain("roulées")
        assertThat(ing).ignoringCase().doesNotContain("Notre")
        assertThat(ing).ignoringCase().doesNotContain("Conseil vin")

        // 4. Validation des instructions fluides du haut
        assertThat(inst).ignoringCase().contains("Nettoyez la salade de blé")
        assertThat(inst).ignoringCase().contains("Egouttez les petites boules de melon")

        // 5. Validation de la recommandation de vin
        assertThat(result.wine).ignoringCase().contains("MDC")
    }
}
