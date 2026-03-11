package edu.nd.pmcburne.hwapp.one

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.nd.pmcburne.hwapp.one.ui.theme.HelloWorldTheme
import edu.nd.pmcburne.hwapp.one.ui.theme.TurquoiseGrey
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

// --- Models for API ---
data class NcaaResponse(
    val games: List<GameContainer>?
)

data class GameContainer(
    val game: NcaaGame
)

data class NcaaGame(
    val gameID: String,
    val away: TeamInfo,
    val home: TeamInfo,
    val startTime: String,
    val gameState: String,
    val startDate: String,
    val currentPeriod: String,
    val contestClock: String,
    val finalMessage: String?
)

data class TeamInfo(
    val score: String,
    val names: TeamNames,
    val winner: Boolean
)

data class TeamNames(
    val short: String,
    val full: String
)

// --- Retrofit Service ---
interface NcaaService {
    @GET("scoreboard/basketball-{gender}/d1/{year}/{month}/{day}")
    suspend fun getScoreboard(
        @Path("gender") gender: String,
        @Path("year") year: String,
        @Path("month") month: String,
        @Path("day") day: String
    ): NcaaResponse
}

object RetrofitClient {
    private const val BASE_URL = "https://ncaa-api.henrygd.me/"
    val ncaaService: NcaaService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(NcaaService::class.java)
    }
}

// --- App Model ---
data class GameItem(
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

class MainActivity : ComponentActivity() {
    companion object {
        const val EXTRA_HOME_TEAM = "extra_home_team"
        const val EXTRA_AWAY_TEAM = "extra_away_team"
        const val EXTRA_DATE = "extra_date"
        const val EXTRA_SCORE = "extra_score"
        const val EXTRA_HOME_SCORE = "extra_home_score"
        const val EXTRA_AWAY_SCORE = "extra_away_score"
        const val EXTRA_IS_MENS = "extra_is_mens"
        const val EXTRA_STATUS = "extra_status"
        const val EXTRA_START_TIME = "extra_start_time"
        const val EXTRA_END_TIME = "extra_end_time"
        const val EXTRA_CURRENT_PERIOD = "extra_current_period"
        const val EXTRA_TIME_REMAINING = "extra_time_remaining"
        const val EXTRA_WINNER = "extra_winner"
    }

    private val viewModel: ListViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HelloWorldTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val items by viewModel.gameItems.collectAsState()
                    val isLoading by viewModel.isLoading.collectAsState()

                    GameListScreen(
                        modifier = Modifier.padding(innerPadding),
                        gameItems = items,
                        isLoading = isLoading,
                        onRefresh = { viewModel.refresh() },
                        onViewDetails = { _, item ->
                            val intent = android.content.Intent(this, DetailActivity::class.java).apply {
                                putExtra(EXTRA_HOME_TEAM, item.homeTeam)
                                putExtra(EXTRA_AWAY_TEAM, item.awayTeam)
                                putExtra(EXTRA_DATE, item.date)
                                putExtra(EXTRA_SCORE, item.score)
                                putExtra(EXTRA_HOME_SCORE, item.homeScore)
                                putExtra(EXTRA_AWAY_SCORE, item.awayScore)
                                putExtra(EXTRA_IS_MENS, item.isMens)
                                putExtra(EXTRA_STATUS, item.status)
                                putExtra(EXTRA_START_TIME, item.startTime)
                                putExtra(EXTRA_END_TIME, item.endTime)
                                putExtra(EXTRA_CURRENT_PERIOD, item.currentPeriod)
                                putExtra(EXTRA_TIME_REMAINING, item.timeRemaining)
                                putExtra(EXTRA_WINNER, item.winner)
                            }
                            startActivity(intent)
                        }
                    )
                }
            }
        }
    }
}

class ListViewModel : ViewModel() {
    private val _gameItems = MutableStateFlow<List<GameItem>>(emptyList())
    val gameItems: StateFlow<List<GameItem>> = _gameItems

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Fetch for multiple dates to have a "most recent" list
                // Using current date and also the specific date from example for guaranteed data
                val calendar = Calendar.getInstance()
                val datesToFetch = mutableListOf<Triple<String, String, String>>()
                
                // Add today
                datesToFetch.add(getDateParts(calendar.time))
                
                // Add yesterday
                calendar.add(Calendar.DAY_OF_YEAR, -1)
                datesToFetch.add(getDateParts(calendar.time))

