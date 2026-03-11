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
                    val status by viewModel.status.collectAsState()
                    val score by viewModel.score.collectAsState()
                    val startTime by viewModel.startTime.collectAsState()
                    val endTime by viewModel.endTime.collectAsState()
                    val currentPeriod by viewModel.currentPeriod.collectAsState()
                    val timeRemaining by viewModel.timeRemaining.collectAsState()
                    val winner by viewModel.winner.collectAsState()
                    val isMens by viewModel.isMens.collectAsState()

                    GameDetailsScreen(
                        modifier = Modifier.padding(innerPadding),
                        homeTeam = homeTeam,
                        awayTeam = awayTeam,
                        status = status,
                        score = score,
                        startTime = startTime,
                        endTime = endTime,
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

    private val _status = MutableStateFlow("")
    val status: StateFlow<String> = _status.asStateFlow()

    private val _score = MutableStateFlow("")
    val score: StateFlow<String> = _score.asStateFlow()

    private val _startTime = MutableStateFlow("")
    val startTime: StateFlow<String> = _startTime.asStateFlow()

    private val _endTime = MutableStateFlow("")
    val endTime: StateFlow<String> = _endTime.asStateFlow()

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
        _status.value = intent.getStringExtra(MainActivity.EXTRA_STATUS) ?: ""
        _score.value = intent.getStringExtra(MainActivity.EXTRA_SCORE) ?: ""
        _startTime.value = intent.getStringExtra(MainActivity.EXTRA_START_TIME) ?: ""
        _endTime.value = intent.getStringExtra(MainActivity.EXTRA_END_TIME) ?: ""
        _currentPeriod.value = intent.getStringExtra(MainActivity.EXTRA_CURRENT_PERIOD) ?: ""
        _timeRemaining.value = intent.getStringExtra(MainActivity.EXTRA_TIME_REMAINING) ?: ""
        _winner.value = intent.getStringExtra(MainActivity.EXTRA_WINNER)
        _isMens.value = intent.getBooleanExtra(MainActivity.EXTRA_IS_MENS, true)
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
    status: String,
    score: String,
    startTime: String,
    endTime: String,
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
                text = "Game Details",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary
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

                DetailRow(label = "Category", value = if (isMens) "Mens" else "Womens")
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))

                DetailRow(label = "Status", value = status)
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))

                DetailRow(label = "Score", value = score)
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))

                DetailRow(label = "Start Time", value = startTime)
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))

                DetailRow(label = "End Time", value = endTime)
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
            status = "finished",
            score = "75 - 70",
            startTime = "7:00 PM",
            endTime = "9:15 PM",
            currentPeriod = "2nd Half",
            timeRemaining = "0:00",
            winner = "Notre Dame",
            isMens = true,
            onBack = {}
        )
    }
}
