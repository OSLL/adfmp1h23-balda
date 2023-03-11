package com.ifmo.balda.activity

import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NavUtils
import com.ifmo.balda.R

class HelpScreenActivity : AppCompatActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_help_screen)
    findViewById<ImageButton>(R.id.backButton).setOnClickListener {
      NavUtils.navigateUpFromSameTask(this)
    }
  }
}
