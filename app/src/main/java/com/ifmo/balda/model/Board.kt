package com.ifmo.balda.model

import com.ifmo.balda.unreachable

class Board {
  class Cell(val connection: BooleanArray = BooleanArray(4))

  companion object {
    private fun cell(vararg sides: Int) = Cell().apply {
      sides.forEach { connection[it] = true }
    }

    const val UP = 0
    const val LEFT = 1
    const val DOWN = 2
    const val RIGHT = 3

    val Int.oppositeDirection: Int
      get() = when (this) {
        UP -> DOWN
        DOWN -> UP
        LEFT -> RIGHT
        RIGHT -> LEFT
        else -> unreachable()
      }
  }

  val cells = Array(8) { i -> Array(8) { j ->
    when ((i % 2) to (j % 2)) {
      0 to 0 -> cell(DOWN, RIGHT)
      0 to 1 -> cell(DOWN, LEFT)
      1 to 0 -> cell(UP, RIGHT)
      1 to 1 -> cell(UP, LEFT)
      else -> unreachable()
    }
  } }

  init {
    assert(connectionsValid())
  }

  operator fun get(ij: Pair<Int, Int>): Cell {
    return cells[ij.first][ij.second]
  }

  fun getOrNull(ij: Pair<Int, Int>): Cell? {
    return cells.getOrNull(ij.first)?.getOrNull(ij.second)
  }

  fun neighbor(cell: Pair<Int, Int>, vararg directions: Int): Pair<Int, Int>? {
    @Suppress("NAME_SHADOWING")
    var cell: Pair<Int, Int>? = cell
    for (direction in directions) {
      val (i, j) = cell ?: continue
      cell = when (direction) {
        UP -> (i - 1 to j).takeIf { i > cells.indices.first }
        LEFT -> (i to j - 1).takeIf { j > cells[0].indices.first }
        DOWN -> (i + 1 to j).takeIf { i < cells.indices.last }
        RIGHT -> (i to j + 1).takeIf { j < cells[0].indices.last }
        else -> unreachable()
      }
    }
    return cell
  }

  fun print(getChar: ((Pair<Int, Int>) -> Char)? = null) {
    require(connectionsValid())

    fun id(vararg sides: Int): Int = sides.map { 1 shl it }.fold(0, Int::or)

    for ((i, row) in cells.withIndex()) {
      for ((j, cell) in row.withIndex()) {
        val char = getChar?.invoke(i to j) ?: when (id(*cell.connection.indices.filter { cell.connection[it] }.toIntArray())) {
          id() -> '•'
          id(UP) -> '│'
          id(    LEFT) -> '─'
          id(UP, LEFT) -> '┘'
          id(          DOWN) -> '│'
          id(UP,       DOWN) -> '│'
          id(    LEFT, DOWN) -> '┐'
          id(UP, LEFT, DOWN) -> '┤'
          id(                RIGHT) -> '─'
          id(UP,             RIGHT) -> '└'
          id(    LEFT,       RIGHT) -> '─'
          id(UP, LEFT,       RIGHT) -> '┴'
          id(          DOWN, RIGHT) -> '┌'
          id(UP,       DOWN, RIGHT) -> '├'
          id(    LEFT, DOWN, RIGHT) -> '┬'
          id(UP, LEFT, DOWN, RIGHT) -> '┼'
          else -> unreachable()
        }
        print(char)
      }
      println()
    }
  }

  fun connectionsValid(): Boolean {
    for ((i, row) in cells.withIndex()) {
      for ((j, cell) in row.withIndex()) {
        if (cell.connection[UP]) {
          if (i == cells.indices.first || !cells[i - 1][j].connection[DOWN]) {
            return false
          }
        }
        if (cell.connection[LEFT]) {
          if (j == row.indices.first || !row[j - 1].connection[RIGHT]) {
            return false
          }
        }
        if (cell.connection[DOWN]) {
          if (i == cells.indices.last || !cells[i + 1][j].connection[UP]) {
            return false
          }
        }
        if (cell.connection[RIGHT]) {
          if (j == row.indices.last || !row[j + 1].connection[LEFT]) {
            return false
          }
        }
      }
    }
    return true
  }

  fun countClusters(): Int {
    require(connectionsValid())

    val visited = mutableSetOf<Pair<Int, Int>>()
    var clusters = 0

    for ((i, row) in cells.withIndex()) {
      for (j in row.indices) {
        if (i to j in visited) {
          continue
        }
        clusters++
        visited += doGetCluster(i to j)
      }
    }

    return clusters
  }

  fun getCluster(cell: Pair<Int, Int>): LinkedHashSet<Pair<Int, Int>> {
    require(connectionsValid())
    return doGetCluster(cell)
  }

  private fun doGetCluster(cell: Pair<Int, Int>): LinkedHashSet<Pair<Int, Int>> {
    val (i, j) = cell
    val cluster = linkedSetOf<Pair<Int, Int>>()

    val bfsQueue = ArrayDeque(listOf(i to j))

    while (bfsQueue.isNotEmpty()) {
      val nextCell = bfsQueue.removeFirst()
      if (nextCell in cluster) {
        continue
      }
      cluster += nextCell

      var addedOne = false

      for (dir in listOf(UP, LEFT, DOWN, RIGHT)) {
        neighbor(nextCell, dir)
          ?.takeIf { this[nextCell].connection[dir] }
          ?.takeIf { it !in cluster }
          ?.let {
            if (addedOne) {
              bfsQueue.add(it)
            } else {
              bfsQueue.addFirst(it)
              addedOne = true
            }
          }
      }
    }

    return cluster
  }
}
