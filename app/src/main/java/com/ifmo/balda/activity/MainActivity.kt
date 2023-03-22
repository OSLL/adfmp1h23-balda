package com.ifmo.balda.activity

import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CompoundButton
import android.widget.ImageButton
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.TooltipCompat
import com.ifmo.balda.Difficulty
import com.ifmo.balda.GameMode
import com.ifmo.balda.IntentExtraNames
import com.ifmo.balda.R

class MainActivity : AppCompatActivity() {
  private val buttonIdToDifficulty = mapOf(
    Pair(R.id.easyDifficultyButton, Difficulty.EASY),
    Pair(R.id.mediumDifficultyButton, Difficulty.MEDIUM),
    Pair(R.id.hardDifficultyButton, Difficulty.HARD)
  )

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    val difficultyChangeHandler = CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
      buttonView.typeface = if (isChecked) Typeface.DEFAULT_BOLD else Typeface.DEFAULT

      if (isChecked) {
        findViewById<TextView>(R.id.selectedDifficulty).text = buttonView.text
      }
    }

    findViewById<ImageButton>(R.id.statButton).setOnClickListener {
      startActivity(Intent(this, StatScreenActivity::class.java))
    }
    findViewById<ImageButton>(R.id.helpButton).setOnClickListener {
      startActivity(Intent(this, HelpScreenActivity::class.java))
    }

    findViewById<Button>(R.id.startGame1PlayerButton).setOnClickListener {
      val intent = Intent(this, ChooseNameActivity::class.java)
        .putExtra(IntentExtraNames.GAME_MODE, GameMode.SINGLE_PLAYER.name)
      startActivity(intent)
    }
    findViewById<Button>(R.id.startGame2PlayerButton).setOnClickListener {
      val intent = Intent(this, ChooseNameActivity::class.java)
        .putExtra(IntentExtraNames.GAME_MODE, GameMode.MULTIPLAYER.name)
      startActivity(intent)
    }

    setOnClickTooltip(findViewById<ImageButton>(R.id.difficultyHelpButton), R.string.difficulty_help)
    for (key in buttonIdToDifficulty.keys) {
      findViewById<RadioButton>(key).setOnCheckedChangeListener(difficultyChangeHandler)
    }

    findViewById<RadioGroup>(R.id.difficultyButtonsGroup).check(R.id.easyDifficultyButton)

    setOnClickTooltip(findViewById<ImageButton>(R.id.difficultyHelpButton), R.string.topic_help)
    findViewById<Spinner>(R.id.topicSelector).adapter = ArrayAdapter(
      this,
      android.R.layout.simple_list_item_1,
      listOf("Общая", "Все", "Программирование", "Dota 2")
    )
  }
}

private fun Context.setOnClickTooltip(view: View, tooltipTextId: Int) {
  TooltipCompat.setTooltipText(view, getString(tooltipTextId))
  view.setOnClickListener {
    it.performLongClick()
  }
}
