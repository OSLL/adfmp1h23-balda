package com.ifmo.balda.activity

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NavUtils
import com.ifmo.balda.IntentExtraNames
import com.ifmo.balda.PreferencesKeys
import com.ifmo.balda.R
import com.ifmo.balda.model.GameMode

class ChooseNameActivity : AppCompatActivity() {
  // WARNING: safe to use ONLY in [onCreate] and after that
  private val gameMode
    get() = GameMode.valueOf(
      intent.getStringExtra(IntentExtraNames.GAME_MODE)
        ?: error("Missing required extra property ${IntentExtraNames.GAME_MODE}")
    )

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_choose_name_screen)

    findViewById<ImageButton>(R.id.backButton).setOnClickListener {
      NavUtils.navigateUpFromSameTask(this)
    }

    val prefs = getSharedPreferences(PreferencesKeys.preferencesFileKey, Context.MODE_PRIVATE)
    val player1NameEdit = findViewById<EditText>(R.id.player1Name)
    val player2NameEdit = findViewById<EditText>(R.id.player2Name)

    when (gameMode) {
      GameMode.SINGLE_PLAYER -> {
        player2NameEdit.visibility = View.GONE

        val playerName = prefs.getString(PreferencesKeys.singlePlayerNameKey, resources.getString(R.string.player))!!
        player1NameEdit.setHint(R.string.playerName)
        player1NameEdit.setText(playerName)
        player1NameEdit.setSelection(player1NameEdit.text.length)
      }
      GameMode.MULTIPLAYER -> {
        val player1Name = prefs.getString(
          PreferencesKeys.multiPlayer1PlayerNameKey,
          resources.getString(R.string.player1)
        )!!
        val player2Name = prefs.getString(
          PreferencesKeys.multiPlayer2PlayerNameKey,
          resources.getString(R.string.player2)
        )!!

        player1NameEdit.setHint(R.string.player1Name)
        player1NameEdit.setText(player1Name)
        player1NameEdit.setSelection(player1NameEdit.text.length)
        player2NameEdit.setHint(R.string.player2Name)
        player2NameEdit.setText(player2Name)
        player2NameEdit.setSelection(player2NameEdit.text.length)
      }
    }

    findViewById<Button>(R.id.playButton).setOnClickListener {
      val intent = Intent(this, GameActivity::class.java).apply {
        putExtra(IntentExtraNames.PLAYER_1_NAME, player1NameEdit.text.toString())
        putExtra(
          IntentExtraNames.PLAYER_2_NAME,
          when (gameMode) {
            GameMode.SINGLE_PLAYER -> resources.getString(R.string.bot)
            GameMode.MULTIPLAYER -> player2NameEdit.text.toString()
          }
        )
      }
      saveNames(prefs, player1NameEdit, player2NameEdit)
      startActivity(intent)
    }
  }

  private fun saveNames(prefs: SharedPreferences, player1NameEdit: EditText, player2NameEdit: EditText) {
    with(prefs.edit()) {
      when (gameMode) {
        GameMode.SINGLE_PLAYER -> {
          val playerName = player1NameEdit.text.toString()
          putString(PreferencesKeys.singlePlayerNameKey, playerName)
        }
        GameMode.MULTIPLAYER -> {
          val player1Name = player1NameEdit.text.toString()
          val player2Name = player2NameEdit.text.toString()

          putString(PreferencesKeys.multiPlayer1PlayerNameKey, player1Name)
          putString(PreferencesKeys.multiPlayer2PlayerNameKey, player2Name)
        }
      }
      apply()
    }
  }
}
