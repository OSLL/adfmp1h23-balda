package com.ifmo.balda

import androidx.room.Database
import androidx.room.RoomDatabase
import com.ifmo.balda.model.dao.StatDao
import com.ifmo.balda.model.dao.WordDao
import com.ifmo.balda.model.entity.Stat
import com.ifmo.balda.model.entity.Word

@Database(entities = [Word::class, Stat::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
  abstract fun wordDao(): WordDao
  abstract fun statDao(): StatDao
}
