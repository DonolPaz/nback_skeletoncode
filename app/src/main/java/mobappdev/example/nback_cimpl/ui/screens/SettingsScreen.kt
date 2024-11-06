package mobappdev.example.nback_cimpl.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import mobappdev.example.nback_cimpl.ui.viewmodels.GameViewModel
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TopAppBar
import mobappdev.example.nback_cimpl.ui.viewmodels.FakeVM

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    vm: GameViewModel,
    onSaveClick: () -> Unit
) {
    // Collect values in a composable context
    val size = vm.size.collectAsState().value
    val combinations = vm.combinations.collectAsState().value
    val percentMatch = vm.percentMatch.collectAsState().value
    val eventInterval = vm.eventInterval.collectAsState().value
    val nBack = vm.nBack.collectAsState().value

    // State for dropdown menu
    var expanded by remember { mutableStateOf(false) }
    val gridOptions = listOf("3x3", "5x5")
    val selectGrid = if (combinations == 9) "3x3" else "5x5"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Configure Game Settings", style = MaterialTheme.typography.headlineMedium)


        // Combinations Setting
        Text(text = "Grid size for visual game")
        OutlinedButton(
            onClick = {expanded = true}
        ) {
            Text(selectGrid)
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = {expanded = false}
        ) {
            gridOptions.forEach { grid ->
                DropdownMenuItem(
                    text = { Text(grid) },
                    onClick = {
                        expanded = false
                        vm.setCombinations(if (grid == "3x3") 9 else 25)
                    }
                )
            }
        }

        // Size Setting
        OutlinedTextField(
            value = size.toString(),
            onValueChange = { vm.setSize(it.toIntOrNull() ?: size) },
            label = { Text("Number of Events per game") }
        )
        // Percent Match Setting
        OutlinedTextField(
            value = percentMatch.toString(),
            onValueChange = { vm.setPercentMatch(it.toIntOrNull() ?: percentMatch) },
            label = { Text("Percent Match") }
        )

        // Event Interval Setting
        OutlinedTextField(
            value = eventInterval.toString(),
            onValueChange = { vm.setEventInterval(it.toLongOrNull() ?: eventInterval) },
            label = { Text("Time between Events (ms)") }
        )

        // N-Back Setting
        OutlinedTextField(
            value = nBack.toString(),
            onValueChange = { vm.setNBack(it.toIntOrNull() ?: nBack) },
            label = { Text("N-Back Value") }
        )
        // Save button
        Button(
            onClick = onSaveClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    val fakeViewModel = FakeVM() // Use the fake ViewModel for preview
    Surface {
        SettingsScreen(
            vm = fakeViewModel,
            onSaveClick = { /* No-op for preview */ }
        )
    }
}