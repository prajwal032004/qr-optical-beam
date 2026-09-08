package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BrightnessHigh
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.CrisisAlert
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.SyncAlt
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.MenuBook
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuideScreen(modifier: Modifier = Modifier) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("Use Cases", "Protocol", "Calibration", "AMOLED", "FAQ")

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Top Header (Cohesive section banner without duplicating the TopAppBar AirQrLogo)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF00E5FF).copy(alpha = 0.12f))
                    .border(1.dp, Color(0xFF00E5FF).copy(alpha = 0.35f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.MenuBook,
                    contentDescription = null,
                    tint = Color(0xFF00E5FF),
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Protocol & Operations Manual",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFF00E5FF).copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "AIR-GAPPED",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF00E5FF),
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    }
                }
                Text(
                    text = "Photon-based optical data transmission manual",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Scrollable Tabs Row: guarantees all 5 tabs render smoothly without truncation
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            edgePadding = 0.dp,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = Color(0xFF00E5FF),
            divider = {},
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = Color(0xFF00E5FF),
                    height = 3.dp
                )
            }
        ) {
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            text = title,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 12.5.sp
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Tab Contents (Scrollable)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            when (selectedTab) {
                0 -> UseCasesTabContent()
                1 -> ProtocolTabContent()
                2 -> CalibrationTabContent()
                3 -> AmoledTabContent()
                4 -> FaqTabContent()
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Author Credit Card (Prajwal A B)
            AuthorCreditBentoCard()

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

// ----------------------------------------------------
// TAB 0: USE CASES & THREAT MITIGATION
// ----------------------------------------------------
@Composable
private fun UseCasesTabContent() {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        // Hero Card: Why Optical Air-Gapping
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                Color(0xFF00E5FF).copy(alpha = 0.35f)
            )
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF00E5FF).copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = Color(0xFF00E5FF),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Why Air-Gapped Optical Beaming?",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF00E5FF)
                        )
                        Text(
                            text = "Absolute physical isolation from network attacks",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Standard wireless sharing (Wi-Fi, Bluetooth, NFC, Cloud) exposes devices to packet sniffing, zero-day baseband exploits, man-in-the-middle routers, and telemetry tracking. AirQR uses visible light photons—creating a mathematically isolated optical data diode where data moves only through line-of-sight visual optics.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 19.sp
                )
            }
        }

        Text(
            text = "Primary High-Impact Scenarios",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 4.dp)
        )

        // Scenario 1: Cold-Storage & Crypto Vaults
        UseCaseDetailCard(
            badge = "WEB3 & CRYPTO ASSETS",
            badgeColor = Color(0xFFFFD54F),
            title = "Cold-Storage Hardware & Bitcoin Vaults",
            scenario = "Signing transactions (PSBT - Partially Signed Bitcoin Transactions), transferring multi-signature witness data, or exporting cryptographic seed backups without ever exposing the offline signing phone to the internet.",
            threatMitigated = "Completely neutralizes clipboard hijackers, remote Trojans (RATs), malicious Wi-Fi payloads, and unauthorized hardware wallet firmware tampering.",
            icon = Icons.Default.AccountBalanceWallet
        )

        // Scenario 2: SCIF & Classified Environments
        UseCaseDetailCard(
            badge = "DEFENSE & INTELLIGENCE",
            badgeColor = Color(0xFF00E5FF),
            title = "SCIFs & High-Security Government Facilities",
            scenario = "Secure Compartmented Information Facilities (SCIFs) forbid wireless radio emissions (RF) and untrusted USB mass storage drives by military protocol. AirQR functions as an optical data diode for verified, one-way ingress of mission files.",
            threatMitigated = "Complies with strict TEMPEST / RF-emission zero-radiation standards. Cannot be snooped with SDR (Software Defined Radio) antenna receivers.",
            icon = Icons.Default.Lock
        )

        // Scenario 3: Confidential Journalism & Whistleblowers
        UseCaseDetailCard(
            badge = "CONFIDENTIAL PRIVACY",
            badgeColor = Color(0xFF64FFDA),
            title = "Investigative Journalism & Whistleblowing",
            scenario = "Meeting confidential sources in high-surveillance jurisdictions. Both parties place their phones in Airplane Mode—shutting off all basebands and GPS—and exchange leaked documents or evidence peer-to-peer using camera optics.",
            threatMitigated = "Zero digital breadcrumbs on cell towers, no IMSI-catcher (Stingray) ping captures, no MAC address beaconing, and zero server log footprints.",
            icon = Icons.Default.Security
        )

        // Scenario 4: Blackouts & Emergency Disaster Relief
        UseCaseDetailCard(
            badge = "DISASTER & FIELD OPS",
            badgeColor = Color(0xFFFF7043),
            title = "Emergency Blackouts & Remote Fieldwork",
            scenario = "Hurricanes, earthquakes, offshore maritime missions, or underground tunnels where telecommunication towers and ISP backbones are destroyed or offline. First responders can share triage records, field maps, and medical data instantly.",
            threatMitigated = "Zero dependence on power grids, cell networks, satellite links, or cables. Only requires phone battery charge and camera optics.",
            icon = Icons.Default.CrisisAlert
        )

        // Scenario 5: Malware Incident Response & Forensics
        UseCaseDetailCard(
            badge = "CYBERSECURITY & FORENSICS",
            badgeColor = Color(0xFFE040FB),
            title = "Malware Quarantine & Forensic Extraction",
            scenario = "Extracting system logs, memory dumps, or captured suspicious payloads from an infected, network-isolated endpoint machine without plugging in a physical USB flash drive that could spread worms.",
            threatMitigated = "Eliminates BadUSB attacks, firmware rootkits, and automated network self-propagation vulnerabilities.",
            icon = Icons.Default.BugReport
        )

        // Scenario 6: Frictionless Cross-Platform Sharing
        UseCaseDetailCard(
            badge = "DAILY UTILITY",
            badgeColor = Color(0xFF00E676),
            title = "Frictionless Cross-Device Sharing",
            scenario = "Quickly sharing photos, codes, keys, or documents between any two phones without needing Apple AirDrop, Quick Share, cloud drive links, login accounts, or pairing codes.",
            threatMitigated = "Bypasses big-tech account requirements, tracking telemetry, and Bluetooth discoverability vulnerabilities.",
            icon = Icons.Default.CompareArrows
        )
    }
}

