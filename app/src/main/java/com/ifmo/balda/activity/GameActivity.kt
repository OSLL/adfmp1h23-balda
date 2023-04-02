package com.ifmo.balda.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NavUtils
import com.ifmo.balda.IntentExtraNames
import com.ifmo.balda.R
import com.ifmo.balda.adapter.BoardGridAdapter
import com.ifmo.balda.db
import com.ifmo.balda.model.BoardGenerator
import com.ifmo.balda.model.DictionaryGenerator
import com.ifmo.balda.model.Topic
import com.ifmo.balda.view.InterceptingGridView
import kotlinx.coroutines.runBlocking
import kotlin.random.Random

// Char is the letter at the position, List<Pair<Int, Int>> is the position of the word it belongs to
private typealias Board = Map<Pair<Int, Int>, Pair<Char, List<Pair<Int, Int>>>>

class GameActivity : AppCompatActivity() {
  private val n = 8 // Depends on difficulty?
  private val boardLetters = mutableListOf<String>()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_game)

    findViewById<Button>(R.id.menu_button).setOnClickListener {
      NavUtils.navigateUpTo(this, Intent(this, MainActivity::class.java))
    }

    findViewById<TextView>(R.id.p1_name).text = this.intent.extras!!.getString(IntentExtraNames.PLAYER_1_NAME)!!
    findViewById<TextView>(R.id.p2_name).text = this.intent.extras!!.getString(IntentExtraNames.PLAYER_2_NAME)!!
    findViewById<TextView>(R.id.p1_score).text = "0"
    findViewById<TextView>(R.id.p2_score).text = "0"

    // TODO: move this out of main thread
    getBoard().also {
      fillBoardWithLetters(it)
      setUpBoardGrid(it)
    }
  }

  private fun setUpBoardGrid(board: Board) {
    val gridView = findViewById<InterceptingGridView>(R.id.board)
    gridView.numColumns = n
    val boardGridAdapter = BoardGridAdapter(layoutInflater, boardLetters, n, board.values.map { it.second })
    gridView.adapter = boardGridAdapter
  }

  private fun fillBoardWithLetters(board: Board) {
    for (i in 0 until n) {
      for (j in 0 until n) {
        boardLetters.add(board[i to j]!!.first.toString())
      }
    }
  }

  // todo: save state correctly
  private fun getBoard(): Board = Random.nextInt().let { seed ->
    Log.d("board", "seed is $seed")
    val random = Random(seed)

    val board = BoardGenerator(random).generate(n, n)
    val trajectory = board.getCluster(0 to 0)

    val len2words = runBlocking { db.wordDao().getLength2WordsByTopic(Topic.COMMON) }
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
