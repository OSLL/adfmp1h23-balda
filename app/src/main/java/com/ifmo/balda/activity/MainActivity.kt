package com.ifmo.balda.activity

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
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
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.TooltipCompat
import androidx.core.content.edit
import androidx.fragment.app.DialogFragment
import com.ifmo.balda.IntentExtraNames
import com.ifmo.balda.PreferencesKeys
import com.ifmo.balda.R
import com.ifmo.balda.model.Difficulty
import com.ifmo.balda.model.GameMode
import com.ifmo.balda.model.Topic
import com.ifmo.balda.setOnClickActivity

class MainActivity : AppCompatActivity() {
  companion object {
    private val buttonIdToDifficulty = mapOf(
      R.id.easyDifficultyButton to Difficulty.EASY,
      R.id.mediumDifficultyButton to Difficulty.MEDIUM,
      R.id.hardDifficultyButton to Difficulty.HARD
    )

    private val difficultyToButtonId = mapOf(*buttonIdToDifficulty.map { (fst, snd) -> Pair(snd, fst) }.toTypedArray())
  }
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    val prefs = getSharedPreferences(PreferencesKeys.preferencesFileKey, Context.MODE_PRIVATE)

    findViewById<ImageButton>(R.id.statButton).setOnClickActivity(this, StatScreenActivity::class)
    findViewById<ImageButton>(R.id.helpButton).setOnClickActivity(this, HelpScreenActivity::class)

    findViewById<Button>(R.id.startGame1PlayerButton).setOnClickListener(
      getStartGameOnClickListener(prefs, GameMode.SINGLE_PLAYER)
    )
    findViewById<Button>(R.id.startGame2PlayerButton).setOnClickListener(
      getStartGameOnClickListener(prefs, GameMode.MULTIPLAYER)
    )

    initDifficultyBlock(prefs)
    initTopicBlock(prefs)
  }

  override fun onPause() {
    super.onPause()
    val checkedDifficultyButton = findViewById<RadioGroup>(R.id.difficultyButtonsGroup).checkedRadioButtonId
    val selectedDifficulty = buttonIdToDifficulty[checkedDifficultyButton]!!

    val selectedTopicItem = findViewById<Spinner>(R.id.topicSelector).selectedItem as TopicSelectorItem
    val selectedTopic = Topic.fromResourceId(selectedTopicItem.resourceId).getOrThrow()

    getSharedPreferences(PreferencesKeys.preferencesFileKey, Context.MODE_PRIVATE).edit {
      putString(PreferencesKeys.difficulty, selectedDifficulty.name)
      putString(PreferencesKeys.topic, selectedTopic.name)
    }
  }

  private fun initTopicBlock(prefs: SharedPreferences) {
    val selector = findViewById<Spinner>(R.id.topicSelector)
    val values = Topic.values()

    setOnClickTooltip(findViewById<ImageButton>(R.id.topicHelpButton), R.string.topic_help)
    selector.adapter = ArrayAdapter(
      this,
      android.R.layout.simple_list_item_1,
      values.map {
        val rId = it.resourceId
        val text = resources.getString(rId)
        TopicSelectorItem(rId, text)
      }
    )

    val savedTopic = Topic.valueOf(prefs.getString(PreferencesKeys.topic, Topic.ALL.name)!!)
    selector.setSelection(values.indexOfFirst { it.resourceId == savedTopic.resourceId })
  }

  private fun initDifficultyBlock(prefs: SharedPreferences) {
    val difficultyChangeHandler = CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
      buttonView.typeface = if (isChecked) Typeface.DEFAULT_BOLD else Typeface.DEFAULT

      if (isChecked) {
        findViewById<TextView>(R.id.selectedDifficulty).text = buttonView.text
      }
    }

    setOnClickTooltip(findViewById<ImageButton>(R.id.difficultyHelpButton), R.string.difficulty_help)

    for (key in buttonIdToDifficulty.keys) {
      findViewById<RadioButton>(key).setOnCheckedChangeListener(difficultyChangeHandler)
    }

    val selectedDifficulty = Difficulty.valueOf(prefs.getString(PreferencesKeys.difficulty, Difficulty.EASY.name)!!)
    findViewById<RadioGroup>(R.id.difficultyButtonsGroup).check(difficultyToButtonId[selectedDifficulty]!!)
  }

  private fun getStartGameOnClickListener(prefs: SharedPreferences, mode: GameMode) = View.OnClickListener {
    val savedGameKey = when (mode) {
      GameMode.SINGLE_PLAYER -> PreferencesKeys.singlePlayerSavedGame
      GameMode.MULTIPLAYER -> PreferencesKeys.multiPlayerSavedGame
    }
    val savedGame = prefs.getString(savedGameKey, null)

    if (savedGame != null) {
      ResumeSavedGameDialogFragment(
        onPositive = { startActivity(getGameActivityIntent(mode, savedGame)) },
        onNegative = { startActivity(getChooseNameActivityIntent(mode)) }
      )
        .show(supportFragmentManager, "Start game dialog")
    } else {
      startActivity(getChooseNameActivityIntent(mode))
    }
  }

  private fun getChooseNameActivityIntent(mode: GameMode) = Intent(this, ChooseNameActivity::class.java)
    .apply { putExtra(IntentExtraNames.GAME_MODE, mode.name) }

  private fun getGameActivityIntent(mode: GameMode, savedGame: String) = Intent(
    this,
    GameActivity::class.java
  ).apply {
    putExtra(IntentExtraNames.GAME_MODE, mode.name)
    putExtra(IntentExtraNames.SAVED_GAME, savedGame)
  }

  internal class TopicSelectorItem(val resourceId: Int, val text: String) {
    override fun toString(): String = text
  }

  class ResumeSavedGameDialogFragment(
    private val onPositive: () -> Unit,
    private val onNegative: () -> Unit
  ) : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog = activity?.let {
      with(AlertDialog.Builder(it)) {
        setMessage(R.string.resumeSavedGamePrompt)
        setPositiveButton(R.string.yes) { _, _ -> onPositive() }
        setNegativeButton(R.string.no) { _, _ -> onNegative() }
        create()
      }
    } ?: throw IllegalStateException("Activity cannot be null")
  }
}

private fun Context.setOnClickTooltip(view: View, tooltipTextId: Int) {
  TooltipCompat.setTooltipText(view, getString(tooltipTextId))
  view.setOnClickListener {
    it.performLongClick()
  }
}
