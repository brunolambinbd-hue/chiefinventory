package com.chefinventory.app.model

import com.google.common.truth.Truth.assertThat
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.Test

/**
 * Tests unitaires de Type B vérifiant le comportement et l'exhaustivité de [OpenFoodFactsProduct].
 */
class OpenFoodFactsProductTest {

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testOpenFoodFactsProduct_DefaultConstructor() {
        val product = OpenFoodFactsProduct(barcode = "3017620422003")

        assertThat(product.barcode).isEqualTo("3017620422003")
        assertThat(product.productName).isNull()
        assertThat(product.brand).isNull()
        assertThat(product.quantityStr).isNull()
        assertThat(product.nutriScore).isNull()
        assertThat(product.imageUrl).isNull()
        assertThat(product.ingredientsText).isNull()
        assertThat(product.timestampAdded).isGreaterThan(0L)
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testOpenFoodFactsProduct_FullConstructorAndGeneratedMethods() {
        val prod1 = OpenFoodFactsProduct(
            barcode = "3017620422003",
            productName = "Nutella",
            brand = "Ferrero",
            quantityStr = "400 g",
            nutriScore = "E",
            imageUrl = "https://images.openfoodfacts.org/nutella.jpg",
            ingredientsText = "Sucre, huile de palme, noisettes 13%, cacao maigre 7.4%",
            timestampAdded = 1000L
        )

        val prod2 = prod1.copy()
        val prod3 = prod1.copy(productName = "Nutella Biscuits")

        assertThat(prod1).isEqualTo(prod2)
        assertThat(prod1).isNotEqualTo(prod3)
        assertThat(prod1.hashCode()).isEqualTo(prod2.hashCode())
        assertThat(prod1.toString()).contains("OpenFoodFactsProduct")

        assertThat(prod1.component1()).isEqualTo("3017620422003")
        assertThat(prod1.component2()).isEqualTo("Nutella")
        assertThat(prod1.component3()).isEqualTo("Ferrero")
        assertThat(prod1.component4()).isEqualTo("400 g")
        assertThat(prod1.component5()).isEqualTo("E")
        assertThat(prod1.component6()).isEqualTo("https://images.openfoodfacts.org/nutella.jpg")
        assertThat(prod1.component7()).isEqualTo("Sucre, huile de palme, noisettes 13%, cacao maigre 7.4%")
        assertThat(prod1.component8()).isEqualTo(1000L)
    }
}