@Composable
private fun UseCaseDetailCard(
    badge: String,
    badgeColor: Color,
    title: String,
    scenario: String,
    threatMitigated: String,
    icon: ImageVector
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = badgeColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = badge,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = badgeColor,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                        fontSize = 10.sp,
                        letterSpacing = 0.8.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(badgeColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = badgeColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = scenario,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Threat mitigation highlight pill
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF00E676),
                        modifier = Modifier
                            .size(15.dp)
                            .padding(top = 1.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Threat Mitigated",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF00E676),
                            fontSize = 10.5.sp
                        )
                        Text(
                            text = threatMitigated,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.5.sp,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------
// TAB 1: PROTOCOL PIPELINE
// ----------------------------------------------------
@Composable
private fun ProtocolTabContent() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Hero Highlight Banner
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                Color(0xFF00E5FF).copy(alpha = 0.35f)
            )
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(Color(0xFF00E5FF).copy(alpha = 0.3f), Color(0xFF00E5FF).copy(alpha = 0.05f))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = Color(0xFF00E5FF),
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = "100% Offline & Air-Gapped",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF00E5FF)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Zero Wi-Fi, Bluetooth, NFC, or cellular radios. Data is carried strictly through visible optical light waves from display to camera lens.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        Text(
            text = "Step-by-Step Optical Transmission",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 6.dp)
        )

        ProtocolStepCard(
            stepNumber = "1",
            title = "File Slicing & QRF Packaging",
            summary = "The chosen file is sliced into micro binary chunks (150 to 400 bytes each). Each chunk is wrapped with rolling index headers, file metadata, and a master SHA-256 integrity hash.",
            icon = Icons.Default.GridOn,
            accentColor = Color(0xFF00E5FF)
        )

        ProtocolStepCard(
            stepNumber = "2",
            title = "High-Rate Animated QR Stream",
            summary = "The sender's screen rapidly loops the generated QR matrix codes like a digital film projector at 4 to 10 FPS. Each frame is a self-contained data packet.",
            icon = Icons.Default.QrCode2,
            accentColor = Color(0xFF64FFDA)
        )

        ProtocolStepCard(
            stepNumber = "3",
            title = "Rolling-Shutter Optical Capture",
            summary = "The receiving phone's camera analyzes optical frames at 30 to 60 FPS using on-device ML Kit. Packets can arrive in any order and duplicate frames are filtered automatically.",
            icon = Icons.Default.CameraAlt,
            accentColor = Color(0xFF00B0FF)
        )

        ProtocolStepCard(
            stepNumber = "4",
            title = "Optical Reverse-Sync (Missing Packets)",
            summary = "If motion blur or lighting caused any packets to drop, the receiver generates a compact 'Missing Chunks QR'. The sender scans this to re-beam only the unreceived chunks in seconds.",
            icon = Icons.Default.SyncAlt,
            accentColor = Color(0xFFFFD54F)
        )

        ProtocolStepCard(
            stepNumber = "5",
            title = "SHA-256 Assembly & Verification",
            summary = "Once all chunks are gathered, the file is reassembled bit-for-bit, verified against the cryptographic SHA-256 hash, and saved directly to device Downloads.",
            icon = Icons.Default.VpnKey,
            accentColor = Color(0xFF00E676)
        )
    }
}

