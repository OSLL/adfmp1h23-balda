package com.ifmo.balda.model

import kotlin.streams.asSequence

object WordBase {
  val nouns by lazy {
    WordBase::class.java.classLoader!!
      .getResourceAsStream("raw/russian_nouns.txt")!!
      .bufferedReader().lines().asSequence()
      .groupBy { it.length }
      .filterKeys { it > 2 }
  }
}
