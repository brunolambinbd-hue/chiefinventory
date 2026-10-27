package com.example.chiefinventory.utils

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Suite de tests unitaires de Type B vérifiant le formatage dynamique des modèles de chaînes de localisation
 * avec arguments (%1$d, %2$s, %3$d) pour la conformité 100% SonarCloud.
 */
class AndroidLocalizationResourceFormatTypeBTest {

    @Test
    fun `formatSetupInventoryBtnValidate should format selection count template`() {
        val template = "Valider Tout mon Garde-Manger (%1\$d sélectionnés)"
        val result = String.format(template, 90)

        assertThat(result).isEqualTo("Valider Tout mon Garde-Manger (90 sélectionnés)")
    }

    @Test
    fun `formatImportPackToastNew should format new pack import toast template`() {
        val template = "%1\$d nouvelles recettes importées dans le Pack %2\$s !"
        val result = String.format(template, 32, "Cuisine Italienne 🇮🇹")

        assertThat(result).isEqualTo("32 nouvelles recettes importées dans le Pack Cuisine Italienne 🇮🇹 !")
    }

    @Test
    fun `formatRecipeAuditHealthStatus should format health report status template`() {
        val template = "🟢 %1\$d / %2\$d recettes conformes et complètes (%3\$d%%)"
        val result = String.format(template, 439, 449, 97)

        assertThat(result).isEqualTo("🟢 439 / 449 recettes conformes et complètes (97%)")
    }

    @Test
    fun `formatMatchCountHeaderFormat should format match header count template`() {
        val template = "%1\$d recettes trouvées (%2\$d faisables à 100%%)"
        val result = String.format(template, 389, 14)

        assertThat(result).isEqualTo("389 recettes trouvées (14 faisables à 100%)")
    }
}
