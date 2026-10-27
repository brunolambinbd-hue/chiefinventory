package com.example.chiefinventory.ui.actvity

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.chiefinventory.R
import com.google.common.truth.Truth.assertThat
import org.hamcrest.Matchers.containsString
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Suite de tests d'intégration End-User instrumentés (Espresso & Device UI)
 * pour l'écran d'initialisation du garde-manger [SetupInventoryActivity].
 */
@RunWith(AndroidJUnit4::class)
class SetupInventoryActivityEndUserTest {

    @Test
    fun testSetupInventoryUIElementsAreDisplayed() {
        val scenario = ActivityScenario.launch(SetupInventoryActivity::class.java)

        // Vérification de la présence de la barre de recherche
        onView(withId(R.id.etSearchCatalog)).check(matches(isDisplayed()))

        // Vérification de la présence des onglets
        onView(withId(R.id.tabLocations)).check(matches(isDisplayed()))

        // Vérification du bouton flottant de validation
        onView(withId(R.id.fabValidateStock)).check(matches(isDisplayed()))

        scenario.close()
    }

    @Test
    fun testSearchCatalogFiltering() {
        val scenario = ActivityScenario.launch(SetupInventoryActivity::class.java)

        // Saisie de "beurre" dans la barre de recherche
        onView(withId(R.id.etSearchCatalog)).perform(typeText("beurre"))

        // Vérification que le texte de recherche est saisi
        onView(withId(R.id.etSearchCatalog)).check(matches(withText("beurre")))

        scenario.close()
    }
}
