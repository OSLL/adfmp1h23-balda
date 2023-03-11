package com.ifmo.balda.activity

import android.os.Bundle
import android.widget.ImageButton
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NavUtils
import com.ifmo.balda.R
import com.ifmo.balda.adapter.StatListAdapter

class StatScreenActivity : AppCompatActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_stat_screen)

    findViewById<ListView>(R.id.statList).adapter = StatListAdapter(
      this,
      listOf( // TODO: get from data storage
        Pair("Monkey King", 100500),
        Pair("Lich", 100300),
        Pair("Sniper", 100000),
        Pair("Invoker", 3)
      )
    )

    findViewById<ImageButton>(R.id.backButton).setOnClickListener {
      NavUtils.navigateUpFromSameTask(this)
    }
  }
}
