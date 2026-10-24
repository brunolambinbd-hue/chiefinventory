package com.example.chiefinventory.utils.rules

import com.example.chiefinventory.utils.OcrVocabulary
import com.example.chiefinventory.utils.OcrVocabularyLoader
import com.example.chiefinventory.utils.RecipeOcrParser
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test

class RecipeOcrMelonCrevettesRuleTest {

    private lateinit var vocabulary: OcrVocabulary

    @Before
    fun setUp() {
        vocabulary = OcrVocabularyLoader.loadFromProjectResources()
    }

    @Test
    fun `parse melon crevettes recipe should merge 4 tranches and de jambon seche into single full ingredient`() {
        val ocrText = """
            Melon aux crevettes
            INGRÉDIENTS
            1 petite salade frisée
            1 melon galia
            4 tranches
            de jambon séché
            200 g de crevettes
            2 dl de vin muscat
            2 c. à soupe d'huile d'olive
            1 c. à soupe de vinaigre
            le jus de 1/2 citron
            sel et poivre
            • Coupez le melon en deux, retirez les pépins.
        """.trimIndent()

        val result = RecipeOcrParser.parse(ocrText, vocabulary)

        val ing = result.ingredients ?: ""

        // Validation : "4 tranches" + "de jambon séché" doivent être fusionnés en "4 tranches de jambon séché"
        assertThat(ing).ignoringCase().contains("4 tranches de jambon séché")
    }

    @Test
    fun `parse exact user logcat melon aux crevettes scan`() {
        val ocrText = """
            Temps de
            préparation
            INGRÉDIENTS
            POUR
            4 PERSONNES
            I petite salade frisée
            I melon galia
            4 tranches
            de jambon séché
            200 g de crevettes
            2 dl de vin muscat
            2c. à soupe
            de crème fraiche
            1c.à soupe
            de ciboulette hachée
            le jus de 112 citron
            sel et poivre
            • Coupez le melon en deux et
            épépinez-le. Mettez-en la
            moitié coupée en 4 quartiers
            sur un lit de salade.
            • Evidez l'autre moitié et
            mélangez la pulpe au vin.
            Assaisonnez.
            lelon aux crevettes et au jambon séché
            Assiette de présentation disponible dans les Maxi GB
            • Mélangez les crevettes et la
            crème fraîche. Ajoutez-y la
            ciboulette hachée et arrosez
            d’un trait de jus de citron.
            Répartissez ce mélange sur les
            tranches de jambon et faites-en
            des roulades.
            • Disposez les roulades de
            crevettes sur 1assiette et
            nappez de la préparation au
            melon. Décorez
            éventuellement de quelques
            brins de ciboulette.
            VIN CONSEILLÉ
            Muscat d'Alsace,
            Caves de Pfaffenheim, MO,
            1995, 75 cl
            MUSCAT
        """.trimIndent()

        val result = RecipeOcrParser.parse(ocrText, vocabulary)

        val ing = result.ingredients ?: ""
        val inst = result.instructions ?: ""

        System.err.println("=== USER LOGCAT RESULT TITLE ===\n" + result.title)
        System.err.println("=== USER LOGCAT RESULT INGREDIENTS ===\n" + ing)
        System.err.println("=== USER LOGCAT RESULT INSTRUCTIONS ===\n" + inst)

        assertThat(result.title).ignoringCase().contains("Melon aux crevettes et au jambon séché")
        assertThat(ing).ignoringCase().contains("1 petite salade frisée")
        assertThat(ing).ignoringCase().contains("1 melon galia")
        assertThat(ing).ignoringCase().contains("4 tranches de jambon séché")
        assertThat(ing).ignoringCase().contains("le jus de 1/2 citron")

        // Validation que Temps de \n préparation est éliminé et ne pollue pas le début des instructions
        assertThat(inst).ignoringCase().doesNotContain("Préparation 1")
        assertThat(inst).ignoringCase().doesNotContain("Temps de préparation")
        assertThat(ing).ignoringCase().doesNotContain("Temps de")

        assertThat(inst).ignoringCase().contains("Coupez le melon en deux et épépinez-le")
        assertThat(inst).ignoringCase().doesNotContain("1 petite salade frisée")
    }
}
