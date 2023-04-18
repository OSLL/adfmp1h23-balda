package com.ifmo.balda.activity

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
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
import com.ifmo.balda.model.PlayerNumber
import com.ifmo.balda.model.Topic
import com.ifmo.balda.model.dto.GameDto
import com.ifmo.balda.model.dto.PlayerDto
import com.ifmo.balda.model.entity.Stat
import com.ifmo.balda.view.InterceptingGridView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.properties.Delegates
import kotlin.random.Random

// Char is the letter at the position, List<Coordinates> is the position of the word it belongs to
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

  private var currentPlayer by Delegates.notNull<PlayerNumber>()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_game)

    findViewById<Button>(R.id.menu_button).setOnClickListener { toMainMenu() }

    if (this.intent.extras!!.getString(IntentExtraNames.SAVED_GAME) == null) {
      initDefault()
    } else {
      initFromSaved()
    }

    findViewById<Button>(R.id.pause_button).setOnClickListener {
      val intent = Intent(this, PauseActivity::class.java).apply {
        putExtra(
          IntentExtraNames.CURRENT_PLAYER,
          when (currentPlayer) {
            PlayerNumber.FIRST -> findViewById<TextView>(R.id.p1_name).text.toString()
            PlayerNumber.SECOND -> findViewById<TextView>(R.id.p2_name).text.toString()
          }
        )
        putExtra(IntentExtraNames.TIME_REMAINING, 0)
      }
      startActivity(intent)
    }

    findViewById<Button>(R.id.pass_button).setOnClickListener { /* TODO: Confirmation */ changeCurrentPlayer() }
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

        putString(key, Json.encodeToString(dtoFromAdapter(board.adapter as BoardGridAdapter)))
      }
    }
  }

  private fun initFromSaved(): Unit = with(
    Json.decodeFromString<GameDto>(intent.getStringExtra(IntentExtraNames.SAVED_GAME)!!)
  ) {
    findViewById<TextView>(R.id.p1_name).text = player1.name
    findViewById<TextView>(R.id.p2_name).text = player2.name
    findViewById<TextView>(R.id.p1_score).text = player1.score.toString()
    findViewById<TextView>(R.id.p2_score).text = player2.score.toString()
    findViewById<InterceptingGridView>(R.id.board).apply {
      numColumns = board.nCols
      adapter = BoardGridAdapter(
        layoutInflater = layoutInflater,
        dto = board,
        onWordSelected = { onWordSelected(it) },
        onLastWordSelectedCallback = { onGameEnded() }
      )
    }
    this@GameActivity.currentPlayer = currentPlayer

    findViewById<TextView>(R.id.current_player).text = when (currentPlayer) {
      PlayerNumber.FIRST -> player1.name
      PlayerNumber.SECOND -> player2.name
    }
  }

  private fun initDefault() {
    val player1Name = intent.getStringExtra(IntentExtraNames.PLAYER_1_NAME)!!
    val player2Name = intent.getStringExtra(IntentExtraNames.PLAYER_2_NAME)!!
    findViewById<TextView>(R.id.p1_name).text = player1Name
    findViewById<TextView>(R.id.p2_name).text = player2Name
    findViewById<TextView>(R.id.p1_score).text = "0"
    findViewById<TextView>(R.id.p2_score).text = "0"
    currentPlayer = PlayerNumber.FIRST
    findViewById<TextView>(R.id.current_player).text = player1Name

    coroutineScope.launch {
      val board = getBoard()
      runOnUiThread { setUpBoardGrid(board) }
    }
  }

  private fun setUpBoardGrid(board: FilledBoard) {
    val boardLetters = fillBoardWithLetters(board)
    val gridView = findViewById<InterceptingGridView>(R.id.board)
    gridView.numColumns = n
    val boardGridAdapter = BoardGridAdapter(
      layoutInflater = layoutInflater,
      letters = boardLetters,
      nCols = n,
      wordPositions = board.values.map { it.second }.toSet().toList(),
      onWordSelected = { onWordSelected(it) },
      onLastWordSelectedCallback = { onGameEnded() }
    )
    gridView.adapter = boardGridAdapter
  }

  private fun toMainMenu() {
    NavUtils.navigateUpTo(this, Intent(this, MainActivity::class.java))
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

  private fun dtoFromAdapter(adapter: BoardGridAdapter) = GameDto(
    player1 = PlayerDto(
      name = findViewById<TextView>(R.id.p1_name).text.toString(),
      score = findViewById<TextView>(R.id.p1_score).text.toString().toInt()
    ),
    player2 = PlayerDto(
      name = findViewById<TextView>(R.id.p2_name).text.toString(),
      score = findViewById<TextView>(R.id.p2_score).text.toString().toInt()
    ),
    currentPlayer = currentPlayer,
    board = adapter.toDto()
  )

  private fun changeCurrentPlayer() {
    currentPlayer = when (currentPlayer) {
      PlayerNumber.FIRST -> PlayerNumber.SECOND
      PlayerNumber.SECOND -> PlayerNumber.FIRST
    }

    findViewById<TextView>(R.id.current_player).text = when (currentPlayer) {
      PlayerNumber.FIRST -> findViewById<TextView>(R.id.p1_name).text.toString()
      PlayerNumber.SECOND -> findViewById<TextView>(R.id.p2_name).text.toString()
    }
  }

  @SuppressLint("SetTextI18n")
  private fun onWordSelected(word: String) {
    val scoreView: TextView = when (currentPlayer) {
      PlayerNumber.FIRST -> findViewById(R.id.p1_score)
      PlayerNumber.SECOND -> findViewById(R.id.p2_score)
    }

    // TODO: Confirmation dialog?

    scoreView.text = (scoreView.text.toString().toInt() + word.length).toString()
    changeCurrentPlayer()
  }

  @SuppressLint("CutPasteId")
  private fun onGameEnded() {
    Log.d("GameActivity", "onGameEnded")
    // Cannot use activity-local scope as it will be cancelled
    val scope = CoroutineScope(Dispatchers.IO)
    val dao = db.statDao()

    when (gameMode) {
      GameMode.SINGLE_PLAYER -> {
        val playerStat = Stat(
          name = findViewById<TextView>(R.id.p1_name).text.toString(),
          score = findViewById<TextView>(R.id.p1_score).text.toString().toInt()
        )
        scope.launch { dao.updateStats(playerStat) }
      }
      GameMode.MULTIPLAYER -> {
        val player1Stat = Stat(
          name = findViewById<TextView>(R.id.p1_name).text.toString(),
          score = findViewById<TextView>(R.id.p1_score).text.toString().toInt()
        )
        val player2Stat = Stat(
          name = findViewById<TextView>(R.id.p2_name).text.toString(),
          score = findViewById<TextView>(R.id.p2_score).text.toString().toInt()
        )

        scope.launch { dao.updateStats(player1Stat, player2Stat) }
      }
    }

    // TODO: Find a way to remove saved game
    Toast.makeText(applicationContext, "Игра завершена", Toast.LENGTH_LONG).show()
    toMainMenu()
  }
}
