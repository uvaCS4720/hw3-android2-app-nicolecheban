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
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.HorizontalDivider
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
                    val title by viewModel.title.collectAsState()
                    val dueDate by viewModel.dueDate.collectAsState()
                    val isCompleted by viewModel.isCompleted.collectAsState()
                    val completedDate by viewModel.completedDate.collectAsState()

                    ItemDetailsScreen(
                        modifier = Modifier.padding(innerPadding),
                        title = title,
                        dueDate = dueDate,
                        isCompleted = isCompleted,
                        completedDate = completedDate,
                        onBack = { finish() }
                    )
                }
            }
        }
    }
}

class DetailViewModel : ViewModel() {
    // Hold title as a StateFlow
    private val _title = MutableStateFlow("")
    val title: StateFlow<String> = _title.asStateFlow()

    // Hold due date as a StateFlow
    private val _dueDate = MutableStateFlow("")
    val dueDate: StateFlow<String> = _dueDate.asStateFlow()

    // Hold completed checkbox as a StateFlow
    private val _isCompleted = MutableStateFlow(false)
    val isCompleted: StateFlow<Boolean> = _isCompleted.asStateFlow()

    // Hold completed date as a StateFlow
    private val _completedDate = MutableStateFlow<String?>(null)
    val completedDate: StateFlow<String?> = _completedDate.asStateFlow()

    fun initializeFromIntent(intent: Intent) {
        // Read data passed from MainActivity and initialize the state using qualified names
        _title.value = intent.getStringExtra(MainActivity.EXTRA_TITLE) ?: ""
        _dueDate.value = intent.getStringExtra(MainActivity.EXTRA_DUE_DATE) ?: ""
        _isCompleted.value = intent.getBooleanExtra(MainActivity.EXTRA_IS_COMPLETED, false)
        _completedDate.value = intent.getStringExtra(MainActivity.EXTRA_COMPLETED_DATE)
    }
}

@Composable
fun DetailRow(label: String, value: @Composable () -> Unit) {
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
        Row(
            modifier = Modifier.weight(1.5f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            value()
        }
    }
}

@Composable
fun ItemDetailsScreen(
    modifier: Modifier = Modifier,
    title: String,
    dueDate: String,
    isCompleted: Boolean,
    completedDate: String?,
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
                text = "Item Details",
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
                DetailRow(label = "Title") {
                    Text(text = title, style = MaterialTheme.typography.bodyLarge)
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                
                DetailRow(label = "Due Date") {
                    Text(text = dueDate, style = MaterialTheme.typography.bodyLarge)
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))

                DetailRow(label = "Status") {
                    Checkbox(
                        checked = isCompleted,
                        onCheckedChange = null,
                        enabled = false,
                        colors = CheckboxDefaults.colors(
                            disabledUncheckedColor = MaterialTheme.colorScheme.outline,
                            disabledCheckedColor = MaterialTheme.colorScheme.primary
                        )
                    )
                    Text(
                        text = if (isCompleted) "Completed" else "Incomplete",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }

                if (isCompleted && completedDate != null) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                    DetailRow(label = "Completed Date") {
                        Text(text = completedDate, style = MaterialTheme.typography.bodyLarge)
                    }
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
                text = "Back to List",
                style = MaterialTheme.typography.titleMedium
            )
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Preview(showBackground = true)
@Composable
fun ItemDetailsScreenPreview() {
    HelloWorldTheme {
        ItemDetailsScreen(
            title = "Visit the Grand Canyon",
            dueDate = "10/15/2025",
            isCompleted = true,
            completedDate = "06/01/2024",
            onBack = {}
        )
    }
}
