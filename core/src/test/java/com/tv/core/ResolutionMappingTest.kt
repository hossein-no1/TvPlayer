package com.tv.core

import mapToResolutionList
import org.junit.Assert.assertEquals
import org.junit.Test

class ResolutionMappingTest {

    @Test
    fun testMapToResolutionList() {
        // Arrange
        val bandwidthList = listOf(200, 500, 1000, 1500, 3000)
        val expectedMap =
            mapOf(200 to "240p", 500 to "360p", 1000 to "720p", 1500 to "1080p", 3000 to "2160p")

        // Act
        val result = mapToResolutionList(bandwidthList)

        // Assert
        assertEquals(expectedMap, result)
    }

    @Test
    fun testMapToResolutionListWithEmptyInput() {
        // Arrange
        val bandwidthList = emptyList<Int>()
        val expectedMap = emptyMap<Int, String>()

        // Act
        val result = mapToResolutionList(bandwidthList)

        // Assert
        assertEquals(expectedMap, result)
    }

    @Test
    fun testMapToResolutionListWithDuplicates() {
        // Arrange
        val bandwidthList = listOf(1000, 1500, 2000, 1500, 1000)
        val expectedMap = mapOf(1000 to "720p", 1500 to "1080p", 2000 to "1440p")

        // Act
        val result = mapToResolutionList(bandwidthList)

        // Assert
        assertEquals(expectedMap, result)
    }
}
