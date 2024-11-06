package mobappdev.example.nback_cimpl.ui.viewmodels

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import mobappdev.example.nback_cimpl.GameApplication
import mobappdev.example.nback_cimpl.NBackHelper
import mobappdev.example.nback_cimpl.data.UserPreferencesRepository
import java.util.Locale
import kotlin.system.exitProcess

/**
 * This is the GameViewModel.
 *
 * It is good practice to first make an interface, which acts as the blueprint
 * for your implementation. With this interface we can create fake versions
 * of the viewmodel, which we can use to test other parts of our app that depend on the VM.
 *
 * Our viewmodel itself has functions to start a game, to specify a gametype,
 * and to check if we are having a match
 *
 * Date: 25-08-2023
 * Version: Version 1.0
 * Author: Yeetivity
 *
 */


interface GameViewModel {
    val gameState: StateFlow<GameState>
    val gameType: StateFlow<GameType>
    val score: StateFlow<Int>
    val highscore: StateFlow<Int>
    val nBack: StateFlow<Int>
    val gridSize: Int
    val size: StateFlow<Int>
    val combinations: StateFlow<Int>
    val percentMatch: StateFlow<Int>
    val eventInterval: StateFlow<Long>

    val gameFinished: StateFlow<Boolean>
    val flashFailure: StateFlow<Boolean> // Match fail feedback
    val isAudioSelected: StateFlow<Boolean>
    val isVisualSelected: StateFlow<Boolean>

    fun toggleAudioSelection()
    fun toggleVisualSelection()

    fun setSize(value: Int)
    fun setCombinations(value: Int)
    fun setPercentMatch(value: Int)
    fun setEventInterval(value: Long)
    fun setNBack(value: Int)

    fun setGameType(gameType: GameType)
    fun getSelectedGameType(): GameType
    fun startGame(size: Int, combinations: Int, percentMatch: Int)
    fun checkMatch()
}

