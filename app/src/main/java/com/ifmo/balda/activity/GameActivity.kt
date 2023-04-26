package com.ifmo.balda.activity

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NavUtils
import androidx.core.content.edit
import com.ifmo.balda.IntentExtraNames
import com.ifmo.balda.OneButtonDialog
import com.ifmo.balda.PreferencesKeys
import com.ifmo.balda.R
import com.ifmo.balda.TwoButtonsDialog
import com.ifmo.balda.adapter.BoardGridAdapter
import com.ifmo.balda.db
import com.ifmo.balda.model.Difficulty
import com.ifmo.balda.model.GameFieldState
import com.ifmo.balda.model.GameMode
import com.ifmo.balda.model.PlayerNumber
import com.ifmo.balda.model.data.Topic
import com.ifmo.balda.model.dto.GameDto
import com.ifmo.balda.model.dto.PlayerDto
import com.ifmo.balda.model.entity.Stat
import com.ifmo.balda.view.InterceptingGridView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.concurrent.TimeUnit
import kotlin.properties.Delegates

class GameActivity : AppCompatActivity() {
  private val n = 8 // Depends on difficulty?
  private var fieldState by Delegates.notNull<GameFieldState>()

  private lateinit var timer: CountDownTimer
  private var timeRemaining: Long? = null

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

    if (this.intent.extras!!.getString(IntentExtraNames.SAVED_GAME) == null) {
      initDefault()
    } else {
      initFromSaved()
    }

    findViewById<Button>(R.id.menu_button).setOnClickListener {
      timer.cancel()
      TwoButtonsDialog(
        message = R.string.exitToMenuPrompt,
        onPositive = { toMainMenu() },
        onNegative = { timer.start() },
        onClose = { timer.start() }
      ).show(supportFragmentManager, "Exit to menu confirmation dialog")
    }

    findViewById<Button>(R.id.pause_button).setOnClickListener {
      timer.cancel()
      val intent = Intent(this, PauseActivity::class.java).apply {
        putExtra(
          IntentExtraNames.CURRENT_PLAYER,
          when (currentPlayer) {
            PlayerNumber.FIRST -> findViewById<TextView>(R.id.p1_name).text.toString()
            PlayerNumber.SECOND -> findViewById<TextView>(R.id.p2_name).text.toString()
          }
        )
        putExtra(IntentExtraNames.TIME_REMAINING, timeRemaining)
      }
      startActivity(intent)
    }
    findViewById<Button>(R.id.pass_button).setOnClickListener {
      timer.cancel()
      TwoButtonsDialog(
        message = R.string.passTurnPrompt,
        onPositive = {
          changeCurrentPlayer()
          timeRemaining = null
          resetTimer()
        },
        onNegative = { timer.start() },
        onClose = { timer.start() }
      ).show(supportFragmentManager, "Pass turn confirmation dialog")
    }
    findViewById<Button>(R.id.tip_button).setOnClickListener {
      val hintShown = fieldState.hint()

      if (hintShown) {
        val viewId = when (currentPlayer) {
          PlayerNumber.FIRST -> R.id.p1_score
          PlayerNumber.SECOND -> R.id.p2_score
        }
        val view = findViewById<TextView>(viewId)
        view.text = (view.text.toString().toInt() - 1).toString()
      }
    }

    timer.start()
  }

  override fun onPause() {
    super.onPause()
    val board = findViewById<InterceptingGridView>(R.id.board)
    fieldState.destroy()

    if (fieldState.published) {
      getSharedPreferences(PreferencesKeys.preferencesFileKey, Context.MODE_PRIVATE).edit {
        val key = when (gameMode) {
          GameMode.SINGLE_PLAYER -> PreferencesKeys.singlePlayerSavedGame
          GameMode.MULTIPLAYER -> PreferencesKeys.multiPlayerSavedGame
        }

        putString(key, Json.encodeToString(dtoFromAdapter(board.adapter as BoardGridAdapter)))
      }
    }
  }

  override fun onResume() {
    super.onResume()
    if (timeRemaining != null) {
      resetTimer()
    }
  }

  private fun resetTimer() {
    timer = getTimer()
    timeRemaining = null
    timer.start()
  }

  private fun getTimer(): CountDownTimer {
    val difficulty = intent.getStringExtra(IntentExtraNames.DIFFICULTY)!!.let { Difficulty.valueOf(it) }
    val millisInFuture = timeRemaining ?: when (difficulty) {
      Difficulty.EASY -> TimeUnit.MINUTES.toMillis(3)
      Difficulty.MEDIUM -> TimeUnit.MINUTES.toMillis(2)
      Difficulty.HARD -> TimeUnit.MINUTES.toMillis(1)
    }

    return object : CountDownTimer(millisInFuture, 1000) {

      override fun onTick(millisUntilFinished: Long) {
        timeRemaining = millisUntilFinished

        findViewById<TextView>(R.id.time_display).setTextColor(Color.WHITE)

        var seconds = (millisUntilFinished / 1000).toInt()
        val minutes = seconds / 60
        seconds %= 60

        val text = if (seconds < 10) {
          "$minutes:0$seconds"
        } else {
          "$minutes:$seconds"
        }

        findViewById<TextView>(R.id.time_display).text = text

        if (seconds <= 5) {
          findViewById<TextView>(R.id.time_display).setTextColor(Color.RED)
        }
      }

      override fun onFinish() {
        cancel()
        changeCurrentPlayer()
        timeRemaining = null
        resetTimer()
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
    fieldState = GameFieldState(
      context = this@GameActivity,
      layoutInflater = layoutInflater,
      view = findViewById(R.id.board),
      dto = board,
      onWordSelectedCallback = { word, _ -> onWordSelected(word) },
      onGameEnded = { onGameEnded() }
    )
    this@GameActivity.currentPlayer = currentPlayer

    findViewById<TextView>(R.id.current_player).text = when (currentPlayer) {
      PlayerNumber.FIRST -> player1.name
      PlayerNumber.SECOND -> player2.name
    }

    this@GameActivity.timeRemaining = timeRemaining
    timer = getTimer()
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

    val topic = intent.getStringExtra(IntentExtraNames.TOPIC)!!.let { Topic.byName(it, this) }

    fieldState = GameFieldState(
      context = this,
      layoutInflater = layoutInflater,
      difficulty = intent.getStringExtra(IntentExtraNames.DIFFICULTY)!!.let { Difficulty.valueOf(it) },
      topic = topic,
      view = findViewById(R.id.board),
      size = n,
      onWordSelectedCallback = { word, _ -> onWordSelected(word) },
      onGameEnded = { onGameEnded() }
    )

    timer = getTimer()
  }

  private fun toMainMenu() {
    NavUtils.navigateUpTo(this, Intent(this, MainActivity::class.java))
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
    board = adapter.toDto(),
    timeRemaining = timeRemaining
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

    scoreView.text = (scoreView.text.toString().toInt() + word.length).toString()
    timer.cancel()
    changeCurrentPlayer()
    timeRemaining = null
    timer.start()
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
    OneButtonDialog(
      message = R.string.game_ended,
      onButtonClick = { toMainMenu() },
      onClose = { toMainMenu() }
    ).show(supportFragmentManager, "Game ended dialog")
  }
}
