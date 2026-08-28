package id.ideahousetech.prayertime_qibla.service.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class NearbySearchResponse(
    @Json(name = "results") val results: List<PlaceResult>?,
    @Json(name = "status") val status: String,
    @Json(name = "next_page_token") val nextPageToken: String?,
    @Json(name = "error_message") val errorMessage: String?
)

@JsonClass(generateAdapter = true)
data class PlaceResult(
    @Json(name = "place_id") val placeId: String,
    @Json(name = "name") val name: String,
    @Json(name = "vicinity") val vicinity: String?,
    @Json(name = "geometry") val geometry: PlaceGeometry?,
    @Json(name = "rating") val rating: Double?,
    @Json(name = "opening_hours") val openingHours: OpeningHours?,
    @Json(name = "photos") val photos: List<PlacePhoto>?,
    @Json(name = "types") val types: List<String>?
)

@JsonClass(generateAdapter = true)
data class PlaceGeometry(
    @Json(name = "location") val location: PlaceLocation?
)

@JsonClass(generateAdapter = true)
data class PlaceLocation(
    @Json(name = "lat") val lat: Double,
    @Json(name = "lng") val lng: Double
)

@JsonClass(generateAdapter = true)
data class OpeningHours(
    @Json(name = "open_now") val openNow: Boolean?
)

@JsonClass(generateAdapter = true)
data class PlacePhoto(
    @Json(name = "photo_reference") val photoReference: String?,
    @Json(name = "height") val height: Int?,
    @Json(name = "width") val width: Int?
)
