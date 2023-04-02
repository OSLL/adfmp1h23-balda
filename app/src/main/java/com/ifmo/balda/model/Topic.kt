package com.ifmo.balda.model

import com.ifmo.balda.R

enum class Topic { // TODO: find a way to check that this list matches with resources
  COMMON,
  PROGRAMMING,
  DOTA_2,
  ALL;

  val resourceId
    get(): Int = when (this) {
      COMMON -> R.string.topic_common
      PROGRAMMING -> R.string.topic_programming
      DOTA_2 -> R.string.topic_dota2
      ALL -> R.string.topic_all
    }

  companion object {
    fun fromResourceId(id: Int): Result<Topic> = when (id) {
      R.string.topic_common -> Result.success(COMMON)
      R.string.topic_programming -> Result.success(PROGRAMMING)
      R.string.topic_dota2 -> Result.success(DOTA_2)
      R.string.topic_all -> Result.success(ALL)
      else -> Result.failure(java.lang.IllegalArgumentException("Cannot get topic from this id"))
    }
  }
}
