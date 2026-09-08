package com.example.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.media.MediaScannerConnection
import android.os.Environment
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.model.TransferRecord
import com.example.data.repository.TransferRepository
import com.example.protocol.HashUtils
import com.example.protocol.MissingChunksRequest
import com.example.protocol.QrFrame
import com.example.qr.QrCodeGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

enum class ReceiveStatus {
    IDLE,
    RECEIVING,
    VERIFYING,
    COMPLETED,
    HASH_MISMATCH,
    ERROR
}

data class ReceiveUiState(
    val status: ReceiveStatus = ReceiveStatus.IDLE,
    val isScanning: Boolean = true,
    val isTorchEnabled: Boolean = false,
    val fileId: String = "",
    val fileName: String = "",
    val fileSize: Long = 0,
    val mimeType: String = "",
    val expectedHash: String = "",
    val calculatedHash: String = "",
    val totalChunks: Int = 0,
    val receivedCount: Int = 0,
    val receivedChunksMap: Map<Int, Boolean> = emptyMap(), // index -> true if received
    val missingIndices: List<Int> = emptyList(),
    val progress: Float = 0f,
    val speedChunksPerSec: Float = 0f,
    val savedFilePath: String? = null,
    val statusMessage: String = "Align camera with sending screen to receive file stream",
    val missingChunksQrBitmap: Bitmap? = null,
    val showMissingChunksDialog: Boolean = false,
    val lastReceivedChunkIndex: Int = -1
)

class ReceiveViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: TransferRepository by lazy {
        val db = AppDatabase.getDatabase(application)
        TransferRepository(db.transferDao())
    }

    private val _uiState = MutableStateFlow(ReceiveUiState())
    val uiState: StateFlow<ReceiveUiState> = _uiState.asStateFlow()

    private val chunkDataMap = mutableMapOf<Int, ByteArray>()
    private var transferStartTime = 0L
    private var framesReceivedCount = 0
    private var reconstructedFileBytes: ByteArray? = null

    fun onQrScanned(rawContent: String) {
        // Only process frames if scanning is active and not already finished
        if (!_uiState.value.isScanning) return
        if (_uiState.value.status == ReceiveStatus.COMPLETED) return

        val frame = QrFrame.fromJson(rawContent) ?: return

        handleQrFrame(frame)
    }

    private fun handleQrFrame(frame: QrFrame) {
        val currentState = _uiState.value

        // Check if starting a new file transfer session
        if (currentState.fileId.isEmpty() || currentState.fileId != frame.fileId) {
            chunkDataMap.clear()
            transferStartTime = System.currentTimeMillis()
            framesReceivedCount = 0
            reconstructedFileBytes = null

            val initialReceivedMap = mutableMapOf<Int, Boolean>()
            for (i in 0 until frame.totalChunks) {
                initialReceivedMap[i] = false
            }

            _uiState.update {
                it.copy(
                    status = ReceiveStatus.RECEIVING,
                    fileId = frame.fileId,
                    fileName = frame.fileName,
                    fileSize = frame.fileSize,
                    mimeType = frame.mimeType,
                    expectedHash = frame.fileHash,
                    totalChunks = frame.totalChunks,
                    receivedCount = 0,
                    receivedChunksMap = initialReceivedMap,
                    missingIndices = (0 until frame.totalChunks).toList(),
                    progress = 0f,
                    savedFilePath = null,
                    statusMessage = "Receiving ${frame.fileName} (${formatBytes(frame.fileSize)})",
                    lastReceivedChunkIndex = -1
                )
            }
        }

        // Check if this chunk is already received
        if (chunkDataMap.containsKey(frame.chunkIndex)) {
            // Already received, duplicate ignore
            return
        }

        // Store chunk payload
        val rawBytes = frame.rawData
        chunkDataMap[frame.chunkIndex] = rawBytes
        framesReceivedCount++

        val total = frame.totalChunks
        val receivedCount = chunkDataMap.size
        val progress = if (total > 0) receivedCount.toFloat() / total.toFloat() else 0f

        val updatedMap = _uiState.value.receivedChunksMap.toMutableMap()
        updatedMap[frame.chunkIndex] = true

        val missing = mutableListOf<Int>()
        for (i in 0 until total) {
            if (!chunkDataMap.containsKey(i)) {
                missing.add(i)
            }
        }

        val elapsedSec = ((System.currentTimeMillis() - transferStartTime) / 1000f).coerceAtLeast(0.1f)
        val speed = framesReceivedCount / elapsedSec

        _uiState.update {
            it.copy(
                receivedCount = receivedCount,
                receivedChunksMap = updatedMap,
                missingIndices = missing,
                progress = progress,
                speedChunksPerSec = speed,
                lastReceivedChunkIndex = frame.chunkIndex,
                statusMessage = "Chunks: $receivedCount / $total (${(progress * 100).toInt()}%)"
            )
        }

        // Check if all chunks received!
        if (receivedCount >= total) {
            finalizeAndReconstruct()
        }
    }

    private fun finalizeAndReconstruct() {
        val state = _uiState.value
        _uiState.update {
            it.copy(
                status = ReceiveStatus.VERIFYING,
                statusMessage = "All chunks received! Verifying SHA-256 hash..."
            )
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val total = state.totalChunks
                val outputStream = ByteArrayOutputStream()
                for (i in 0 until total) {
                    val chunk = chunkDataMap[i] ?: ByteArray(0)
                    outputStream.write(chunk)
                }

                val fullBytes = outputStream.toByteArray()
                reconstructedFileBytes = fullBytes

                val computedHash = HashUtils.sha256(fullBytes)

                if (computedHash.equals(state.expectedHash, ignoreCase = true)) {
                    // Valid! Save file
                    val savedFile = saveReconstructedFile(state.fileName, fullBytes)
                    val duration = System.currentTimeMillis() - transferStartTime

                    // Save to Room DB
                    repository.insertTransfer(
                        TransferRecord(
                            fileId = state.fileId,
                            fileName = state.fileName,
                            fileSize = state.fileSize,
                            mimeType = state.mimeType,
                            sha256 = computedHash,
                            direction = "RECEIVED",
                            durationMs = duration,
                            filePath = savedFile.absolutePath,
                            isSuccess = true
                        )
                    )

                    withContext(Dispatchers.Main) {
                        _uiState.update {
                            it.copy(
                                status = ReceiveStatus.COMPLETED,
                                isScanning = false,
                                calculatedHash = computedHash,
                                savedFilePath = savedFile.absolutePath,
                                statusMessage = "Transfer complete & verified! Saved to device."
                            )
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        _uiState.update {
                            it.copy(
                                status = ReceiveStatus.HASH_MISMATCH,
                                calculatedHash = computedHash,
                                statusMessage = "Checksum mismatch: original ${state.expectedHash.take(8)}... vs received ${computedHash.take(8)}..."
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _uiState.update {
                        it.copy(
                            status = ReceiveStatus.ERROR,
                            statusMessage = "Reconstruction error: ${e.localizedMessage}"
                        )
                    }
                }
            }
        }
    }

    private fun saveReconstructedFile(fileName: String, bytes: ByteArray): File {
        val context = getApplication<Application>()
        // Save to public Downloads directory or app external files
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val targetDir = File(downloadsDir, "OpticalBeam").apply { if (!exists()) mkdirs() }
        var targetFile = File(targetDir, fileName)

        // Prevent overwrite if name collisions exist
        if (targetFile.exists()) {
            val baseName = fileName.substringBeforeLast('.', fileName)
            val extension = fileName.substringAfterLast('.', "")
            val extStr = if (extension.isNotEmpty()) ".$extension" else ""
            targetFile = File(targetDir, "${baseName}_${System.currentTimeMillis()}$extStr")
        }

        FileOutputStream(targetFile).use { it.write(bytes) }

        // Trigger media scanner so it's immediately visible to user file managers
        MediaScannerConnection.scanFile(
            context,
            arrayOf(targetFile.absolutePath),
            null,
            null
        )

        return targetFile
    }

    fun generateMissingChunksRequestQr() {
        val state = _uiState.value
        if (state.missingIndices.isEmpty() || state.fileId.isEmpty()) return

        val request = MissingChunksRequest(
            fileId = state.fileId,
            neededChunks = state.missingIndices
        )
        val json = request.toJson()
        val bitmap = QrCodeGenerator.generateQrBitmap(json, size = 512)

        _uiState.update {
            it.copy(
                missingChunksQrBitmap = bitmap,
                showMissingChunksDialog = true
            )
        }
    }

    fun dismissMissingChunksDialog() {
        _uiState.update { it.copy(showMissingChunksDialog = false) }
    }

    fun toggleTorch(enabled: Boolean) {
        _uiState.update { it.copy(isTorchEnabled = enabled) }
    }

    fun resetSession() {
        chunkDataMap.clear()
        reconstructedFileBytes = null
        _uiState.update {
            ReceiveUiState(isScanning = true, isTorchEnabled = it.isTorchEnabled)
        }
    }

    private fun formatBytes(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> "%.1f KB".format(bytes / 1024.0)
            else -> "%.2f MB".format(bytes / (1024.0 * 1024.0))
        }
    }
}
