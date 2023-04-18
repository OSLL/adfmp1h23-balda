package com.ifmo.balda.model.dto

import kotlinx.serialization.Serializable

@Serializable
data class PlayerDto(
  val name: String,
  val score: Int
)
