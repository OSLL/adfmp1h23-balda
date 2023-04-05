package com.ifmo.balda.model.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Stat(
  @PrimaryKey val name: String,
  val score: Int
)
