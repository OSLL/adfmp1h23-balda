package com.ifmo.balda.model.dao

import androidx.room.Dao
import androidx.room.MapInfo
import androidx.room.Query
import com.ifmo.balda.model.Topic

@Dao
abstract class WordDao {
  @MapInfo(keyColumn = "len", valueColumn = "word")
  @Query("select length(word) as len, word from word where topic = :topicName")
  protected abstract suspend fun getLength2WordsByTopic(topicName: String): Map<Int, List<String>>

  @MapInfo(keyColumn = "len", valueColumn = "word")
  @Query("select length(word) as len, word from word")
  protected abstract suspend fun getLength2Words(): Map<Int, List<String>>

  suspend fun getLength2WordsByTopic(topic: Topic): Map<Int, List<String>> = when (topic) {
    Topic.ALL -> getLength2Words()
    else -> getLength2WordsByTopic(topic.name)
  }
}
