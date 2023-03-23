package com.ifmo.balda.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NavUtils
import com.ifmo.balda.GameMode
import com.ifmo.balda.IntentExtraNames
import com.ifmo.balda.R
import com.ifmo.balda.setOnClickActivity

class ChooseNameActivity : AppCompatActivity() {
  @SuppressLint("CutPasteId")
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_choose_name_screen)

    findViewById<ImageButton>(R.id.backButton).setOnClickListener {
      NavUtils.navigateUpFromSameTask(this)
    }

    val gameMode = GameMode.valueOf(
      intent.getStringExtra(IntentExtraNames.GAME_MODE)
        ?: error("Missing required extra property ${IntentExtraNames.GAME_MODE}")
    )

    when (gameMode) {
      GameMode.SINGLE_PLAYER -> {
        findViewById<EditText>(R.id.player2Name).visibility = View.GONE

        val nameEdit = findViewById<EditText>(R.id.player1Name)
        nameEdit.setHint(R.string.playerName)
        nameEdit.setText(R.string.player)
        nameEdit.setSelection(nameEdit.text.length)
      }
      GameMode.MULTIPLAYER -> {
        val name1Edit = findViewById<EditText>(R.id.player1Name)
        val name2Edit = findViewById<EditText>(R.id.player2Name)

        name1Edit.setHint(R.string.player1Name)
        name1Edit.setText(R.string.player1)
        name1Edit.setSelection(name1Edit.text.length)
        name2Edit.setHint(R.string.player2Name)
        name2Edit.setText(R.string.player2)
        name2Edit.setSelection(name2Edit.text.length)
      }
    }

    findViewById<Button>(R.id.playButton).setOnClickActivity(
      this,
      GameActivity::class,
      IntentExtraNames.PLAYER_1_NAME to findViewById<EditText>(R.id.player1Name).text?.toString(),
      IntentExtraNames.PLAYER_2_NAME to findViewById<EditText>(R.id.player2Name).text?.toString()
    )
  }
}
