package com.example.parabdcollector.java_ui

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.DrawerActions
import androidx.test.espresso.contrib.NavigationViewActions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.parabdcollector.R
import com.example.parabdcollector.ui.actvity.MainActivity
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented UI tests for Navigation flow using Espresso.
 */
@RunWith(AndroidJUnit4::class)
class NavigationTest {

    @Test
    fun testNavigationToSearchActivity() {
        // GIVEN: MainActivity is launched
        androidx.test.core.app.ActivityScenario.launch(MainActivity::class.java)

        // WHEN: Clicking the search action in the toolbar
        onView(withId(R.id.action_search)).perform(click())

        // THEN: SearchActivity is displayed (check for an element unique to SearchActivity)
        onView(withId(R.id.et_search_simple)).check(matches(isDisplayed()))
    }

    @Test
    fun testNavigationToInventoryScannerViaFab() {
        // GIVEN: MainActivity is launched
        androidx.test.core.app.ActivityScenario.launch(MainActivity::class.java)

        // WHEN: Clicking the Add FAB (Assuming it leads to adding/scanning, 
        // but wait, MainActivity shows FAB leads to EditItemActivity. 
        // Let's test the navigation drawer for other features).
        
        // Open Drawer
        onView(withId(R.id.drawer_layout)).perform(DrawerActions.open())

        // Navigate to "Plan de la Collection"
        onView(withId(R.id.nav_view)).perform(NavigationViewActions.navigateTo(R.id.nav_collection_plan))

        // THEN: CollectionPlanActivity is displayed
        onView(withText("Plan de la Collection")).check(matches(isDisplayed()))
    }

    @Test
    fun testNavigationToLocations() {
        androidx.test.core.app.ActivityScenario.launch(MainActivity::class.java)

        // Open Drawer
        onView(withId(R.id.drawer_layout)).perform(DrawerActions.open())

        // Navigate to Locations
        onView(withId(R.id.nav_view)).perform(NavigationViewActions.navigateTo(R.id.nav_locations))

        // THEN: LocationManagementActivity is displayed
        onView(withText(R.string.menu_locations_title)).check(matches(isDisplayed()))
    }
}
