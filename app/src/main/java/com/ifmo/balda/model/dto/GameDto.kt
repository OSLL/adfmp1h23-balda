package com.ifmo.balda.model.dto

import com.ifmo.balda.model.PlayerNumber

@kotlinx.serialization.Serializable
data class GameDto(
  val player1: PlayerDto,
  val player2: PlayerDto,
  val currentPlayer: PlayerNumber,
  val board: BoardAdapterDto,
  val timeRemaining: Long?
)
