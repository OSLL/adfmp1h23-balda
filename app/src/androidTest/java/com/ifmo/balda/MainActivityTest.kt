package com.ifmo.balda

import android.content.Context
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onData
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers.isClickable
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withSpinnerText
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.ifmo.balda.activity.ChooseNameActivity
import com.ifmo.balda.activity.GameActivity
import com.ifmo.balda.activity.HelpScreenActivity
import com.ifmo.balda.activity.MainActivity
import com.ifmo.balda.activity.StatScreenActivity
import com.ifmo.balda.activity.currentLanguage
import com.ifmo.balda.model.data.dictionaries
import org.hamcrest.CoreMatchers.instanceOf
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivityTest {
  @get:Rule
  val activityRule = ActivityScenarioRule(MainActivity::class.java)

  companion object {
    private val buttonIdToResourceId = mapOf(
      R.id.easyDifficultyButton to R.string.easy,
      R.id.mediumDifficultyButton to R.string.medium,
      R.id.hardDifficultyButton to R.string.hard
    )
  }

  @Before
  fun setup() {
    Intents.init()
    clearPreferences()
  }

  @After
  fun teardown() {
    Intents.release()
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

  @Test
  fun testTopicSelector() {
    val topicSelector = onView(withId(R.id.topicSelector))
    topicSelector
      .check(matches(isDisplayed()))
      .check(matches(isClickable()))

    val topics = context.dictionaries.topics(context.currentLanguage(context.prefs))
    for ((idx, topic) in topics.withIndex()) {
      topicSelector.perform(click())
      onData(instanceOf(MainActivity.TopicSelectorItem::class.java))
        .atPosition(idx)
        .perform(click())
      topicSelector.check(matches(withSpinnerText(topic.name(context))))
    }
  }

  @Test
  fun testDifficultyButtons() {
    for ((viewId, resourceId) in buttonIdToResourceId) {
      onView(withId(viewId))
        .check(matches(isDisplayed()))
        .check(matches(isClickable()))
        .perform(click())
      onView(withId(R.id.selectedDifficulty)).check(matches(withText(resourceId)))
    }
  }

  @Test
  fun testDifficultyTooltip() {
    onView(withId(R.id.difficultyHelpButton))
      .check(matches(isDisplayed()))
      .check(matches(isClickable()))
      .check(matches(withTooltip(R.string.difficulty_help)))
      .perform(click())
    // TODO: can we check that tooltip is actually shown
  }

  @Test
  fun testTopicTooltip() {
    onView(withId(R.id.topicHelpButton))
      .check(matches(isDisplayed()))
      .check(matches(isClickable()))
      .check(matches(withTooltip(R.string.topic_help)))
      .perform(click())
  }

  @Test
  fun testTopicPersists() {
    activityRule.scenario.close() // For this test we will manage activities manually

    val topics = context.dictionaries.topics(context.currentLanguage(context.prefs))
    for ((idx, topic) in topics.withIndex()) {
      launch().use {
        onView(withId(R.id.topicSelector)).perform(click())
        onData(instanceOf(MainActivity.TopicSelectorItem::class.java))
          .atPosition(idx)
          .perform(click())
      }

      launch().use {
        onView(withId(R.id.topicSelector))
          .check(matches(withSpinnerText(topic.name(context))))
      }
    }
  }

  @Test
  fun testDifficultyPersists() {
    activityRule.scenario.close() // For this test we will manage activities manually

    for ((buttonId, resourceId) in buttonIdToResourceId) {
      launch().use { onView(withId(buttonId)).perform(click()) }
      launch().use { onView(withId(R.id.selectedDifficulty)).check(matches(withText(resourceId))) }
    }
  }

  @Test
  fun testGameResumeDialogAppear() {
    triggerGameResumeDialog()
    onView(withText(R.string.resumeSavedGamePrompt))
      .check(matches(isDisplayed()))
  }

  @Test
  fun testGameResumeAgreed() {
    triggerGameResumeDialog()
    onView(withText(R.string.yes)).perform(click())
    intended(hasComponent(GameActivity::class.java.name), last())
  }

  @Test
  fun testGameResumeRejected() {
    triggerGameResumeDialog()
    onView(withText(R.string.no)).perform(click())
    intended(hasComponent(ChooseNameActivity::class.java.name), last())
  }

  @Test
  fun testGameModesSavedIndependently() {
    onView(withId(R.id.startGame1PlayerButton)).perform(click())
    onView(withId(R.id.playButton)).perform(click())
    waitViewShown(withId(R.id.board))
    reopenApp()
    onView(withId(R.id.startGame2PlayerButton)).perform(click())
    intended(hasComponent(ChooseNameActivity::class.java.name), last())
  }

  private fun clearPreferences() {
    InstrumentationRegistry.getInstrumentation().targetContext.getSharedPreferences(
      PreferencesKeys.preferencesFileKey,
      Context.MODE_PRIVATE
    ).edit().clear().apply()
  }

  private fun triggerGameResumeDialog() {
    onView(withId(R.id.startGame1PlayerButton)).perform(click())
    onView(withId(R.id.playButton)).perform(click())
    waitViewShown(withId(R.id.board))
    reopenApp()
    onView(withId(R.id.startGame1PlayerButton)).perform(click())
  }

  private fun launch() = ActivityScenario.launch(MainActivity::class.java)

  // Works only one time per test.
  // If you need reopen app multiple times do it via creating scenario manually for each time
  // @see [launch]
  private fun reopenApp(clearPrefs: Boolean = false) {
    activityRule.scenario.close()
    if (clearPrefs) clearPreferences()
    ActivityScenario.launch(MainActivity::class.java)
  }
}