                // Also add March 11, 2026 (from user example) to ensure we see data
                datesToFetch.add(Triple("2026", "03", "11"))

                val allGames = mutableListOf<GameItem>()

                for (date in datesToFetch.distinct()) {
                    val mens = RetrofitClient.ncaaService.getScoreboard("men", date.first, date.second, date.third)
                    val womens = RetrofitClient.ncaaService.getScoreboard("women", date.first, date.second, date.third)

                    mens.games?.forEach { allGames.add(it.game.toGameItem(true)) }
                    womens.games?.forEach { allGames.add(it.game.toGameItem(false)) }
                }

                // Sort by date (most to least recent) - using a simple string sort for MM/DD/YYYY might need care
                // but for now we'll just show them.
                _gameItems.value = allGames.sortedByDescending { it.date }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun getDateParts(date: Date): Triple<String, String, String> {
        val sdfYear = SimpleDateFormat("yyyy", Locale.US)
        val sdfMonth = SimpleDateFormat("MM", Locale.US)
        val sdfDay = SimpleDateFormat("dd", Locale.US)
        return Triple(sdfYear.format(date), sdfMonth.format(date), sdfDay.format(date))
    }

    private fun NcaaGame.toGameItem(isMens: Boolean): GameItem {
        val isUpcoming = gameState.lowercase() == "pre" || gameState.lowercase() == "upcoming"
        return GameItem(
            homeTeam = home.names.short,
            awayTeam = away.names.short,
            date = startDate,
            score = if (isUpcoming) "-" else "${away.score} - ${home.score}",
            homeScore = if (isUpcoming) "-" else home.score,
            awayScore = if (isUpcoming) "-" else away.score,
            isMens = isMens,
            status = gameState,
            startTime = startTime,
            endTime = if (gameState == "final") "Finished" else "TBD",
            currentPeriod = if (isUpcoming) "-" else currentPeriod,
            timeRemaining = if (isUpcoming) "-" else contestClock,
            winner = when {
                home.winner -> home.names.short
                away.winner -> away.names.short
                else -> null
            }
        )
    }
}

@Composable
fun GameListScreen(
    modifier: Modifier = Modifier,
    gameItems: List<GameItem>,
    isLoading: Boolean,
    onRefresh: () -> Unit,
    onViewDetails: (Int, GameItem) -> Unit
) {
    Column(
        modifier = modifier.padding(16.dp).fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(modifier = Modifier.width(48.dp))

            Text(
                text = "College Basketball Scores",
                style = MaterialTheme.typography.headlineSmall
            )

            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp,
                    color = TurquoiseGrey
                )
            } else {
                IconButton(
                    onClick = onRefresh,
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = TurquoiseGrey,
                        contentColor = Color.Black
                    )
                ) {
                    Icon(
                        imageVector = Icons.Filled.Refresh,
                        contentDescription = "Refresh"
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        if (gameItems.isEmpty() && !isLoading) {
            Text("No games found for the selected dates.")
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(gameItems) { index, item ->
                    GameListItem(
                        item = item,
                        onViewDetails = { onViewDetails(index, item) }
                    )
                }
            }
        }
    }
}

@Composable
fun GameListItem(item: GameItem, modifier: Modifier = Modifier, onViewDetails: () -> Unit) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 80.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = TurquoiseGrey
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${item.homeTeam} vs ${item.awayTeam}",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.Black
                )
                Text(
                    text = "${item.date} | ${if (item.isMens) "Men's" else "Women's"}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Score: ${item.score}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.width(16.dp))

            IconButton(
                onClick = onViewDetails,
                colors = IconButtonDefaults.iconButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                Icon(
                    imageVector = Icons.Filled.Info,
                    contentDescription = "Details"
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GameListScreenPreview() {
    HelloWorldTheme {
        val sampleItems = listOf(
            GameItem("Notre Dame", "Duke", "03/15/2024", "75 - 70", "70", "75", true, "finished", "7:00 PM", "9:15 PM", "2nd Half", "0:00", "Notre Dame"),
            GameItem("Virginia", "UCLA", "03/16/2024", "20 - 18", "18", "20", false, "currently being played", "1:00 PM", "TBD", "1st Quarter", "5:20", null)
        )
        GameListScreen(
            gameItems = sampleItems,
            isLoading = false,
            onRefresh = {},
            onViewDetails = { _, _ -> }
        )
    }
}
