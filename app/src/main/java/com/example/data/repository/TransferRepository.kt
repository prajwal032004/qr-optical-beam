package com.example.data.repository

import com.example.data.db.TransferDao
import com.example.data.model.TransferRecord
import kotlinx.coroutines.flow.Flow

class TransferRepository(private val transferDao: TransferDao) {

    val allTransfers: Flow<List<TransferRecord>> = transferDao.getAllTransfers()

    suspend fun insertTransfer(transfer: TransferRecord): Long = transferDao.insertTransfer(transfer)

    suspend fun deleteTransferById(id: Long) = transferDao.deleteTransferById(id)

    suspend fun clearAll() = transferDao.clearAll()
}
