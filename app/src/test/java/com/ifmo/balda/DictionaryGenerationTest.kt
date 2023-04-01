package com.ifmo.balda

import com.ifmo.balda.model.DictionaryGenerator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.TimeUnit
import kotlin.random.Random

class DictionaryGenerationTest {
  @Suppress("RemoveExplicitTypeArguments")
  val wordBase = buildMap<Int, List<String>> {
    val alphabet = ('0'..'9') + ('a'..'z') + ('A'..'Z')
    for (length in 3 until 10) {
      var wordsSequence = sequenceOf("")
      repeat(length) {
        wordsSequence = wordsSequence.flatMap { wordPrefix -> alphabet.map { letter -> wordPrefix + letter } }
      }
      put(length, wordsSequence.take(100).toList())
    }
  }

  @Test
  fun test_length() = randomTesting(seed = Random.nextInt()) { random ->
    for (length in 3 until 100 step 5) {
      val dictionary = DictionaryGenerator(random).generate(length, wordBase, minWords = 1)
      assertEquals(length, dictionary.sumOf { it.length })
    }
  }

  @Test
  fun test_count() = randomTesting(seed = Random.nextInt()) { random ->
    for (count in 3 until 30) {
      val dictionary = DictionaryGenerator(random).generate(totalLength = count * 6, wordBase, minWords = count)
      assertTrue(count <= dictionary.size)
    }
  }

  @Test
  fun test_validity() {
    repeat(10) {
      randomTesting(seed = Random.nextInt()) { random ->
        val minWords = random.nextInt(5, 20)
        val totalLength = minWords * 6
        val dictionary = DictionaryGenerator(random).generate(totalLength, wordBase, minWords)
        assertTrue(dictionary.all { it in wordBase[it.length].orEmpty() })
      }
    }
  }

  @Test
  fun test_completes(): Unit = randomTesting(seed = Random.nextInt()) { random ->
    val minWords = 30
    assertCompletes(1, TimeUnit.SECONDS) {
      DictionaryGenerator(random).generate(totalLength = minWords * 6, wordBase, minWords)
    }
  }
}
