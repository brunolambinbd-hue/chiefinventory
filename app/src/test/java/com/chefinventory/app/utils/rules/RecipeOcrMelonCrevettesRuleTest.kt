package com.chefinventory.app.utils.rules

import com.chefinventory.app.utils.OcrVocabulary
import com.chefinventory.app.utils.OcrVocabularyLoader
import com.chefinventory.app.utils.RecipeOcrParser
import com.google.common.truth.Truth.assertWithMessage
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

        assertWithMessage("Les '4 tranches' et 'de jambon séché' doivent être fusionnées en '4 tranches de jambon séché'")
            .that(ing)
            .ignoringCase()
            .contains("4 tranches de jambon séché")
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

        assertWithMessage("Le titre 'Melon aux crevettes et au jambon séché' doit être extrait")
            .that(result.title)
            .ignoringCase()
            .contains("Melon aux crevettes et au jambon séché")

        assertWithMessage("Les ingrédients doivent contenir '1 petite salade frisée'")
            .that(ing)
            .ignoringCase()
            .contains("1 petite salade frisée")

        assertWithMessage("Les ingrédients doivent contenir '1 melon galia'")
            .that(ing)
            .ignoringCase()
            .contains("1 melon galia")

        assertWithMessage("Les ingrédients doivent contenir '4 tranches de jambon séché'")
            .that(ing)
            .ignoringCase()
            .contains("4 tranches de jambon séché")

        assertWithMessage("Les ingrédients doivent contenir 'le jus de 1/2 citron'")
            .that(ing)
            .ignoringCase()
            .contains("le jus de 1/2 citron")

        assertWithMessage("L'en-tête de temps ne doit pas polluer les instructions")
            .that(inst)
            .ignoringCase()
            .doesNotContain("Préparation 1")

        assertWithMessage("L'en-tête de temps ne doit pas figurer dans les instructions")
            .that(inst)
            .ignoringCase()
            .doesNotContain("Temps de préparation")

        assertWithMessage("L'en-tête de temps ne doit pas figurer dans les ingrédients")
            .that(ing)
            .ignoringCase()
            .doesNotContain("Temps de")

        assertWithMessage("Les instructions doivent démarrer par 'Coupez le melon en deux et épépinez-le'")
            .that(inst)
            .ignoringCase()
            .contains("Coupez le melon en deux et épépinez-le")

        assertWithMessage("Les ingrédients ne doivent pas fuiter dans les instructions")
            .that(inst)
            .ignoringCase()
            .doesNotContain("1 petite salade frisée")
    }
}
