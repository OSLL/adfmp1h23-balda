package com.ifmo.balda.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NavUtils
import androidx.core.content.edit
import com.ifmo.balda.IntentExtraNames
import com.ifmo.balda.PreferencesKeys
import com.ifmo.balda.R
import com.ifmo.balda.adapter.BoardGridAdapter
import com.ifmo.balda.db
import com.ifmo.balda.model.BoardGenerator
import com.ifmo.balda.model.DictionaryGenerator
import com.ifmo.balda.model.GameMode
import com.ifmo.balda.model.Topic
import com.ifmo.balda.model.dto.GameDto
import com.ifmo.balda.view.InterceptingGridView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.random.Random

// Char is the letter at the position, List<Pair<Int, Int>> is the position of the word it belongs to
private typealias Coordinates = Pair<Int, Int>
private typealias FilledBoard = Map<Coordinates, Pair<Char, List<Coordinates>>>

class GameActivity : AppCompatActivity() {
  private val n = 8 // Depends on difficulty?
  private val coroutineScope = CoroutineScope(Dispatchers.Default)

  // WARNING: this property are safe to use ONLY in [onCreate] and after that
  private val gameMode
    get() = GameMode.valueOf(
      intent.getStringExtra(IntentExtraNames.GAME_MODE)
        ?: error("Missing required extra property ${IntentExtraNames.GAME_MODE}")
    )

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_game)

    findViewById<Button>(R.id.menu_button).setOnClickListener {
      NavUtils.navigateUpTo(this, Intent(this, MainActivity::class.java))
    }

    if (this.intent.extras!!.getString(IntentExtraNames.SAVED_GAME) == null) {
      initDefault()
    } else {
      initFromSaved()
    }
  }

  override fun onPause() {
    super.onPause()
    val board = findViewById<InterceptingGridView>(R.id.board)
    coroutineScope.cancel()

    if (board.adapter != null) {
      getSharedPreferences(PreferencesKeys.preferencesFileKey, Context.MODE_PRIVATE).edit {
        val key = when (gameMode) {
          GameMode.SINGLE_PLAYER -> PreferencesKeys.singlePlayerSavedGame
          GameMode.MULTIPLAYER -> PreferencesKeys.multiPlayerSavedGame
        }

        val adapter = board.adapter as BoardGridAdapter
        putString(
          key,
          Json.encodeToString(
            GameDto(
              player1Name = findViewById<TextView>(R.id.p1_name).text.toString(),
              player2Name = findViewById<TextView>(R.id.p2_name).text.toString(),
              player1Score = findViewById<TextView>(R.id.p1_score).text.toString().toInt(),
              player2Score = findViewById<TextView>(R.id.p2_score).text.toString().toInt(),
              board = adapter.toDto()
            )
          )
        )
      }
    }
  }

  private fun initFromSaved(): Unit = with(
    Json.decodeFromString<GameDto>(intent.getStringExtra(IntentExtraNames.SAVED_GAME)!!)
  ) {
    findViewById<TextView>(R.id.p1_name).text = player1Name
    findViewById<TextView>(R.id.p2_name).text = player2Name
    findViewById<TextView>(R.id.p1_score).text = player1Score.toString()
    findViewById<TextView>(R.id.p2_score).text = player2Score.toString()
    findViewById<InterceptingGridView>(R.id.board).apply {
      numColumns = board.n
      adapter = BoardGridAdapter(layoutInflater, board)
    }
  }

  private fun initDefault() {
    findViewById<TextView>(R.id.p1_name).text = intent.getStringExtra(IntentExtraNames.PLAYER_1_NAME)!!
    findViewById<TextView>(R.id.p2_name).text = intent.getStringExtra(IntentExtraNames.PLAYER_2_NAME)!!
    findViewById<TextView>(R.id.p1_score).text = "0"
    findViewById<TextView>(R.id.p2_score).text = "0"

    coroutineScope.launch {
      val board = getBoard()
      runOnUiThread { setUpBoardGrid(board) }
    }
  }

  private fun setUpBoardGrid(board: FilledBoard) {
    val boardLetters = fillBoardWithLetters(board)
    val gridView = findViewById<InterceptingGridView>(R.id.board)
    gridView.numColumns = n
    val boardGridAdapter = BoardGridAdapter(layoutInflater, boardLetters, n, board.values.map { it.second })
    gridView.adapter = boardGridAdapter
  }

  private fun fillBoardWithLetters(board: FilledBoard): List<String> = buildList {
    for (i in 0 until n) {
      for (j in 0 until n) {
        add(board[i to j]!!.first.toString())
      }
    }
  }

  // todo: save state correctly
  private suspend fun getBoard(): FilledBoard = Random.nextInt().let { seed ->
    Log.d("board", "seed is $seed")
    val random = Random(seed)

    val board = BoardGenerator(random).generate(n, n)
    val trajectory = board.getCluster(0 to 0)

    val len2words = withContext(Dispatchers.IO) { db.wordDao().getLength2WordsByTopic(Topic.COMMON) }
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
