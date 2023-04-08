package com.ifmo.balda.model.dto

@kotlinx.serialization.Serializable
data class GameDto(
  val player1Name: String,
  val player2Name: String,
  val player1Score: Int,
  val player2Score: Int,
  // TODO: save time and turn
  val board: BoardAdapterDto
)
