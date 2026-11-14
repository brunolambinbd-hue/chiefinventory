package com.example.chiefinventory.java_integration.utils

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.chefinventory.app.R
import com.google.common.truth.Truth.assertWithMessage
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Test instrumenté exécuté directement sur l'Émulateur Android (ou device physique)
 * interrogeant directement le binaire [android.content.res.Resources] du package installé.
 */
@RunWith(AndroidJUnit4::class)
class EmulatorXmlResourceTest {

    @Test
    fun testEmulatorResources_CommonIngredientsNoQty_ContainsThymEtRomarinFrais() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val commonIngrs = appContext.resources.getStringArray(R.array.common_ingredients_no_qty)

        System.out.println("EMULATOR RESOURCE TEST: Taille du tableau R.array.common_ingredients_no_qty = ${commonIngrs.size}")

        assertWithMessage("Le binaire R.array.common_ingredients_no_qty sur l'Émulateur ne doit pas être vide")
            .that(commonIngrs)
            .isNotEmpty()

        assertWithMessage("Le binaire R.array.common_ingredients_no_qty sur l'Émulateur doit contenir 'du thym et du romarin frais'")
            .that(commonIngrs)
            .asList()
            .contains("du thym et du romarin frais")
    }
}
