package com.ifmo.balda.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NavUtils
import com.ifmo.balda.R
import com.ifmo.balda.adapter.BoardGridAdapter
import com.ifmo.balda.view.InterceptingGridView

class GameActivity : AppCompatActivity() {
  private val n = 8 // Depends on difficulty?

  private val boardLetters = mutableListOf<String>()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_game)

    findViewById<Button>(R.id.menu_button).setOnClickListener {
      NavUtils.navigateUpTo(this, Intent(this, MainActivity::class.java))
    }

    fillBoardWithLetters()
    setUpBoardGrid()
  }

  private fun setUpBoardGrid() {
    val gridView = findViewById<InterceptingGridView>(R.id.board)
    gridView.numColumns = n
    val boardGridAdapter = BoardGridAdapter(layoutInflater, boardLetters, n)
    gridView.adapter = boardGridAdapter
  }

  private fun fillBoardWithLetters() {
    for (i in 0 until n) {
      for (j in 0 until n) {
        boardLetters.add(setLetterValue())
      }
    }
  }

  private fun setLetterValue() = "A"
}
