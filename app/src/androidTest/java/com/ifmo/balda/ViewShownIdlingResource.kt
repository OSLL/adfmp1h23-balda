package com.ifmo.balda

import android.view.View
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingResource
import androidx.test.espresso.ViewFinder
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import org.hamcrest.Matcher

class ViewShownIdlingResource(private val matcher: Matcher<View>) : IdlingResource {
  var callback: IdlingResource.ResourceCallback? = null

  companion object {
    private fun getView(viewMatcher: Matcher<View>): View? = try {
      val viewInteraction = onView(viewMatcher)
      val finderField = viewInteraction.javaClass.getDeclaredField("viewFinder")
      finderField.isAccessible = true
      val finder = finderField.get(viewInteraction) as ViewFinder
      finder.view
    } catch (e: Exception) {
      null
    }
  }

  override fun getName(): String = "$this $matcher"

  override fun registerIdleTransitionCallback(callback: IdlingResource.ResourceCallback) {
    this.callback = callback
  }

  override fun isIdleNow(): Boolean {
    val view = getView(matcher)
    val isIdle = isDisplayed().matches(view)

    if (isIdle) {
      this.callback?.onTransitionToIdle()
    }

    return isIdle
  }
}
