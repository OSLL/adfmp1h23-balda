package com.ifmo.balda.activity

import android.content.Intent
import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.View.OnClickListener
import android.widget.Button
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.ToggleButton
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NavUtils
import com.ifmo.balda.R

class GameActivity : AppCompatActivity() {
  private val n = 8 // Depends on difficulty?

  private val boardLetters = mutableListOf<MutableList<ToggleButton>>()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_game)

    findViewById<Button>(R.id.menu_button).setOnClickListener {
      NavUtils.navigateUpTo(this, Intent(this, MainActivity::class.java))
    }

    val tableLayout = findViewById<TableLayout>(R.id.board)

    for (i in 0 until n) {
      val row = TableRow(ContextThemeWrapper(this, R.style.letter_background))
      tableLayout.addView(row)

      val lettersRow = mutableListOf<ToggleButton>()
      for (j in 0 until n) {
        val letter = (layoutInflater.inflate(R.layout.letter, row, false) as ToggleButton).apply {
          setLetterValue()
          setOnClickListener(getOnLetterClickListener(this, i, j))
        }

        lettersRow.add(letter)
        row.addView(letter)
      }
      boardLetters.add(lettersRow)
    }
  }

  private fun ToggleButton.setLetterValue() {
    text = "A"
    textOn = "A"
    textOff = "A"
  }

  private fun getOnLetterClickListener(letter: Button, i: Int, j: Int): OnClickListener {
    return OnClickListener {
      for (row in boardLetters) {
        for (l in row) {
          l.isEnabled = false
        }
      }

      // Update when selected sequence will be stored
      letter.isEnabled = true
      boardLetters.getOrNull(i - 1)?.getOrNull(j)?.isEnabled = true
      boardLetters.getOrNull(i + 1)?.getOrNull(j)?.isEnabled = true
      boardLetters.getOrNull(i)?.getOrNull(j - 1)?.isEnabled = true
      boardLetters.getOrNull(i)?.getOrNull(j + 1)?.isEnabled = true
    }
  }
}
