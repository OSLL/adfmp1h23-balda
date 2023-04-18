package com.ifmo.balda

import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ifmo.balda.activity.GameActivity
import com.ifmo.balda.activity.PauseActivity
import com.ifmo.balda.activity.currentLanguage
import com.ifmo.balda.model.Difficulty
import com.ifmo.balda.model.GameMode
import com.ifmo.balda.model.data.dictionaries
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GameActivityTest {
  companion object {
    private const val player1Name = "P1"
    private const val player2Name = "P2"
  }

  @Before
  fun setup() {
    Intents.init()
  }

  @After
  fun teardown() {
    Intents.release()
  }

  @Test
  fun testPlayerNames(): Unit = launch(GameMode.SINGLE_PLAYER).use {
    onView(withId(R.id.p1_name)).check(matches(withText(player1Name)))
    onView(withId(R.id.p2_name)).check(matches(withText(player2Name)))
  }

  @Test
  fun testPlayerScore(): Unit = launch(GameMode.SINGLE_PLAYER).use {
    onView(withId(R.id.p1_score)).check(matches(withText("0")))
    onView(withId(R.id.p2_score)).check(matches(withText("0")))
  }

  @Test
  fun testCurrentPlayerName(): Unit = launch(GameMode.SINGLE_PLAYER).use {
    onView(withId(R.id.current_player)).check(matches(withText(player1Name)))
  }

  @Test
  fun testPassTurnButton(): Unit = launch(GameMode.SINGLE_PLAYER).use {
    onView(withId(R.id.pass_button)).perform(click())
    onView(withId(R.id.current_player)).check(matches(withText(player2Name)))
  }

  @Test
  fun testPauseResume() = launch(GameMode.SINGLE_PLAYER).use {
    onView(withId(R.id.pause_button)).perform(click())
    intended(hasComponent(PauseActivity::class.java.name))
    // TODO: check name, maybe time
    onView(withId(R.id.resumeButton)).perform(click())
    intended(hasComponent(GameActivity::class.java.name))
  }

  private fun launch(mode: GameMode): ActivityScenario<GameActivity> = ActivityScenario.launch(getIntent(mode))

  private fun getIntent(mode: GameMode) = Intent(context, GameActivity::class.java).apply {
    putExtra(IntentExtraNames.PLAYER_1_NAME, player1Name)
    putExtra(IntentExtraNames.PLAYER_2_NAME, player2Name)
    putExtra(IntentExtraNames.GAME_MODE, mode.name)
    putExtra(IntentExtraNames.TOPIC, context.dictionaries.topics(context.currentLanguage(context.prefs)).first().name(context))
    putExtra(IntentExtraNames.DIFFICULTY, Difficulty.EASY.name)
  }
}
