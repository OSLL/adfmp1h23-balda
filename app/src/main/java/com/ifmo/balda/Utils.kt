package com.ifmo.balda

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.View
import kotlin.reflect.KClass

fun View.setOnClickActivity(
  context: Context,
  activity: KClass<out Activity>,
  vararg extraSuppliers: Pair<String, () -> String?>
) {
  setOnClickListener {
    val intent = Intent(context, activity.java)
    for ((key, valueSupplier) in extraSuppliers) {
      intent.putExtra(key, valueSupplier())
    }
    context.startActivity(intent)
  }
}

fun unreachable(): Nothing = error("unreachable")

typealias Coordinates = Pair<Int, Int>
