package id.ideahousetech.prayertime_qibla.service

import id.ideahousetech.prayertime_qibla.service.dto.OverpassResponse
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Interface Retrofit untuk berkomunikasi dengan OpenStreetMap Overpass API
 */
interface OverpassApi {
    @GET("api/interpreter")
    suspend fun query(
        @Query("data") queryString: String
    ): OverpassResponse
}
