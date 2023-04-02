package com.ifmo.balda

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.ifmo.balda.model.Topic
import com.ifmo.balda.model.dao.StatDao
import com.ifmo.balda.model.dao.WordDao
import com.ifmo.balda.model.entity.Stat
import com.ifmo.balda.model.entity.Word
import kotlin.streams.asSequence

@Database(entities = [Word::class, Stat::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
  abstract fun wordDao(): WordDao
  abstract fun statDao(): StatDao
}

private fun initDb(ctx: Context): AppDatabase = Room.databaseBuilder(ctx, AppDatabase::class.java, "balda.db")
  .addCallback(object : RoomDatabase.Callback() {
    override fun onCreate(db: SupportSQLiteDatabase) {
      ctx.resources.openRawResource(R.raw.russian_nouns)
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

@Volatile
private var dbInstance: AppDatabase? = null

val Context.db: AppDatabase
  get() = dbInstance ?: synchronized(applicationContext) {
    dbInstance ?: initDb(applicationContext).also { dbInstance = it }
  }
