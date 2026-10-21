package com.example.chiefinventory.utils.rules

import com.example.chiefinventory.utils.OcrVocabulary
import com.example.chiefinventory.utils.OcrVocabularyLoader
import com.example.chiefinventory.utils.RecipeOcrParser
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test

class RecipeOcrBuchettesMelonRuleTest {

    private lateinit var vocabulary: OcrVocabulary

    @Before
    fun setUp() {
        vocabulary = OcrVocabularyLoader.loadFromProjectResources()
    }

    @Test
    fun `parse buchettes de melon recipe should extract complete two line title with connector au`() {
        val ocrText = """
            ingrédients
            POUR
            4 PERSONNES
            1 melon Galia
            8 tranches de jambon
            italien
            4 c. à soupe de porto
            1 c. à soupe de vinaigre de
            framboise
            4 c. à soupe de yoghourt
            4 c. à soupe d'huile
            d'olive
            1 c. à café de confiture de
            framboises
            4 branches d'aneth
            sel et poivre
            30
            • Pelez le melon, épépinez-le et
            coupez la chair en fines
            lamelles.
            •Coupez les tranches de
            jambon en deux dans le sens de
            la longueur et déposez-y les
            lamelles de melon. Roulez les
            tranches de façon à obtenir
            4 petites bûches par personne.
            Büchettes de melon
            au jambon italien et au porto
            • Mixez le porto, le vinaigre, le
            yoghourt, l'huile d'olive et la
            confiture de framboises pour
            en faire une vinaigrette.
            Assaisonnez généreusement.
            Assiette de présentation disponible dans les Maxi GB
            • Disposez les bûchettes sur
            une assiette avec la vinaigrette.
            Décorez de brins d'aneth.
            VIN CONSEILLÉ
            Atinum, Bordeaux Graves
            MO, 1996, 75 cl
        """.trimIndent()

        val result = RecipeOcrParser.parse(ocrText, vocabulary)

        println("=== WINE RESULT ===")
        println(result.wine)

        val title = result.title ?: ""
        val inst = result.instructions ?: ""

        // 1. Validation du titre complet sur 2 lignes
        assertThat(title).ignoringCase().contains("Büchettes de melon au jambon italien et au porto")

        // 2. Validation que la seconde ligne du titre ne pollue pas les instructions
        assertThat(inst).ignoringCase().doesNotContain("au jambon italien et au porto Mixez")
        assertThat(inst).ignoringCase().contains("Mixez le porto")
    }
}
