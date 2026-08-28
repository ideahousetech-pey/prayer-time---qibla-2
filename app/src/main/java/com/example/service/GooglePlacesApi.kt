package id.ideahousetech.prayertime_qibla.service

import id.ideahousetech.prayertime_qibla.service.dto.NearbySearchResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface GooglePlacesApi {
    @GET("maps/api/place/nearbysearch/json")
    suspend fun searchNearbyMosques(
        @Query("location") location: String,
        @Query("radius") radius: Int,
        @Query("type") type: String = "mosque",
        @Query("keyword") keyword: String = "masjid|musholla",
        @Query("language") language: String = "id",
        @Query("key") key: String
    ): NearbySearchResponse

    @GET("maps/api/place/nearbysearch/json")
    suspend fun searchNextPage(
        @Query("pagetoken") pageToken: String,
        @Query("key") key: String
    ): NearbySearchResponse
}
