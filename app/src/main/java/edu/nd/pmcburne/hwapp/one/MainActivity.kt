package edu.nd.pmcburne.hwapp.one

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import edu.nd.pmcburne.hwapp.one.ui.theme.HelloWorldTheme
import android.app.Activity
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import edu.nd.pmcburne.hwapp.one.ui.theme.TurquoiseGrey
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class GameItem(
    val homeTeam: String,
    val awayTeam: String,
    val date: String,
    val score: String,
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
            val detailLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.StartActivityForResult()
            ) { _ -> }

            HelloWorldTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val items by viewModel.gameItems.collectAsState()

                    GameListScreen(
                        modifier = Modifier.padding(innerPadding),
                        gameItems = items,
                        onViewDetails = { _, item ->
                            val intent = Intent(this, DetailActivity::class.java)
                            intent.putExtra(EXTRA_HOME_TEAM, item.homeTeam)
                            intent.putExtra(EXTRA_AWAY_TEAM, item.awayTeam)
                            intent.putExtra(EXTRA_DATE, item.date)
                            intent.putExtra(EXTRA_SCORE, item.score)
                            intent.putExtra(EXTRA_IS_MENS, item.isMens)
                            intent.putExtra(EXTRA_STATUS, item.status)
                            intent.putExtra(EXTRA_START_TIME, item.startTime)
                            intent.putExtra(EXTRA_END_TIME, item.endTime)
                            intent.putExtra(EXTRA_CURRENT_PERIOD, item.currentPeriod)
                            intent.putExtra(EXTRA_TIME_REMAINING, item.timeRemaining)
                            intent.putExtra(EXTRA_WINNER, item.winner)
                            detailLauncher.launch(intent)
                        }
                    )
                }
            }
        }
    }
}

class ListViewModel : ViewModel() {
    private val _gameItems = MutableStateFlow<List<GameItem>>(
        listOf(
            GameItem("Notre Dame", "Duke", "03/15/2024", "75 - 70", true, "finished", "7:00 PM", "9:15 PM", "2nd Half", "0:00", "Notre Dame"),
            GameItem("Virginia", "UCLA", "03/16/2024", "20 - 18", false, "currently being played", "1:00 PM", "TBD", "1st Quarter", "5:20", null),
            GameItem("Michigan", "Ohio State", "03/20/2024", "0 - 0", true, "upcoming", "8:00 PM", "TBD", "N/A", "N/A", null)
        )
    )
    val gameItems: StateFlow<List<GameItem>> = _gameItems
}

@Composable
fun GameListScreen(
    modifier: Modifier = Modifier,
    gameItems: List<GameItem>,
    onViewDetails: (Int, GameItem) -> Unit
) {
    Column(
        modifier = modifier.padding(16.dp).fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Sports Game Tracker",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(Modifier.height(16.dp))

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
                    text = "${item.homeTeam} vs. ${item.awayTeam}",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.Black
                )
                Text(
                    text = "Date: ${item.date} | ${if (item.isMens) "Mens" else "Womens"}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Score: ${item.score}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
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
            GameItem("Notre Dame", "Duke", "03/15/2024", "75 - 70", true, "finished", "7:00 PM", "9:15 PM", "2nd Half", "0:00", "Notre Dame"),
            GameItem("Virginia", "UCLA", "03/16/2024", "20 - 18", false, "currently being played", "1:00 PM", "TBD", "1st Quarter", "5:20", null)
        )
        GameListScreen(
            gameItems = sampleItems,
            onViewDetails = { _, _ -> }
        )
    }
}
