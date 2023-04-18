package com.ifmo.balda.model.data

import androidx.core.os.LocaleListCompat

class Lang private constructor(val code: String) {
  companion object {
    val list
      get() = (0 until LocaleListCompat.getDefault().size())
        .map { Lang(LocaleListCompat.getDefault()[it]!!.language) }

    val default get() = list.first()

    fun byCode(code: String): Lang =
      list.single { it.code == code }
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
