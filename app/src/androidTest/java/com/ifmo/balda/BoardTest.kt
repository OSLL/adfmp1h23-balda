package com.ifmo.balda

import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ifmo.balda.activity.GameActivity
import com.ifmo.balda.model.Board
import com.ifmo.balda.testing.SelectCellsAction
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BoardTest {
  @get:Rule
  val activityRule = ActivityScenarioRule<GameActivity>(
    Intent(ApplicationProvider.getApplicationContext(), GameActivity::class.java).apply {
      putExtra(IntentExtraNames.PLAYER_1_NAME, "Player 1")
      putExtra(IntentExtraNames.PLAYER_2_NAME, "Player 2")
    })

  @Test
  fun testHelpScreenText() {
    Espresso.onView(ViewMatchers.withId(R.id.board))
      .perform(SelectCellsAction(0 to 0, Board.RIGHT, Board.DOWN, Board.RIGHT))
      .check()
//      .check()

    Espresso.onView(ViewMatchers.withId(R.id.board))
      .perform(SelectCellsAction(1 to 1, Board.UP, Board.RIGHT, Board.RIGHT, Board.DOWN, Board.DOWN, Board.LEFT, Board.LEFT, Board.UP, Board.RIGHT, Board.RIGHT))
  }
}
