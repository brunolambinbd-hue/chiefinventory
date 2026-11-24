package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertWithMessage
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.Test

/**
 * Suite de tests unitaires TDD d'isolation vérifiant que la nouvelle fonction extractTitleBanners
 * extrait les bannières de titres trans-colonnes (width >= 500 px sans puce, quantité ou verbe d'action)
 * et sépare proprement le corps de texte.
 */
class Ocr2DSpatialTitleBannerExtractorTest {

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testExtractTitleBanners_MelonRecipe_IsolatesTitleBannersFromInstructionBody() {
        val linesMelon = listOf(
            OcrTextLine("Büchettes de melon", top = 608, left = 454, bottom = 778, right = 1392),
            OcrTextLine("au jambon italien et au porto", top = 740, left = 672, bottom = 865, right = 1827),
            OcrTextLine("I melon Galia", top = 208, left = 175, bottom = 238, right = 337),
            OcrTextLine("8 tranches de jambon", top = 255, left = 116, bottom = 283, right = 382),
            OcrTextLine("• Pelez le melon, épépinez-le et", top = 910, left = 93, bottom = 951, right = 623),
            OcrTextLine("• Mixez le porto, le vinaigre, le", top = 941, left = 686, bottom = 989, right = 1191),
            OcrTextLine("VIN CONSEILLE", top = 1169, left = 1454, bottom = 1217, right = 1781)
        )

        val (banners, body) = Ocr2DSpatialLayoutSorter.extractTitleBanners(linesMelon)
        val bannerTexts = banners.map { it.text }
        val bodyTexts = body.map { it.text }

        assertWithMessage("Les bannières de titre doivent contenir 'Büchettes de melon' et 'au jambon italien et au porto'")
            .that(bannerTexts)
            .containsExactly("Büchettes de melon", "au jambon italien et au porto")

        assertWithMessage("Le corps de texte doit conserver l'instruction large '• Pelez le melon...'")
            .that(bodyTexts)
            .contains("• Pelez le melon, épépinez-le et")

        assertWithMessage("Le corps de texte doit conserver l'ingrédient 'I melon Galia'")
            .that(bodyTexts)
            .contains("I melon Galia")

        assertWithMessage("Le corps de texte doit conserver l'en-tête de vin 'VIN CONSEILLE'")
            .that(bodyTexts)
            .contains("VIN CONSEILLE")
    }
}
