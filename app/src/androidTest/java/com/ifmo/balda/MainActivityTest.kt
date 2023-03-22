package com.ifmo.balda

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivityTest {
  @Test
  fun testDifficultyButtons() {
    val buttonIdToResourceId = mapOf(
      Pair(R.id.easyDifficultyButton, R.string.easy),
      Pair(R.id.mediumDifficultyButton, R.string.medium),
      Pair(R.id.hardDifficultyButton, R.string.hard)
    )

    for ((viewId, resourceId) in buttonIdToResourceId) {
      onView(withId(viewId)).perform(click())
      onView(withId(R.id.selectedDifficulty)).check(matches(withText(resourceId)))
    }
  }
}
