package com.ifmo.balda

import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.ifmo.balda.activity.GameActivity
import com.ifmo.balda.activity.PauseActivity
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GameActivityTest {
  private val context
    get() = InstrumentationRegistry.getInstrumentation().targetContext

  private val intent
    get() = Intent(context, GameActivity::class.java).apply {
      putExtra(IntentExtraNames.PLAYER_1_NAME, "P1")
      putExtra(IntentExtraNames.PLAYER_2_NAME, "P2")
    }

  companion object {
    @BeforeClass
    @JvmStatic
    fun init() {
      Intents.init()
    }

    @AfterClass
    @JvmStatic
    fun release() {
      Intents.release()
    }
  }

  @Test
  fun testPauseResume() = launch().use {
    onView(withId(R.id.pause_button)).perform(click())
    intended(hasComponent(PauseActivity::class.java.name))
    // TODO: check name, maybe time
    onView(withId(R.id.resumeButton)).perform(click())
    intended(hasComponent(GameActivity::class.java.name))
  }

  private fun launch(): ActivityScenario<GameActivity> = ActivityScenario.launch(intent)
}
