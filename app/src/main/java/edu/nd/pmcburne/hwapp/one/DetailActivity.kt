package edu.nd.pmcburne.hwapp.one

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import edu.nd.pmcburne.hwapp.one.ui.theme.HelloWorldTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class DetailActivity : ComponentActivity() {

    private val viewModel: DetailViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize the ViewModel with data from the Intent
        viewModel.initializeFromIntent(intent)

        enableEdgeToEdge()
        setContent {
            HelloWorldTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    // Observe state from the ViewModel
                    val homeTeam by viewModel.homeTeam.collectAsState()
                    val awayTeam by viewModel.awayTeam.collectAsState()
                    val date by viewModel.date.collectAsState()
                    val status by viewModel.status.collectAsState()
                    val score by viewModel.score.collectAsState()
                    val startTime by viewModel.startTime.collectAsState()
                    val currentPeriod by viewModel.currentPeriod.collectAsState()
                    val timeRemaining by viewModel.timeRemaining.collectAsState()
                    val winner by viewModel.winner.collectAsState()
                    val isMens by viewModel.isMens.collectAsState()

                    GameDetailsScreen(
                        modifier = Modifier.padding(innerPadding),
                        homeTeam = homeTeam,
                        awayTeam = awayTeam,
                        date = date,
                        status = status,
                        score = score,
                        startTime = startTime,
                        currentPeriod = currentPeriod,
                        timeRemaining = timeRemaining,
                        winner = winner,
                        isMens = isMens,
                        onBack = { finish() }
                    )
                }
            }
        }
    }
}

class DetailViewModel : ViewModel() {
    private val _homeTeam = MutableStateFlow("")
    val homeTeam: StateFlow<String> = _homeTeam.asStateFlow()

    private val _awayTeam = MutableStateFlow("")
    val awayTeam: StateFlow<String> = _awayTeam.asStateFlow()

    private val _date = MutableStateFlow("")
    val date: StateFlow<String> = _date.asStateFlow()

    private val _status = MutableStateFlow("")
    val status: StateFlow<String> = _status.asStateFlow()

    private val _score = MutableStateFlow("")
    val score: StateFlow<String> = _score.asStateFlow()

    private val _startTime = MutableStateFlow("")
    val startTime: StateFlow<String> = _startTime.asStateFlow()

    private val _currentPeriod = MutableStateFlow("")
    val currentPeriod: StateFlow<String> = _currentPeriod.asStateFlow()

    private val _timeRemaining = MutableStateFlow("")
    val timeRemaining: StateFlow<String> = _timeRemaining.asStateFlow()

    private val _winner = MutableStateFlow<String?>(null)
    val winner: StateFlow<String?> = _winner.asStateFlow()

    private val _isMens = MutableStateFlow(true)
    val isMens: StateFlow<Boolean> = _isMens.asStateFlow()

    fun initializeFromIntent(intent: Intent) {
        _homeTeam.value = intent.getStringExtra(MainActivity.EXTRA_HOME_TEAM) ?: ""
        _awayTeam.value = intent.getStringExtra(MainActivity.EXTRA_AWAY_TEAM) ?: ""
        _date.value = intent.getStringExtra(MainActivity.EXTRA_DATE) ?: ""
        
        // Map raw status to capitalized/formatted versions
        val rawStatus = intent.getStringExtra(MainActivity.EXTRA_STATUS) ?: ""
        _status.value = when (rawStatus.lowercase()) {
            "upcoming", "pre" -> "Upcoming"
            "currently being played", "live" -> "In progress"
            "finished", "final" -> "Finished"
            else -> rawStatus.replaceFirstChar { it.uppercase() }
        }
        
        _score.value = intent.getStringExtra(MainActivity.EXTRA_SCORE) ?: ""
        _startTime.value = intent.getStringExtra(MainActivity.EXTRA_START_TIME) ?: ""
        _isMens.value = intent.getBooleanExtra(MainActivity.EXTRA_IS_MENS, true)

        // Handle period formatting (Halves for Men, Quarters for Women)
        val rawPeriod = intent.getStringExtra(MainActivity.EXTRA_CURRENT_PERIOD) ?: ""
        _currentPeriod.value = if (_status.value == "Finished") "FINAL" else formatPeriod(rawPeriod, _isMens.value)
        
        // Display "FINAL" if the game is finished, otherwise use the provided time remaining
        val rawTimeRemaining = intent.getStringExtra(MainActivity.EXTRA_TIME_REMAINING) ?: ""
        _timeRemaining.value = if (_status.value == "Finished") "FINAL" else rawTimeRemaining
        
        _winner.value = intent.getStringExtra(MainActivity.EXTRA_WINNER)
    }

    private fun formatPeriod(period: String, isMens: Boolean): String {
        if (period.lowercase() == "final") return "FINAL"
        
        val periodInt = period.toIntOrNull() ?: return period // Return as is if already formatted or non-numeric (e.g., "OT")
        
        return if (isMens) {
            when (periodInt) {
                1 -> "1st Half"
                2 -> "2nd Half"
                else -> "${periodInt - 2} OT"
            }
        } else {
            when (periodInt) {
                1 -> "1st Quarter"
                2 -> "2nd Quarter"
                3 -> "3rd Quarter"
                4 -> "4th Quarter"
                else -> "${periodInt - 4} OT"
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = value,
            modifier = Modifier.weight(1.5f),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun GameDetailsScreen(
    modifier: Modifier = Modifier,
    homeTeam: String,
    awayTeam: String,
    date: String,
    status: String,
    score: String,
    startTime: String,
    currentPeriod: String,
    timeRemaining: String,
    winner: String?,
    isMens: Boolean,
    onBack: () -> Unit
) {
    Column(
        modifier = modifier
            .padding(16.dp)
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "$homeTeam vs $awayTeam",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            
            Text(
                text = date,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Table Container
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outline, MaterialTheme.shapes.medium)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), MaterialTheme.shapes.medium)
            ) {
                DetailRow(label = "Home Team", value = homeTeam)
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                
                DetailRow(label = "Away Team", value = awayTeam)
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))

                DetailRow(label = "Category", value = if (isMens) "Men's" else "Women's")
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))

                DetailRow(label = "Status", value = status)
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))

                DetailRow(label = "Score", value = score)
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))

                DetailRow(label = "Start Time", value = startTime)
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))

                DetailRow(label = "Current Period", value = currentPeriod)
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))

                DetailRow(label = "Time Remaining", value = timeRemaining)
                
                if (winner != null) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                    DetailRow(label = "Winner", value = winner)
                }
            }
        }

        // Back button
        Button(
            onClick = onBack,
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .height(50.dp)
        ) {
            Text(
                text = "Back to Games",
                style = MaterialTheme.typography.titleMedium
            )
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Preview(showBackground = true)
@Composable
fun GameDetailsScreenPreview() {
    HelloWorldTheme {
        GameDetailsScreen(
            homeTeam = "Notre Dame",
            awayTeam = "Duke",
            date = "03/20/2026",
            status = "Upcoming",
            score = "0 - 0",
            startTime = "8:00 PM",
            currentPeriod = "N/A",
            timeRemaining = "N/A",
            winner = null,
            isMens = true,
            onBack = {}
        )
    }
}