class GameVM(
    private val userPreferencesRepository: UserPreferencesRepository,
    context: Context
) : GameViewModel, ViewModel(), TextToSpeech.OnInitListener {

    // MutableStateFlows to manage game settings
    private val _gameType = MutableStateFlow(GameType.Visual)
    private val _nBack = MutableStateFlow(2)  // Changed to MutableStateFlow for consistency
    private val _size = MutableStateFlow(10)
    private val _combinations = MutableStateFlow(9)
    private val _percentMatch = MutableStateFlow(30)
    private val _eventInterval = MutableStateFlow(2000L)
    private val _gameFinished = MutableStateFlow(false)

    // Expose the settings as StateFlows
    override val nBack: StateFlow<Int> = _nBack.asStateFlow()
    override val gameType: StateFlow<GameType> = _gameType.asStateFlow()
    override val size: StateFlow<Int> = _size.asStateFlow()
    override val combinations: StateFlow<Int> = _combinations.asStateFlow()
    override val percentMatch: StateFlow<Int> = _percentMatch.asStateFlow()
    override val eventInterval: StateFlow<Long> = _eventInterval.asStateFlow()
    override val gameFinished: StateFlow<Boolean> = _gameFinished.asStateFlow()

    // Functions to update game settings
    override fun setGameType(type: GameType) { _gameType.value = type }
    override fun setSize(value: Int) { _size.value = value }
    override fun setCombinations(value: Int) { _combinations.value = value }
    override fun setPercentMatch(value: Int) { _percentMatch.value = value }
    override fun setEventInterval(value: Long) { _eventInterval.value = value }
    override fun setNBack(value: Int) { _nBack.value = value }


    // Game state and score handling
    private val _gameState = MutableStateFlow(GameState())
    private val _score = MutableStateFlow(0)
    private val _highscore = MutableStateFlow(0)

    // Flash failure feedback for UI
    private val _flashFailure = MutableStateFlow(false)

    // Audio/Visual selections for game type
    private val _isAudioSelected = MutableStateFlow(false)
    private val _isVisualSelected = MutableStateFlow(false)

    // Exposed StateFlows for UI
    override val flashFailure: StateFlow<Boolean> = _flashFailure.asStateFlow()
    override val gameState: StateFlow<GameState> = _gameState.asStateFlow()
    override val score: StateFlow<Int> = _score
    override val highscore: StateFlow<Int> = _highscore
    override val isAudioSelected: StateFlow<Boolean> = _isAudioSelected.asStateFlow()
    override val isVisualSelected: StateFlow<Boolean> = _isVisualSelected.asStateFlow()

    // Calculated grid size based on combinations
    override val gridSize: Int
        get() = when (_combinations.value) {
            25 -> 5
            else -> 3
        }

    // Toggle audio/visual selection
    override fun toggleAudioSelection() {
        _isAudioSelected.value = !_isAudioSelected.value
    }

    override fun toggleVisualSelection() {
        _isVisualSelected.value = !_isVisualSelected.value
    }

    override fun getSelectedGameType(): GameType {
        return when {
            _isAudioSelected.value && _isVisualSelected.value -> GameType.AudioVisual
            _isAudioSelected.value -> GameType.Audio
            _isVisualSelected.value -> GameType.Visual
            else -> GameType.Visual
        }
    }

    private var job: Job? = null
    private val nBackHelper = NBackHelper()
    private var events = emptyArray<Int>()
    private var secondEventArray = emptyArray<Int>()

    override fun startGame(size: Int, combinations: Int, percentMatch: Int) {
        _gameFinished.value = false
        _score.value = 0
        job?.cancel()

        events = nBackHelper.generateNBackString(size, combinations, percentMatch, _nBack.value).toList().toTypedArray()
        Log.d("GameVM", "First events generated: ${events.contentToString()}")

        // Generate a second distinct array for audio events if in AudioVisual mode
        if (getSelectedGameType() == GameType.AudioVisual) {
            secondEventArray = generateDistinctArray(size, combinations, percentMatch)
            Log.d("GameVM", "Audio events generated: ${secondEventArray.contentToString()}")
        }

        job = viewModelScope.launch {
            when (getSelectedGameType()) {
                GameType.Audio -> runAudioGame(events)
                GameType.AudioVisual -> runAudioVisualGame(events, secondEventArray)
                GameType.Visual -> runVisualGame(events)
            }
        }
    }

    // Helper function to generate a distinct array for AudioVisual mode
    private fun generateDistinctArray(size: Int, combinations: Int, percentMatch: Int): Array<Int> {
        var newArray: Array<Int>
        do {
            newArray = nBackHelper.generateNBackString(size, combinations, percentMatch, _nBack.value).toList().toTypedArray()
        } while (newArray.contentEquals(events)) // Repeat until a distinct array is created
        return newArray
    }

    fun stopGame() {
        job?.cancel()
        job = null
        _gameFinished.value = true // Optionally set gameFinished to true if you want to reset UI
    }

    private fun triggerFailureFlash() {
        viewModelScope.launch {
            _flashFailure.value = true
            delay(200)
            _flashFailure.value = false
        }
    }

    override fun checkMatch() {
        val currentIndex = _gameState.value.currentEventIndex
        val nBackIndex = currentIndex - _nBack.value

        if (nBackIndex >= 0 && events[currentIndex] == events[nBackIndex]) {
            if (!_gameState.value.isMatched) {
                _score.value += 1
                _gameState.value = _gameState.value.copy(isMatched = true)
            }
        } else {
            triggerFailureFlash()
        }
    }

    fun checkAudioMatch() {
        val currentIndex = _gameState.value.currentEventIndex
        val nBackIndex = currentIndex - _nBack.value

        // Ensure n-back index is within bounds for audio matching
        if (nBackIndex >= 0 && secondEventArray[currentIndex] == secondEventArray[nBackIndex]) {
            if (!_gameState.value.isAudioMatched) {  // Assuming isAudioMatched is added to track audio matches
                _score.value += 1  // Increment audio match score
                _gameState.value = _gameState.value.copy(isAudioMatched = true)
            }
        } else {
            triggerFailureFlash()  // Provide feedback if match fails
        }
    }

    private fun resetMatchStatus() {
        _gameState.value = _gameState.value.copy(isMatched = false, isAudioMatched = false)
    }

    private fun saveHighScore(highScore: Int) {
        viewModelScope.launch {
            userPreferencesRepository.saveHighScore(highScore)
        }
    }

    private fun finalizeGame() {
        if (_score.value > _highscore.value) saveHighScore(_score.value)
        _gameFinished.value = true // Set gameFinished to true for victory popup
        job?.cancel() // Cancel the job to prevent further actions if the game has finished
    }

    private suspend fun runVisualGame(events: Array<Int>) {
        for ((index, value) in events.withIndex()) {
            resetMatchStatus() // Allow new matches for the next event
            _gameState.value = _gameState.value.copy(eventValue = value, currentEventIndex = index)

            Log.d("GameVM", "Visual Event at index $index set to value: $value")

            delay(eventInterval.value) // Use the current interval directly from the flow
        }
        finalizeGame()
    }

    private suspend fun runAudioVisualGame(visualEvents: Array<Int>, audioEvents: Array<Int>) {
        for (index in visualEvents.indices) {
            resetMatchStatus()  // Reset match status for a new event

            // Set the current visual and audio events separately
            _gameState.value = _gameState.value.copy(
                eventValue = visualEvents[index],  // Visual event
                audioEventValue = audioEvents[index],  // Audio event
                currentEventIndex = index  // Track current event index
            )

            // Play the audio
            val letter = numberToLetter(audioEvents[index])
            speakLetter(letter)

            // Wait for the eventInterval before moving to the next event
            delay(eventInterval.value)
        }
        finalizeGame()
    }


    private suspend fun runAudioGame(events: Array<Int>) {
        for ((index, value) in events.withIndex()) {
            resetMatchStatus() // Allow new matches for the next event
            _gameState.value = _gameState.value.copy(eventValue = value, currentEventIndex = index)

            // Convert the number to a letter and speak it
            val letter = numberToLetter(value)
            speakLetter(letter)

            Log.d("GameVM", "Audio Event at index $index, letter: $letter")

            delay(eventInterval.value) // Use the current interval directly from the flow
        }
        finalizeGame()
    }

    private var textToSpeech: TextToSpeech? = TextToSpeech(context, this)

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            textToSpeech?.language = Locale.US
        }
    }

    private fun speakLetter(letter: String) {
        textToSpeech?.speak(letter, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    override fun onCleared() {
        stopGame() // Stop the game when the ViewModel is cleared
        textToSpeech?.shutdown() // Shut down TTS
        super.onCleared()
    }

    private fun numberToLetter(number: Int): String {
        val letters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        return letters.getOrNull((number % letters.length) - 1)?.toString() ?: ""
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as GameApplication)
                GameVM(
                    userPreferencesRepository = application.userPreferencesRespository,
                    context = application.applicationContext
                )
            }
        }
    }

    init {
        viewModelScope.launch {
            userPreferencesRepository.highscore.collect {
                _highscore.value = it
            }
        }
    }
}

