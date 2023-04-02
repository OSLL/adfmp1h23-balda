package com.ifmo.balda.activity

import android.os.Bundle
import android.widget.ImageButton
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NavUtils
import com.ifmo.balda.R
import com.ifmo.balda.adapter.StatListAdapter
import com.ifmo.balda.db
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class StatScreenActivity : AppCompatActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_stat_screen)

    CoroutineScope(Dispatchers.IO).launch {
      val stat = db.statDao().getStat()
      runOnUiThread {
        findViewById<ListView>(R.id.statList).adapter = StatListAdapter(
          this@StatScreenActivity,
          stat.map { Pair(it.name, it.score) }
        )
      }
    }


    findViewById<ImageButton>(R.id.backButton).setOnClickListener {
      NavUtils.navigateUpFromSameTask(this)
    }
  }
}
