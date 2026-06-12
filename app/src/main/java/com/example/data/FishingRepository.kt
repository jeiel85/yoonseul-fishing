package com.example.data

import kotlinx.coroutines.flow.Flow

class FishingRepository(private val caughtFishDao: CaughtFishDao) {
    val allCaughtFish: Flow<List<CaughtFishEntity>> = caughtFishDao.getAllCaughtFish()

    suspend fun insert(fish: CaughtFishEntity) {
        caughtFishDao.insertCaughtFish(fish)
    }

    suspend fun clear() {
        caughtFishDao.clearAll()
    }

    suspend fun delete(id: Int) {
        caughtFishDao.deleteById(id)
    }
}
