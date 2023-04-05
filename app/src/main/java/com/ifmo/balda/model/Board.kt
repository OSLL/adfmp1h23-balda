package com.ifmo.balda.model

import com.ifmo.balda.unreachable

/**
 * A grid of cells. Height and width cannot be both odd at the same time.
 * Each cell may be connected to its direct neighbors.
 *
 * In graph theory terms, each cell represents a vertex,
 * and if two cells are connected to each other,
 * it means that there is an edge between them.
 *
 * The Board class is used for the creation of trajectories (paths of edges)
 * that the words will be aligned to in the game's grid.
 *
 * The Board is initialized with 2x2 squares of connected cells, like this:
 *    ┌┐┌┐┌┐┌┐
 *    └┘└┘└┘└┘
 *    ┌┐┌┐┌┐┌┐
 *    └┘└┘└┘└┘
 *    ┌┐┌┐┌┐┌┐
 *    └┘└┘└┘└┘
 *    ┌┐┌┐┌┐┌┐
 *    └┘└┘└┘└┘
 *  or, if the width is odd, like this:
 *    ┌┐┌┐┌┐┌─┐
 *    └┘└┘└┘└─┘
 *    ┌┐┌┐┌┐┌─┐
 *    └┘└┘└┘└─┘
 *    ┌┐┌┐┌┐┌─┐
 *    └┘└┘└┘└─┘
 *    ┌┐┌┐┌┐┌─┐
 *    └┘└┘└┘└─┘
 *  or, if the height is odd, like this:
 *    ┌┐┌┐┌┐┌┐
 *    └┘└┘└┘└┘
 *    ┌┐┌┐┌┐┌┐
 *    └┘└┘└┘└┘
 *    ┌┐┌┐┌┐┌┐
 *    └┘└┘└┘└┘
 *    ┌┐┌┐┌┐┌┐
 *    ││││││││
 *    └┘└┘└┘└┘
 */
class Board(height: Int, width: Int) {
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

  val cells = Array(height) { i ->
    Array(width) { j ->
      if (width % 2 == 1 && j == width - 2) {
        cell(LEFT, RIGHT)
      } else if (width % 2 == 1 && j == width - 1) {
        if (i % 2 == 0) {
          cell(DOWN, LEFT)
        } else {
          cell(UP, LEFT)
        }
      } else if (height % 2 == 1 && i == height - 2) {
        cell(DOWN, UP)
      } else if (height % 2 == 1 && i == height - 1) {
        if (j % 2 == 0) {
          cell(UP, RIGHT)
        } else {
          cell(UP, LEFT)
        }
      } else {
        when ((i % 2) to (j % 2)) {
          0 to 0 -> cell(DOWN, RIGHT)
          0 to 1 -> cell(DOWN, LEFT)
          1 to 0 -> cell(UP, RIGHT)
          1 to 1 -> cell(UP, LEFT)
          else -> unreachable()
        }
      }
    }
  }

  init {
    require(height > 0 && width > 0)
    require(!(height % 2 == 1 && width % 2 == 1))
    assert(connectionsValid())
  }

  operator fun get(ij: Pair<Int, Int>): Cell {
    return cells[ij.first][ij.second]
  }

  fun getOrNull(ij: Pair<Int, Int>): Cell? {
    return cells.getOrNull(ij.first)?.getOrNull(ij.second)
  }

  /**
   * Goes through the path specified by the [directions], returns the cell it ends up with.
   */
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
        val char = getChar?.invoke(i to j) ?: when (
          id(
            *cell.connection.indices.filter { cell.connection[it] }.toIntArray()
          )
        ) {
          id() -> '•'
          id(UP) -> '│'
          id(LEFT) -> '─'
          id(UP, LEFT) -> '┘'
          id(DOWN) -> '│'
          id(UP, DOWN) -> '│'
          id(LEFT, DOWN) -> '┐'
          id(UP, LEFT, DOWN) -> '┤'
          id(RIGHT) -> '─'
          id(UP, RIGHT) -> '└'
          id(LEFT, RIGHT) -> '─'
          id(UP, LEFT, RIGHT) -> '┴'
          id(DOWN, RIGHT) -> '┌'
          id(UP, DOWN, RIGHT) -> '├'
          id(LEFT, DOWN, RIGHT) -> '┬'
          id(UP, LEFT, DOWN, RIGHT) -> '┼'
          else -> unreachable()
        }
        print(char)
      }
      println()
    }
  }

  /**
   * Ensures that for each cell with the `connection` flag set to `true` in some direction,
   * there is a corresponding neighbor cell with the `connection` flag set to `true` in the opposite direction.
   * This basically means that we don't have any unmatched, "hanging" connections.
   */
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

  /**
   * Returns the number of unique clusters, that may be returned by the [getCluster] function.
   * The order of the cells in a cluster doesn't matter here, so a cluster is basically a connectivity component.
   */
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

  /**
   * Traverses the board starting at the given cell.
   * Returns all reachable cells in the order they were visited.
   * Since some cells may have more than 2 connections, paths may fork.
   * It's difficult to explain the result in words, so I'll proceed with an example.
   * Imagine this board (all adjacent cells with letters are connected):
   *
   *    A B C D E F • • t h e s e • c l u s t e r s • a r e • u n r e a c h a b l e
   *    • • K • G • • • • • • • • • • • • • • • • • • • • • • • • • • • • • • • • •
   *    • • J I H • • • • • • • • • • • • • • • • • • • • • • • • • • • • • • • • •
   *
   *  Starting at the letter A, this function may return any of the following (extra spaces are solely for readability):
   *    - A B C D E F   K J I H G
   *    - A B C D E G H I J K   F
   *    - A B C K J I H G E D   F
   *    - A B C K J I H G E F   D
   */
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
