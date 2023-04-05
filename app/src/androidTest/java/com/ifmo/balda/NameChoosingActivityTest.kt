package com.ifmo.balda

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.isClickable
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ifmo.balda.activity.MainActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NameChoosingActivityTest {
  @get:Rule
  val activityRule = ActivityScenarioRule(MainActivity::class.java)

  @Test
  fun testSinglePlayer() {
    openSinglePlayerScreen()

    onView(withId(R.id.player1Name))
      .check(matches(isDisplayed()))
      .check(matches(withAnyText()))
    onView(withId(R.id.player2Name))
      .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)))
    onView(withId(R.id.playButton))
      .check(matches(isDisplayed()))
      .check(matches(isClickable()))
      .perform(click())
  }

  @Test
  fun testMultiplayer() {
    openMultiPlayerScreen()

    onView(withId(R.id.player1Name))
      .check(matches(isDisplayed()))
      .check(matches(withAnyText()))
    onView(withId(R.id.player2Name))
      .check(matches(isDisplayed()))
      .check(matches(withAnyText()))
    onView(withId(R.id.playButton))
      .check(matches(isDisplayed()))
      .check(matches(isClickable()))
      .perform(click())
  }

  @Test
  fun testSinglePlayerNamePersists() {
    val nameToCheck = "TestPersist"

    openSinglePlayerScreen()
    onView(withId(R.id.player1Name))
      .perform(replaceText(nameToCheck))
    onView(withId(R.id.playButton))
      .perform(click())
    reopenApp()
    openSinglePlayerScreen()

    onView(withId(R.id.player1Name))
      .check(matches(withText(nameToCheck)))
  }

  @Test
  fun testMultiPlayerNamePersists() {
    val nameToCheck1 = "TestPersist1"
    val nameToCheck2 = "TestPersist2"

    openMultiPlayerScreen()
    onView(withId(R.id.player1Name))
      .perform(replaceText(nameToCheck1))
    onView(withId(R.id.player2Name))
      .perform(replaceText(nameToCheck2))
    onView(withId(R.id.playButton))
      .perform(click())
    reopenApp()
    openMultiPlayerScreen()

    onView(withId(R.id.player1Name))
      .check(matches(withText(nameToCheck1)))
    onView(withId(R.id.player2Name))
      .check(matches(withText(nameToCheck2)))
  }

  private fun openSinglePlayerScreen() {
    onView(withId(R.id.startGame1PlayerButton))
      .perform(click())
  }

  private fun openMultiPlayerScreen() {
    onView(withId(R.id.startGame2PlayerButton))
      .perform(click())
  }

  private fun reopenApp() {
    activityRule.scenario.close()
    ActivityScenario.launch(MainActivity::class.java)
  }
}