// ----------------------------------------------------
// TAB 2: INTERACTIVE CALIBRATION SIMULATOR
// ----------------------------------------------------
@Composable
private fun CalibrationTabContent() {
    var testDistance by remember { mutableFloatStateOf(20f) }
    var testBrightness by remember { mutableFloatStateOf(85f) }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text(
            text = "Interactive Calibration Simulator",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Adjust the sliders below to see recommended optical alignment parameters for optimal QR stream capture.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Distance Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CompareArrows,
                            contentDescription = null,
                            tint = Color(0xFF00E5FF),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Camera Distance",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleSmall
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF00E5FF).copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "${testDistance.roundToInt()} cm",
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF00E5FF),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Slider(
                    value = testDistance,
                    onValueChange = { testDistance = it },
                    valueRange = 5f..45f,
                    colors = SliderDefaults.colors(
                        thumbColor = Color(0xFF00E5FF),
                        activeTrackColor = Color(0xFF00E5FF)
                    )
                )

                val distanceFeedback = when {
                    testDistance < 12f -> Pair("⚠️ Too Close: QR code corners may exceed the camera field of view.", MaterialTheme.colorScheme.error)
                    testDistance in 14f..26f -> Pair("🎯 Optimal Sweet Spot: Ideal framing & sharp barcode resolution.", Color(0xFF00E676))
                    else -> Pair("⚠️ Too Far: Micro modules may be too small for fast rolling-shutter focus.", Color(0xFFFFB74D))
                }

                Text(
                    text = distanceFeedback.first,
                    style = MaterialTheme.typography.bodySmall,
                    color = distanceFeedback.second,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // Screen Brightness Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.BrightnessHigh,
                            contentDescription = null,
                            tint = Color(0xFFFFD54F),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Sender Screen Brightness",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleSmall
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFFFD54F).copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "${testBrightness.roundToInt()}%",
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFFFD54F),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Slider(
                    value = testBrightness,
                    onValueChange = { testBrightness = it },
                    valueRange = 20f..100f,
                    colors = SliderDefaults.colors(
                        thumbColor = Color(0xFFFFD54F),
                        activeTrackColor = Color(0xFFFFD54F)
                    )
                )

                val brightnessFeedback = when {
                    testBrightness < 60f -> Pair("⚠️ Low Contrast: Dark screen slows down camera sensor exposure time.", MaterialTheme.colorScheme.error)
                    testBrightness < 80f -> Pair("ℹ️ Acceptable: Works indoors, but higher brightness speeds up frame lock.", Color(0xFFFFB74D))
                    else -> Pair("✨ Perfect: Maximum photon emission for high-speed 6-10 FPS optical decoding!", Color(0xFF00E676))
                }

                Text(
                    text = brightnessFeedback.first,
                    style = MaterialTheme.typography.bodySmall,
                    color = brightnessFeedback.second,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // Speed / Density Matrix Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Speed, contentDescription = null, tint = Color(0xFF00E5FF), modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Speed & Density Recommendations", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                }

                Spacer(modifier = Modifier.height(12.dp))

                DensityRow(preset = "Balanced", fps = "6 FPS", density = "256 B", useCase = "Best for most files & hand-held distances")
                DensityRow(preset = "High Speed", fps = "10 FPS", density = "400 B", useCase = "Super fast transfer with steady hands / desk rest")
                DensityRow(preset = "Robust", fps = "4 FPS", density = "150 B", useCase = "Dim light, farther distances, or shaky hands")
            }
        }
    }
}

