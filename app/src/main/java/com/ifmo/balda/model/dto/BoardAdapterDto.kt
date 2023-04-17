package com.ifmo.balda.model.dto

import com.ifmo.balda.adapter.BoardGridAdapter

@kotlinx.serialization.Serializable
data class BoardAdapterDto(
  val nCols: Int,
  val buttonStates: List<BoardGridAdapter.LetterButtonState>,
  val wordPositions: List<List<Pair<Int, Int>>>
)
