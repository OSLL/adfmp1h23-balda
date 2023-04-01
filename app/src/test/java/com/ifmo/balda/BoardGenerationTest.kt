package com.ifmo.balda

import com.ifmo.balda.model.Board
import com.ifmo.balda.model.BoardGenerator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.TimeUnit
import kotlin.random.Random

class BoardGenerationTest {
  @Test
  fun test_even() = randomTesting(seed = Random.nextInt()) { random ->
    for (height in 2..16 step 2) {
      for (width in 2..16 step 2) {
        val board = test_completes(height, width, random)
        test_continuity(board)
      }
    }
  }

  @Test
  fun test_odd() = randomTesting(seed = Random.nextInt()) { random ->
    for (height in 2..16 step 2) {
      for (width in 3..16 step 2) {
        val board = test_completes(height, width, random)
        test_continuity(board)
      }
    }

    for (height in 3..16 step 2) {
      for (width in 2..16 step 2) {
        val board = test_completes(height, width, random)
        test_continuity(board)
      }
    }
  }

  private fun test_completes(height: Int, width: Int, random: Random): Board {
    return assertCompletes(1, TimeUnit.SECONDS) {
      BoardGenerator(random).generate(height, width)
    }
  }


  private fun test_continuity(board: Board) {
    val cluster = board.getCluster(0 to 0)
    val height = board.cells.size
    val width = board.cells[0].size
    val size = height * width
    assertEquals(size, cluster.size)
    assertEquals(cluster.toList(), cluster.distinct())
    assertTrue(cluster.map { it.first }.all { it in 0 until height })
    assertTrue(cluster.map { it.second }.all { it in 0 until width })
  }
}