// Class with the different game types
enum class GameType{
    Audio,
    Visual,
    AudioVisual
}

data class GameState(
    // You can use this state to push values from the VM to your UI.
    val gameType: GameType = GameType.Visual,  // Type of the game
    val eventValue: Int = -1,  // The value of the array string
    val audioEventValue: Int = -1,  // Audio event value
    val currentEventIndex: Int = -1, // Track the index of the current event
    val isMatched: Boolean = false,  // Track if the current event is matched
    val isAudioMatched: Boolean = false
)


class FakeVM : GameViewModel {
    override val gameState = MutableStateFlow(GameState())
    override val gameType = MutableStateFlow(GameType.Visual)
    override val score = MutableStateFlow(10)
    override val highscore = MutableStateFlow(42)
    override val nBack = MutableStateFlow(2)
    override val gridSize = 3
    override val size = MutableStateFlow(10)
    override val combinations = MutableStateFlow(9)
    override val percentMatch = MutableStateFlow(30)
    override val eventInterval = MutableStateFlow(2000L)

    override val gameFinished = MutableStateFlow(false)
    override val flashFailure = MutableStateFlow(false)
    override val isAudioSelected = MutableStateFlow(false)
    override val isVisualSelected = MutableStateFlow(true)

    override fun toggleAudioSelection() {
        isAudioSelected.value = !isAudioSelected.value
    }

    override fun toggleVisualSelection() {
        isVisualSelected.value = !isVisualSelected.value
    }

    override fun setSize(value: Int) {
        size.value = value
    }

    override fun setCombinations(value: Int) {
        combinations.value = value
    }

    override fun setPercentMatch(value: Int) {
        percentMatch.value = value
    }

    override fun setEventInterval(value: Long) {
        eventInterval.value = value
    }

    override fun setNBack(value: Int) {
        nBack.value = value
    }

    override fun setGameType(gameType: GameType) {}
    override fun getSelectedGameType() = GameType.Visual
    override fun startGame(size: Int, combinations: Int, percentMatch: Int) {}
    override fun checkMatch() {}
}