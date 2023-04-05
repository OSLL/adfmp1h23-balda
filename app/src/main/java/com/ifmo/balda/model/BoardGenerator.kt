package com.ifmo.balda.model

import com.ifmo.balda.model.Board.Companion.DOWN
import com.ifmo.balda.model.Board.Companion.LEFT
import com.ifmo.balda.model.Board.Companion.RIGHT
import com.ifmo.balda.model.Board.Companion.UP
import com.ifmo.balda.model.Board.Companion.oppositeDirection
import com.ifmo.balda.unreachable
import kotlin.random.Random

class BoardGenerator(private val random: Random) {
  /**
   *  The board is first initialized with a lot of little cycles (see [Board] doc).
   *  Then, this algorithm merges some random cycles at a random point,
   *  and does this until there is only one cycle left.
   *
   *  Consider this example:
   *    ┌┐
   *    ││┌┐
   *    ││││
   *    └┘││
   *      └┘
   *   The cycles in the above example can be merged into this:
   *    ┌┐
   *    │└─┐
   *    │┌┐│
   *    └┘││
   *      └┘
   *
   *   However, this example shows two cycles that cannot be merged into a single closed cycle:
   *    ┌┐
   *    ││
   *    ││
   *    └┘┌┐
   *      └┘
   *   This means that even though these two cycles each own a cell that connects to the other,
   *   the actual precondition for cycle merging is that they both have at least two adjacent cells,
   *   that are neighbors to the cells of the other cycle.
   */
  fun generate(height: Int, width: Int): Board {
    val board = Board(height, width)

    while (board.countClusters() > 1) {
      merging@ for ((i, j) in board.cells.flatMapIndexed { i, row -> row.indices.map { i to it } }.shuffled(random)) {
        val cell = board.cells[i][j]

        for (dir in listOf(UP, LEFT, DOWN, RIGHT).shuffled(random)) {
          if (!cell.connection[dir]) {
            continue
          }
          val nextCell = board[board.neighbor(i to j, dir) ?: continue]

          val neighborDirs = when (dir) {
            UP, DOWN -> listOf(LEFT, RIGHT)
            LEFT, RIGHT -> listOf(UP, DOWN)
            else -> unreachable()
          }

          for (neighDir in neighborDirs.shuffled(random)) {
            val neighCell = board[board.neighbor(i to j, neighDir) ?: continue]
            if (!neighCell.connection[dir]) {
              continue
            }
            val cluster = board.getCluster(i to j)
            if (board.neighbor(i to j, neighDir) in cluster || board.neighbor(i to j, dir, neighDir) in cluster) {
              continue
            }

            val nextNeighCell = board[board.neighbor(i to j, dir, neighDir)!!]

            cell.connection[dir] = false
            nextCell.connection[dir.oppositeDirection] = false
            neighCell.connection[dir] = false
            nextNeighCell.connection[dir.oppositeDirection] = false

            cell.connection[neighDir] = true
            nextCell.connection[neighDir] = true
            neighCell.connection[neighDir.oppositeDirection] = true
            nextNeighCell.connection[neighDir.oppositeDirection] = true

            assert(board.connectionsValid())

            break@merging
          }
        }
      }
    }

    return board
  }
}
