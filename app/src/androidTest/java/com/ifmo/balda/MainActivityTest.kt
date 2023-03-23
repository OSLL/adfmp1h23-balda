package com.ifmo.balda

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers.isClickable
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.ifmo.balda.activity.HelpScreenActivity
import com.ifmo.balda.activity.MainActivity
import com.ifmo.balda.activity.StatScreenActivity
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivityTest {
  @get:Rule
  val activityRule = ActivityScenarioRule(MainActivity::class.java)

  companion object {
    @BeforeClass
    @JvmStatic
    fun init() {
      Intents.init()
    }
  }
  @Test
  fun testStatsButton() {
    onView(withId(R.id.statButton))
      .check(matches(isDisplayed()))
      .check(matches(isClickable()))
      .perform(click())
    intended(hasComponent(StatScreenActivity::class.java.name))
  }

  @Test
  fun testHelpButton() {
    onView(withId(R.id.helpButton))
      .check(matches(isDisplayed()))
      .perform(click())
    intended(hasComponent(HelpScreenActivity::class.java.name))
  }

  @Test
  fun testPlayButtons() {
    val playButtons = listOf(R.id.startGame1PlayerButton, R.id.startGame2PlayerButton)

    for (buttonId in playButtons) {
      onView(withId(buttonId))
        .check(matches(isDisplayed()))
        .check(matches(isClickable()))
    }
  }

  fun testTopicSelector() {
    onView(withId(R.id.topicSelector))
      .check(matches(isDisplayed()))
      .check(matches(isClickable()))
      // TODO: probably find a way to check content
  }

  @Test
  fun testDifficultyButtons() {
    val buttonIdToResourceId = mapOf(
      R.id.easyDifficultyButton to R.string.easy,
      R.id.mediumDifficultyButton to R.string.medium,
      R.id.hardDifficultyButton to R.string.hard
    )

    for ((viewId, resourceId) in buttonIdToResourceId) {
      onView(withId(viewId))
        .check(matches(isDisplayed()))
        .check(matches(isClickable()))
        .perform(click())
      onView(withId(R.id.selectedDifficulty)).check(matches(withText(resourceId)))
    }
  }
}
