package com.ifmo.balda

import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit
import kotlin.random.Random

fun <T> randomTesting(seed: Int, body: (Random) -> T): T {
  return runCatching { body(Random(seed)) }
    .getOrElse {
      throw java.lang.AssertionError("Test failed for seed $seed", it)
    }
}

fun <T> assertCompletes(timeout: Long, timeUnit: TimeUnit, body: () -> T): T {
  return CompletableFuture.supplyAsync(body).get(timeout, timeUnit)
}
