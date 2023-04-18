package com.ifmo.balda.model.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.ifmo.balda.model.entity.Stat

@Dao
abstract class StatDao {
  @Query("select * from stat")
  abstract suspend fun getStat(): List<Stat>

  @Query("select * from stat where name = :name")
  protected abstract suspend fun getStatByNameQuery(name: String): List<Stat>

  @Insert
  protected abstract suspend fun insertNewStat(stat: Stat)

  @Update
  protected abstract suspend fun updateStat(stat: Stat)

  private suspend fun getStatByName(name: String): Stat? {
    val stat = getStatByNameQuery(name)
    return if (stat.isEmpty()) null else stat[0]
  }

  suspend fun updateStats(vararg stats: Stat) {
    for (p in stats) {
      val s = getStatByName(p.name)
      if (s == null) insertNewStat(p) else updateStat(Stat(s.name, s.score + p.score))
    }
  }
}
