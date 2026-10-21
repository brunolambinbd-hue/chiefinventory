package com.example.chiefinventory.utils.rules

import com.example.chiefinventory.utils.OcrVocabulary
import com.example.chiefinventory.utils.OcrVocabularyLoader
import com.example.chiefinventory.utils.RecipeOcrParser
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test

class OcrTitleArtichautRuleTest {

    private lateinit var vocabulary: OcrVocabulary

    @Before
    fun setUp() {
        vocabulary = OcrVocabularyLoader.loadFromProjectResources()
    }

    @Test
    fun `parse artichauts recipe should repair o slash rtichauts to Artichauts et vinaigrette aux echalotes`() {
        val ocrText = """
            ingrédients
            POUR
            4 PERSONNES
            4 artichauts
            2 l. d'eau
            le jus de 1 citron
            4 c. à soupe de farine
            sel et poivre
            Pour la vinaigrette
            2 c. à soupe de moutarde
            forte
            2 échalotes
            4 c. à soupe de vinaigre de
            vin blanc
            2 c. à soupe d'eau
            16 c. à soupe de huile
            d'olive douce
            1 c. à soupe de persil
            haché
            • Mélangez la farine avec un
            peu d'eau et le jus de citron.
            Mettez ce mélange avec le
            reste d'eau dans une grande
            casserole et ajoutez-y les
            4 artichauts. Salez et poivrez.
            o/rtichauts et vinaigrette
            aux échalotes
            • Faites cuire pendant 1 heure
            à feu doux.
            Assiette de présentation disponible dans les Maxi GB
            • Pendant ce temps, préparez
            la vinaigrette et ajoutez-y au
            dernier moment les échalotes
            émincées et le persil.
            • Egoutez les artichauts.
            Servez avec la vinaigrette
            dans laquelle vous tremperez
            les feuilles et le cœur.
            VIN CONSEILLE
            Château de la Fossardière
            Muscadet de Sèvre-et-Maine sur Lie
            MDC, I996, 75 cl
            CHATEAU
            ala FoSARDJERE
        """.trimIndent()

        val result = RecipeOcrParser.parse(ocrText, vocabulary)

        // Validation du titre réparé sans le slash o/rtichauts -> Artichauts et vinaigrette aux échalotes
        assertThat(result.title).ignoringCase().contains("Artichauts et vinaigrette aux échalotes")
    }
}
