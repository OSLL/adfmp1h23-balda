package com.ifmo.balda

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isClickable
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ifmo.balda.activity.HelpScreenActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HelpActivityTest {
  @get:Rule
  val activityRule = ActivityScenarioRule(HelpScreenActivity::class.java)

  @Test
  fun testHelpScreenText() {
    onView(withId(R.id.helpText))
      .check(matches(isDisplayed()))
      .check(matches(withText(R.string.game_description)))
  }

  @Test
  fun testBackButton() {
    onView(withId(R.id.backButton))
      .check(matches(isDisplayed()))
      .check(matches(isClickable()))
      .perform(click())
  }
}
