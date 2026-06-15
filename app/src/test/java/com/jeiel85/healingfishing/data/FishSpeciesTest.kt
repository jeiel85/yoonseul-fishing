package com.jeiel85.healingfishing.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Validates the integrity of the fish species catalog that drives the dex,
 * spawn tables and reward logic. Pure-data tests — no Android dependencies.
 */
class FishSpeciesTest {

  private val species = FishSpecies.list

  @Test
  fun catalog_isNotEmpty() {
    assertTrue("Species catalog should not be empty", species.isNotEmpty())
  }

  @Test
  fun ids_areUnique() {
    val ids = species.map { it.id }
    assertEquals("Duplicate species ids found", ids.size, ids.toSet().size)
  }

  @Test
  fun lengthAndWeightRanges_areValid() {
    species.forEach { fish ->
      assertTrue("${fish.id}: length min must be > 0", fish.baseLengthMin > 0f)
      assertTrue("${fish.id}: length min must be <= max", fish.baseLengthMin <= fish.baseLengthMax)
      assertTrue("${fish.id}: weight min must be > 0", fish.baseWeightMin > 0f)
      assertTrue("${fish.id}: weight min must be <= max", fish.baseWeightMin <= fish.baseWeightMax)
    }
  }

  @Test
  fun localizedFields_areNeverBlank() {
    species.forEach { fish ->
      assertTrue("${fish.id}: Korean name blank", fish.name.isNotBlank())
      assertTrue("${fish.id}: English name blank", fish.nameEn.isNotBlank())
      assertTrue("${fish.id}: Korean description blank", fish.description.isNotBlank())
      assertTrue("${fish.id}: English description blank", fish.descriptionEn.isNotBlank())
    }
  }

  @Test
  fun rarityValues_areWithinAllowedSet() {
    val allowed = setOf("일반", "희귀", "전설", "신화")
    species.forEach { fish ->
      assertTrue("${fish.id}: unknown rarity '${fish.rarity}'", fish.rarity in allowed)
    }
  }

  @Test
  fun find_returnsMatchingSpecies_orNull() {
    val first = species.first()
    assertEquals(first, FishSpecies.find(first.id))
    assertNotNull(FishSpecies.find(first.id))
    assertNull(FishSpecies.find("does_not_exist"))
  }
}
