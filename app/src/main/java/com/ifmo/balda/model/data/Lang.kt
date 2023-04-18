package com.ifmo.balda.model.data

import android.content.Context

class Lang(val code: String) {
  companion object {
    @LargeIO
    fun list(ctx: Context): List<Lang> =
      ctx.dictionaries.languages.toList()

    @LargeIO
    fun default(ctx: Context): Lang =
      list(ctx).let { list ->
        list.find { it.code.lowercase() == "ru" } ?: list.first()
      }

    @LargeIO
    fun byCode(code: String, ctx: Context): Lang =
      list(ctx).single { it.code == code }
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as Lang

    if (code != other.code) return false

    return true
  }

  override fun hashCode(): Int {
    return code.hashCode()
  }
}
