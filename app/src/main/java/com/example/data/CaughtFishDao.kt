package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CaughtFishDao {
    @Query("SELECT * FROM caught_fish ORDER BY caughtTime DESC")
    fun getAllCaughtFish(): Flow<List<CaughtFishEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCaughtFish(fish: CaughtFishEntity)

    @Query("DELETE FROM caught_fish")
    suspend fun clearAll()

    @Query("DELETE FROM caught_fish WHERE id = :id")
    suspend fun deleteById(id: Int)
}
