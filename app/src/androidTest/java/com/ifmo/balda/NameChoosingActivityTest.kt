package com.ifmo.balda

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
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
    onView(withId(R.id.startGame1PlayerButton)).perform(click())

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
    onView(withId(R.id.startGame2PlayerButton)).perform(click())

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
}
