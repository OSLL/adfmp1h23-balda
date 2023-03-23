package com.ifmo.balda.adapter

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Toast
import androidx.appcompat.widget.AppCompatToggleButton
import com.ifmo.balda.R
import kotlin.properties.Delegates

class BoardGridAdapter(
  private val layoutInflater: LayoutInflater,
  private val letters: MutableList<String>,
  private val n: Int,
  private val wordPositions: List<List<Pair<Int, Int>>>
) : BaseAdapter() {

  private var currentWord = linkedSetOf<LetterButton>()

  override fun getCount() = letters.size

  override fun getItem(position: Int): String = letters[position]

  override fun getItemId(position: Int): Long = position.toLong()

  override fun getView(position: Int, grid: View?, parent: ViewGroup?): View {
    return grid ?: (layoutInflater.inflate(R.layout.letter, null) as LetterButton).apply {
      this.position = position
      setLetterValue(getItem(position))
      setOnClickListener(getOnLetterClickListener(position))
      setOnTouchListener(getOnLetterTouchListener(this, position))
    }
  }

  private fun getOnLetterClickListener(i: Int): View.OnClickListener {
    return View.OnClickListener {
      Log.d("click", "letter $i")
    }
  }

  private fun getOnLetterTouchListener(letter: LetterButton, i: Int): View.OnTouchListener {
    return View.OnTouchListener { v, event ->
      when (event.actionMasked) {
        MotionEvent.ACTION_DOWN -> {
          Log.d("touch", "letter $i")
          currentWord = linkedSetOf(letter)
          return@OnTouchListener v.performClick()
        }
        MotionEvent.ACTION_MOVE -> {
          Log.d("move", "letter $i")
          Log.d("currWord", getCurrentWord())
          currentWord.indexOfFirst { it.position == i }.takeIf { it != -1 }
            ?.let { letterPos ->
              repeat(currentWord.indices.last - letterPos) {
                currentWord.last().performClick()
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
          currentWord.add(letter)
          return@OnTouchListener v.performClick()
        }
        MotionEvent.ACTION_UP -> {
          Log.d("up", "letter $i")
          val isValid = isCurrentWordValid()
          if (isValid) {
            val color = getRandomColor()
            currentWord.forEach {
              it.setBackgroundColor(color)
              it.isActivated = false
            }
          } else {
            Toast.makeText(v.context, "Слово ${getCurrentWord()} не загадывали", Toast.LENGTH_LONG).show()
            currentWord.forEach { it.performClick() }
            currentWord = linkedSetOf()
            return@OnTouchListener true
          }
        }
      }
      return@OnTouchListener false
    }
  }

  private fun isLetterValid(i: Int): Boolean {
    val lastPosition = currentWord.last().position

    return i == lastPosition - 1 ||
      i == lastPosition + 1 ||
      i == lastPosition - n ||
      i == lastPosition + n
  }

  private fun getCurrentWord() = currentWord.fold("") { acc, it -> acc + it.text }

  private fun isCurrentWordValid(): Boolean {
    return currentWord.map { (it.position / n) to (it.position % n) } in wordPositions
  }

  private fun getRandomColor(): Int {
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

  class LetterButton : AppCompatToggleButton {
    var position by Delegates.notNull<Int>()

    init {
      isActivated = true
    }

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    fun setLetterValue(value: String) {
      text = value
      textOn = value
      textOff = value
    }
  }
}
