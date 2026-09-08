package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transfers")
data class TransferRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val fileId: String,
    val fileName: String,
    val fileSize: Long,
    val mimeType: String,
    val sha256: String,
    val direction: String, // "SENT" or "RECEIVED"
    val timestamp: Long = System.currentTimeMillis(),
    val durationMs: Long = 0,
    val filePath: String? = null,
    val isSuccess: Boolean = true
)
