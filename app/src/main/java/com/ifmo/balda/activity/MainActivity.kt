package com.ifmo.balda.activity

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
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
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.TooltipCompat
import androidx.core.content.edit
import androidx.core.os.LocaleListCompat
import androidx.fragment.app.DialogFragment
import com.ifmo.balda.IntentExtraNames
import com.ifmo.balda.PreferencesKeys
import com.ifmo.balda.R
import com.ifmo.balda.model.Difficulty
import com.ifmo.balda.model.GameMode
import com.ifmo.balda.model.data.Lang
import com.ifmo.balda.model.data.LargeIO
import com.ifmo.balda.model.data.Topic
import com.ifmo.balda.model.data.dictionaries
import com.ifmo.balda.setOnClickActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
  companion object {
    private val buttonIdToDifficulty = mapOf(
      R.id.easyDifficultyButton to Difficulty.EASY,
      R.id.mediumDifficultyButton to Difficulty.MEDIUM,
      R.id.hardDifficultyButton to Difficulty.HARD
    )

    private val difficultyToButtonId = mapOf(*buttonIdToDifficulty.map { (fst, snd) -> Pair(snd, fst) }.toTypedArray())
  }

  private lateinit var coroutineScope: CoroutineScope

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    coroutineScope = CoroutineScope(kotlinx.coroutines.Dispatchers.IO)
    val prefs = getSharedPreferences(PreferencesKeys.preferencesFileKey, Context.MODE_PRIVATE)

    coroutineScope.launch {
      val lang = currentLanguage(prefs)
      runOnUiThread {
        selectLanguage(lang, prefs) // reset ui & persist
      }
    }

    findViewById<Button>(R.id.langButton).setOnClickListener {
      coroutineScope.launch {
        val languages = Lang.list(this@MainActivity) + Lang.list(this@MainActivity) // not to run out of bounds
        val current = currentLanguage(prefs).code

        runOnUiThread {
          val next = languages[1 + languages.indexOfFirst { it.code == current }]
          selectLanguage(next, prefs)
        }
      }
    }

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
    coroutineScope.cancel()
  }

  private fun initTopicBlock(prefs: SharedPreferences) {
    val selector = findViewById<Spinner>(R.id.topicSelector)

    coroutineScope.launch {
      val topics = sortedTopics(prefs)

      runOnUiThread {
        setOnClickTooltip(findViewById<ImageButton>(R.id.topicHelpButton), R.string.topic_help)

        selector.adapter = ArrayAdapter(
          this@MainActivity,
          android.R.layout.simple_list_item_1,
          topics.map {
            TopicSelectorItem(it.name(this@MainActivity))
          }
        )

        val topicToSet = currentTopic(prefs).takeIf { it in topics.toSet() } ?: Topic.Common

        selector.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
          override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
            coroutineScope.launch {
              selectTopic(topics[position], prefs)
            }
          }

          override fun onNothingSelected(p0: AdapterView<*>?) {
            val topic = currentTopic(prefs).takeIf { it in topics.toSet() } ?: Topic.Common
            coroutineScope.launch {
              selectTopic(topic, prefs)
            }
          }
        }

        selector.setSelection(topics.indexOf(topicToSet))
      }
    }
  }

  private fun initDifficultyBlock(prefs: SharedPreferences) {
    val difficultyChangeHandler = CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
      buttonView.typeface = if (isChecked) Typeface.DEFAULT_BOLD else Typeface.DEFAULT

      if (isChecked) {
        findViewById<TextView>(R.id.selectedDifficulty).text = buttonView.text
      }
      if (isChecked) {
        selectDifficulty(buttonIdToDifficulty[buttonView.id]!!, prefs)
      }
    }

    setOnClickTooltip(findViewById<ImageButton>(R.id.difficultyHelpButton), R.string.difficulty_help)

    for (key in buttonIdToDifficulty.keys) {
      findViewById<RadioButton>(key).setOnCheckedChangeListener(difficultyChangeHandler)
    }

    val difficulty = currentDifficulty(prefs)
    findViewById<RadioGroup>(R.id.difficultyButtonsGroup)
      .check(difficultyToButtonId[difficulty]!!)
  }

  private fun getStartGameOnClickListener(prefs: SharedPreferences, mode: GameMode) = View.OnClickListener {
    val savedGameKey = when (mode) {
      GameMode.SINGLE_PLAYER -> PreferencesKeys.singlePlayerSavedGame
      GameMode.MULTIPLAYER -> PreferencesKeys.multiPlayerSavedGame
    }
    val savedGame = prefs.getString(savedGameKey, null)

    if (savedGame != null) {
      ResumeSavedGameDialogFragment(
        onPositive = {
          startActivity(
            getGameActivityIntent(mode, savedGame, currentDifficulty(prefs), currentTopic(prefs))
          )
        },
        onNegative = { startActivity(getChooseNameActivityIntent(mode, currentDifficulty(prefs), currentTopic(prefs))) }
      )
        .show(supportFragmentManager, "Start game dialog")
    } else {
      startActivity(getChooseNameActivityIntent(mode, currentDifficulty(prefs), currentTopic(prefs)))
    }
  }

  private fun getChooseNameActivityIntent(
    mode: GameMode,
    difficulty: Difficulty,
    topic: Topic
  ) = Intent(this, ChooseNameActivity::class.java)
    .apply {
      putExtra(IntentExtraNames.GAME_MODE, mode.name)
      putExtra(IntentExtraNames.TOPIC, topic.name(this@MainActivity))
      putExtra(IntentExtraNames.DIFFICULTY, difficulty.name)
    }

  private fun getGameActivityIntent(
    mode: GameMode,
    savedGame: String,
    difficulty: Difficulty,
    topic: Topic
  ) = Intent(
    this,
    GameActivity::class.java
  ).apply {
    putExtra(IntentExtraNames.GAME_MODE, mode.name)
    putExtra(IntentExtraNames.TOPIC, topic.name(this@MainActivity))
    putExtra(IntentExtraNames.DIFFICULTY, difficulty.name)
    putExtra(IntentExtraNames.SAVED_GAME, savedGame)
  }

  private fun selectLanguage(lang: Lang, prefs: SharedPreferences) {
    prefs.edit {
      putString(PreferencesKeys.language, lang.code)
    }
    AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(lang.code))
  }

  private fun currentDifficulty(prefs: SharedPreferences): Difficulty {
    return prefs.getString(PreferencesKeys.difficulty, null)
      ?.let { Difficulty.valueOf(it) }
      ?: Difficulty.EASY
  }

  private fun selectDifficulty(difficulty: Difficulty, prefs: SharedPreferences) {
    findViewById<RadioGroup>(R.id.difficultyButtonsGroup).check(difficultyToButtonId[difficulty]!!)
    prefs.edit {
      putString(PreferencesKeys.difficulty, difficulty.name)
    }
    Log.d("difficulty", "selected difficulty ${difficulty.name}")
  }

  private fun currentTopic(prefs: SharedPreferences): Topic {
    return prefs.getString(PreferencesKeys.topic, null)
      ?.let { Topic.byName(it, this) }
      ?: Topic.Common
  }

  @LargeIO
  private fun selectTopic(topic: Topic, prefs: SharedPreferences) {
    val selector = findViewById<Spinner>(R.id.topicSelector)
    val topics = sortedTopics(prefs)
    runOnUiThread {
      selector.setSelection(topics.indexOfFirst { it == topic })
      prefs.edit {
        putString(PreferencesKeys.topic, topic.name(this@MainActivity))
      }
      Log.d("topic", "selected topic ${topic.name(this)}")
    }
  }

  @LargeIO
  private fun sortedTopics(prefs: SharedPreferences): List<Topic> {
    return dictionaries.topics(currentLanguage(prefs))
      .sortedBy { if (it == Topic.Common) "" else it.name(this) }
  }

  internal class TopicSelectorItem(val text: String) {
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

@LargeIO
fun Context.currentLanguage(prefs: SharedPreferences): Lang {
  return AppCompatDelegate.getApplicationLocales()[0]?.language?.let { Lang.byCode(it, this) }
    ?: prefs.getString(PreferencesKeys.language, null)?.let { Lang.byCode(it, this) }
    ?: Lang.default(this)
}
