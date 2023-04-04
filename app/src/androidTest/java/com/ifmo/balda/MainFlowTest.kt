package com.ifmo.balda

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ifmo.balda.activity.MainActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainFlowTest {
  @get:Rule
  val activityRule = ActivityScenarioRule(MainActivity::class.java)

  @Test
  fun testSinglePlayer() {
    onView(withId(R.id.startGame1PlayerButton)).perform(click())
    onView(withId(R.id.player1Name)).perform(replaceText("Test"))
    onView(withId(R.id.playButton)).perform(click())

    onView(withId(R.id.p1_name)).check(matches(withText("Test")))
    onView(withId(R.id.p2_name)).check(matches(withText(R.string.bot)))
  }

  @Test
  fun testMultiPlayer() {
    onView(withId(R.id.startGame2PlayerButton)).perform(click())
    onView(withId(R.id.player1Name)).perform(replaceText("Test1"))
    onView(withId(R.id.player2Name)).perform(replaceText("Test2"))
    onView(withId(R.id.playButton)).perform(click())

    onView(withId(R.id.p1_name)).check(matches(withText("Test1")))
    onView(withId(R.id.p2_name)).check(matches(withText("Test2")))
  }
}
