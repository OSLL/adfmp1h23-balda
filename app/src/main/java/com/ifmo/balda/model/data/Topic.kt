package com.ifmo.balda.model.data

import android.content.Context
import com.ifmo.balda.R


sealed class Topic {
  companion object {
    fun byName(name: String, ctx: Context): Topic {
      return if (name == Common.name(ctx)) {
        Common
      } else {
        Theme(name)
      }
    }
  }

  abstract fun name(ctx: Context): String

  object Common : Topic() {
    override fun name(ctx: Context): String {
      return ctx.resources.getString(R.string.topic_common)
    }
  }

  class Theme(val name: String) : Topic() {
    override fun name(ctx: Context) = name
    override fun equals(other: Any?): Boolean {
      if (this === other) return true
      if (javaClass != other?.javaClass) return false

      other as Theme

      if (name != other.name) return false

      return true
    }

    override fun hashCode(): Int {
      return name.hashCode()
    }
  }
}
