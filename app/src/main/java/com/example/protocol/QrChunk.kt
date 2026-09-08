package com.example.protocol

import android.util.Base64
import org.json.JSONArray
import org.json.JSONObject
import java.security.MessageDigest

/**
 * Optical QR File Transfer Frame Protocol (QRF v1)
 * Self-contained frame format designed for high-speed simplex optical streaming.
 */
data class QrFrame(
    val fileId: String,
    val chunkIndex: Int,
    val totalChunks: Int,
    val fileSize: Long,
    val fileName: String,
    val mimeType: String,
    val fileHash: String, // SHA-256 hex string
    val dataBase64: String
) {
    fun toJson(): String {
        val json = JSONObject()
        json.put("t", "qrf")
        json.put("id", fileId)
        json.put("i", chunkIndex)
        json.put("n", totalChunks)
        json.put("s", fileSize)
        json.put("name", fileName)
        json.put("mime", mimeType)
        json.put("hash", fileHash)
        json.put("d", dataBase64)
        return json.toString()
    }

    val rawData: ByteArray
        get() = try {
            Base64.decode(dataBase64, Base64.NO_WRAP)
        } catch (e: Exception) {
            ByteArray(0)
        }

    companion object {
        fun fromJson(jsonStr: String): QrFrame? {
            return try {
                if (!jsonStr.startsWith("{") || !jsonStr.contains("\"qrf\"")) return null
                val json = JSONObject(jsonStr)
                if (json.optString("t") != "qrf") return null

                QrFrame(
                    fileId = json.getString("id"),
                    chunkIndex = json.getInt("i"),
                    totalChunks = json.getInt("n"),
                    fileSize = json.getLong("s"),
                    fileName = json.getString("name"),
                    mimeType = json.getString("mime"),
                    fileHash = json.getString("hash"),
                    dataBase64 = json.getString("d")
                )
            } catch (e: Exception) {
                null
            }
        }
    }
}

/**
 * Optical reverse feedback packet: allows receiver to request missing chunk numbers
 */
data class MissingChunksRequest(
    val fileId: String,
    val neededChunks: List<Int>
) {
    fun toJson(): String {
        val json = JSONObject()
        json.put("t", "req")
        json.put("id", fileId)
        val arr = JSONArray()
        neededChunks.forEach { arr.put(it) }
        json.put("need", arr)
        return json.toString()
    }

    companion object {
        fun fromJson(jsonStr: String): MissingChunksRequest? {
            return try {
                if (!jsonStr.startsWith("{") || !jsonStr.contains("\"req\"")) return null
                val json = JSONObject(jsonStr)
                if (json.optString("t") != "req") return null
                val arr = json.getJSONArray("need")
                val list = mutableListOf<Int>()
                for (i in 0 until arr.length()) {
                    list.add(arr.getInt(i))
                }
                MissingChunksRequest(
                    fileId = json.getString("id"),
                    neededChunks = list
                )
            } catch (e: Exception) {
                null
            }
        }
    }
}

object HashUtils {
    fun sha256(bytes: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(bytes)
        return hash.joinToString("") { "%02x".format(it) }
    }
}

object FileChunker {
    fun splitFile(
        fileBytes: ByteArray,
        fileName: String,
        mimeType: String,
        chunkSize: Int = 256
    ): List<QrFrame> {
        val fileId = (System.currentTimeMillis() % 1000000).toString(16).padStart(6, '0')
        val totalSize = fileBytes.size.toLong()
        val fileHash = HashUtils.sha256(fileBytes)

        val totalChunks = if (fileBytes.isEmpty()) 1 else (fileBytes.size + chunkSize - 1) / chunkSize
        val frames = mutableListOf<QrFrame>()

        for (i in 0 until totalChunks) {
            val start = i * chunkSize
            val end = (start + chunkSize).coerceAtMost(fileBytes.size)
            val chunkBytes = if (fileBytes.isNotEmpty()) fileBytes.copyOfRange(start, end) else ByteArray(0)
            val chunkBase64 = Base64.encodeToString(chunkBytes, Base64.NO_WRAP)

            frames.add(
                QrFrame(
                    fileId = fileId,
                    chunkIndex = i,
                    totalChunks = totalChunks,
                    fileSize = totalSize,
                    fileName = fileName,
                    mimeType = mimeType,
                    fileHash = fileHash,
                    dataBase64 = chunkBase64
                )
            )
        }
        return frames
    }
}
