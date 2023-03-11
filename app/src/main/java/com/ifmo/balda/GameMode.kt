package com.ifmo.balda

enum class GameMode {
  SINGLE_PLAYER,
  MULTIPLAYER;

  override fun toString(): String = when (this) {
    SINGLE_PLAYER -> "GameMode.SINGLE_PLAYER"
    MULTIPLAYER -> "GameMode.MULTIPLAYER"
  }

  companion object {
    fun fromString(s: String): GameMode = when (s) {
      SINGLE_PLAYER.toString() -> SINGLE_PLAYER
      MULTIPLAYER.toString() -> MULTIPLAYER
      else -> error("Cannot parse $s to GameMode")
    }
  }
}
