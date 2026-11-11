package com.chefinventory.app.utils.rules

import com.chefinventory.app.utils.IngredientParser
import com.chefinventory.app.utils.OcrVocabularyLoader
import com.google.common.truth.Truth.assertWithMessage
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.Test
import java.io.File

/**
 * Test de règle de non-régression (Type A) dédié à la prévention de la pollution du cache
 * [IngredientParser.defaultUnits] en cas d'erreur de chargement I/O initiale.
 */
class IngredientParserDefaultUnitsCachePollutionRuleTest {

    @Test
    @CoveredRequirement("6.1.1.9")
    fun `loadDefaultUnits on invalid directory should not pollute defaultUnits cache`() {
        OcrVocabularyLoader.clearCache()

        // 1. Invoquer le chargement sur un répertoire isolé sans fichiers XML
        val isolatedDir = File(System.getProperty("java.io.tmpdir"), "isolated_test_dir_parser_rule_999")
        isolatedDir.mkdirs()
        val emptyUnits = IngredientParser.loadDefaultUnits(isolatedDir)
        assertWithMessage("Le chargement d'unités sur un dossier sans XML doit retourner une liste vide")
            .that(emptyUnits)
            .isEmpty()

        isolatedDir.delete()

        // 2. Vérifier que la propriété defaultUnits parvient toujours à charger les unités de production
        val result = IngredientParser.parse("250 g de farine")

        assertWithMessage("L'unité de mesure 'g' doit être extraite malgré l'échec initial du dossier isolé")
            .that(result.unit)
            .isEqualTo("g")

        assertWithMessage("Le nom de l'ingrédient 'farine' doit être extrait")
            .that(result.name)
            .isEqualTo("farine")

        assertWithMessage("La quantité '250.0' doit être extraite")
            .that(result.quantity)
            .isEqualTo(250.0)
    }
}
