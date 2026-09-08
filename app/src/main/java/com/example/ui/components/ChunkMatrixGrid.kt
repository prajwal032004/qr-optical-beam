package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CrisisAlert
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class MatrixFilter {
    ALL,
    MISSING,
    RECEIVED
}

/**
 * Dynamic 'Bit-Grid' view for Receiver stream health diagnostics.
 * Highlights specifically which chunks are missing in vivid red,
 * providing instant feedback on optical stream health and packet loss.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ChunkMatrixGrid(
    totalChunks: Int,
    receivedChunksMap: Map<Int, Boolean>,
    lastReceivedIndex: Int = -1,
    chunkSizeBytes: Int = 320,
    modifier: Modifier = Modifier,
    maxDisplayChunks: Int = 200,
    onRequestMissingQr: (() -> Unit)? = null,
    onInspectMissing: (() -> Unit)? = null
) {
    if (totalChunks <= 0) {
        StandbyBitGrid(modifier = modifier)
        return
    }

    var currentFilter by remember { mutableStateOf(MatrixFilter.ALL) }
    var inspectedChunkIndex by remember { mutableStateOf<Int?>(null) }

    val receivedCount = receivedChunksMap.count { it.value }
    val missingCount = (totalChunks - receivedCount).coerceAtLeast(0)
    val displayLimit = totalChunks.coerceAtMost(maxDisplayChunks)

    // Stream Health metrics
    val healthScore = if (totalChunks > 0) {
        ((receivedCount.toFloat() / totalChunks.toFloat()) * 100).toInt().coerceIn(0, 100)
    } else 100
    val packetLossPct = 100 - healthScore

    // Pulse animation for specifically highlighting missing chunks in vivid red
    val infiniteTransition = rememberInfiniteTransition(label = "missing_chunk_pulse")
    val missingPulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.45f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(750, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "missingPulseAlpha"
    )

    // Calculate contiguous missing chunk ranges (e.g. 4-7, 12, 18-20)
    val missingRanges = remember(totalChunks, receivedChunksMap) {
        val ranges = mutableListOf<Pair<Int, Int>>()
        var start = -1
        for (i in 0 until totalChunks) {
            val isReceived = receivedChunksMap[i] == true
            if (!isReceived) {
                if (start == -1) start = i
            } else {
                if (start != -1) {
                    ranges.add(Pair(start, i - 1))
                    start = -1
                }
            }
        }
        if (start != -1) {
            ranges.add(Pair(start, totalChunks - 1))
        }
        ranges
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .testTag("dynamic_bit_grid"),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (missingCount > 0 && receivedCount > 0) Color(0xFFFF1744).copy(alpha = 0.5f)
            else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header: Dynamic Bit-Grid Title & Stream Health Score
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (missingCount > 0 && receivedCount > 0) Color(0xFFFF1744).copy(alpha = 0.15f)
                                else Color(0xFF00E5FF).copy(alpha = 0.15f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.GridOn,
                            contentDescription = null,
                            tint = if (missingCount > 0 && receivedCount > 0) Color(0xFFFF5252) else Color(0xFF00E5FF),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Dynamic Bit-Grid",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (missingCount == 0) "Optical Sync Locked • 0 Missing"
                            else "$missingCount chunks missing in red • Instant health check",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Instant Stream Health Badge
                if (missingCount == 0 && receivedCount > 0) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF00E676).copy(alpha = 0.18f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00E676).copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF00E676))
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "100% HEALTHY",
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF00E676),
                                fontSize = 10.5.sp
                            )
                        }
                    }
                } else if (receivedCount > 0) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFFF1744).copy(alpha = 0.18f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF1744).copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFFF1744).copy(alpha = missingPulseAlpha))
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "$healthScore% HEALTH ($missingCount RED)",
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFFFF5252),
                                fontSize = 10.5.sp
                            )
                        }
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF00E5FF).copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = "AWAITING FRAMES",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF00E5FF),
                            fontSize = 10.sp,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Stream Health Telemetry Bar (Green = Received, Vivid Red = Missing)
            if (totalChunks > 0) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Stream Health: $healthScore%",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (healthScore > 80) Color(0xFF00E676) else if (healthScore > 50) Color(0xFFFFB300) else Color(0xFFFF5252)
                        )
                        Text(
                            text = if (missingCount > 0) "$missingCount missing ($packetLossPct% loss)" else "0 packets dropped",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (missingCount > 0) Color(0xFFFF5252) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Segmented health bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        val receivedWeight = (receivedCount.toFloat() / totalChunks).coerceIn(0f, 1f)
                        val missingWeight = (missingCount.toFloat() / totalChunks).coerceIn(0f, 1f)

                        if (receivedWeight > 0f) {
                            Box(
                                modifier = Modifier
                                    .weight(receivedWeight)
                                    .fillMaxWidth()
                                    .background(Color(0xFF00E676))
                            )
                        }
                        if (missingWeight > 0f) {
                            Box(
                                modifier = Modifier
                                    .weight(missingWeight)
                                    .fillMaxWidth()
                                    .background(Color(0xFFFF1744).copy(alpha = missingPulseAlpha))
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Filter Selector Chips (All, Missing Chunks in Red, Received)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FilterChip(
                    selected = currentFilter == MatrixFilter.ALL,
                    onClick = { currentFilter = MatrixFilter.ALL },
                    label = { Text("All ($totalChunks)", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF00E5FF).copy(alpha = 0.18f),
                        selectedLabelColor = Color(0xFF00E5FF)
                    ),
                    modifier = Modifier.height(28.dp)
                )

                FilterChip(
                    selected = currentFilter == MatrixFilter.MISSING,
                    onClick = { currentFilter = MatrixFilter.MISSING },
                    label = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFFF1744))
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Missing ($missingCount)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFFFF1744).copy(alpha = 0.25f),
                        selectedLabelColor = Color(0xFFFF5252)
                    ),
                    modifier = Modifier.height(28.dp)
                )

                FilterChip(
                    selected = currentFilter == MatrixFilter.RECEIVED,
                    onClick = { currentFilter = MatrixFilter.RECEIVED },
                    label = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF00E676))
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Received ($receivedCount)", fontSize = 11.sp)
                        }
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF00E676).copy(alpha = 0.18f),
                        selectedLabelColor = Color(0xFF00E676)
                    ),
                    modifier = Modifier.height(28.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Visual Bit-Grid Modules (Missing Chunks explicitly highlighted in Vivid Red)
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(3.5.dp),
                verticalArrangement = Arrangement.spacedBy(3.5.dp)
            ) {
                for (i in 0 until displayLimit) {
                    val isReceived = receivedChunksMap[i] == true
                    val isLatest = i == lastReceivedIndex
                    val isSelected = inspectedChunkIndex == i

                    val matchesFilter = when (currentFilter) {
                        MatrixFilter.ALL -> true
                        MatrixFilter.MISSING -> !isReceived
                        MatrixFilter.RECEIVED -> isReceived
                    }

                    if (matchesFilter) {
                        val blockColor = when {
                            isLatest -> Color(0xFF00F0FF) // Glowing cyan for active packet
                            isReceived -> Color(0xFF00E676) // Vibrant emerald for received
                            else -> Color(0xFFFF1744).copy(alpha = missingPulseAlpha) // Specifically highlighted in RED for missing
                        }

                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(blockColor)
                                .clickable {
                                    inspectedChunkIndex = if (inspectedChunkIndex == i) null else i
                                }
                                .then(
                                    if (isSelected) {
                                        Modifier.border(1.8.dp, Color.White, RoundedCornerShape(3.dp))
                                    } else if (isLatest) {
                                        Modifier.border(1.2.dp, Color(0xFF00E5FF), RoundedCornerShape(3.dp))
                                    } else if (!isReceived) {
                                        Modifier.border(0.8.dp, Color(0xFFFF5252), RoundedCornerShape(3.dp))
                                    } else {
                                        Modifier
                                    }
                                )
                        )
                    }
                }
            }

            if (totalChunks > maxDisplayChunks) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "+ ${totalChunks - maxDisplayChunks} more packets represented",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 10.sp
                )
            }

            // Legend below the Bit-Grid
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).clip(RoundedCornerShape(2.dp)).background(Color(0xFF00E676)))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Received", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).clip(RoundedCornerShape(2.dp)).background(Color(0xFFFF1744)))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Missing (Red)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFF5252))
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).clip(RoundedCornerShape(2.dp)).background(Color(0xFF00F0FF)))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Live Ingest", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            // Interactive Packet Inspector Card (when user taps a specific chunk)
            AnimatedVisibility(visible = inspectedChunkIndex != null) {
                inspectedChunkIndex?.let { chunkIdx ->
                    val isReceived = receivedChunksMap[chunkIdx] == true
                    val startByte = chunkIdx.toLong() * chunkSizeBytes
                    val endByte = startByte + chunkSizeBytes

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isReceived) Color(0xFF00E676).copy(alpha = 0.08f) else Color(0xFFFF1744).copy(alpha = 0.10f),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isReceived) Color(0xFF00E676).copy(alpha = 0.5f) else Color(0xFFFF1744).copy(alpha = 0.5f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (isReceived) Icons.Default.CheckCircle else Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = if (isReceived) Color(0xFF00E676) else Color(0xFFFF1744),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Packet #${chunkIdx + 1} of $totalChunks",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = if (isReceived) Color(0xFF00E676).copy(alpha = 0.15f) else Color(0xFFFF1744).copy(alpha = 0.2f)
                                    ) {
                                        Text(
                                            text = if (isReceived) "RECEIVED" else "MISSING (RED)",
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 9.sp,
                                            color = if (isReceived) Color(0xFF00E676) else Color(0xFFFF5252),
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "Offset: $startByte – $endByte bytes • ${if (isReceived) "Stored in buffer" else "Dropped / Camera frame skipped"}",
                                    fontSize = 10.5.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            IconButton(
                                onClick = { inspectedChunkIndex = null },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Close", modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }

            // Grouped Missing Ranges Breakdown (specifically showing which chunks are missing)
            if (missingRanges.isNotEmpty() && receivedCount > 0) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFFF1744).copy(alpha = 0.08f),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        Color(0xFFFF1744).copy(alpha = 0.35f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFFF1744))
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Missing Chunks ($missingCount in ${missingRanges.size} segment${if (missingRanges.size > 1) "s" else ""}):",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFF5252)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            missingRanges.take(14).forEach { (start, end) ->
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFFFF1744).copy(alpha = 0.16f),
                                    border = androidx.compose.foundation.BorderStroke(
                                        0.8.dp,
                                        Color(0xFFFF1744).copy(alpha = 0.5f)
                                    )
                                ) {
                                    val text = if (start == end) "Chunk #${start + 1}" else "Chunks #${start + 1}–${end + 1}"
                                    Text(
                                        text = text,
                                        fontSize = 10.5.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFFF8A80),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            if (missingRanges.size > 14) {
                                Text(
                                    text = "+ ${missingRanges.size - 14} more segments",
                                    fontSize = 10.sp,
                                    color = Color(0xFFFF5252),
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Dynamic Bit-Grid Standby state when no stream is actively being received.
 * Informs the user that the optical packet matrix is armed and ready to monitor stream health.
 */
@Composable
fun StandbyBitGrid(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "standby_bit_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .testTag("dynamic_bit_grid_standby"),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            Color(0xFF00E5FF).copy(alpha = 0.25f)
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF00E5FF).copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.GridOn,
                            contentDescription = null,
                            tint = Color(0xFF00E5FF),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Dynamic Bit-Grid",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Optical Stream Health Monitor",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF00E5FF).copy(alpha = 0.12f)
                ) {
                    Text(
                        text = "SENSOR ARMED",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF00E5FF),
                        fontSize = 10.sp,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Animated standby matrix pattern (3 rows of 16 dots)
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                for (row in 0 until 3) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        for (col in 0 until 14) {
                            val dotColor = if (col % 4 == 0) {
                                Color(0xFF00E5FF).copy(alpha = pulseAlpha)
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                            }
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(dotColor)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = Color(0xFF00E5FF),
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Stream health updates live. Dropped chunks highlight in red.",
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
