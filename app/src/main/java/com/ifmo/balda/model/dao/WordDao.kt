package com.ifmo.balda.model.dao

import androidx.room.Dao
import androidx.room.Query

@Dao
interface WordDao {
  @Query("select word from word")
  suspend fun getAll(): List<WordSlice>

  @Query("select word from word where topic = :topicName")
  suspend fun getByTopic(topicName: String): List<WordSlice>
}

data class WordSlice(
  val word: String
)
