package com.chefinventory.app.utils.rules

import com.chefinventory.app.utils.IngredientParser
import com.google.common.truth.Truth.assertWithMessage
import org.junit.Test

class IngredientParserRuleTest {

    @Test
    fun `parse should extract standard quantity, unit and name`() {
        val result = IngredientParser.parse("200 g de farine")
        assertWithMessage("La quantité '200.0' doit être extraite")
            .that(result.quantity)
            .isEqualTo(200.0)

        assertWithMessage("L'unité de mesure 'g' doit être extraite")
            .that(result.unit)
            .isEqualTo("g")

        assertWithMessage("Le nom 'farine' doit être extrait")
            .that(result.name)
            .isEqualTo("farine")
    }

    @Test
    fun `parse should extract parenthetical information`() {
        val result = IngredientParser.parse("3 oeufs (gros calibre)")
        assertWithMessage("La quantité '3.0' doit être extraite")
            .that(result.quantity)
            .isEqualTo(3.0)

        assertWithMessage("L'unité doit être null pour '3 oeufs'")
            .that(result.unit)
            .isNull()

        assertWithMessage("Le nom 'oeufs' doit être extrait")
            .that(result.name)
            .isEqualTo("oeufs")

        assertWithMessage("L'information supplémentaire entre parenthèses 'gros calibre' doit être extraite")
            .that(result.supplementalInfo)
            .isEqualTo("gros calibre")
    }

    @Test
    fun `parse should handle fractions`() {
        val result = IngredientParser.parse("1/2 litre de lait")
        assertWithMessage("La fraction '1/2' doit être convertie en 0.5")
            .that(result.quantity)
            .isEqualTo(0.5)

        assertWithMessage("L'unité 'litre' doit être extraite")
            .that(result.unit)
            .isEqualTo("litre")

        assertWithMessage("Le nom 'lait' doit être extrait")
            .that(result.name)
            .isEqualTo("lait")
    }

    @Test
    fun `parse should preserve parenthetical weight range as supplementalInfo`() {
        val result = IngredientParser.parse("2 ou 3 paquets de jambon de Parme en fines tranches (125 à 180 g)")
        assertWithMessage("La quantité maximale de la plage '3.0' doit être extraite")
            .that(result.quantity)
            .isEqualTo(3.0)

        assertWithMessage("L'unité 'paquets' doit être extraite")
            .that(result.unit)
            .isEqualTo("paquets")

        assertWithMessage("Le nom doit contenir 'jambon de Parme'")
            .that(result.name)
            .contains("jambon de Parme")

        assertWithMessage("La plage de poids entre parenthèses '(125 à 180 g)' doit être conservée")
            .that(result.supplementalInfo)
            .isEqualTo("125 à 180 g")
    }
}
