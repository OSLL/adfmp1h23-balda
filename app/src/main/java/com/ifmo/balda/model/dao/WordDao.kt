package com.ifmo.balda.model.dao

import androidx.room.Dao
import androidx.room.MapInfo
import androidx.room.Query

@Dao
interface WordDao {
  @MapInfo(keyColumn = "len", valueColumn = "word")
  @Query("select length(word) as len, word from word where topic = :topicName")
  suspend fun getWordsByLength(topicName: String): Map<Int, List<String>>
}
