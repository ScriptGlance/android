package com.scriptglance.ui.screen.presentation.teleprompter

import android.content.Context
import android.content.res.AssetManager
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.scriptglance.R
import com.scriptglance.data.local.ZoomDataStore
import com.scriptglance.data.model.api.ApiResult
import com.scriptglance.data.model.presentation.PresentationEvent
import com.scriptglance.data.model.teleprompter.IncomingReadingPositionPayload
import com.scriptglance.data.model.teleprompter.PartReadingConfirmationRequiredPayload
import com.scriptglance.data.model.teleprompter.PartReassignRequest
import com.scriptglance.data.model.teleprompter.PartReassignRequiredPayload
import com.scriptglance.data.model.teleprompter.PartWithWords
import com.scriptglance.data.model.teleprompter.PresentationActiveCurrentReadingPosition
import com.scriptglance.data.model.teleprompter.PresentationActiveJoinedUser
import com.scriptglance.data.model.teleprompter.PresentationPartFull
import com.scriptglance.data.model.teleprompter.ReadingConfirmationRequest
import com.scriptglance.data.model.teleprompter.RenderableWord
import com.scriptglance.data.model.teleprompter.SidebarPartItem
import com.scriptglance.domain.manager.socket.PresentationSocketManager
import com.scriptglance.domain.manager.socket.TeleprompterSocketManager
import com.scriptglance.domain.repository.AuthRepository
import com.scriptglance.domain.repository.PresentationsRepository
import com.scriptglance.domain.repository.TeleprompterRepository
import com.scriptglance.domain.repository.UserRepository
import com.scriptglance.utils.constants.PartReassignReason
import com.scriptglance.utils.constants.PresenceEventType
import com.scriptglance.utils.constants.PresentationEventType
import com.scriptglance.utils.handleSpeechRecognitionResult
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import org.json.JSONException
import org.json.JSONObject
import org.vosk.LibVosk
import org.vosk.LogLevel
import org.vosk.Model
import org.vosk.Recognizer
import org.vosk.android.RecognitionListener
import org.vosk.android.SpeechService
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class TeleprompterViewModel @Inject constructor(
    private val presentationsRepository: PresentationsRepository,
    private val teleprompterRepository: TeleprompterRepository,
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository,
    private val presentationSocketManager: PresentationSocketManager,
    private val teleprompterSocketManager: TeleprompterSocketManager,
    @ApplicationContext private val context: Context,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    companion object {
        private const val TAG = "TeleprompterViewModel"
    }

    private val presentationId: Int = savedStateHandle.get<Int>("presentationId") ?: 0
    private var token: String? = null

    private val _state = MutableStateFlow(TeleprompterState())
    val state: StateFlow<TeleprompterState> = _state.asStateFlow()

    var fontSizeEm by mutableFloatStateOf(2.4f)
        private set

    private var recognitionStartAttempted = false
    private var isRecognizing = false
    private var forceRestarting = false
    private var lastRecognitionResultTime = 0L
    private var timeAtCurrentPosition = 0L
    private var lastSentFinalPosition = mutableMapOf<Int, Boolean>()
    private var lastWordAdvanceTime = 0L

    private var processedWordsIndices = mutableMapOf<Int, Int>()
    private var currentPosition: Pair<Int, Int>? = null
    private var inEndZone = false

    var snackbarMessage by mutableStateOf<String?>(null)
        private set

    var readingConfirmationActive by mutableStateOf(false)
        private set

    var speechApiAvailable by mutableStateOf<Boolean?>(null)
        private set
    var networkSpeechApiError by mutableStateOf<String?>(null)
        private set

    @Volatile
    private var isModelInitializing = false

    private var model: Model? = null
    private var speechService: SpeechService? = null
    private var restartRecognitionTimer: kotlinx.coroutines.Job? = null
    private var progressCheckTimer: kotlinx.coroutines.Job? = null

    private var socketInitialized = false
    private var listenersRegistered = false

    private val zoomDataStore = ZoomDataStore(context)

    init {
        viewModelScope.launch {
            val savedZoom = zoomDataStore.getZoom()
            if (savedZoom != null) {
                fontSizeEm = savedZoom.coerceIn(0.8f, 2.8f)
            }

            token = authRepository.getToken()
            if (!token.isNullOrEmpty()) {
                fetchUserProfile()

                fetchPresentationDetails()
                fetchParticipants()
                fetchParts()
                fetchConfig()
                initializeSockets()
                initVoskModel()

                launch {
                    delay(8000)
                    if (_state.value.isLoading) {
                        Log.w(TAG, "Loading timeout reached, showing UI anyway")
                        _state.update {
                            it.copy(
                                initialLoadComplete = true,
                                isLoading = false
                            )
                        }
                    }
                }
            } else {
                _state.update { it.copy(error = true) }
            }
        }
    }

    private suspend fun initVoskModel() = withContext(Dispatchers.IO) {
        if (model != null) {
            Log.d(TAG, "Vosk model already initialized, skipping.")
            withContext(Dispatchers.Main) {
                speechApiAvailable = true
                networkSpeechApiError = null
            }
            return@withContext
        }
        if (isModelInitializing) {
            Log.d(TAG, "Vosk model is already initializing, skipping new attempt.")
            return@withContext
        }
        isModelInitializing = true

        try {
            LibVosk.setLogLevel(LogLevel.INFO)

            val modelFolderName = "vosk-model-uk"
            val externalFilesDir = context.getExternalFilesDir(null)
            val modelDir = File(externalFilesDir, modelFolderName)

            if (modelDir.exists() && modelDir.listFiles()?.isEmpty() == true) {
                Log.d(TAG, "Cleaning up empty model directory")
                modelDir.delete()
            }

            if (!modelDir.exists() || !hasRequiredModelFiles(modelDir)) {
                Log.d(TAG, "Extracting model from assets to ${modelDir.absolutePath}")

                modelDir.mkdirs()

                try {
                    val assetManager = context.assets
                    extractAssetFolder(assetManager, modelFolderName, modelDir)

                    if (hasRequiredModelFiles(modelDir)) {
                        Log.d(TAG, "Model extracted successfully")
                    } else {
                        Log.e(TAG, "Model extraction incomplete, missing required files")
                        withContext(Dispatchers.Main) {
                            speechApiAvailable = false
                            networkSpeechApiError = "extraction-failed"
                        }
                        return@withContext
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to extract model: ${e.message}", e)
                    withContext(Dispatchers.Main) {
                        speechApiAvailable = false
                        networkSpeechApiError = "extraction-failed"
                    }
                    return@withContext
                }
            }

            try {
                Log.d(TAG, "Initializing Vosk model from ${modelDir.absolutePath}")
                model = Model(modelDir.absolutePath)

                withContext(Dispatchers.Main) {
                    speechApiAvailable = true
                    networkSpeechApiError = null
                }
                Log.d(TAG, "Vosk model initialized successfully")
            } catch (e: IOException) {
                Log.e(TAG, "Failed to initialize Vosk model: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    speechApiAvailable = false
                    networkSpeechApiError = "init-failed"
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in model initialization: ${e.message}", e)
            withContext(Dispatchers.Main) {
                speechApiAvailable = false
                networkSpeechApiError = "error"
            }
        } finally {
            isModelInitializing = false
        }
    }

    private fun hasRequiredModelFiles(modelDir: File): Boolean {
        val requiredFiles = listOf(
            "am/final.mdl",
            "conf/mfcc.conf",
            "conf/model.conf",
            "graph/Gr.fst",
            "graph/HCLr.fst",
            "graph/words.txt"
        )

        return requiredFiles.all { path ->
            val file = File(modelDir, path)
            val exists = file.exists()
            if (!exists) {
                Log.w(TAG, "Missing required model file: $path")
            }
            exists
        }
    }

    private fun extractAssetFolder(assetManager: AssetManager, assetPath: String, destDir: File) {
        try {
            val assets = assetManager.list(assetPath) ?: return

            if (assets.isEmpty()) {
                extractAssetFile(assetManager, assetPath, destDir)
            } else {
                var newDir = File(destDir, assetPath.substringAfterLast("/"))
                if (!assetPath.contains("/")) {
                    newDir = destDir
                }

                if (!newDir.exists()) {
                    newDir.mkdirs()
                }

                for (asset in assets) {
                    val newAssetPath = if (assetPath.isEmpty()) asset else "$assetPath/$asset"
                    extractAssetFolder(assetManager, newAssetPath, newDir)
                }
            }
        } catch (e: IOException) {
            Log.e(TAG, "Error extracting asset folder $assetPath: ${e.message}", e)
            throw e
        }
    }

    private fun extractAssetFile(assetManager: AssetManager, assetPath: String, destDir: File) {
        try {
            val fileName = assetPath.substringAfterLast("/")
            val destFile = File(destDir, fileName)

            destFile.parentFile?.mkdirs()

            val input = assetManager.open(assetPath)
            val output = FileOutputStream(destFile)

            val buffer = ByteArray(8192)
            var read: Int
            while (input.read(buffer).also { read = it } != -1) {
                output.write(buffer, 0, read)
            }

            input.close()
            output.flush()
            output.close()

            Log.d(TAG, "Extracted asset $assetPath to ${destFile.absolutePath}")
        } catch (e: IOException) {
            Log.e(TAG, "Error extracting asset file $assetPath: ${e.message}", e)
            throw e
        }
    }

    fun onCheckSpeechApiAvailability() {
        viewModelScope.launch {
            if (model == null) {
                initVoskModel()
            } else {
                speechApiAvailable = true
            }
        }
    }

    private fun fetchUserProfile() {
        viewModelScope.launch {
            val currentToken = token ?: return@launch
            when (val result = userRepository.getProfile(currentToken)) {
                is ApiResult.Success -> {
                    _state.update { it.copy(userProfile = result.data) }
                    Log.d(TAG, "User profile loaded: ${result.data?.firstName}")
                }

                is ApiResult.Error -> {
                    _state.update { it.copy(error = true) }
                    Log.e(TAG, "Error loading user profile: ${result}")
                }
            }
        }
    }

    private fun fetchPresentationDetails() {
        viewModelScope.launch {
            val currentToken = token ?: return@launch
            when (val result =
                presentationsRepository.getPresentation(currentToken, presentationId)) {
                is ApiResult.Success -> {
                    _state.update { it.copy(presentation = result.data) }
                    Log.d(TAG, "Presentation loaded: ${result.data?.name}")
                    checkInitialLoadComplete()
                }

                is ApiResult.Error -> {
                    _state.update { it.copy(error = true) }
                    Log.e(TAG, "Error loading presentation: ${result}")
                }
            }
        }
    }

    private fun fetchParticipants() {
        viewModelScope.launch {
            val currentToken = token ?: return@launch
            when (val result =
                presentationsRepository.getParticipants(currentToken, presentationId)) {
                is ApiResult.Success -> {
                    val participants = result.data ?: emptyList()
                    _state.update { it.copy(participants = participants) }
                    Log.d(TAG, "Participants loaded: ${participants.size}")
                    checkInitialLoadComplete()
                }

                is ApiResult.Error -> {
                    _state.update { it.copy(error = true) }
                    Log.e(TAG, "Error loading participants: ${result}")
                }
            }
        }
    }

    private fun fetchParts() {
        viewModelScope.launch {
            val currentToken = token ?: return@launch

            when (val partsResult =
                presentationsRepository.getParts(currentToken, presentationId)) {
                is ApiResult.Success -> {
                    val parts = partsResult.data ?: emptyList()
                    Log.d(TAG, "Parts loaded: ${parts.size}")

                    if (parts.isNotEmpty()) {
                        processFullParts(parts)
                    }
                    _state.update { it.copy(structure = parts) }
                    checkInitialLoadComplete()
                }

                is ApiResult.Error -> {
                    _state.update { it.copy(error = true) }
                    Log.e(TAG, "Error loading parts: ${partsResult}")
                }
            }
        }
    }

    private fun processFullParts(parts: List<PresentationPartFull>) {
        try {
            Log.d(TAG, "Processing ${parts.size} parts")

            val sortedParts = parts.sortedBy { it.partOrder }
            val partsWithWords = sortedParts.map { part ->
                val text = part.partText

                val pattern = Regex("(\\s+|\\n)")
                val wordsAndDelimiters = pattern.split(text)
                val delimiters = pattern.findAll(text).map { it.value }.toList()
                val wordsArray = mutableListOf<String>()

                for ((i, word) in wordsAndDelimiters.withIndex()) {
                    if (word.isNotEmpty()) {
                        wordsArray.add(word)
                    }
                    if (i < delimiters.size) {
                        wordsArray.add(delimiters[i])
                    }
                }

                PartWithWords(
                    partId = part.partId,
                    partOrder = part.partOrder,
                    partName = part.partName,
                    partText = text,
                    wordsArray = wordsArray,
                    assignedParticipant = state.value.participants
                        .find { it.participantId == part.assigneeParticipantId },
                    renderableWords = wordsArray.mapIndexed { idx, word ->
                        RenderableWord(
                            id = "${part.partId}-word-$idx",
                            text = word,
                            isSpaceOrNewline = """\s+|\n""".toRegex().matches(word)
                        )
                    }
                )
            }

            _state.update { it.copy(partsWithWords = partsWithWords) }
            Log.d(
                TAG,
                "Processed ${partsWithWords.size} parts with ${partsWithWords.sumOf { it.wordsArray.size }} words"
            )

            val sidebarParts = sortedParts.map { part ->
                val assigneeUserId = state.value.participants
                    .find { it.participantId == part.assigneeParticipantId }
                    ?.user?.userId

                SidebarPartItem(
                    partId = part.partId,
                    assigneeUserId = assigneeUserId,
                    partName = part.partName
                )
            }
            _state.update { it.copy(sidebarParts = sidebarParts) }

        } catch (e: Exception) {
            Log.e(TAG, "Error processing parts: ${e.message}", e)
            _state.update {
                it.copy(
                    partsWithWords = emptyList(),
                    sidebarParts = emptyList()
                )
            }
        }
    }

    private fun fetchConfig() {
        viewModelScope.launch {
            val currentToken = token ?: return@launch
            when (val result = presentationsRepository.getConfig(currentToken)) {
                is ApiResult.Success -> {
                    _state.update { it.copy(config = result.data) }
                    Log.d(TAG, "Config loaded")
                    checkInitialLoadComplete()
                }

                is ApiResult.Error -> {
                    _state.update { it.copy(error = true) }
                    Log.e(TAG, "Error loading config: ${result}")
                }
            }
        }
    }

    private fun fetchActiveTeleprompterData() {
        viewModelScope.launch {
            val currentToken = token ?: return@launch
            when (val result =
                presentationsRepository.getActivePresentation(currentToken, presentationId)) {
                is ApiResult.Success -> {
                    val activeData = result.data
                    if (activeData != null) {
                        Log.d(
                            TAG,
                            "Active teleprompter data loaded. Presentation active: ${activeData.currentPresentationStartDate != null}"
                        )

                        _state.update { state ->
                            state.copy(
                                currentPresentationStartDate = activeData.currentPresentationStartDate,
                                teleprompterActiveUsers = activeData.joinedUsers?.map {
                                    PresentationActiveJoinedUser(
                                        userId = it.userId,
                                        isRecordingModeActive = it.isRecordingModeActive
                                    )
                                } ?: emptyList(),
                                currentTeleprompterOwnerId = activeData.currentOwnerUserId,
                                initialReadingPosition = activeData.currentReadingPosition.let { pos ->
                                    PresentationActiveCurrentReadingPosition(
                                        partId = pos.partId,
                                        position = pos.position
                                    )
                                }
                            )
                        }

                        setInitialReadingPosition()
                    }
                    checkInitialLoadComplete()
                }

                is ApiResult.Error -> {
                    _state.update { it.copy(error = true) }
                    Log.e(TAG, "Error loading active presentation: ${result}")
                }
            }
        }
    }

    private fun checkInitialLoadComplete() {
        val currentState = _state.value

        Log.d(
            TAG, "Check initial load: " +
                    "presentation=${currentState.presentation != null}, " +
                    "participants=${currentState.participants.isNotEmpty()}, " +
                    "structure=${currentState.structure != null && currentState.structure.isNotEmpty()}, " +
                    "config=${currentState.config != null}, " +
                    "partsWithWords=${currentState.partsWithWords.isNotEmpty()}"
        )

        if (currentState.presentation != null &&
            currentState.participants.isNotEmpty() &&
            currentState.structure != null && currentState.structure.isNotEmpty() &&
            currentState.config != null &&
            currentState.partsWithWords.isNotEmpty()
        ) {
            Log.d(TAG, "All data loaded, showing teleprompter screen")
            _state.update {
                it.copy(
                    initialLoadComplete = true,
                    isLoading = false
                )
            }
        }
    }

    private fun initializeSockets() {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Connecting to sockets...")
                teleprompterSocketManager.connect()
                presentationSocketManager.connect()

                val connected = withTimeoutOrNull(5000L) {
                    var attempts = 0
                    while (!teleprompterSocketManager.isConnected() && attempts < 10) {
                        delay(500)
                        attempts++
                        Log.d(TAG, "Waiting for socket connection, attempt $attempts")
                    }
                    teleprompterSocketManager.isConnected() &&
                            presentationSocketManager.isConnected()
                } == true

                if (connected) {
                    Log.d(TAG, "Sockets connected successfully")
                    _state.update { it.copy(isSocketConnected = true) }
                    setupSocketListeners()
                    subscribeToTeleprompter()
                    subscribeToPresentation()

                    fetchActiveTeleprompterData()
                } else {
                    Log.e(TAG, "Failed to connect to sockets after timeout")
                    _state.update { it.copy(isSocketConnected = false) }
                    delay(3000)
                    retrySocketConnection()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error initializing sockets: ${e.message}", e)
                _state.update { it.copy(isSocketConnected = false) }
                delay(3000)
                retrySocketConnection()
            }
        }
    }

    private fun retrySocketConnection() {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Retrying socket connection...")
                teleprompterSocketManager.disconnect()
                delay(1000)
                teleprompterSocketManager.connect()
                presentationSocketManager.connect()

                val connected = withTimeoutOrNull(5000L) {
                    var attempts = 0
                    while (!teleprompterSocketManager.isConnected() && attempts < 10) {
                        delay(500)
                        attempts++
                    }
                    teleprompterSocketManager.isConnected() &&
                            presentationSocketManager.isConnected()
                } == true

                if (connected) {
                    Log.d(TAG, "Socket reconnected successfully")
                    _state.update { it.copy(isSocketConnected = true) }
                    if (!listenersRegistered) {
                        setupSocketListeners()
                    }
                    subscribeToTeleprompter()
                    subscribeToPresentation()

                    fetchActiveTeleprompterData()
                } else {
                    Log.e(TAG, "Failed to reconnect to sockets")
                    _state.update { it.copy(isSocketConnected = false) }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error retrying socket connection: ${e.message}", e)
                _state.update { it.copy(isSocketConnected = false) }
            }
        }
    }

    private fun subscribeToTeleprompter() {
        if (!teleprompterSocketManager.isConnected()) {
            Log.e(TAG, "Cannot subscribe to teleprompter: socket not connected")
            return
        }

        try {
            Log.d(TAG, "Subscribing to teleprompter for presentation $presentationId")
            teleprompterSocketManager.subscribeToTeleprompter(presentationId)
            socketInitialized = true
        } catch (e: Exception) {
            Log.e(TAG, "Error subscribing to teleprompter: ${e.message}", e)
        }
    }

    private fun subscribeToPresentation() {
        if (!presentationSocketManager.isConnected()) {
            Log.e(TAG, "Cannot subscribe to presentation: socket not connected")
            return
        }

        try {
            Log.d(TAG, "Subscribing to presentation for ID $presentationId")
            presentationSocketManager.subscribePresentation(presentationId)
        } catch (e: Exception) {
            Log.e(TAG, "Error subscribing to presentation: ${e.message}", e)
        }
    }

    private fun setupSocketListeners() {
        try {
            Log.d(TAG, "Setting up socket listeners")

            val presentationListener = presentationSocketManager.onPresentationEvent { event ->
                Log.d(TAG, "Received presentation event: ${event.eventType}")
                handlePresentationEvent(event)
            }

            val presenceListener = teleprompterSocketManager.onTeleprompterPresence { data ->
                Log.d(TAG, "Received presence event: ${data.type} for user ${data.userId}")
                _state.update { state ->
                    val existingUsers = state.teleprompterActiveUsers.toMutableList()
                    val existingUser = existingUsers.find { it.userId == data.userId }

                    when (PresenceEventType.fromValue(data.type)) {
                        PresenceEventType.USER_JOINED -> {
                            if (existingUser != null) {
                                val updatedUsers = existingUsers.map {
                                    if (it.userId == data.userId) it else it
                                }
                                state.copy(teleprompterActiveUsers = updatedUsers)
                            } else {
                                existingUsers.add(
                                    PresentationActiveJoinedUser(
                                        userId = data.userId,
                                        isRecordingModeActive = false
                                    )
                                )
                                state.copy(teleprompterActiveUsers = existingUsers)
                            }
                        }

                        PresenceEventType.USER_LEFT -> {
                            state.copy(teleprompterActiveUsers = existingUsers.filterNot { it.userId == data.userId })
                        }

                        else -> state
                    }
                }
            }

            val ownerListener = teleprompterSocketManager.onOwnerChanged { data ->
                Log.d(TAG, "Owner changed to: ${data.currentOwnerChangeId}")
                _state.update { it.copy(currentTeleprompterOwnerId = data.currentOwnerChangeId) }
            }

            val recordingListener = teleprompterSocketManager.onRecordingModeChanged { data ->
                Log.d(
                    TAG,
                    "Recording mode changed for user ${data.userId}: ${data.isRecordingModeActive}"
                )
                _state.update { state ->
                    val updatedUsers = state.teleprompterActiveUsers.map { user ->
                        if (user.userId == data.userId) {
                            user.copy(isRecordingModeActive = data.isRecordingModeActive)
                        } else user
                    }
                    state.copy(teleprompterActiveUsers = updatedUsers)
                }
            }

            val readingListener = teleprompterSocketManager.onReadingPositionChanged { data ->
                Log.d(
                    TAG,
                    "Reading position changed: partId=${data.partId}, position=${data.position}"
                )
                handleReadingPositionChange(data)
            }

            val partReassignListener = teleprompterSocketManager.onPartReassignRequired { data ->
                Log.d(TAG, "Part reassign required: partId=${data.partId}, reason=${data.reason}")
                handlePartReassignRequired(data)
            }

            val partReassignCancelledListener = teleprompterSocketManager.onPartReassignCancelled {
                Log.d(TAG, "Part reassign cancelled")
                _state.update { it.copy(partReassignRequest = null) }
            }

            val partReadingConfirmationListener = teleprompterSocketManager.onPartReadingConfirmationRequired { data ->
                Log.d(TAG, "Reading confirmation required: partId=${data.partId}")
                handleReadingConfirmationRequired(data)
            }

            val partReadingConfirmationCancelledListener = teleprompterSocketManager.onPartReadingConfirmationCancelled {
                Log.d(TAG, "Reading confirmation cancelled")
                _state.update { it.copy(readingConfirmationRequest = null) }
            }

            val partReassignedListener = teleprompterSocketManager.onPartReassigned { data ->
                updatePartAssigneeLocally(data.partId, data.userId)
            }

            teleprompterSocketManager.onConnect {
                Log.d(TAG, "Socket connected")
                _state.update { it.copy(isSocketConnected = true) }
                subscribeToTeleprompter()

                viewModelScope.launch {
                    fetchActiveTeleprompterData()
                }
            }

            presentationSocketManager.onConnect {
                subscribeToPresentation()
            }

            val disconnectListener = teleprompterSocketManager.onDisconnect { reason ->
                Log.d(TAG, "Socket disconnected: $reason")
                _state.update { it.copy(isSocketConnected = false) }
                viewModelScope.launch {
                    delay(3000)
                    retrySocketConnection()
                }
            }

            addCloseable {
                Log.d(TAG, "Cleaning up socket listeners")
                teleprompterSocketManager.offTeleprompterPresence(presenceListener)
                teleprompterSocketManager.offOwnerChanged(ownerListener)
                teleprompterSocketManager.offRecordingModeChanged(recordingListener)
                teleprompterSocketManager.offReadingPositionChanged(readingListener)
                teleprompterSocketManager.offDisconnect(disconnectListener)
                teleprompterSocketManager.offPartReassignRequired(partReassignListener)
                teleprompterSocketManager.offPartReassignCancelled(partReassignCancelledListener)
                teleprompterSocketManager.offPartReadingConfirmationRequired(partReadingConfirmationListener)
                teleprompterSocketManager.offPartReadingConfirmationCancelled(partReadingConfirmationCancelledListener)
                teleprompterSocketManager.offPartReassigned(partReassignedListener)
                teleprompterSocketManager.disconnect()
                presentationSocketManager.offPresentationEvent(presentationListener)
                presentationSocketManager.disconnect()
                stopSpeechRecognition()
            }

            listenersRegistered = true

            if (teleprompterSocketManager.isConnected()) {
                Log.d(TAG, "Socket already connected, fetching active data immediately")
                viewModelScope.launch {
                    fetchActiveTeleprompterData()
                }
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error setting up socket listeners: ${e.message}", e)
        }
    }

    private fun handlePresentationEvent(event: PresentationEvent) {
        val eventType = PresentationEventType.fromEvent(event.eventType)
        Log.d(TAG, "Handling presentation event: $eventType")

        when (eventType) {
            PresentationEventType.PRESENTATION_STARTED -> {
                _state.update { it.copy(currentPresentationStartDate = Date().toString()) }
                recognitionStartAttempted = false

                Log.d(TAG, "Presentation started, initializing recognition if needed")
                viewModelScope.launch {
                    val currentPartSpeakerId =
                        state.value.processedTexts.currentHighlightedPartId?.let { partId ->
                            getPartAssigneeUserId(partId)
                        }

                    if (currentPartSpeakerId == state.value.userProfile?.userId) {
                        initAndStartRecognition()
                    }
                }
            }

            PresentationEventType.PRESENTATION_STOPPED -> {
                Log.d(TAG, "Presentation stopped")
                _state.update { it.copy(currentPresentationStartDate = null) }
                resetReadingProgressToStart()
                stopSpeechRecognition()
                readingConfirmationActive = false
                snackbarMessage = null
            }

            PresentationEventType.NAME_CHANGED -> {
                Log.d(TAG, "Presentation name changed")
                fetchPresentationDetails()
            }

            PresentationEventType.PARTICIPANTS_CHANGED -> {
                Log.d(TAG, "Participants changed")
                fetchParticipants()
            }

            PresentationEventType.TEXT_CHANGED -> {
                Log.d(TAG, "Presentation text changed")
                fetchParts()
            }

            else -> {
                Log.d(TAG, "Unhandled presentation event: $eventType")
            }
        }
    }

    private fun handleReadingPositionChange(data: IncomingReadingPositionPayload) {
        Log.d(
            TAG,
            "Handling reading position change: partId=${data.partId}, position=${data.position}"
        )

        val oldHighlightedPartId = _state.value.processedTexts.currentHighlightedPartId
        val isPartTransition = data.partId != oldHighlightedPartId

        val wordIdx = findWordIndexFromCharPosition(data.partId, data.position)
        Log.d(TAG, "Word index for position ${data.position} in part ${data.partId} is $wordIdx")

        var oldPartSpeaker: Int? = null
        var newPartSpeaker: Int? = null

        if (oldHighlightedPartId != null) {
            oldPartSpeaker = getPartAssigneeUserId(oldHighlightedPartId)
        }

        newPartSpeaker = getPartAssigneeUserId(data.partId)

        val isCurrentUserOldSpeaker = state.value.userProfile?.userId == oldPartSpeaker
        val isCurrentUserNewSpeaker = state.value.userProfile?.userId == newPartSpeaker

        if (isPartTransition && isCurrentUserOldSpeaker && !isCurrentUserNewSpeaker && isRecognizing) {
            Log.d(TAG, "Stopping speech recognition due to part transition")
            stopSpeechRecognition()
        }

        updateHighlightAndScroll(data.partId, wordIdx, true)

        _state.update { state ->
            val updatedTexts = state.processedTexts.copy(
                currentHighlightedPartId = data.partId,
                currentSpeakerUserId = newPartSpeaker
            )
            state.copy(processedTexts = updatedTexts)
        }

        if (isCurrentUserNewSpeaker && state.value.currentPresentationStartDate != null) {
            Log.d(TAG, "Current user is the new speaker, initializing recognition")
            recognitionStartAttempted = false

            viewModelScope.launch {
                delay(300)
                val currentPart = _state.value.processedTexts.currentHighlightedPartId
                val currentPartSpeakerId = currentPart?.let { getPartAssigneeUserId(it) }
                val isUserSpeaker = state.value.userProfile?.userId == currentPartSpeakerId

                if (isUserSpeaker && state.value.currentPresentationStartDate != null && !isRecognizing) {
                    initAndStartRecognition()
                }
            }
        }
    }

    private fun setInitialReadingPosition() {
        val initialPos = _state.value.initialReadingPosition
        val parts = _state.value.partsWithWords
        val isPresentationActive = _state.value.currentPresentationStartDate != null

        if (isPresentationActive && initialPos != null && parts.any { it.partId == initialPos.partId }) {
            Log.d(
                TAG,
                "Setting initial reading position from server: partId=${initialPos.partId}, position=${initialPos.position}, presentationActive=$isPresentationActive"
            )
            val wordIdx = findWordIndexFromCharPosition(initialPos.partId, initialPos.position)

            updateHighlightAndScroll(initialPos.partId, wordIdx, isPresentationActive)
        } else if (parts.isNotEmpty()) {
            val firstPart = parts.first()
            val firstPartId = firstPart.partId

            if (isPresentationActive) {
                val firstWordIndex = findFirstNonWhitespaceWordIndex(firstPart)
                Log.d(TAG, "Setting initial reading position to first word of first part: $firstPartId, word: $firstWordIndex")
                updateHighlightAndScroll(firstPartId, firstWordIndex, true)
            } else {
                Log.d(TAG, "Setting initial reading position to first part but no word highlighted: $firstPartId")
                updateHighlightAndScroll(firstPartId, -1, false)
            }
        }
    }

    fun updateHighlightAndScroll(partId: Int, wordIdx: Int, shouldScroll: Boolean = true) {
        val parts = _state.value.partsWithWords
        val partContent = parts.find { it.partId == partId }

        if (partContent == null) {
            Log.w(TAG, "Part with ID $partId not found in updateHighlightAndScroll")
            return
        }

        val adjustedWordIdx = wordIdx.coerceIn(-1, partContent.wordsArray.size - 1)
        Log.d(TAG, "Updating highlight to partId=$partId, wordIdx=$adjustedWordIdx")

        val prevPosition = currentPosition
        val hasPositionChanged =
            prevPosition == null || prevPosition.first != partId || prevPosition.second != adjustedWordIdx

        if (hasPositionChanged) {
            currentPosition = Pair(partId, adjustedWordIdx)
            timeAtCurrentPosition = System.currentTimeMillis()
            inEndZone = isNearEndOfPart(partId, adjustedWordIdx)
            Log.d(
                TAG,
                "Position changed to part $partId, word $adjustedWordIdx, inEndZone=$inEndZone"
            )
        }

        val newProcessedWordsIndices = processedWordsIndices.toMutableMap()
        newProcessedWordsIndices[partId] = adjustedWordIdx
        processedWordsIndices = newProcessedWordsIndices

        _state.update { state ->
            val updatedTexts = state.processedTexts.copy(
                processedWordsIndices = newProcessedWordsIndices,
                currentHighlightedPartId = partId
            )
            state.copy(processedTexts = updatedTexts)
        }
    }

    private fun isNearEndOfPart(partId: Int, wordIdx: Int): Boolean {
        val partContent = _state.value.partsWithWords.find { it.partId == partId }
        if (partContent == null || partContent.wordsArray.isEmpty()) return false

        val endThreshold = (partContent.wordsArray.size * 0.1).coerceAtLeast(5.0).toInt()
        return wordIdx >= partContent.wordsArray.size - endThreshold
    }

    private fun findWordIndexFromCharPosition(partId: Int, charPos: Int): Int {
        val partContent = _state.value.partsWithWords.find { it.partId == partId }
        if (partContent == null || partContent.wordsArray.isEmpty()) {
            Log.w(TAG, "Cannot find word index: part $partId not found or has no words")
            return -1
        }

        if (charPos == 0) {
            for (i in partContent.wordsArray.indices) {
                if (partContent.wordsArray[i].trim().isNotEmpty()) {
                    return i
                }
            }
            return 0
        }

        var currentWordIdx = -1
        var accumulatedChars = 0

        for (i in partContent.wordsArray.indices) {
            val wordLength = partContent.wordsArray[i].length
            accumulatedChars += wordLength
            if (accumulatedChars > charPos) {
                currentWordIdx = i
                break
            }
        }

        if (charPos > 0 && accumulatedChars <= charPos && partContent.wordsArray.isNotEmpty()) {
            currentWordIdx = partContent.wordsArray.size - 1
        }

        return currentWordIdx
    }

    private fun calculateCharPosition(partId: Int, wordIdx: Int): Int {
        val partContent = _state.value.partsWithWords.find { it.partId == partId }
        if (partContent == null) {
            Log.w(TAG, "Part with ID $partId not found in calculateCharPosition")
            return 0
        }

        if (wordIdx >= partContent.wordsArray.size) {
            val fullText = partContent.partText
            return if (fullText.isNotEmpty()) fullText.length - 1 else 0
        }

        if (wordIdx < 0) return 0

        val actualWordIdx = wordIdx.coerceIn(0, partContent.wordsArray.size - 1)

        var charPosAccumulator = 0
        for (i in 0..actualWordIdx) {
            charPosAccumulator += partContent.wordsArray[i].length
        }

        if (actualWordIdx == partContent.wordsArray.size - 1) {
            val fullText = partContent.partText
            return (charPosAccumulator - 1).coerceIn(0, (fullText.length - 1).coerceAtLeast(0))
        }

        return if (charPosAccumulator > 0) charPosAccumulator - 1 else 0
    }

    fun sendReadingPosition(position: Int) {
        if (!socketInitialized || !teleprompterSocketManager.isConnected()) {
            Log.w(TAG, "Cannot send reading position: socket not initialized or not connected")
            return
        }

        val partId = _state.value.processedTexts.currentHighlightedPartId ?: return
        Log.d(TAG, "Sending reading position: position=$position for partId=$partId")
        teleprompterSocketManager.sendReadingPosition(position, presentationId)
    }

    fun sendFinalPosition(partId: Int) {
        if (lastSentFinalPosition[partId] == true) {
            Log.d(TAG, "Final position for part $partId already sent, skipping")
            return
        }

        lastSentFinalPosition[partId] = true

        val partContent = _state.value.partsWithWords.find { it.partId == partId } ?: return

        val lastWordIdx = partContent.wordsArray.size - 1
        val fullText = partContent.partText
        val lastCharPos = if (fullText.isNotEmpty()) fullText.length - 1 else 0

        Log.d(TAG, "Sending final position for part $partId: word=$lastWordIdx, char=$lastCharPos")
        updateHighlightAndScroll(partId, lastWordIdx)
        sendReadingPosition(lastCharPos)
    }

    fun initAndStartRecognition() {
        Log.d(TAG, "initAndStartRecognition called")

        if (model == null) {
            Log.w(TAG, "Vosk model is not initialized")
            viewModelScope.launch {
                initVoskModel()
                delay(1000)
                if (model != null) {
                    initAndStartRecognition()
                } else {
                    speechApiAvailable = false
                    networkSpeechApiError = "model-not-available"
                }
            }
            return
        }

        if (isRecognizing || forceRestarting) {
            Log.d(TAG, "Already recognizing or force restarting, skipping init")
            return
        }

        if (readingConfirmationActive) {
            Log.d(TAG, "Reading confirmation active, skipping recognition start")
            return
        }

        val isCurrentUserSpeakerOfCurrentPart = isCurrentUserSpeakerOfCurrentPart()
        val currentPresentationStartDate = _state.value.currentPresentationStartDate

        if (!isCurrentUserSpeakerOfCurrentPart || currentPresentationStartDate == null) {
            Log.d(
                TAG,
                "Not the current speaker or presentation not started, skipping recognition init"
            )
            return
        }

        Log.d(
            TAG,
            "Initializing Vosk speech recognition for part ${_state.value.processedTexts.currentHighlightedPartId}"
        )

        if (speechService != null) {
            Log.w(TAG, "Recognition service not null at init, trying to stop first")
            stopSpeechRecognition()
        }

        try {
            val recognizer = Recognizer(model, 16000.0f)
            speechService = SpeechService(recognizer, 16000.0f)
            speechService?.startListening(object : RecognitionListener {
                override fun onPartialResult(hypothesis: String?) {
                    if (hypothesis == null) return

                    lastRecognitionResultTime = System.currentTimeMillis()

                    if (_state.value.processedTexts.currentHighlightedPartId == null ||
                        !isCurrentUserSpeakerOfCurrentPart() ||
                        _state.value.currentPresentationStartDate == null ||
                        !_state.value.isSocketConnected
                    ) {
                        return
                    }

                    try {
                        val jsonResult = JSONObject(hypothesis)
                        val partialText = jsonResult.optString("partial", "")

                        if (partialText.isNotEmpty()) {
                            processRecognitionResult(partialText)
                        }
                    } catch (e: JSONException) {
                        Log.e(TAG, "Error parsing partial result: ${e.message}")
                    }
                }

                override fun onResult(hypothesis: String?) {
                    if (hypothesis == null) return

                    lastRecognitionResultTime = System.currentTimeMillis()

                    if (_state.value.processedTexts.currentHighlightedPartId == null ||
                        !isCurrentUserSpeakerOfCurrentPart() ||
                        _state.value.currentPresentationStartDate == null ||
                        !_state.value.isSocketConnected
                    ) {
                        return
                    }

                    try {
                        val jsonResult = JSONObject(hypothesis)
                        val text = jsonResult.optString("text", "")

                        if (text.isNotEmpty()) {
                            processRecognitionResult(text)
                        }

                        restartRecognition()
                    } catch (e: JSONException) {
                        Log.e(TAG, "Error parsing result: ${e.message}")
                    }
                }

                override fun onFinalResult(hypothesis: String?) {

                }

                override fun onError(exception: Exception?) {
                    Log.e(TAG, "Vosk recognition error: ${exception?.message}")
                    stopSpeechRecognition()

                    viewModelScope.launch {
                        delay(500)
                        if (isCurrentUserSpeakerOfCurrentPart() &&
                            _state.value.currentPresentationStartDate != null
                        ) {
                            initAndStartRecognition()
                        }
                    }
                }

                override fun onTimeout() {
                    Log.d(TAG, "Vosk recognition timeout")
                    restartRecognition()
                }
            })

            isRecognizing = true
            _state.update { it.copy(isRecognizing = true) }

            Log.d(TAG, "Vosk recognition started successfully")

        } catch (e: Exception) {
            Log.e(TAG, "Failed to start Vosk recognition: ${e.message}", e)
            stopSpeechRecognition()
        }
    }

    private fun processRecognitionResult(transcript: String) {
        val currentPartId = _state.value.processedTexts.currentHighlightedPartId ?: return
        val partContent = _state.value.partsWithWords.find { it.partId == currentPartId } ?: return

        if (partContent.wordsArray.isEmpty()) return

        handleSpeechRecognitionResult(
            transcript = transcript,
            scriptWords = partContent.wordsArray,
            currentWordIndex = processedWordsIndices[currentPartId] ?: -1,
            currentPartId = currentPartId,
            isNearEndOfPart = { partId, idx -> isNearEndOfPart(partId, idx) },
            calculateCharPosition = { partId, idx -> calculateCharPosition(partId, idx) },
            updateHighlightAndScroll = { partId, idx -> updateHighlightAndScroll(partId, idx) },
            sendReadingPosition = { pos -> sendReadingPosition(pos) },
            sendFinalPosition = { partId -> sendFinalPosition(partId) },
            timeAtCurrentPositionRef = timeAtCurrentPosition,
            lastWordAdvanceTimeRef = lastWordAdvanceTime
        )
    }

    private fun restartRecognition() {
        if (isCurrentUserSpeakerOfCurrentPart() &&
            _state.value.currentPresentationStartDate != null &&
            !forceRestarting
        ) {

            restartRecognitionTimer?.cancel()
            restartRecognitionTimer = viewModelScope.launch {
                stopSpeechRecognition()
                delay(300)
                if (isCurrentUserSpeakerOfCurrentPart() &&
                    _state.value.currentPresentationStartDate != null &&
                    !forceRestarting
                ) {
                    Log.d(TAG, "Auto-restarting speech recognition")
                    recognitionStartAttempted = false
                    initAndStartRecognition()
                }
            }
        }
    }

    fun stopSpeechRecognition() {
        try {
            Log.d(TAG, "Stopping Vosk speech recognition")
            speechService?.stop()
            speechService = null
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping speech recognition: ${e.message}")
        }

        isRecognizing = false
        _state.update { it.copy(isRecognizing = false) }

        restartRecognitionTimer?.cancel()
        restartRecognitionTimer = null

        progressCheckTimer?.cancel()
        progressCheckTimer = null

        recognitionStartAttempted = false
        forceRestarting = false

        Log.d(TAG, "Speech recognition stopped and cleaned up")
    }

    fun getPartAssigneeUserId(partId: Int): Int? {
        val structurePart = _state.value.structure?.find { it.partId == partId }
        if (structurePart != null) {
            return _state.value.participants.find { it.participantId == structurePart.assigneeParticipantId }?.user?.userId
        }

        val partWithWords = _state.value.partsWithWords.find { it.partId == partId }
        if (partWithWords != null) {
            val participant =
                _state.value.participants.find { it.participantId == partWithWords.assignedParticipant?.participantId }
            return participant?.user?.userId
        }

        return null
    }

    fun isCurrentUserSpeakerOfCurrentPart(): Boolean {
        val userProfile = _state.value.userProfile
        val currentHighlightedPartId = _state.value.processedTexts.currentHighlightedPartId
        if (userProfile == null || currentHighlightedPartId == null) return false

        val assigneeUserId = getPartAssigneeUserId(currentHighlightedPartId)
        return assigneeUserId == userProfile.userId
    }

    val isCurrentUserOwner: Boolean
        get() {
            val userProfile = _state.value.userProfile
            val currentTeleprompterOwnerId = _state.value.currentTeleprompterOwnerId
            if (userProfile == null || currentTeleprompterOwnerId == null) return false
            return userProfile.userId == currentTeleprompterOwnerId
        }

    fun handlePlayPauseClick() {
        Log.d(TAG, "Play/pause button clicked. IsOwner: ${isCurrentUserOwner}")

        if (!isCurrentUserOwner) {
            Log.w(TAG, "User is not the owner, cannot control presentation")
            snackbarMessage = context.getString(R.string.presentation_starting_not_owner_error)
            return
        }

        viewModelScope.launch {
            try {
                val currentToken = token ?: run {
                    Log.e(TAG, "Cannot start/stop presentation: token is null")
                    snackbarMessage = context.getString(R.string.auth_error)
                    return@launch
                }

                if (_state.value.currentPresentationStartDate != null) {
                    Log.d(TAG, "Stopping presentation...")
                    val result =
                        teleprompterRepository.stopPresentation(currentToken, presentationId)

                    if (result is ApiResult.Success) {
                        Log.d(TAG, "Presentation stopped successfully")
                        _state.update { it.copy(currentPresentationStartDate = null) }
                        resetReadingProgressToStart()
                        stopSpeechRecognition()
                        readingConfirmationActive = false
                    } else if (result is ApiResult.Error) {
                        Log.e(TAG, "Error stopping presentation: ${result}")
                        snackbarMessage = context.getString(R.string.error_stopping_presentation)
                    }
                } else {
                    Log.d(TAG, "Starting presentation...")
                    resetReadingProgressToFirstWord()

                    val result =
                        teleprompterRepository.startPresentation(currentToken, presentationId)

                    if (result is ApiResult.Success) {
                        Log.d(TAG, "Presentation started successfully")
                        val startDate = System.currentTimeMillis().toString()
                        _state.update { it.copy(currentPresentationStartDate = startDate) }
                        recognitionStartAttempted = false
                    } else if (result is ApiResult.Error) {
                        Log.e(TAG, "Error starting presentation: ${result}")
                        snackbarMessage = context.getString(R.string.error_starting_presentation)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception handling play/pause: ${e.message}", e)
                snackbarMessage = context.getString(R.string.error_managing_presentation)
            }
        }
    }

    private fun resetReadingProgressToFirstWord() {
        val sortedParts = _state.value.partsWithWords.sortedBy { it.partOrder }
        if (sortedParts.isEmpty()) return

        val firstPart = sortedParts.first()
        val firstWordIndex = findFirstNonWhitespaceWordIndex(firstPart)

        val initialProcessedIndices = mutableMapOf<Int, Int>()
        sortedParts.forEach { part ->
            initialProcessedIndices[part.partId] = if (part.partId == firstPart.partId) firstWordIndex else -1
        }

        processedWordsIndices = initialProcessedIndices

        updateHighlightAndScroll(firstPart.partId, firstWordIndex, true)

        _state.update { state ->
            val updatedTexts = state.processedTexts.copy(
                processedWordsIndices = initialProcessedIndices,
                currentHighlightedPartId = firstPart.partId,
                currentSpeakerUserId = getPartAssigneeUserId(firstPart.partId)
            )
            state.copy(processedTexts = updatedTexts)
        }

        Log.d(TAG, "Reading progress reset to first word: partId=${firstPart.partId}, wordIndex=$firstWordIndex")
    }

    private fun resetReadingProgressToStart() {
        val sortedParts = _state.value.partsWithWords.sortedBy { it.partOrder }
        if (sortedParts.isEmpty()) return

        val firstPart = sortedParts.first()

        val initialProcessedIndices = mutableMapOf<Int, Int>()
        sortedParts.forEach { part ->
            initialProcessedIndices[part.partId] = -1
        }

        processedWordsIndices = initialProcessedIndices

        updateHighlightAndScroll(firstPart.partId, -1, false)

        _state.update { state ->
            val updatedTexts = state.processedTexts.copy(
                processedWordsIndices = initialProcessedIndices,
                currentHighlightedPartId = firstPart.partId,
                currentSpeakerUserId = null
            )
            state.copy(processedTexts = updatedTexts)
        }

        Log.d(TAG, "Reading progress reset to start: no words highlighted")
    }

    private fun findFirstNonWhitespaceWordIndex(part: PartWithWords): Int {
        for (i in part.wordsArray.indices) {
            if (part.wordsArray[i].trim().isNotEmpty()) {
                return i
            }
        }
        return 0
    }

    private fun updatePartAssigneeLocally(partId: Int, newUserId: Int) {
        val newParticipant = _state.value.participants.find { it.user.userId == newUserId }

        if (newParticipant == null) {
            Log.w(TAG, "Could not find participant with userId $newUserId for part reassignment")
            return
        }

        val currentUserId = _state.value.userProfile?.userId
        val isReassignedToCurrentUser = newUserId == currentUserId
        val isCurrentPartActive = partId == _state.value.processedTexts.currentHighlightedPartId
        val isPresentationActive = _state.value.currentPresentationStartDate != null

        val updatedPartsWithWords = _state.value.partsWithWords.map { part ->
            if (part.partId == partId) {
                part.copy(assignedParticipant = newParticipant)
            } else {
                part
            }
        }

        val updatedStructure = _state.value.structure?.map { structurePart ->
            if (structurePart.partId == partId) {
                structurePart.copy(assigneeParticipantId = newParticipant.participantId)
            } else {
                structurePart
            }
        }

        val updatedSidebarParts = _state.value.sidebarParts.map { sidebarPart ->
            if (sidebarPart.partId == partId) {
                sidebarPart.copy(assigneeUserId = newUserId)
            } else {
                sidebarPart
            }
        }

        _state.update { state ->
            state.copy(
                partsWithWords = updatedPartsWithWords,
                structure = updatedStructure,
                sidebarParts = updatedSidebarParts,
                processedTexts = state.processedTexts.copy(
                    currentSpeakerUserId = if (isCurrentPartActive) newUserId else state.processedTexts.currentSpeakerUserId
                )
            )
        }

        Log.d(TAG, "Part $partId reassigned locally to user $newUserId (participant ${newParticipant.participantId})")

        if (isReassignedToCurrentUser && isCurrentPartActive && isPresentationActive) {
            Log.d(TAG, "Part reassigned to current user and is active, starting speech recognition")
            viewModelScope.launch {
                delay(300)

                if (isCurrentUserSpeakerOfCurrentPart() && !isRecognizing) {
                    recognitionStartAttempted = false
                    initAndStartRecognition()
                }
            }
        }

        val wasCurrentUserSpeaker = _state.value.processedTexts.currentSpeakerUserId == currentUserId
        if (wasCurrentUserSpeaker && !isReassignedToCurrentUser && isCurrentPartActive && isRecognizing) {
            Log.d(TAG, "Part reassigned from current user, stopping speech recognition")
            stopSpeechRecognition()
        }
    }

    private fun handlePartReassignRequired(data: PartReassignRequiredPayload) {
        if (!isCurrentUserOwner) {
            Log.d(TAG, "User is not owner, ignoring part reassign request")
            return
        }

        val availableParticipants = _state.value.participants.filter { participant ->
            _state.value.teleprompterActiveUsers.any { it.userId == participant.user.userId } &&
                    participant.user.userId != data.userId
        }

        _state.update {
            it.copy(
                partReassignRequest = PartReassignRequest(
                    part = _state.value.sidebarParts.find { it.partId == data.partId },
                    reason = PartReassignReason.fromValue(data.reason) ?: PartReassignReason.MISSING_ASSIGNEE,
                    availableParticipants = availableParticipants,
                    missingParticipant = _state.value.participants.find { it.user.userId == data.userId }
                )
            )
        }
    }

    private fun handleReadingConfirmationRequired(data: PartReadingConfirmationRequiredPayload) {

        _state.update {
            it.copy(
                readingConfirmationRequest = ReadingConfirmationRequest(
                    part = _state.value.sidebarParts.find { it.partId == data.partId },
                    timeToConfirmSeconds = data.timeToConfirmSeconds,
                    canContinueFromLastPosition = data.canContinueFromLastPosition,
                )
            )
        }
    }

    fun onReassignPartToUser(partId: Int, newUserId: Int) {
        viewModelScope.launch {
            try {
                val currentToken = token ?: return@launch
                val result = teleprompterRepository.setActiveReader(currentToken, presentationId, newUserId)

                if (result is ApiResult.Success) {
                    Log.d(TAG, "Part reassigned successfully")
                    _state.update { it.copy(partReassignRequest = null) }
                } else {
                    Log.e(TAG, "Failed to reassign part: $result")
                    snackbarMessage = context.getString(R.string.error_reassinging_part)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error reassigning part: ${e.message}", e)
                snackbarMessage = context.getString(R.string.error_reassinging_part)
            }
        }
    }

    fun onConfirmReading(isFromStartPosition: Boolean, sendRequest: Boolean) {
        if (!sendRequest) {
            _state.update { it.copy(readingConfirmationRequest = null) }
            val isCurrentUserSpeaker = isCurrentUserSpeakerOfCurrentPart()
            val isPresentationActive = _state.value.currentPresentationStartDate != null

            if (isCurrentUserSpeaker && isPresentationActive && !isRecognizing) {
                recognitionStartAttempted = false
                initAndStartRecognition()
            }
            return
        }
        viewModelScope.launch {
            try {
                val currentToken = token ?: return@launch
                val result = teleprompterRepository.confirmActiveReader(currentToken, presentationId, isFromStartPosition)

                if (result is ApiResult.Success) {
                    Log.d(TAG, "Reading confirmed successfully")
                    _state.update { it.copy(readingConfirmationRequest = null) }
                } else {
                    Log.e(TAG, "Failed to confirm reading: $result")
                    snackbarMessage = context.getString(R.string.error_confirmation)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error confirming reading: ${e.message}", e)
                snackbarMessage = context.getString(R.string.error_confirmation)
            }
        }
    }

    fun onDismissPartReassignDialog() {
        _state.update { it.copy(partReassignRequest = null) }
    }

    fun onDismissReadingConfirmationDialog() {
        _state.update { it.copy(readingConfirmationRequest = null) }
    }

    fun handleLeave() {
        stopSpeechRecognition()
        teleprompterSocketManager.disconnect()
    }

    fun zoomIn() {
        val newZoom = (fontSizeEm + 0.1f).coerceAtMost(2.8f)
        fontSizeEm = newZoom
        saveZoom(newZoom)
    }

    fun zoomOut() {
        val newZoom = (fontSizeEm - 0.1f).coerceAtLeast(0.8f)
        fontSizeEm = newZoom
        saveZoom(newZoom)
    }

    private fun saveZoom(value: Float) {
        viewModelScope.launch {
            zoomDataStore.saveZoom(value)
        }
    }

    override fun onCleared() {
        super.onCleared()
        speechService?.stop()
        speechService = null
        model?.close()
        model = null
    }
}
