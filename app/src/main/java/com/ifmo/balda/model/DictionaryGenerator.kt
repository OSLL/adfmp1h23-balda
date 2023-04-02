package com.ifmo.balda.model

import kotlin.random.Random

class DictionaryGenerator(private val random: Random) {
  /**
   * Returns a set of at least [minWords] words, whose lengths sum up to [totalLength].
   * The [wordBase] should be big enough to contain many words of each length.
   * This algorithm is probabilistic, so there is no guarantee that it finishes,
   * but in practice it does, if the [wordBase] satisfies the above condition.
   */
  fun generate(totalLength: Int, wordBase: Map<Int, List<String>>, minWords: Int = 8): Set<String> {
    require(minWords * wordBase.keys.min() <= totalLength)

    val words = mutableMapOf<Int, MutableSet<String>>()

    while (words.values.sumOf { it.sumOf { it.length } } < totalLength ||
      words.values.flatten().toSet().size < minWords
    ) {
      val diff = totalLength - words.values.sumOf { it.sumOf { it.length } }
      if (diff < wordBase.keys.min()) {
        val maxLength = words.keys.max()
        words[maxLength]!!.remove(words[maxLength]!!.first())
        if (words[maxLength]!!.isEmpty()) {
          words -= maxLength
        }
      }
      val nextWord = wordBase.filterKeys { it <= diff }.flatMap { it.value }.randomOrNull(random) ?: continue
      words.getOrPut(nextWord.length) { mutableSetOf() } += nextWord
    }

    return words.values.flatMapTo(mutableSetOf()) { it }
  }
}
