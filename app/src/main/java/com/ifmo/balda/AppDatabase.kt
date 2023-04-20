package com.ifmo.balda

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import com.ifmo.balda.model.dao.StatDao
import com.ifmo.balda.model.entity.Stat

@Database(entities = [Stat::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
  abstract fun statDao(): StatDao
}

private fun initDb(ctx: Context): AppDatabase = Room.databaseBuilder(ctx, AppDatabase::class.java, "balda.db")
  .addMigrations(
    Migration(1, 2) {
      it.query("DROP TABLE Word")
    }
  )
  .build()

@Volatile
private var dbInstance: AppDatabase? = null

val Context.db: AppDatabase
  get() = dbInstance ?: synchronized(applicationContext) {
    dbInstance ?: initDb(applicationContext).also { dbInstance = it }
  }
