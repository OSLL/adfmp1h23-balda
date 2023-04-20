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
import com.ifmo.balda.Coordinates
import com.ifmo.balda.R
import com.ifmo.balda.model.dto.BoardAdapterDto
import kotlin.properties.Delegates

typealias OnWordSelectedCallback = (word: String, positions: List<Coordinates>) -> Unit
typealias OnLastWordSelectedCallback = () -> Unit

class BoardGridAdapter private constructor(
  private val layoutInflater: LayoutInflater,
  private val nCols: Int,
  private val buttonStates: List<LetterButtonState>,
  private val wordPositions: MutableList<List<Coordinates>>,
  private val onWordSelected: OnWordSelectedCallback,
  private val onLastWordSelectedCallback: OnLastWordSelectedCallback
) : BaseAdapter() {
  private var currentWord = mutableListOf<LetterButton>()
  val positions // outside positions are immutable
    get(): List<List<Coordinates>> = wordPositions

  constructor(
    layoutInflater: LayoutInflater,
    dto: BoardAdapterDto,
    onWordSelected: OnWordSelectedCallback,
    onLastWordSelectedCallback: OnLastWordSelectedCallback
  ) : this(
    layoutInflater,
    dto.nCols,
    dto.buttonStates,
    dto.wordPositions.toMutableList(),
    onWordSelected,
    onLastWordSelectedCallback
  )

  constructor(
    layoutInflater: LayoutInflater,
    letters: List<String>,
    nCols: Int,
    wordPositions: List<List<Pair<Int, Int>>>,
    onWordSelected: OnWordSelectedCallback,
    onLastWordSelectedCallback: OnLastWordSelectedCallback
  ) : this(
    layoutInflater,
    nCols,
    letters.withIndex().map { (idx, letter) -> LetterButtonState(position = idx, value = letter) },
    wordPositions.toMutableList(),
    onWordSelected,
    onLastWordSelectedCallback
  )

  override fun getCount() = buttonStates.size

  override fun getItem(position: Int): String = buttonStates[position].value

  override fun getItemId(position: Int): Long = position.toLong()

  override fun getView(position: Int, grid: View?, parent: ViewGroup?): View {
    return grid ?: (layoutInflater.inflate(R.layout.letter, null) as LetterButton).apply {
      init(buttonStates[position])
      setOnClickListener(getOnLetterClickListener(position))
      setOnTouchListener(getOnLetterTouchListener(this, position))
    }
  }

  fun toDto(): BoardAdapterDto = BoardAdapterDto(
    nCols,
    buttonStates,
    wordPositions
  )

  private fun getOnLetterClickListener(i: Int): View.OnClickListener {
    return View.OnClickListener {
      Log.d("click", "letter $i")
    }
  }

  private fun getOnLetterTouchListener(letter: LetterButton, position: Int): View.OnTouchListener {
    return View.OnTouchListener { v, event ->
      when (event.actionMasked) {
        MotionEvent.ACTION_DOWN -> {
          Log.d("touch", "letter $position")
          currentWord = mutableListOf(letter)
          return@OnTouchListener v.performClick()
        }
        MotionEvent.ACTION_MOVE -> {
          Log.d("move", "letter $position")
          Log.d("currWord", getCurrentWord())
          currentWord.indexOfFirst { it.position == position }.takeIf { it != -1 }
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
          if (!isLetterValid(position)) {
            Log.d("move", "letter $position is not valid")
            return@OnTouchListener true
          }
          currentWord.add(letter)
          return@OnTouchListener v.performClick()
        }
        MotionEvent.ACTION_UP -> {
          Log.d("up", "letter $position")
          val isValid = isCurrentWordValid()

          if (!isValid) {
            Toast.makeText(v.context, "Слово ${getCurrentWord()} не загадывали", Toast.LENGTH_LONG).show()
            currentWord.forEach { it.performClick() }
            currentWord = mutableListOf()
            return@OnTouchListener true
          }

          val color = getRandomColor()
          currentWord.forEach {
            it.setBackgroundColor(color)
            it.isActivated = false

            // TODO: Is there a way for LetterButton and LetterButtonState share same state
            it.state.apply {
              this.color = color
              isActive = false
            }
          }

          val positions = toPositions(currentWord)
          onWordSelected(getCurrentWord(), positions)
          wordPositions.remove(positions)
          if (wordPositions.isEmpty()) onLastWordSelectedCallback()

          return@OnTouchListener true
        }
        else -> return@OnTouchListener false
      }
    }
  }

  private fun isLetterValid(i: Int): Boolean {
    val lastPosition = currentWord.last().position

    return i == lastPosition - 1 ||
      i == lastPosition + 1 ||
      i == lastPosition - nCols ||
      i == lastPosition + nCols
  }

  private fun getCurrentWord() = currentWord.joinToString("") { it.text }

  private fun isCurrentWordValid(): Boolean = toPositions(currentWord) in wordPositions

  private fun toPositions(word: List<LetterButton>): List<Pair<Int, Int>> =
    word.map { (it.position / nCols) to (it.position % nCols) }

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

  @kotlinx.serialization.Serializable
  data class LetterButtonState(
    val position: Int,
    var isActive: Boolean = true,
    var color: Int? = null,
    val value: String
  )

  class LetterButton : AppCompatToggleButton {
    var position by Delegates.notNull<Int>()
    var state by Delegates.notNull<LetterButtonState>()

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    internal fun init(state: LetterButtonState) {
      position = state.position
      setLetterValue(state.value)
      isActivated = state.isActive
      if (state.color != null) {
        setBackgroundColor(state.color!!)
      }
      this.state = state
    }

    private fun setLetterValue(value: String) {
      text = value
      textOn = value
      textOff = value
    }
  }
}
