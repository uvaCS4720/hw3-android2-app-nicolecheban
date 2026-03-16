package edu.nd.pmcburne.hwapp.one

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import edu.nd.pmcburne.hwapp.one.data.GameDatabase
import edu.nd.pmcburne.hwapp.one.data.GameEntity
import edu.nd.pmcburne.hwapp.one.data.toGameItem
import edu.nd.pmcburne.hwapp.one.ui.theme.HelloWorldTheme
import edu.nd.pmcburne.hwapp.one.ui.theme.TurquoiseGrey
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

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
    val gameID: String = "",
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
                    val selectedDate by viewModel.selectedDate.collectAsState()
                    val showMens by viewModel.showMens.collectAsState()
                    val showWomens by viewModel.showWomens.collectAsState()

                    GameListScreen(
                        modifier = Modifier.padding(innerPadding),
                        gameItems = items,
                        isLoading = isLoading,
                        selectedDate = selectedDate,
                        showMens = showMens,
                        showWomens = showWomens,
                        onRefresh = { viewModel.refresh() },
                        onDateChange = { viewModel.updateDate(it) },
                        onMensToggle = { viewModel.toggleMens() },
                        onWomensToggle = { viewModel.toggleWomens() },
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

class ListViewModel(application: Application) : AndroidViewModel(application) {
    private val db = GameDatabase.getDatabase(application)
    private val gameDao = db.gameDao()

    private val _gameItems = MutableStateFlow<List<GameItem>>(emptyList())
    val gameItems: StateFlow<List<GameItem>> = _gameItems

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _selectedDate = MutableStateFlow(Calendar.getInstance())
    val selectedDate: StateFlow<Calendar> = _selectedDate.asStateFlow()

    private val _showMens = MutableStateFlow(false)
    val showMens: StateFlow<Boolean> = _showMens.asStateFlow()

    private val _showWomens = MutableStateFlow(false)
    val showWomens: StateFlow<Boolean> = _showWomens.asStateFlow()

    init {
        refresh()
    }

    fun updateDate(newDate: Calendar) {
        _selectedDate.value = newDate
        refresh()
    }

    fun toggleMens() {
        if (_showMens.value) {
            _showMens.value = false
        } else {
            _showMens.value = true
            _showWomens.value = false
        }
        refresh()
    }

    fun toggleWomens() {
        if (_showWomens.value) {
            _showWomens.value = false
        } else {
            _showWomens.value = true
            _showMens.value = false
        }
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _isLoading.value = true
            val cal = _selectedDate.value
            val dateParts = getDateParts(cal.time)
            val dbDate = "${dateParts.second}/${dateParts.third}/${dateParts.first}"

            // 1. Try to load from Database first for offline support
            loadFromDb(dbDate)

            try {
                val allGamesApi = mutableListOf<GameEntity>()
                val fetchMens = _showMens.value || (!_showMens.value && !_showWomens.value)
                val fetchWomens = _showWomens.value || (!_showMens.value && !_showWomens.value)

                if (fetchMens) {
                    val mens = RetrofitClient.ncaaService.getScoreboard("men", dateParts.first, dateParts.second, dateParts.third)
                    mens.games?.forEach { allGamesApi.add(it.game.toGameEntity(true)) }
                }

                if (fetchWomens) {
                    val womens = RetrofitClient.ncaaService.getScoreboard("women", dateParts.first, dateParts.second, dateParts.third)
                    womens.games?.forEach { allGamesApi.add(it.game.toGameEntity(false)) }
                }

                if (allGamesApi.isNotEmpty()) {
                    // 2. Update Database with new data
                    gameDao.insertGames(allGamesApi)
                    // 3. Refresh UI from Database to ensure consistency
                    loadFromDb(dbDate)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun loadFromDb(date: String) {
        val gamesFromDb = if (!_showMens.value && !_showWomens.value) {
            gameDao.getGamesByDate(date)
        } else {
            gameDao.getGamesByDateAndGender(date, _showMens.value)
        }
        _gameItems.value = gamesFromDb.map { it.toGameItem() }.sortedByDescending { it.date }
    }

    private fun getDateParts(date: Date): Triple<String, String, String> {
        val sdfYear = SimpleDateFormat("yyyy", Locale.US)
        val sdfMonth = SimpleDateFormat("MM", Locale.US)
        val sdfDay = SimpleDateFormat("dd", Locale.US)
        return Triple(sdfYear.format(date), sdfMonth.format(date), sdfDay.format(date))
    }

    private fun NcaaGame.toGameEntity(isMens: Boolean): GameEntity {
        val isUpcoming = gameState.lowercase() == "pre" || gameState.lowercase() == "upcoming"
        return GameEntity(
            gameID = gameID,
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameListScreen(
    modifier: Modifier = Modifier,
    gameItems: List<GameItem>,
    isLoading: Boolean,
    selectedDate: Calendar,
    showMens: Boolean,
    showWomens: Boolean,
    onRefresh: () -> Unit,
    onDateChange: (Calendar) -> Unit,
    onMensToggle: () -> Unit,
    onWomensToggle: () -> Unit,
    onViewDetails: (Int, GameItem) -> Unit
) {
    var showDatePicker by remember { mutableStateOf(false) }
    
    // Use UTC for the DatePicker state to avoid timezone offset issues when converting back to Calendar
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = selectedDate.timeInMillis + TimeZone.getDefault().getOffset(selectedDate.timeInMillis)
    )

    Column(
        modifier = modifier.padding(16.dp).fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().height(48.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "College Basketball Scores",
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(modifier = Modifier.width(16.dp))

            Box(
                modifier = Modifier.size(48.dp),
                contentAlignment = Alignment.Center
            ) {
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
        }

        Spacer(Modifier.height(8.dp))

        // --- Filters Section ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilterChip(
                selected = true,
                onClick = { showDatePicker = true },
                label = { Text(SimpleDateFormat("MMM dd, yyyy", Locale.US).format(selectedDate.time)) },
                leadingIcon = { Icon(Icons.Filled.DateRange, contentDescription = null, modifier = Modifier.size(18.dp)) },
                trailingIcon = { Icon(Icons.Filled.ArrowDropDown, contentDescription = null) }
            )

            FilterChip(
                selected = showMens,
                onClick = onMensToggle,
                label = { Text("Men's") }
            )
            FilterChip(
                selected = showWomens,
                onClick = onWomensToggle,
                label = { Text("Women's") }
            )
        }

        if (showDatePicker) {
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        val selected = datePickerState.selectedDateMillis
                        if (selected != null) {
                            // DatePicker selectedDateMillis is UTC midnight. 
                            // Convert it to local timezone Calendar.
                            val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                                timeInMillis = selected
                            }
                            val localCal = Calendar.getInstance().apply {
                                set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))
                            }
                            onDateChange(localCal)
                        }
                        showDatePicker = false
                    }) {
                        Text("OK")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) {
                        Text("Cancel")
                    }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }

        Spacer(Modifier.height(16.dp))

        if (isLoading && gameItems.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth().weight(1f),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = TurquoiseGrey)
            }
        } else if (gameItems.isEmpty() && !isLoading) {
            Box(
                modifier = Modifier.fillMaxWidth().weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text("No games found for the selected filters.")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth().weight(1f),
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
            GameItem("1", "Notre Dame", "Duke", "03/15/2024", "75 - 70", "70", "75", true, "finished", "7:00 PM", "9:15 PM", "2nd Half", "0:00", "Notre Dame"),
            GameItem("2", "Virginia", "UCLA", "03/16/2024", "20 - 18", "18", "20", false, "currently being played", "1:00 PM", "TBD", "1st Quarter", "5:20", null)
        )
        GameListScreen(
            gameItems = sampleItems,
            isLoading = false,
            selectedDate = Calendar.getInstance(),
            showMens = true,
            showWomens = true,
            onRefresh = {},
            onDateChange = {},
            onMensToggle = {},
            onWomensToggle = {},
            onViewDetails = { _, _ -> }
        )
    }
}