@Composable
private fun DensityRow(preset: String, fps: String, density: String, useCase: String) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = preset, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(shape = RoundedCornerShape(4.dp), color = Color(0xFF00E5FF).copy(alpha = 0.15f)) {
                        Text(text = "$fps • $density", fontSize = 10.sp, color = Color(0xFF00E5FF), modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = useCase, fontSize = 11.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

// ----------------------------------------------------
// TAB 3: SAMSUNG GALAXY AMOLED TIPS
// ----------------------------------------------------
@Composable
private fun AmoledTabContent() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                Color(0xFF00E5FF).copy(alpha = 0.3f)
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.PhoneAndroid, contentDescription = null, tint = Color(0xFF00E5FF), modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(text = "Super AMOLED Screen Advantage", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "On Samsung Galaxy Super AMOLED displays, each pixel is individually illuminated. Pure black pixels (#000000) emit zero light, creating an infinite contrast ratio against bright white modules.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                )
            }
        }

        AmoledTipItem(
            title = "120Hz Screen Refresh Advantage",
            description = "High-refresh Super AMOLED displays eliminate frame ghosting between consecutive QR codes, allowing the receiver camera to lock onto clean frames even at 10 FPS.",
            icon = Icons.Default.ElectricBolt
        )

        AmoledTipItem(
            title = "Automatic Screen-Awake Protocol",
            description = "AirQR automatically sets FLAG_KEEP_SCREEN_ON during active beaming so the Samsung Galaxy display never times out or dims mid-transfer.",
            icon = Icons.Default.BrightnessHigh
        )

        AmoledTipItem(
            title = "True-Black Surround in Fullscreen Mode",
            description = "When tapping 'Fullscreen' on the Transmit screen, all surrounding UI chrome disappears and the borders become true deep black to prevent optical flare on the camera lens.",
            icon = Icons.Default.AutoAwesome
        )
    }
}

@Composable
private fun AmoledTipItem(title: String, description: String, icon: ImageVector) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
        )
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF00E5FF).copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = Color(0xFF00E5FF), modifier = Modifier.size(18.dp))
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 17.sp
                )
            }
        }
    }
}

// ----------------------------------------------------
// TAB 4: FREQUENTLY ASKED QUESTIONS (ACCORDION)
// ----------------------------------------------------
@Composable
private fun FaqTabContent() {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "Frequently Asked Questions",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        FaqAccordionItem(
            question = "Can I beam any file type?",
            answer = "Yes! AirQR handles 100% binary transparency. You can transfer photos, MP3 audio, video clips, PDFs, APKs, Zip archives, and raw text files. Everything is verified with cryptographic SHA-256."
        )

        FaqAccordionItem(
            question = "What if some frames are missed due to movement?",
            answer = "The receiver automatically identifies any missing chunk indices. It displays an instant 'Missing Chunks QR' which the sender scans. The sender then automatically beams only the missing chunks!"
        )

        FaqAccordionItem(
            question = "Is internet, Wi-Fi, or Bluetooth required?",
            answer = "No. AirQR requires zero wireless radios. It operates completely offline in Airplane Mode. Only camera hardware access is used."
        )

        FaqAccordionItem(
            question = "What is the recommended file size for QR streaming?",
            answer = "AirQR is optimized for documents, keys, text, photos, and files up to several megabytes. Larger files can be transferred by increasing FPS (e.g. 10 FPS with 400 bytes/frame)."
        )

        FaqAccordionItem(
            question = "Where are received files stored?",
            answer = "Reconstructed files are saved directly into your device's public Downloads folder under 'OpticalBeam' or 'AirQR'. You can preview, open, share, or export them anytime from the History tab."
        )
    }
}

@Composable
private fun FaqAccordionItem(question: String, answer: String) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .clip(RoundedCornerShape(16.dp))
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (expanded) Color(0xFF00E5FF).copy(alpha = 0.5f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = question,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.weight(1f),
                    color = if (expanded) Color(0xFF00E5FF) else MaterialTheme.colorScheme.onSurface
                )
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = if (expanded) Color(0xFF00E5FF) else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    Text(
                        text = answer,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun ProtocolStepCard(
    stepNumber: String,
    title: String,
    summary: String,
    icon: ImageVector,
    accentColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
        )
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Surface(
                shape = CircleShape,
                color = accentColor,
                modifier = Modifier.size(30.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = stepNumber,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.Black,
                        fontSize = 13.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = summary,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

// Author & Developer Credit Bento Card
@Composable
private fun AuthorCreditBentoCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            Color(0xFF00E5FF).copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF00E5FF), Color(0xFF00B0FF))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column {
                Text(
                    text = "Crafted by Prajwal A B",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "AirQR & Luma Optical Protocol • Air-Gapped Transmission",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF00E5FF)
                )
            }
        }
    }
}
