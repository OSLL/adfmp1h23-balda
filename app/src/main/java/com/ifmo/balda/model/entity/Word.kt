package com.ifmo.balda.model.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Word(
  @PrimaryKey val id: Int,
  val word: String,
  val topic: String
)
