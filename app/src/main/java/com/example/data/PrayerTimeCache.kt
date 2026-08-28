package id.ideahousetech.prayertime_qibla.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "prayer_cache", primaryKeys = ["year", "month"])
data class PrayerTimeCache(
    val year: Int,
    val month: Int,
    val latitude: Double,
    val longitude: Double,
    val jsonData: String,       // JSON serialized List<PrayerTime>
    val cachedAt: Long = System.currentTimeMillis()
)

@Dao
interface PrayerCacheDao {
    @Query("SELECT * FROM prayer_cache WHERE year = :year AND month = :month LIMIT 1")
    suspend fun getCache(year: Int, month: Int): PrayerTimeCache?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCache(cache: PrayerTimeCache)

    @Query("DELETE FROM prayer_cache WHERE cachedAt < :expiry")
    suspend fun clearExpiredCache(expiry: Long)
}

@Database(entities = [PrayerTimeCache::class, TasbihSession::class, PrayerTracker::class], version = 3, exportSchema = false)
@TypeConverters(PrayerStatusConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun prayerCacheDao(): PrayerCacheDao
    abstract fun tasbihDao(): TasbihDao
    abstract fun prayerTrackerDao(): PrayerTrackerDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: android.content.Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "prayer_qibla_db"
                ).fallbackToDestructiveMigration().build().also { INSTANCE = it }
            }
        }
    }
}

@Entity(tableName = "tasbih_history")
data class TasbihSession(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val dzikirName: String,     // Nama dzikir (e.g. "Subhanallah")
    val count: Int,             // Jumlah dzikir
    val timestamp: Long = System.currentTimeMillis() // Waktu simpan
)

@Dao
interface TasbihDao {
    @Query("SELECT * FROM tasbih_history ORDER BY timestamp DESC")
    fun getAllSessions(): Flow<List<TasbihSession>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: TasbihSession)

    @Query("DELETE FROM tasbih_history WHERE id = :id")
    suspend fun deleteSession(id: Int)

    @Query("DELETE FROM tasbih_history")
    suspend fun clearHistory()
}

