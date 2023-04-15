package com.ifmo.balda.activity

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.ifmo.balda.IntentExtraNames
import com.ifmo.balda.R

class PauseActivity : AppCompatActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_pause)

    val (currentPlayer, timeRemaining) = with(this.intent) {
      val currentPlayer = getStringExtra(IntentExtraNames.CURRENT_PLAYER)
        ?: error("Missing required property '${IntentExtraNames.CURRENT_PLAYER}'")
      val timeRemaining = getIntExtra(IntentExtraNames.TIME_REMAINING, -1)
      if (timeRemaining == -1) error("Missing required property '${IntentExtraNames.TIME_REMAINING}'")

      Pair(currentPlayer, timeRemaining)
    }

    findViewById<TextView>(R.id.currentPlayerName).text = currentPlayer
    findViewById<TextView>(R.id.timeRemaining).text = timeStringFromSeconds(timeRemaining)
    findViewById<Button>(R.id.resumeButton).setOnClickListener { finish() }
  }

  private fun timeStringFromSeconds(seconds: Int): String {
    val minutes = seconds / 60
    val secondsRemaining = seconds - (minutes * 60)
    val minutesString = if (minutes < 10) "0$minutes" else "$minutes"
    val secondsString = if (secondsRemaining < 10) "0$secondsRemaining" else "$secondsRemaining"

    return "$minutesString:$secondsString"
  }
}
