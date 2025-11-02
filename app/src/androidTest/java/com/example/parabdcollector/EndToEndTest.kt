package com.example.parabdcollector

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.example.parabdcollector.ui.MainActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest // On indique que c'est un test qui simule un parcours utilisateur complet.
class EndToEndTest {

    // Cette règle garantit que MainActivity est lancée avant chaque test.
    @get:Rule
    var activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun addItemAndCheckIfDisplayed() {
        val itemTitle = "Mon premier test Espresso"

        // 1. On clique sur le bouton "+" (Floating Action Button).
        onView(withId(R.id.fabAdd)).perform(click())

        // 2. On écrit le titre dans le champ de texte.
        onView(withId(R.id.etTitle)).perform(typeText(itemTitle))

        // 3. On clique sur le bouton "Enregistrer".
        onView(withId(R.id.btnSave)).perform(click())

        // 4. On vérifie que le texte de notre nouvel objet est bien affiché à l'écran.
        onView(withText(itemTitle)).check(matches(isDisplayed()))
    }
}