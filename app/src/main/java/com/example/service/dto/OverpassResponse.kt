package id.ideahousetech.prayertime_qibla.service.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Data Transfer Object untuk menangani respons JSON dari OpenStreetMap Overpass API
 */
@JsonClass(generateAdapter = true)
data class OverpassResponse(
    @Json(name = "elements") val elements: List<OverpassElement>? = null
)

@JsonClass(generateAdapter = true)
data class OverpassElement(
    @Json(name = "type") val type: String,              // "node" atau "way"
    @Json(name = "id") val id: Long,
    @Json(name = "lat") val lat: Double? = null,        // Ada jika type == "node"
    @Json(name = "lon") val lon: Double? = null,        // Ada jika type == "node"
    @Json(name = "center") val center: OverpassCenter? = null, // Ada jika type == "way"
    @Json(name = "tags") val tags: OverpassTags? = null
)

@JsonClass(generateAdapter = true)
data class OverpassCenter(
    @Json(name = "lat") val lat: Double,
    @Json(name = "lon") val lon: Double
)

@JsonClass(generateAdapter = true)
data class OverpassTags(
    @Json(name = "name") val name: String? = null,
    @Json(name = "addr:street") val addrStreet: String? = null,
    @Json(name = "addr:full") val addrFull: String? = null
)
