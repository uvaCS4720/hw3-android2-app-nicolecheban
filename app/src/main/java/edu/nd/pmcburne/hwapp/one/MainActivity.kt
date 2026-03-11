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
import edu.nd.pmcburne.hwapp.one.ui.theme.HWStarterRepoTheme
import android.app.Activity
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class MainActivity : ComponentActivity() {
    // Create a ViewModel instance for this Activity
    private val viewModel: ListViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // Launch Create activity and get data back from it
            val createItemLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.StartActivityForResult()
            ) { result ->
                // This block is called when CreateActivity finishes
                if (result.resultCode == Activity.RESULT_OK) {
                    val intent = result.data
                    val title = intent?.getStringExtra(EXTRA_TITLE)
                    val dueDate = intent?.getLongExtra(EXTRA_DUE_DATE, -1)

                    if (title != null && dueDate != null && dueDate != -1L) {
                        viewModel.addItem(title, dueDate)
                    }
                }
            }
            // Launch Detail activity and get data back from it
            val editItemLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.StartActivityForResult()
            ) { result ->
                // Extract index and all updated fields of edited item
                if (result.resultCode == Activity.RESULT_OK) {
                    val intent = result.data
                    val index = intent?.getIntExtra(EXTRA_ITEM_INDEX, -1)
                    val title = intent?.getStringExtra(EXTRA_TITLE)
                    val dueDate = intent?.getStringExtra(EXTRA_DUE_DATE)
                    val isCompleted = intent?.getBooleanExtra(EXTRA_IS_COMPLETED, false)
                    val completedDate = intent?.getStringExtra(EXTRA_COMPLETED_DATE)

                    if (index != null && index != -1 && title != null && dueDate != null) {
                        viewModel.updateItem(index, title, dueDate, isCompleted ?: false, completedDate)
                    }
                }
            }

            HelloWorldTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val items by viewModel.bucketListItems.collectAsState()

                    BucketListScreen(
                        modifier = Modifier.padding(innerPadding),
                        bucketListItems = items,
                        onLaunchCreateActivity = {
                            val intent = Intent(this, CreateActivity::class.java)
                            createItemLauncher.launch(intent)
                        },
                        // Pass the item's data to the DetailActivity
                        onEditItem = { index, item ->
                            val intent = Intent(this, DetailActivity::class.java)
                            intent.putExtra(EXTRA_ITEM_INDEX, index)
                            intent.putExtra(EXTRA_TITLE, item.title)
                            intent.putExtra(EXTRA_DUE_DATE, item.dueDate)
                            intent.putExtra(EXTRA_IS_COMPLETED, item.isCompleted)
                            intent.putExtra(EXTRA_COMPLETED_DATE, item.completedDate)
                            editItemLauncher.launch(intent)
                        },
                        onToggleItem = { index -> viewModel.toggleCompleted(index) }
                    )
                }
            }
        }
    }
}

class ListViewModel : ViewModel() {
    private val _bucketListItems = MutableStateFlow<List<BucketItem>>(emptyList())
    val bucketListItems: StateFlow<List<BucketItem>> = _bucketListItems

    fun addItem(title: String, dueDateMillis: Long) {
        val sdf = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        val dueDate = sdf.format(Date(dueDateMillis))
        val newItem = BucketItem(title = title, dueDate = dueDate, isCompleted = false, completedDate = null)
        // Sort by completion (incomplete first), then by due date
        _bucketListItems.value = (_bucketListItems.value + newItem).sortedWith(
            compareBy<BucketItem> { it.isCompleted }
                .thenBy { SimpleDateFormat("MM/dd/yyyy", Locale.US).parse(it.dueDate) }
        )
    }

    // Update item with new values edited in Detail Activity
    fun updateItem(index: Int, title: String, dueDate: String, isCompleted: Boolean, completedDate: String?) {
        val currentList = _bucketListItems.value.toMutableList()
        if (index >= 0 && index < currentList.size) {
            val updatedItem = BucketItem(title, dueDate, isCompleted, completedDate)
            currentList[index] = updatedItem
            // Sort by completion (incomplete first), then by due date
            _bucketListItems.value = currentList.sortedWith(
                compareBy<BucketItem> { it.isCompleted }
                    .thenBy { SimpleDateFormat("MM/dd/yyyy", Locale.US).parse(it.dueDate) }
            )
        }
    }

    fun toggleCompleted(index: Int) {
        val currentList = _bucketListItems.value.toMutableList()
        if (index >= 0 && index < currentList.size) {
            val item = currentList[index]
            val newIsCompleted = !item.isCompleted
            val newCompletedDate = if (newIsCompleted) {
                SimpleDateFormat("MM/dd/yyyy", Locale.getDefault()).format(Date())
            } else {
                null
            }
            val updatedItem = item.copy(isCompleted = newIsCompleted, completedDate = newCompletedDate)
            currentList[index] = updatedItem
            // Sort by completion (incomplete first), then by due date
            _bucketListItems.value = currentList.sortedWith(
                compareBy<BucketItem> { it.isCompleted }
                    .thenBy { SimpleDateFormat("MM/dd/yyyy", Locale.US).parse(it.dueDate) }
            )
        }
    }
}

@Composable
fun BucketListScreen(
    modifier: Modifier = Modifier,
    bucketListItems: List<BucketItem>,
    onLaunchCreateActivity: () -> Unit,
    onEditItem: (Int, BucketItem) -> Unit,
    onToggleItem: (Int) -> Unit
) {
    Column(
        modifier = modifier.padding(16.dp).fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Your Bucket List Items",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = onLaunchCreateActivity) {
            Text(
                text ="Create new item",
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(bucketListItems) { index, item ->
                BucketListItem(
                    item = item,
                    onEdit = { onEditItem(index, item) },
                    onToggle = { onToggleItem(index) }
                )
            }
        }
    }
}

@Composable
fun BucketListItem(item: BucketItem, modifier: Modifier = Modifier, onEdit: () -> Unit, onToggle: () -> Unit) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 100.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = TurquoiseGrey
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
            verticalAlignment = Alignment.Top
        ) {
            Checkbox(
                checked = item.isCompleted,
                onCheckedChange = { onToggle() }, // Call the hoisted toggle function
                colors = CheckboxDefaults.colors(
                    uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.Black
                )
                Text(
                    text = "Due: ${item.dueDate}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = if (item.isCompleted && item.completedDate != null) "Completed: ${item.completedDate}" else " ",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.width(16.dp))

            IconButton(
                onClick = onEdit,
                modifier = Modifier.align(Alignment.CenterVertically),
                colors = IconButtonDefaults.iconButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                Icon(
                    imageVector = Icons.Filled.Edit,
                    contentDescription = "Edit"
                )
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun BucketListScreenWithSampleDataPreview() {
    HelloWorldTheme {
        val sampleItems = listOf(
            BucketItem("Go skydiving", "12/31/2024", false, null),
            BucketItem("Visit the Grand Canyon", "10/15/2025", true, "06/01/2024"),
            BucketItem("Learn to play the guitar", "01/01/2026", false, null)
        )
        BucketListScreen(
            bucketListItems = sampleItems,
            onLaunchCreateActivity = {},
            onEditItem = { _, _ -> },
            onToggleItem = {}
        )
    }
}