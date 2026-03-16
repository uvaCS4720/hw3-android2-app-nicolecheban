package edu.nd.pmcburne.hwapp.one.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import edu.nd.pmcburne.hwapp.one.GameItem

// --- Database Entity ---
@Entity(tableName = "games")
data class GameEntity(
    @PrimaryKey val gameID: String,
    val homeTeam: String,
    val awayTeam: String,
    val date: String,
    val score: String,
    val homeScore: String,
    val awayScore: String,
    val isMens: Boolean,
    val status: String,
    val startTime: String,
    val endTime: String,
    val currentPeriod: String,
    val timeRemaining: String,
    val winner: String?
)

// --- Data Access Object ---
@Dao
interface GameDao {
    @Query("SELECT * FROM games WHERE date = :date AND isMens = :isMens")
    suspend fun getGamesByDateAndGender(date: String, isMens: Boolean): List<GameEntity>

    @Query("SELECT * FROM games WHERE date = :date")
    suspend fun getGamesByDate(date: String): List<GameEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGames(games: List<GameEntity>)
}

// --- Room Database Implementation ---
@Database(entities = [GameEntity::class], version = 1)
abstract class GameDatabase : RoomDatabase() {
    abstract fun gameDao(): GameDao

    companion object {
        @Volatile
        private var INSTANCE: GameDatabase? = null

        fun getDatabase(context: Context): GameDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    GameDatabase::class.java,
                    "game_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

// --- Mapper Extension ---
fun GameEntity.toGameItem() = GameItem(
    gameID = gameID,
    homeTeam = homeTeam,
    awayTeam = awayTeam,
    date = date,
    score = score,
    homeScore = homeScore,
    awayScore = awayScore,
    isMens = isMens,
    status = status,
    startTime = startTime,
    endTime = endTime,
    currentPeriod = currentPeriod,
    timeRemaining = timeRemaining,
    winner = winner
)
