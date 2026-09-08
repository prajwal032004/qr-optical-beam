package com.example.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.model.TransferRecord
import com.example.data.repository.TransferRepository
import com.example.protocol.FileChunker
import com.example.protocol.QrFrame
import com.example.qr.QrCodeGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap

data class TransmitUiState(
    val fileName: String = "",
    val fileSize: Long = 0,
    val mimeType: String = "",
    val fileHash: String = "",
    val isFileLoaded: Boolean = false,
    val totalChunks: Int = 0,
    val currentChunkIndex: Int = 0,
    val isPlaying: Boolean = false,
    val fps: Int = 8, // frames per second (smooth default)
    val chunkSize: Int = 320, // bytes per QR code (balanced high-density default)
    val loopCount: Int = 0,
    val currentQrBitmap: Bitmap? = null,
    val filteredChunks: List<Int>? = null, // if only transmitting requested missing chunks
    val cachedFrameCount: Int = 0,
    val statusMessage: String = "Select a file to begin optical transmission"
)

class TransmitViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: TransferRepository by lazy {
        val db = AppDatabase.getDatabase(application)
        TransferRepository(db.transferDao())
    }

    private val _uiState = MutableStateFlow(TransmitUiState())
    val uiState: StateFlow<TransmitUiState> = _uiState.asStateFlow()

    private var fileBytes: ByteArray? = null
    private var allFrames: List<QrFrame> = emptyList()
    private val bitmapCache = ConcurrentHashMap<Int, Bitmap>()
    private var transmissionJob: Job? = null
    private var preRenderJob: Job? = null

    fun loadFileFromUri(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                pause()
                preRenderJob?.cancel()
                clearBitmapCache()

                val context = getApplication<Application>()
                var name = "file"
                var size = 0L
                val mime = context.contentResolver.getType(uri) ?: "application/octet-stream"

                context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                        if (nameIndex != -1) name = cursor.getString(nameIndex)
                        if (sizeIndex != -1) size = cursor.getLong(sizeIndex)
                    }
                }

                val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                    ?: return@launch

                fileBytes = bytes
                val effectiveSize = if (size > 0) size else bytes.size.toLong()

                rebuildChunks(bytes, name, mime, _uiState.value.chunkSize)

                _uiState.update {
                    it.copy(
                        fileName = name,
                        fileSize = effectiveSize,
                        mimeType = mime,
                        fileHash = allFrames.firstOrNull()?.fileHash ?: "",
                        isFileLoaded = true,
                        totalChunks = allFrames.size,
                        currentChunkIndex = 0,
                        loopCount = 0,
                        filteredChunks = null,
                        cachedFrameCount = 0,
                        statusMessage = "Ready: ${allFrames.size} optical chunks created"
                    )
                }

                // Render first frame immediately
                updateBitmapForIndex(0)

                // Pre-render the rest in background for zero-lag streaming
                startBackgroundPreRendering()
            } catch (e: Exception) {
                _uiState.update { it.copy(statusMessage = "Error reading file: ${e.localizedMessage}") }
            }
        }
    }

    fun setChunkSize(size: Int) {
        if (_uiState.value.chunkSize == size) return
        val bytes = fileBytes ?: return
        pause()
        preRenderJob?.cancel()
        clearBitmapCache()

        _uiState.update { it.copy(chunkSize = size, cachedFrameCount = 0) }
        rebuildChunks(bytes, _uiState.value.fileName, _uiState.value.mimeType, size)
        _uiState.update {
            it.copy(
                totalChunks = allFrames.size,
                currentChunkIndex = 0,
                filteredChunks = null,
                statusMessage = "Re-chunked: ${allFrames.size} chunks (${size} B/frame)"
            )
        }
        updateBitmapForIndex(0)
        startBackgroundPreRendering()
    }

    fun setFps(fps: Int) {
        val clamped = fps.coerceIn(1, 16)
        _uiState.update { it.copy(fps = clamped) }
        if (_uiState.value.isPlaying) {
            // Restart ticker with new frame rate
            startTransmissionLoop()
        }
    }

    fun togglePlay() {
        if (_uiState.value.isPlaying) {
            pause()
        } else {
            play()
        }
    }

    fun play() {
        if (allFrames.isEmpty()) return
        _uiState.update { it.copy(isPlaying = true, statusMessage = "Streaming optical QR codes...") }
        startTransmissionLoop()

        // Log transfer to history on initial send
        recordSendHistory()
    }

    fun pause() {
        transmissionJob?.cancel()
        transmissionJob = null
        _uiState.update { it.copy(isPlaying = false, statusMessage = "Stream paused") }
    }

    fun nextChunk() {
        if (allFrames.isEmpty()) return
        val activeIndices = getActiveIndices()
        val current = _uiState.value.currentChunkIndex
        val nextPos = (activeIndices.indexOf(current) + 1) % activeIndices.size
        val nextIndex = activeIndices[nextPos]
        _uiState.update { it.copy(currentChunkIndex = nextIndex) }
        updateBitmapForIndex(nextIndex)
    }

    fun previousChunk() {
        if (allFrames.isEmpty()) return
        val activeIndices = getActiveIndices()
        val current = _uiState.value.currentChunkIndex
        val prevPos = (activeIndices.indexOf(current) - 1 + activeIndices.size) % activeIndices.size
        val prevIndex = activeIndices[prevPos]
        _uiState.update { it.copy(currentChunkIndex = prevIndex) }
        updateBitmapForIndex(prevIndex)
    }

    fun seekTo(chunkIndex: Int) {
        if (chunkIndex in allFrames.indices) {
            _uiState.update { it.copy(currentChunkIndex = chunkIndex) }
            updateBitmapForIndex(chunkIndex)
        }
    }

    fun filterMissingChunks(indices: List<Int>) {
        val valid = indices.filter { it in allFrames.indices }
        if (valid.isNotEmpty()) {
            pause()
            _uiState.update {
                it.copy(
                    filteredChunks = valid,
                    currentChunkIndex = valid.first(),
                    statusMessage = "Targeted mode: streaming ${valid.size} requested missing chunks"
                )
            }
            updateBitmapForIndex(valid.first())
            play()
        }
    }

    fun clearFilter() {
        _uiState.update {
            it.copy(
                filteredChunks = null,
                statusMessage = "Standard loop mode: all ${allFrames.size} chunks"
            )
        }
    }

    private fun getActiveIndices(): List<Int> {
        return _uiState.value.filteredChunks ?: allFrames.indices.toList()
    }

    private fun rebuildChunks(bytes: ByteArray, name: String, mime: String, size: Int) {
        allFrames = FileChunker.splitFile(bytes, name, mime, size)
    }

    private fun startBackgroundPreRendering() {
        preRenderJob?.cancel()
        preRenderJob = viewModelScope.launch(Dispatchers.Default) {
            val total = allFrames.size
            for (i in 0 until total) {
                if (!isActive) break
                if (!bitmapCache.containsKey(i)) {
                    val frame = allFrames[i]
                    val json = frame.toJson()
                    val bitmap = QrCodeGenerator.generateQrBitmap(json, size = 480)
                    if (bitmap != null) {
                        bitmapCache[i] = bitmap
                    }
                }
                val cachedCount = bitmapCache.size
                if (cachedCount % 5 == 0 || cachedCount == total) {
                    _uiState.update { it.copy(cachedFrameCount = cachedCount) }
                }
            }
        }
    }

    private fun startTransmissionLoop() {
        transmissionJob?.cancel()
        transmissionJob = viewModelScope.launch(Dispatchers.Default) {
            while (isActive) {
                val delayMs = (1000L / _uiState.value.fps.coerceAtLeast(1))
                delay(delayMs)

                val activeIndices = getActiveIndices()
                if (activeIndices.isEmpty()) break

                val currentIndex = _uiState.value.currentChunkIndex
                val currentPos = activeIndices.indexOf(currentIndex)
                val nextPos = (currentPos + 1) % activeIndices.size
                val nextIndex = activeIndices[nextPos]

                val isNewLoop = nextPos == 0

                _uiState.update {
                    it.copy(
                        currentChunkIndex = nextIndex,
                        loopCount = if (isNewLoop) it.loopCount + 1 else it.loopCount
                    )
                }

                updateBitmapForIndex(nextIndex)
            }
        }
    }

    private fun updateBitmapForIndex(index: Int) {
        if (index !in allFrames.indices) return
        val cached = bitmapCache[index]
        if (cached != null) {
            _uiState.update { it.copy(currentQrBitmap = cached) }
        } else {
            // If not yet pre-rendered, generate synchronously on Default dispatcher
            viewModelScope.launch(Dispatchers.Default) {
                val frame = allFrames[index]
                val json = frame.toJson()
                val bitmap = QrCodeGenerator.generateQrBitmap(json, size = 480)
                if (bitmap != null) {
                    bitmapCache[index] = bitmap
                    _uiState.update { it.copy(currentQrBitmap = bitmap) }
                }
            }
        }
    }

    private fun clearBitmapCache() {
        bitmapCache.values.forEach { it.recycle() }
        bitmapCache.clear()
    }

    private var hasRecordedHistory = false
    private fun recordSendHistory() {
        if (hasRecordedHistory || allFrames.isEmpty()) return
        hasRecordedHistory = true
        viewModelScope.launch(Dispatchers.IO) {
            val state = _uiState.value
            repository.insertTransfer(
                TransferRecord(
                    fileId = allFrames.first().fileId,
                    fileName = state.fileName,
                    fileSize = state.fileSize,
                    mimeType = state.mimeType,
                    sha256 = state.fileHash,
                    direction = "SENT",
                    isSuccess = true
                )
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        transmissionJob?.cancel()
        preRenderJob?.cancel()
        clearBitmapCache()
    }
}
