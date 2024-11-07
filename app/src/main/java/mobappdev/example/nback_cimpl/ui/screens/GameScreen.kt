package mobappdev.example.nback_cimpl.ui.screens

import android.annotation.SuppressLint
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import mobappdev.example.nback_cimpl.ui.viewmodels.GameType
import mobappdev.example.nback_cimpl.ui.viewmodels.GameViewModel
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.draw.scale
import android.util.Log
import androidx.compose.animation.animateColorAsState
import androidx.compose.material3.AlertDialog

/**
 * Displays the main Game screen for different game types (Visual, Audio, Audio-Visual).
 * Renders game events and provides match buttons based on the selected game type.
 *
 * @param gameType The type of the game (Visual, Audio, or Audio-Visual).
 * @param vm The [GameViewModel] that holds the state and logic for the game.
 * @param onMatchClick Callback invoked when the visual match button is clicked.
 * @param onAudioMatchClick Callback invoked when the audio match button is clicked (only for Audio-Visual game).
 * @param onHomeClick Callback invoked when navigating back to the Home screen.
 * @param onPlayAgainClick Callback invoked to start a new game with the same configuration.
 */
@SuppressLint("StateFlowValueCalledInComposition")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    gameType: GameType,
    vm: GameViewModel,
    onMatchClick: () -> Unit,
    onAudioMatchClick: () -> Unit,  // Add this parameter for audio match
    onHomeClick: () -> Unit,
    onPlayAgainClick: () -> Unit
) {
    val flashFailure by vm.flashFailure.collectAsState()

    // Animate background color based on flashFailure state
    val backgroundColor by animateColorAsState(
        targetValue = if (flashFailure) Color.Red else MaterialTheme.colorScheme.background,
        animationSpec = tween(durationMillis = 300)
    )

    LaunchedEffect(Unit) {
        vm.startGame(vm.size.value, vm.combinations.value, vm.percentMatch.value)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("${vm.nBack.value}-Back ${vm.getSelectedGameType()} Game") }
            )
        },
        containerColor = backgroundColor
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Observe currentEventIndex and size for the display
            val gameState by vm.gameState.collectAsState()
            val currentEventIndex = gameState.currentEventIndex + 1  // Make it 1-based for display
            val totalEvents by vm.size.collectAsState()
            Text(
                text = "Event $currentEventIndex/$totalEvents",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            val gridSize = vm.gridSize
            GameEventDisplay(gameType = gameType, vm = vm, gridSize = gridSize)

            Spacer(modifier = Modifier.height(16.dp))

            // Row to contain match buttons
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onMatchClick) {
                    Text("Match")
                }

                // Show the second button only in AudioVisual mode
                if (gameType == GameType.AudioVisual) {
                    Button(onClick = onAudioMatchClick) {
                        Text("Audio Match")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Score display
            Text("Score: ${vm.score.collectAsState().value}")

            val gameFinished by vm.gameFinished.collectAsState()

            if (gameFinished) {
                AlertDialog(
                    onDismissRequest = {},
                    title = { Text("Game Finished!") },
                    text = { Text("Score: ${vm.score.collectAsState().value}") },
                    confirmButton = {
                        Button(onClick = onHomeClick) {
                            Text("Home")
                        }
                    },
                    dismissButton = {
                        Button(onClick = {
                            onPlayAgainClick()
                            vm.startGame(vm.size.value, vm.combinations.value, vm.percentMatch.value)
                        }) {
                            Text("Play Again")
                        }
                    }
                )
            }
        }
    }
}

/**
 * Renders the game event display based on the selected game type.
 * Shows either a visual grid, an audio letter, or both.
 *
 * @param gameType The type of the game (Visual, Audio, or Audio-Visual).
 * @param vm The [GameViewModel] providing the state of the current event.
 * @param gridSize The size of the visual grid.
 */
@Composable
fun GameEventDisplay(gameType: GameType, vm: GameViewModel, gridSize: Int) {
    val eventValue = vm.gameState.collectAsState().value.eventValue
    val audioEventValue = vm.gameState.collectAsState().value.audioEventValue

    when (gameType) {
        GameType.Visual -> VisualEventDisplay(eventValue, gridSize)
        GameType.Audio -> AudioEventDisplay(eventValue)
        GameType.AudioVisual -> AudioVisualEventDisplay(eventValue, audioEventValue, gridSize)
    }
}

/**
 * Displays the visual event as a highlighted item in a grid based on event value and grid size.
 *
 * @param eventValue The value representing the visual event to be displayed.
 * @param gridSize The size of the grid.
 */
@Composable
fun VisualEventDisplay(eventValue: Int, gridSize: Int) {
    // Adjust eventValue by subtracting 1 to map 1-9 to 0-8
    val adjustedEventValue = eventValue - 1

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        for (row in 0 until gridSize) {
            Row(horizontalArrangement = Arrangement.Center) {
                for (col in 0 until gridSize) {
                    val index = row * gridSize + col
                    // Highlight the cell if it matches the adjusted event value
                    val isHighlighted = (index == adjustedEventValue)
                    GridItem(isHighlighted)
                }
            }
        }
    }
}

/**
 * Displays a single grid item. Highlights the cell if it matches the specified condition.
 *
 * @param isHighlighted Boolean indicating whether the item should be highlighted.
 */
@Composable
fun GridItem(isHighlighted: Boolean) {
    // Define an Animatable scale for finer control
    val scale = remember { Animatable(1f) }

    // Launch an animation whenever the cell becomes highlighted
    LaunchedEffect(isHighlighted) {
        if (isHighlighted) {
            // Animate the scale to 1.2f over 500ms, then back to 1f
            scale.animateTo(1.2f, animationSpec = tween(500))
            scale.animateTo(1f, animationSpec = tween(500))
        } else {
            scale.snapTo(1f)  // Ensure it’s reset to normal when not highlighted
        }
    }

    Box(
        modifier = Modifier
            .size(60.dp)
            .padding(4.dp)
            .scale(scale.value)  // Apply scale animation
            .background(if (isHighlighted) Color.Blue else Color.DarkGray),
        contentAlignment = Alignment.Center
    ) {}
}

/**
 * Displays the audio event as a letter.
 *
 * @param eventValue The value representing the audio event, mapped to a letter.
 */
@Composable
fun AudioEventDisplay(eventValue: Int) {
    val letter = remember(eventValue) {
        val letters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        letters.getOrNull((eventValue - 1) % letters.length)?.toString() ?: ""
    }
    Log.d("GameScreen", "audible letter $letter")

    Box(
        modifier = Modifier
            .size(200.dp)
            .background(Color.LightGray),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = letter,
            color = Color.Blue,
            style = MaterialTheme.typography.headlineLarge
        )
    }
}

/**
 * Displays both the visual and audio events together for the Audio-Visual game type.
 *
 * @param visualEvent The value representing the visual event.
 * @param audioEvent The value representing the audio event, mapped to a letter.
 * @param gridSize The size of the visual grid.
 */
@Composable
fun AudioVisualEventDisplay(visualEvent: Int, audioEvent: Int, gridSize: Int) {
    // Display the visual event as a highlighted grid item
    VisualEventDisplay(visualEvent, gridSize)

    Spacer(modifier = Modifier.height(16.dp))

    // Display the audio event as a letter
    AudioEventDisplay(audioEvent)
}

