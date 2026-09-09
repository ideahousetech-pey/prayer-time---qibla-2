package com.example

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import id.ideahousetech.prayertime_qibla.model.Mosque
import id.ideahousetech.prayertime_qibla.service.MosqueService
import id.ideahousetech.prayertime_qibla.service.dto.OverpassCenter
import id.ideahousetech.prayertime_qibla.service.dto.OverpassElement
import id.ideahousetech.prayertime_qibla.service.dto.OverpassResponse
import id.ideahousetech.prayertime_qibla.service.dto.OverpassTags
import org.junit.Assert.*
import org.junit.Test

class OverpassMosqueServiceTest {

    private val mosqueService = MosqueService()

    @Test
    fun testMosqueModelIsMockDataField() {
        val realMosque = Mosque(
            placeId = "osm_12345",
            name = "Masjid Raya",
            address = "Jl. Merdeka No. 1",
            lat = -6.2000,
            lon = 106.8166,
            distanceMeters = 350.0,
            rating = null,
            isOpen = null,
            isMockData = false
        )
        assertFalse(realMosque.isMockData)

        val mockMosque = Mosque(
            placeId = "mock_1",
            name = "Masjid Mock",
            address = "Jl. Contoh",
            lat = -6.2000,
            lon = 106.8166,
            distanceMeters = 500.0,
            rating = 4.5,
            isOpen = true,
            isMockData = true
        )
        assertTrue(mockMosque.isMockData)
    }

    @Test
    fun testMoshiJsonParsingOverpassResponse() {
        val json = """
            {
              "elements": [
                {
                  "type": "node",
                  "id": 101,
                  "lat": -6.2088,
                  "lon": 106.8456,
                  "tags": {
                    "name": "Masjid Al-Barkah",
                    "addr:street": "Jl. Barkah No. 5"
                  }
                },
                {
                  "type": "way",
                  "id": 202,
                  "center": {
                    "lat": -6.2100,
                    "lon": 106.8500
                  },
                  "tags": {
                    "name": "Masjid Agung Al-Falah",
                    "addr:full": "Jl. Protokol No. 10 Jakarta"
                  }
                },
                {
                  "type": "node",
                  "id": 303,
                  "lat": -6.2120,
                  "lon": 106.8520,
                  "tags": {
                    "name": null
                  }
                }
              ]
            }
        """.trimIndent()

        val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
        val adapter = moshi.adapter(OverpassResponse::class.java)
        val response = adapter.fromJson(json)

        assertNotNull(response)
        assertNotNull(response?.elements)
        assertEquals(3, response?.elements?.size)

        val nodeElement = response?.elements?.get(0)
        assertEquals("node", nodeElement?.type)
        assertEquals(101L, nodeElement?.id)
        assertEquals(-6.2088, nodeElement?.lat ?: 0.0, 0.0001)
        assertEquals("Masjid Al-Barkah", nodeElement?.tags?.name)
        assertEquals("Jl. Barkah No. 5", nodeElement?.tags?.addrStreet)

        val wayElement = response?.elements?.get(1)
        assertEquals("way", wayElement?.type)
        assertEquals(202L, wayElement?.id)
        assertNotNull(wayElement?.center)
        assertEquals(-6.2100, wayElement?.center?.lat ?: 0.0, 0.0001)
        assertEquals("Masjid Agung Al-Falah", wayElement?.tags?.name)
        assertEquals("Jl. Protokol No. 10 Jakarta", wayElement?.tags?.addrFull)
    }

    @Test
    fun testHaversineDistance() {
        // Jarak Monas (-6.175392, 106.827153) ke Masjid Istiqlal (-6.1702, 106.8315) sekitar 700-900 meter
        val distance = mosqueService.haversineDistance(-6.175392, 106.827153, -6.1702, 106.8315)
        assertTrue("Distance should be between 500m and 1200m, actual: $distance", distance in 500.0..1200.0)
    }

    @Test
    fun testCoordinateValidation() {
        assertTrue(mosqueService.isValidCoordinate(-6.2000, 106.8166))
        assertFalse(mosqueService.isValidCoordinate(0.0, 0.0))
        assertFalse(mosqueService.isValidCoordinate(95.0, 106.8166))
        assertFalse(mosqueService.isValidCoordinate(-6.2000, 185.0))
    }
}
