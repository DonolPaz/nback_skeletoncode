package mobappdev.example.nback_cimpl.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import kotlinx.coroutines.launch
import mobappdev.example.nback_cimpl.R
import mobappdev.example.nback_cimpl.ui.viewmodels.FakeVM
import mobappdev.example.nback_cimpl.ui.viewmodels.GameType
import mobappdev.example.nback_cimpl.ui.viewmodels.GameViewModel
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField


@Composable
fun ConfigDialog(
    onConfirm: (size: Int, combinations: Int, percentMatch: Int) -> Unit,
    onDismiss: () -> Unit
) {
    var size by remember { mutableStateOf(10) }
    var combinations by remember { mutableStateOf(9) }
    var percentMatch by remember { mutableStateOf(30) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = { onConfirm(size, combinations, percentMatch) }) {
                Text("Start")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        title = { Text("Game Configuration") },
        text = {
            Column {
                OutlinedTextField(
                    value = size.toString(),
                    onValueChange = { size = it.toIntOrNull() ?: size },
                    label = { Text("Size") }
                )
                OutlinedTextField(
                    value = combinations.toString(),
                    onValueChange = { combinations = it.toIntOrNull() ?: combinations },
                    label = { Text("Combinations") }
                )
                OutlinedTextField(
                    value = percentMatch.toString(),
                    onValueChange = { percentMatch = it.toIntOrNull() ?: percentMatch },
                    label = { Text("Percent Match") }
                )
            }
        }
    )
}

/**
 * This is the Home screen composable
 *
 * Currently this screen shows the saved highscore
 * It also contains a button which can be used to show that the C-integration works
 * Furthermore it contains two buttons that you can use to start a game
 *
 * Date: 25-08-2023
 * Version: Version 1.0
 * Author: Yeetivity
 *
 */

@Composable
fun HomeScreen(
    vm: GameViewModel,
    onStartGameClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    val highscore by vm.highscore.collectAsState()
    val nBack by vm.nBack.collectAsState()
    val eventInterval by vm.eventInterval.collectAsState()
    val isAudioSelected by vm.isAudioSelected.collectAsState()
    val isVisualSelected by vm.isVisualSelected.collectAsState()

    Scaffold {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Title with nBack value
            Text(
                text = "$nBack-Back Game",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // High Score display with smaller text size
            Text(
                text = "High Score = $highscore",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(16.dp)
            )

            // Game Speed display
            Text(
                text = "Game Speed: ${eventInterval / 1000} sec",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Toggle buttons for audio and visual game modes
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { vm.toggleAudioSelection() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isAudioSelected) MaterialTheme.colorScheme.primary else Color.Gray
                    )
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.sound_on),
                        contentDescription = "Audio",
                        modifier = Modifier.height(48.dp)
                    )
                }
                Button(
                    onClick = { vm.toggleVisualSelection() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isVisualSelected) MaterialTheme.colorScheme.primary else Color.Gray
                    )
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.visual),
                        contentDescription = "Visual",
                        modifier = Modifier.height(48.dp)
                    )
                }
            }

            // Start Game Button
            Button(onClick = onStartGameClick) {
                Text(
                    text = "Start Game".uppercase(),
                    style = MaterialTheme.typography.displaySmall
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Settings Button
            Button(onClick = onSettingsClick) {
                Text("Settings")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    val fakeViewModel = FakeVM()
    Surface {
        HomeScreen(
            vm = fakeViewModel,
            onStartGameClick = { /* no-op for preview */ },
            onSettingsClick = { /* no-op for preview */ }
        )
    }
}
