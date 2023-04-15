package com.ifmo.balda.testing

import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.core.view.get
import androidx.test.espresso.NoMatchingViewException
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.ViewAssertion
import androidx.test.espresso.action.*
import androidx.test.espresso.matcher.ViewMatchers
import com.ifmo.balda.adapter.BoardGridAdapter
import com.ifmo.balda.model.Board
import com.ifmo.balda.unreachable
import com.ifmo.balda.view.InterceptingGridView
import org.hamcrest.Matcher
import org.hamcrest.Matchers


class PressReleaseActionContext(private var downEvent: MotionEvent? = null) {
  fun down(downEvent: MotionEvent) {
    require(this.downEvent == null)
    this.downEvent = downEvent
  }

  fun up(): MotionEvent {
    return downEvent ?: error("Can't send 'up' without prior 'down'")
  }
}

class PressAction(private val pos: Int, private val context: PressReleaseActionContext): ViewAction {
  override fun getDescription(): String {
    return "Presses the button and holds"
  }

  override fun getConstraints(): Matcher<View> {
    // as in GeneralClickAction
    return ViewMatchers.isDisplayingAtLeast(90)
  }

  override fun perform(uiController: UiController, view: View) {
    view as InterceptingGridView
    val coordinates = GeneralLocation.CENTER.calculateCoordinates(view[pos])
    val precision = Press.FINGER.describePrecision()
    val down = MotionEvents.sendDown(uiController, coordinates, precision).down
    context.down(down)
  }
}

class ReleaseAction(private val pos: Int, private val context: PressReleaseActionContext): ViewAction {
  override fun getDescription(): String {
    return "Releases the button (requires it to be pressed)"
  }

  override fun getConstraints(): Matcher<View> {
    // as in GeneralClickAction
    return ViewMatchers.isDisplayingAtLeast(90)
  }

  override fun perform(uiController: UiController, view: View) {
    view as InterceptingGridView
    val coordinates = GeneralLocation.CENTER.calculateCoordinates(view[pos])
//    val coordinates = GeneralLocation.CENTER.calculateCoordinates(actualView)
    MotionEvents.sendUp(uiController, context.up(), coordinates)
    uiController.loopMainThreadForAtLeast(1000);
  }
}

//class SwipeFromToAction(private val posFrom: Int, private val posTo: Int): ViewAction {
//  override fun getDescription(): String {
//    TODO("Not yet implemented")
//  }
//
//  override fun getConstraints(): Matcher<View> {
//    TODO("Not yet implemented")
//  }
//
//  override fun perform(uiController: UiController, view: View) {
//    MotionEvents.sendMovement(§)
//  }
//
//}

// todo: add tests for fast movement to a distant cell, to test that continues working
class SelectCellsAction(
  start: Pair<Int, Int>,
  vararg directions: Int
): ViewAction {
  private val trajectory: List<Pair<Int, Int>> =
    directions.fold(mutableListOf(start)) { trajectory, direction ->
      val (lastY, lastX) = trajectory.last()
      trajectory += when (direction) {
        Board.UP -> lastY - 1 to lastX
        Board.DOWN -> lastY + 1 to lastX
        Board.LEFT -> lastY to lastX - 1
        Board.RIGHT -> lastY to lastX + 1
        else -> unreachable()
      }
      trajectory
    }

  override fun getDescription(): String {
    return "TODO('Not yet implemented')"
  }

  override fun getConstraints(): Matcher<View> {
    return Matchers.allOf(
      ViewMatchers.isAssignableFrom(InterceptingGridView::class.java),
      ViewMatchers.isDisplayingAtLeast(90),
    )
  }

  override fun perform(uiController: UiController, view: View) {
    view as InterceptingGridView
    val boardAdapter = view.adapter as BoardGridAdapter

    require(trajectory.maxOf { it.first } >= 0)
    require(trajectory.maxOf { it.first } < boardAdapter.n)
    require(trajectory.maxOf { it.second } >= 0)
    require(trajectory.maxOf { it.second } < boardAdapter.n)

    fun Pair<Int, Int>.toIndex() = first * boardAdapter.n + second

    val downEvent = run {
      val coordinates = GeneralLocation.CENTER.calculateCoordinates(view[trajectory.first().toIndex()])
      val precision = Press.FINGER.describePrecision()
      MotionEvents.sendDown(uiController, coordinates, precision).down
    }

    for (cell in trajectory.drop(1)) {
      val coordinates = GeneralLocation.CENTER.calculateCoordinates(view[cell.toIndex()])
      val success = MotionEvents.sendMovement(uiController, downEvent, coordinates)
      assert(success)
      uiController.loopMainThreadForAtLeast(100)
    }

    MotionEvents.sendUp(uiController, downEvent, GeneralLocation.CENTER.calculateCoordinates(view[trajectory.last().toIndex()]))
  }
}

class BoardAssertion : ViewAssertion {
  override fun check(view: View?, noViewFoundException: NoMatchingViewException?) {
    @Suppress("NAME_SHADOWING") val view =
      noViewFoundException?.let { throw it } ?: view!! as InterceptingGridView


  }
}
