package com.chefinventory.app.utils.rules

import com.chefinventory.app.utils.StandardCatalogLoader
import com.google.common.truth.Truth.assertWithMessage
import org.junit.Test

/**
 * Suite de tests unitaires de Niveau A vérifiant le parsing et le chargement du catalogue d'ingrédients types
 * d'après la provenance Open Food Facts & ANSES CIQUAL.
 */
class StandardCatalogLoaderRuleTest {

    @Test
    fun `parseRawCatalogLine should parse pipe separated line into StandardCatalogItem`() {
        val raw = "Chicons / Endives|1|kg|Légumes frais"
        val item = StandardCatalogLoader.parseRawCatalogLine(raw, "Frigo")

        assertWithMessage("Le parsing de la ligne du catalogue ne doit pas retourner null")
            .that(item)
            .isNotNull()

        assertWithMessage("Le nom de l'ingrédient doit être 'Chicons / Endives'")
            .that(item!!.name)
            .isEqualTo("Chicons / Endives")

        assertWithMessage("La quantité par défaut doit être 1.0")
            .that(item.defaultQuantity)
            .isEqualTo(1.0)

        assertWithMessage("L'unité par défaut doit être 'kg'")
            .that(item.defaultUnit)
            .isEqualTo("kg")

        assertWithMessage("La catégorie doit être 'Légumes frais'")
            .that(item.category)
            .isEqualTo("Légumes frais")

        assertWithMessage("L'emplacement par défaut doit être 'Frigo'")
            .that(item.defaultLocationName)
            .isEqualTo("Frigo")
    }

    @Test
    fun `parseRawCatalogLine should handle missing optional fields with defaults`() {
        val raw = "Beurre doux"
        val item = StandardCatalogLoader.parseRawCatalogLine(raw, "Frigo")

        assertWithMessage("Le parsing d'un nom simple ne doit pas retourner null")
            .that(item)
            .isNotNull()

        assertWithMessage("Le nom de l'ingrédient doit être 'Beurre doux'")
            .that(item!!.name)
            .isEqualTo("Beurre doux")

        assertWithMessage("La quantité par défaut doit valoir 1.0")
            .that(item.defaultQuantity)
            .isEqualTo(1.0)

        assertWithMessage("L'unité par défaut doit être null en l'absence de valeur")
            .that(item.defaultUnit)
            .isNull()

        assertWithMessage("La catégorie par défaut doit être 'Épicerie'")
            .that(item.category)
            .isEqualTo("Épicerie")

        assertWithMessage("L'emplacement par défaut doit être 'Frigo'")
            .that(item.defaultLocationName)
            .isEqualTo("Frigo")
    }
}
