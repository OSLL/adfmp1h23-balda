package com.ifmo.balda

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.View
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.ifmo.balda.model.Topic
import kotlin.reflect.KClass
import kotlin.streams.asSequence

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

val Activity.db: AppDatabase
  get() = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "balda.db")
    .addCallback(object : RoomDatabase.Callback() {
      override fun onCreate(db: SupportSQLiteDatabase) {
        applicationContext.resources.openRawResource(R.raw.russian_nouns)
          .bufferedReader()
          .lines().asSequence()
          .chunked(900) // SQLITE_MAX_VARIABLE_NUMBER is 999
          .forEach { nouns ->
            db.execSQL(
              "insert into word (word, topic) values ${nouns.joinToString(", ") { "(?, '${Topic.COMMON.name}')" }}",
              nouns.toTypedArray()
            )
          }
      }

      override fun onDestructiveMigration(db: SupportSQLiteDatabase) = onCreate(db)
    })
    .build()
