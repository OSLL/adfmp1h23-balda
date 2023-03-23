package com.ifmo.balda.adapter

import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ToggleButton
import com.ifmo.balda.R


class BoardGridAdapter(
  private val layoutInflater: LayoutInflater,
  private val letters: MutableList<String>,
  private val n: Int
) : BaseAdapter() {

  private var currentWord = linkedSetOf<Pair<ToggleButton, Int>>()

  override fun getCount() = letters.size

  override fun getItem(position: Int): String = letters[position]

  override fun getItemId(position: Int): Long = position.toLong()

  override fun getView(position: Int, grid: View?, parent: ViewGroup?): View {
    var view = grid
    if (view == null) {
      view = (layoutInflater.inflate(R.layout.letter, null) as ToggleButton).apply {
        setLetterValue(position)
        isActivated = true
        setOnClickListener(getOnLetterClickListener(position))
        setOnTouchListener(getOnLetterTouchListener(this, position))
      }
    }
    return view
  }

  private fun ToggleButton.setLetterValue(position: Int) {
    val letter = getItem(position)
    text = letter
    textOn = letter
    textOff = letter
  }

  private fun getOnLetterClickListener(i: Int): View.OnClickListener {
    return View.OnClickListener {
      Log.d("click", "letter $i")
    }
  }

  private fun getOnLetterTouchListener(letter: ToggleButton, i: Int): View.OnTouchListener {
    return View.OnTouchListener { v, event ->
      when (event.actionMasked) {
        MotionEvent.ACTION_DOWN -> {
          Log.d("touch", "letter $i")
          currentWord = linkedSetOf(letter to i)
          return@OnTouchListener v.performClick()
        }
        MotionEvent.ACTION_MOVE -> {
          Log.d("move", "letter $i")
          Log.d("currWord", getCurrentWord())
          currentWord.indexOfFirst { it.second == i }.takeIf { it != -1 }
            ?.let { letterPos ->
              repeat(currentWord.indices.last - letterPos) {
                currentWord.last().first.performClick()
                currentWord.remove(currentWord.last())
              }
              return@OnTouchListener true
            }
          if (!letter.isActivated) {
            return@OnTouchListener true
          }
          if (!isLetterValid(i)) {
            Log.d("move", "letter $i is not valid")
            return@OnTouchListener true
          }
          currentWord.add(letter to i)
          return@OnTouchListener v.performClick()
        }
        MotionEvent.ACTION_UP -> {
          Log.d("up", "letter $i")
          val isValid = isCurrentWordValid()
          if (isValid) {
            val color: Int = randomColor
            currentWord.forEach {
              it.first.setBackgroundColor(color)
              it.first.isActivated = false
            }
          } else {
            Toast.makeText(v.context, "Слово ${getCurrentWord()} не загадывали", Toast.LENGTH_LONG).show()
            currentWord.forEach { it.first.performClick() }
            currentWord = linkedSetOf()
            return@OnTouchListener true
          }
        }
      }
      return@OnTouchListener false
    }
  }

  private fun isLetterValid(i: Int): Boolean {
    val lastPosition = currentWord.last().second

    return i == lastPosition - 1
      || i == lastPosition + 1
      || i == lastPosition - n
      || i == lastPosition + n
  }

  private fun getCurrentWord() = currentWord.fold("") { acc, it -> acc + it.first.text }

  private fun isCurrentWordValid(): Boolean {
    return getCurrentWord() == "AAA"
  }

  private val randomColor: Int
    get() {
      return Color.HSVToColor(
        FloatArray(3) {
          when (it) {
            0 -> (0..360).random().toFloat()
            1 -> (10..50).random().toFloat() / 100
            2 -> (60..100).random().toFloat() / 100
            else -> 0.0f
          }
        }
      )
    }
}
