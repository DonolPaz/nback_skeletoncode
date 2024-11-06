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
import androidx.compose.ui.tooling.preview.Preview
import mobappdev.example.nback_cimpl.ui.viewmodels.FakeVM


@SuppressLint("StateFlowValueCalledInComposition")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    gameType: GameType,
    vm: GameViewModel,
    onMatchClick: () -> Unit,
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

            // Single Match button for all game types
            Button(onClick = onMatchClick) {
                Text("Match")
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

@Composable
fun AudioVisualEventDisplay(visualEvent: Int, audioEvent: Int, gridSize: Int) {
    // Display the visual event as a highlighted grid item
    VisualEventDisplay(visualEvent, gridSize)

    Spacer(modifier = Modifier.height(16.dp))

    // Display the audio event as a letter
    AudioEventDisplay(audioEvent)
}

