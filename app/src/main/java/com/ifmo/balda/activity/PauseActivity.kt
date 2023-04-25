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
      val timeRemaining = getLongExtra(IntentExtraNames.TIME_REMAINING, -1)
      if (timeRemaining == -1L) error("Missing required property '${IntentExtraNames.TIME_REMAINING}'")

      Pair(currentPlayer, timeRemaining)
    }

    findViewById<TextView>(R.id.currentPlayerName).text = currentPlayer
    findViewById<TextView>(R.id.timeRemaining).text = timeStringFromMillis(timeRemaining)
    findViewById<Button>(R.id.resumeButton).setOnClickListener { finish() }
  }

  /**
   * Assuming there are less than 10 minutes
   */
  private fun timeStringFromMillis(millis: Long): String {
    var seconds = (millis / 1000)
    val minutes = seconds / 60
    seconds %= 60

    return if (seconds < 10) {
      "$minutes:0$seconds"
    } else {
      "$minutes:$seconds"
    }
  }
}
