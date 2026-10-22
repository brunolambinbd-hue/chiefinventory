package com.example.chiefinventory.utils

import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test

/**
 * Suite de tests unitaires de Type B vérifiant le scoring, la détection multi-lignes et la réparation de lettrines de [OcrTitleExtractor].
 */
class OcrTitleExtractorTest {

    private lateinit var vocabulary: OcrVocabulary

    @Before
    fun setUp() {
        vocabulary = OcrVocabularyLoader.loadFromProjectResources()
    }

    /**
     * Vérifie l'identification des connecteurs et prépositions de suite de titre multi-lignes.
     */
    @Test
    fun `isMultiLineTitleConnector should return true for title connector prepositions`() {
        assertThat(OcrTitleExtractor.isMultiLineTitleConnector("au jambon italien et au porto")).isTrue()
        assertThat(OcrTitleExtractor.isMultiLineTitleConnector("aux échalotes")).isTrue()
        assertThat(OcrTitleExtractor.isMultiLineTitleConnector("et aux crevettes")).isTrue()
        assertThat(OcrTitleExtractor.isMultiLineTitleConnector("à la crème")).isTrue()
        assertThat(OcrTitleExtractor.isMultiLineTitleConnector("de campagne")).isTrue()

        assertThat(OcrTitleExtractor.isMultiLineTitleConnector("1 melon Galia")).isFalse()
        assertThat(OcrTitleExtractor.isMultiLineTitleConnector("INGRÉDIENTS")).isFalse()
    }

    /**
     * Vérifie le post-traitement et la réparation des lettrines et fautes de débuts de titres via `title_repairs.xml`.
     */
    @Test
    fun `postProcessTitle should repair title OCR typos like Toocktail to Cocktail and Buchettes to Buchettes`() {
        assertThat(OcrTitleExtractor.postProcessTitle("Toocktail de crevettes aux concombres", vocabulary))
            .isEqualTo("Cocktail de crevettes aux concombres")

        assertThat(OcrTitleExtractor.postProcessTitle("Büchettes de melon au jambon italien", vocabulary))
            .isEqualTo("Bûchettes de melon au jambon italien")

        assertThat(OcrTitleExtractor.postProcessTitle("Salade de Drabe", vocabulary))
            .isEqualTo("Salade de Crabe")
    }

    /**
     * Vérifie l'extraction complète d'un titre multi-lignes relié par une préposition et réparé orthographiquement.
     */
    @Test
    fun `extractTitle should extract two line title connected by au and post process spelling repairs`() {
        val lines = listOf(
            "INGRÉDIENTS",
            "1 melon Galia",
            "8 tranches de jambon",
            "sel et poivre",
            "Büchettes de melon",
            "au jambon italien et au porto",
            "• Pelez le melon et servez."
        )

        val result = OcrTitleExtractor.extractTitle(lines, vocabulary)

        assertThat(result.title).isEqualTo("Bûchettes de melon au jambon italien et au porto")
    }
}
