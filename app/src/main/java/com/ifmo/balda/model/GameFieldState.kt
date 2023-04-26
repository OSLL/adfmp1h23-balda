package com.ifmo.balda.model

import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.widget.GridView
import androidx.core.content.ContextCompat
import androidx.core.view.get
import com.ifmo.balda.Coordinates
import com.ifmo.balda.PreferencesKeys
import com.ifmo.balda.R
import com.ifmo.balda.activity.currentLanguage
import com.ifmo.balda.adapter.BoardGridAdapter
import com.ifmo.balda.adapter.OnLastWordSelectedCallback
import com.ifmo.balda.adapter.OnWordSelectedCallback
import com.ifmo.balda.model.data.LargeIO
import com.ifmo.balda.model.data.Topic
import com.ifmo.balda.model.data.dictionaries
import com.ifmo.balda.model.dto.BoardAdapterDto
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.random.Random

// Char is the letter at the position, List<Coordinates> is the position of the word it belongs to
private typealias FilledBoard = Map<Coordinates, Pair<Char, List<Coordinates>>>

class GameFieldState private constructor(
  private val context: Context,
  private val view: GridView,
  private val onWordSelectedCallback: OnWordSelectedCallback
) {
  private val coroutineScope = CoroutineScope(Dispatchers.Default)
  private var activeHint: Hint? = null

  @Volatile
  var published = false

  constructor(
    context: Context,
    difficulty: Difficulty,
    topic: Topic,
    layoutInflater: LayoutInflater,
    view: GridView,
    size: Int,
    onWordSelectedCallback: OnWordSelectedCallback,
    onGameEnded: OnLastWordSelectedCallback
  ) : this(context, view, onWordSelectedCallback) {
    coroutineScope.launch {
      val board = getBoard(difficulty, topic, size)
      val positions = board.values.map { it.second }.toSet().toList()

      withContext(Dispatchers.Main) {
        view.apply {
          numColumns = size
          adapter = BoardGridAdapter(
            layoutInflater = layoutInflater,
            letters = getBoardLetters(board, size),
            nCols = size,
            wordPositions = positions,
            onWordSelected = { word, positions -> onWordSelected(word, positions) },
            onLastWordSelectedCallback = onGameEnded
          )
        }
        published = true
      }
    }
  }

  constructor(
    context: Context,
    layoutInflater: LayoutInflater,
    view: GridView,
    dto: BoardAdapterDto,
    onWordSelectedCallback: OnWordSelectedCallback,
    onGameEnded: OnLastWordSelectedCallback
  ) : this(context, view, onWordSelectedCallback) {
    view.apply {
      numColumns = dto.nCols
      adapter = BoardGridAdapter(
        layoutInflater = layoutInflater,
        dto = dto,
        onWordSelected = { word, positions -> onWordSelected(word, positions) },
        onLastWordSelectedCallback = onGameEnded
      )
    }
    published = true
  }

  fun destroy() {
    coroutineScope.cancel()
  }

  fun hint(): Boolean {
    if (!published) return false
    val adapter = view.adapter as BoardGridAdapter

    if (activeHint == null) {
      val idx = (0 until adapter.positions.size).random()
      activeHint = Hint(
        positions = adapter.positions[idx],
        lastHintedIdx = -1
      )
    }

    if (activeHint!!.lastHintedIdx == activeHint!!.positions.size - 1) return false

    activeHint!!.lastHintedIdx++
    val positions = activeHint!!.positions
    val idx = activeHint!!.lastHintedIdx

    val btn = getButtonByCoordinates(positions[idx])
    btn.setTextColor(Color.RED) // TODO: change color?

    coroutineScope.launch { // Blink
      repeat(3) {
        repeat(2) {
          withContext(Dispatchers.Main) {
            btn.performClick()
          }
          delay(250)
        }
      }
    }

    return true
  }

  private fun getBoardLetters(board: FilledBoard, size: Int): List<String> = buildList {
    for (i in 0 until size) {
      for (j in 0 until size) {
        add(board[i to j]!!.first.toString())
      }
    }
  }

  // todo: save state correctly
  @LargeIO
  private suspend fun getBoard(difficulty: Difficulty, topic: Topic, size: Int): FilledBoard =
    withContext(Dispatchers.Default) {
      Random.nextInt().let { seed ->
        Log.d("board", "seed is $seed")
        val random = Random(seed)

        val board = BoardGenerator(random).generate(size, size)
        val trajectory = board.getCluster(0 to 0)

        val len2words = withContext(Dispatchers.IO) {
          val dictionary = with(context) {
            val prefs = getSharedPreferences(PreferencesKeys.preferencesFileKey, Context.MODE_PRIVATE)
            dictionaries.loadDictionary(currentLanguage(prefs), topic, this)
          }
          val frequencies = dictionary.values.map { it } /*.toSortedSet()*/.toList().sorted()
          val difficultEnd = frequencies[frequencies.size / 3]
          val mediumEnd = frequencies[frequencies.size * 2 / 3]

          dictionary
            .filterValues {
              it in when (difficulty) {
                Difficulty.EASY -> mediumEnd until frequencies.last()
                Difficulty.MEDIUM -> difficultEnd until mediumEnd
                Difficulty.HARD -> 0..difficultEnd
              }
            }
            .keys
            .groupBy { it.length }
        }
        val dict = DictionaryGenerator(random).generate(trajectory.size, len2words).shuffled(random)
        Log.d("board", "dict is ${dict.toList()}")

        val result = buildMap {
          val restOfTrajectory = trajectory.toMutableList()
          for (word in dict.asReversed()) {
            val wordTraj = restOfTrajectory.takeLast(word.length)
            repeat(word.length) { restOfTrajectory.removeLast() }

            this += wordTraj.zip(word.toList().map { it.uppercaseChar() }).toMap()
              .mapValues { it.value to wordTraj }
          }
        }

        Log.d("board", "word positions are ${result.values.map { it.second }}")

        result
      }
    }

  private fun onWordSelected(word: String, positions: List<Coordinates>) {
    Log.v("GameFieldState", "onWordSelected: positions: $positions, hint positions: ${activeHint?.positions}")
    if (activeHint?.positions == positions) resetHint()

    onWordSelectedCallback(word, positions)
  }

  private fun getButtonByCoordinates(coords: Coordinates): BoardGridAdapter.LetterButton =
    view[coords.first * view.numColumns + coords.second] as BoardGridAdapter.LetterButton

  private fun resetHint() {
    Log.v("GameFieldState", "resetHint")
    if (activeHint == null) return

    for (i in 0..activeHint!!.lastHintedIdx) {
      val btn = getButtonByCoordinates(activeHint!!.positions[i])
      btn.setTextColor(ContextCompat.getColor(context, R.color.black))
    }

    activeHint = null
  }

  private data class Hint(
    val positions: List<Coordinates>,
    var lastHintedIdx: Int
  )
}
