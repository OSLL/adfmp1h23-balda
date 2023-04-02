package com.ifmo.balda.model.dao

import androidx.room.Dao
import androidx.room.Query
import com.ifmo.balda.model.entity.Stat

@Dao
interface StatDao {
  @Query("select * from stat")
  suspend fun getStat(): List<Stat>
}
