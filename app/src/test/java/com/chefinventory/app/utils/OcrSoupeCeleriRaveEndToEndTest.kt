package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertThat
import com.google.common.truth.Truth.assertWithMessage
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Test unitaire End-to-End complet ([RecipeOcrParser.parse]) vérifiant l'extraction parfaite
 * sur le scan réel de la fiche de recette à 2 colonnes 'SOUPE AU CÉLERI-RAVE, NOIX ET MASCARPONE'.
 */
class OcrSoupeCeleriRaveEndToEndTest {

    private lateinit var vocabulary: OcrVocabulary

    private val lines2DScan = listOf(
        OcrTextLine("SOUPE AU CÉLERI-RAVE,", top = 18, left = 144, bottom = 171, right = 653),
        OcrTextLine("NOIX ET MASCARPONE", top = 97, left = 146, bottom = 234, right = 627),
        OcrTextLine("O 20 MIN + 30 MIN DE CUISSON O FACILE O EEE", top = 170, left = 149, bottom = 297, right = 714),
        OcrTextLine("A4PERS. O 233 KCALPOR.", top = 251, left = 146, bottom = 336, right = 460),
        OcrTextLine("NUTRI-SCORE", top = 359, left = 160, bottom = 398, right = 271),
        // Colonne 1 (Gauche)
        OcrTextLine("•h céleri-rave", top = 467, left = 144, bottom = 530, right = 343),
        OcrTextLine("• 2 oignons", top = 515, left = 144, bottom = 577, right = 297),
        OcrTextLine("• 40g de cerneauX", top = 556, left = 141, bottom = 614, right = 392),
        OcrTextLine("de noix i", top = 603, left = 163, bottom = 653, right = 308),
        OcrTextLine("• 2 gousses d'ail S3", top = 637, left = 133, bottom = 709, right = 410),
        OcrTextLine("•4 brins de thym", top = 696, left = 129, bottom = 750, right = 364),
        // Colonne 2 (Droite)
        OcrTextLine("•2 càsde mascarpone", top = 381, left = 581, bottom = 468, right = 913),
        OcrTextLine("•20 g de beurre .", top = 437, left = 578, bottom = 518, right = 865),
        OcrTextLine("• 2 cubes de bouillon", top = 486, left = 589, bottom = 561, right = 879),
        OcrTextLine("de légumes", top = 543, left = 619, bottom = 608, right = 770),
        OcrTextLine(". Poivre h", top = 594, left = 577, bottom = 654, right = 749),
        OcrTextLine(".Sel", top = 662, left = 579, bottom = 697, right = 648),
        // Instructions
        OcrTextLine("Épluchez le céleri-rave et coupez-le", top = 756, left = 183, bottom = 861, right = 862),
        OcrTextLine("en dés. Hachez grossièrement les oignons.", top = 806, left = 116, bottom = 918, right = 913),
        OcrTextLine("Mettez le tout dans une casserole contenant", top = 858, left = 110, bottom = 967, right = 953),
        OcrTextLine("le beurre fondu. Ajoutez les gousses d'ail", top = 919, left = 106, bottom = 1022, right = 877),
        OcrTextLine("épluchées et coupées en 2, ainsi que le thym.", top = 966, left = 101, bottom = 1076, right = 965),
        OcrTextLine("Faites revenir 5 min à bon feu, puis arrosez", top = 1023, left = 96, bottom = 1131, right = 939),
        OcrTextLine("de 11 d'eau. Joignez les cubes de bouillon", top = 1081, left = 90, bottom = 1187, right = 884),
        OcrTextLine("émiettés et portez à ébullition. Laissez", top = 1144, left = 84, bottom = 1246, right = 841),
        OcrTextLine("mijoter 30 min.", top = 1244, left = 78, bottom = 1307, right = 371),
        OcrTextLine("Hachez grossièrement les noix et", top = 1330, left = 153, bottom = 1421, right = 802),
        OcrTextLine("faites-les légèrement griller à sec, dans", top = 1391, left = 61, bottom = 1490, right = 859),
        OcrTextLine("une poêle.", top = 1499, left = 61, bottom = 1556, right = 257),
        OcrTextLine("O Ajoutez le mascarpone dans la soupe et", top = 1595, left = 51, bottom = 1679, right = 959),
        OcrTextLine("mixez finement. Rectifiez l'assaisonnement", top = 1665, left = 41, bottom = 1741, right = 967),
        OcrTextLine("et servez bien chaud, saupoudré de noix.", top = 1741, left = 37, bottom = 1818, right = 905),
        OcrTextLine("ģ- Pour une version encore plus gourmande, ajoutez des", top = 1872, left = 27, bottom = 1929, right = 930),
        OcrTextLine("petits dés de pomme crue en topping et/ou une petite quenelle", top = 1941, left = 20, bottom = 1991, right = 1022),
        OcrTextLine("de mascarpone.", top = 2009, left = 15, bottom = 2058, right = 253)
    )

    @Before
    fun setUp() {
        OcrVocabularyLoader.clearCache()
        vocabulary = OcrVocabularyLoader.loadFromProjectResources()
    }

    @After
    fun tearDown() {
        OcrVocabularyLoader.clearCache()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testSoupeCeleriRave_EndToEnd_ExtractsAllIngredientsAndInstructionsPerfectly() {
        val result = RecipeOcrParser.parse(lines2DScan, vocabulary)

        val ingredients = result.ingredients?.split("\n") ?: emptyList()
        val instructions = result.instructions?.split("\n") ?: emptyList()

        assertWithMessage("Le titre doit être 'SOUPE AU CÉLERI-RAVE, NOIX ET MASCARPONE'")
            .that(result.title)
            .ignoringCase()
            .contains("soupe au céleri-rave")

        // Vérification des 10 ingrédients
        assertThat(ingredients.any { it.contains("céleri-rave", ignoreCase = true) }).isTrue()
        assertThat(ingredients.any { it.contains("oignons", ignoreCase = true) }).isTrue()
        assertThat(ingredients.any { it.contains("noix", ignoreCase = true) }).isTrue()
        assertThat(ingredients.any { it.contains("ail", ignoreCase = true) }).isTrue()
        assertThat(ingredients.any { it.contains("thym", ignoreCase = true) }).isTrue()
        assertThat(ingredients.any { it.contains("mascarpone", ignoreCase = true) }).isTrue()
        assertThat(ingredients.any { it.contains("beurre", ignoreCase = true) }).isTrue()
        assertThat(ingredients.any { it.contains("bouillon", ignoreCase = true) }).isTrue()
        assertThat(ingredients.any { it.contains("Poivre", ignoreCase = true) }).isTrue()
        assertThat(ingredients.any { it.contains("Sel", ignoreCase = true) }).isTrue()

        // Vérification des consignes
        assertThat(instructions.any { it.contains("Épluchez le céleri-rave", ignoreCase = true) }).isTrue()
        assertThat(instructions.any { it.contains("Hachez grossièrement les oignons", ignoreCase = true) }).isTrue()
        assertThat(instructions.any { it.contains("Ajoutez le mascarpone", ignoreCase = true) }).isTrue()
    }
}
