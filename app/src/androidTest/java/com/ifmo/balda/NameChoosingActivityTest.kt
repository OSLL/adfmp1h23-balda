package com.ifmo.balda

import android.content.Context
import android.content.Intent
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
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.ifmo.balda.activity.ChooseNameActivity
import com.ifmo.balda.activity.currentLanguage
import com.ifmo.balda.model.data.dictionaries
import com.ifmo.balda.model.Difficulty
import com.ifmo.balda.model.GameMode
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NameChoosingActivityTest {
  private val context
    get() = InstrumentationRegistry.getInstrumentation().targetContext

  private val preferences
    get() = context.getSharedPreferences(
      PreferencesKeys.preferencesFileKey,
      Context.MODE_PRIVATE
    )

  @Before
  fun setup() {
    clearPreferences()
  }

  @Test
  fun testSinglePlayer(): Unit = launch(GameMode.SINGLE_PLAYER).use {
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
  fun testMultiplayer(): Unit = launch(GameMode.MULTIPLAYER).use {
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

    launch(GameMode.SINGLE_PLAYER).use {
      onView(withId(R.id.player1Name))
        .perform(replaceText(nameToCheck))
      onView(withId(R.id.playButton))
        .perform(click())
    }

    preferences.edit().remove(PreferencesKeys.singlePlayerSavedGame).apply()

    launch(GameMode.SINGLE_PLAYER).use {
      onView(withId(R.id.player1Name))
        .check(matches(withText(nameToCheck)))
    }
  }

  @Test
  fun testMultiPlayerNamePersists() {
    val nameToCheck1 = "TestPersist1"
    val nameToCheck2 = "TestPersist2"

    launch(GameMode.MULTIPLAYER).use {
      onView(withId(R.id.player1Name))
        .perform(replaceText(nameToCheck1))
      onView(withId(R.id.player2Name))
        .perform(replaceText(nameToCheck2))
      onView(withId(R.id.playButton))
        .perform(click())
    }

    preferences.edit().remove(PreferencesKeys.multiPlayerSavedGame).apply()

    launch(GameMode.MULTIPLAYER).use {
      onView(withId(R.id.player1Name))
        .check(matches(withText(nameToCheck1)))
      onView(withId(R.id.player2Name))
        .check(matches(withText(nameToCheck2)))
    }
  }

  private fun launch(mode: GameMode): ActivityScenario<ChooseNameActivity> =
    ActivityScenario.launch(getIntent(mode))

  private fun getIntent(mode: GameMode) = Intent(context, ChooseNameActivity::class.java).apply {
    putExtra(IntentExtraNames.GAME_MODE, mode.name)
    putExtra(IntentExtraNames.TOPIC, context.dictionaries.topics(currentLanguage(context.prefs)).first().name(context))
    putExtra(IntentExtraNames.DIFFICULTY, Difficulty.EASY.name)
  }

  private fun clearPreferences(): Unit = preferences.edit().clear().apply()
}
