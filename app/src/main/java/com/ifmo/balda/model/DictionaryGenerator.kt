package com.ifmo.balda.model

import kotlin.random.Random

class DictionaryGenerator(private val random: Random) {
  fun generate(totalLength: Int, wordBase: Map<Int, List<String>>): Set<String> {
    val words = mutableMapOf<Int, MutableSet<String>>()

    while (words.values.sumOf { it.sumOf { it.length } } < totalLength) {
      val diff = totalLength - words.values.sumOf { it.sumOf { it.length } }
      if (diff < wordBase.keys.min()) {
        val maxLength = words.keys.max()
        words[maxLength]!!.remove(words[maxLength]!!.first())
      }
      val nextWord = wordBase.filterKeys { it <= diff }.flatMap { it.value }.randomOrNull(random) ?: continue
      words.getOrPut(nextWord.length) { mutableSetOf() } += nextWord
    }

    return words.values.flatMapTo(mutableSetOf()) { it }
  }
}
