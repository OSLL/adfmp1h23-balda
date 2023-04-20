package com.ifmo.balda.model.data

import android.content.Context
import java.nio.file.Path
import kotlin.io.path.Path

class Dictionaries private constructor(
  private val dictionaries: Map<Lang, Map<Topic, Path>>
) {
  companion object {
    private const val dictDir = "dict"
    private val commonDictRegex = Regex("""([a-z]+)\.txt""")
    private val thematicDictRegex = Regex("""([a-z]+)-(.+)\.txt""")

    @LargeIO
    fun loadMeta(ctx: Context): Dictionaries {
      val dictionaries = ctx.assets.list(dictDir).orEmpty()

      val commonDictionaries = dictionaries
        .mapNotNull { commonDictRegex.matchEntire(it) }
        .associateBy(keySelector = { Lang(it.groupValues[1]) }, valueTransform = { Path(dictDir, it.value) })

      val thematicDictionaries = dictionaries
        .mapNotNull { thematicDictRegex.matchEntire(it) }
        .groupBy(keySelector = { Lang(it.groupValues[1]) })
        .mapValues { (_, matches) ->
          matches.associateBy(
            keySelector = { Topic.Theme(it.groupValues[2]) },
            valueTransform = { Path(dictDir, it.value) }
          )
        }

      return Dictionaries(
        buildMap {
          for (lang in commonDictionaries.keys + thematicDictionaries.keys) {
            this[lang] = buildMap {
              commonDictionaries[lang]?.let { commonPath -> put(Topic.Common, commonPath) }
              thematicDictionaries[lang]?.let { thematicPaths -> putAll(thematicPaths) }
            }
          }
        }
      )
    }
  }

  val languages: Set<Lang> = dictionaries.keys

  fun topics(lang: Lang): Set<Topic> {
    return dictionaries[lang]?.keys.orEmpty()
  }

  /**
   * Returns words with their frequencies.
   */
  @LargeIO
  fun loadDictionary(lang: Lang, topic: Topic, context: Context): Map<String, Int> {
//    if (topic == Topic.Everything) {
//      // todo
//    }

    val path = dictionaries[lang]?.get(topic)
      ?: error("Unknown lang ${lang.code} with topic ${topic.name(context)}")

    return context.assets.open(path.toString()).bufferedReader()
      .lineSequence()
      .associate { it.takeWhile { it != ',' } to it.takeLastWhile { it != ',' }.toInt() }
  }
}

@Volatile
private var dictionaries_: Dictionaries? = null

val Context.dictionaries: Dictionaries
  @LargeIO
  get() = dictionaries_ ?: synchronized(applicationContext) {
    dictionaries_ ?: Dictionaries.loadMeta(applicationContext).also { dictionaries_ = it }
  }
