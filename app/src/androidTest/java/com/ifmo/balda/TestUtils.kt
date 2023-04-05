package com.ifmo.balda

import android.view.View
import android.widget.TextView
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
