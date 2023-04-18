package com.ifmo.balda

import android.content.Context
import android.content.SharedPreferences
import android.view.View
import android.widget.TextView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.VerificationMode
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.platform.app.InstrumentationRegistry
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher

fun withTooltip(str: String): Matcher<View> = object : TypeSafeMatcher<View>() {
  override fun describeTo(description: Description) = Unit
  override fun matchesSafely(item: View): Boolean = item.tooltipText == str
}

fun withTooltip(resourceId: Int): Matcher<View> = withTooltip(
  InstrumentationRegistry.getInstrumentation().targetContext.getString(resourceId)
)

fun withAnyText(): Matcher<View> = object : TypeSafeMatcher<View> () {
  override fun describeTo(description: Description) = Unit
  override fun matchesSafely(item: View): Boolean = item is TextView && item.text.isNotEmpty()
}

fun waitViewShown(matcher: Matcher<View>) {
  val idlingResource = ViewShownIdlingResource(matcher)
  try {
    IdlingRegistry.getInstance().register(idlingResource)
    onView(matcher).check(matches(isDisplayed()))
    onView(withId(0)).check(doesNotExist())
  } finally {
    IdlingRegistry.getInstance().unregister(idlingResource)
  }
}

fun last(): VerificationMode = VerificationMode { matcher, recordedIntents ->
  val last = recordedIntents.last()

  if (matcher.matches(last)) {
    last.markAsVerified()
  }
}

val context: Context
  get() = InstrumentationRegistry.getInstrumentation().targetContext

val Context.prefs: SharedPreferences
  get() = getSharedPreferences(PreferencesKeys.preferencesFileKey, Context.MODE_PRIVATE)
