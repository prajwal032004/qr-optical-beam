package com.example.camera

import android.content.Context
import androidx.annotation.OptIn
import androidx.camera.core.Camera
import androidx.camera.core.CameraControl
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.FlipCameraAndroid
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors

@Composable
fun CameraPreview(
    modifier: Modifier = Modifier,
    isTorchEnabled: Boolean = false,
    onTorchToggle: (Boolean) -> Unit = {},
    onQrCodeScanned: (String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var lensFacing by remember { mutableStateOf(CameraSelector.LENS_FACING_BACK) }
    var cameraControl by remember { mutableStateOf<CameraControl?>(null) }
    var hasFlashUnit by remember { mutableStateOf(false) }
    var cameraProvider by remember { mutableStateOf<ProcessCameraProvider?>(null) }

    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    val scanner = remember {
        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .build()
        BarcodeScanning.getClient(options)
    }

    DisposableEffect(lifecycleOwner) {
        onDispose {
            try {
                cameraControl?.enableTorch(false)
            } catch (ignored: Exception) {}
            try {
                cameraProvider?.unbindAll()
            } catch (ignored: Exception) {}
            try {
                scanner.close()
            } catch (ignored: Exception) {}
            cameraExecutor.shutdown()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx).apply {
                    implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                }

                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                cameraProviderFuture.addListener({
                    try {
                        val provider = cameraProviderFuture.get()
                        cameraProvider = provider
                        val preview = Preview.Builder().build().also {
                            it.surfaceProvider = previewView.surfaceProvider
                        }

                        val imageAnalysis = ImageAnalysis.Builder()
                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                            .build()

                        imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
                            if (!cameraExecutor.isShutdown) {
                                processImageProxy(scanner, imageProxy, onQrCodeScanned)
                            } else {
                                try {
                                    imageProxy.close()
                                } catch (ignored: Exception) {}
                            }
                        }

                        val cameraSelector = CameraSelector.Builder()
                            .requireLensFacing(lensFacing)
                            .build()

                        provider.unbindAll()
                        val camera = provider.bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector,
                            preview,
                            imageAnalysis
                        )
                        cameraControl = camera.cameraControl
                        hasFlashUnit = camera.cameraInfo.hasFlashUnit()
                        if (isTorchEnabled && hasFlashUnit) {
                            camera.cameraControl.enableTorch(true)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }, ContextCompat.getMainExecutor(ctx))

                previewView
            },
            update = { previewView ->
                try {
                    cameraControl?.enableTorch(isTorchEnabled && hasFlashUnit)
                } catch (ignored: Exception) {}
            },
            modifier = Modifier.fillMaxSize()
        )

        // Visual Target Reticle with glowing corners
        ScannerReticleOverlay(
            modifier = Modifier.fillMaxSize()
        )

        // Overlay camera quick controls
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 16.dp, end = 16.dp)
        ) {
            if (hasFlashUnit) {
                FilledIconButton(
                    onClick = {
                        val nextState = !isTorchEnabled
                        onTorchToggle(nextState)
                        cameraControl?.enableTorch(nextState)
                    },
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = if (isTorchEnabled) Color(0xFF00E5FF) else Color(0x99000000),
                        contentColor = if (isTorchEnabled) Color.Black else Color.White
                    ),
                    modifier = Modifier.testTag("torch_button")
                ) {
                    Icon(
                        imageVector = if (isTorchEnabled) Icons.Default.FlashOn else Icons.Default.FlashOff,
                        contentDescription = "Toggle Torch"
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalGetImage::class)
private fun processImageProxy(
    scanner: BarcodeScanner,
    imageProxy: ImageProxy,
    onQrDetected: (String) -> Unit
) {
    val mediaImage = imageProxy.image
    if (mediaImage != null) {
        val inputImage = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        scanner.process(inputImage)
            .addOnSuccessListener { barcodes ->
                for (barcode in barcodes) {
                    barcode.rawValue?.let { raw ->
                        onQrDetected(raw)
                    }
                }
            }
            .addOnCompleteListener {
                try {
                    imageProxy.close()
                } catch (ignored: Exception) {}
            }
    } else {
        try {
            imageProxy.close()
        } catch (ignored: Exception) {}
    }
}

@Composable
fun ScannerReticleOverlay(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        val boxSize = (canvasWidth * 0.75f).coerceAtMost(320.dp.toPx())
        val left = (canvasWidth - boxSize) / 2f
        val top = (canvasHeight - boxSize) / 2.3f
        val cornerLength = 36.dp.toPx()
        val cornerStroke = 4.dp.toPx()
        val cornerColor = Color(0xFF00F0FF)

        // Semi-transparent darkened background outside scan area
        // Draw top
        drawRect(Color(0x80000000), Offset.Zero, Size(canvasWidth, top))
        // Draw bottom
        drawRect(Color(0x80000000), Offset(0f, top + boxSize), Size(canvasWidth, canvasHeight - (top + boxSize)))
        // Draw left
        drawRect(Color(0x80000000), Offset(0f, top), Size(left, boxSize))
        // Draw right
        drawRect(Color(0x80000000), Offset(left + boxSize, top), Size(canvasWidth - (left + boxSize), boxSize))

        // Center reticle border (faint dashed)
        drawRoundRect(
            color = Color(0x3300F0FF),
            topLeft = Offset(left, top),
            size = Size(boxSize, boxSize),
            cornerRadius = CornerRadius(16.dp.toPx()),
            style = Stroke(
                width = 1.5.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
            )
        )

        // Top-left corner
        drawLine(cornerColor, Offset(left, top), Offset(left + cornerLength, top), cornerStroke)
        drawLine(cornerColor, Offset(left, top), Offset(left, top + cornerLength), cornerStroke)

        // Top-right corner
        drawLine(cornerColor, Offset(left + boxSize, top), Offset(left + boxSize - cornerLength, top), cornerStroke)
        drawLine(cornerColor, Offset(left + boxSize, top), Offset(left + boxSize, top + cornerLength), cornerStroke)

        // Bottom-left corner
        drawLine(cornerColor, Offset(left, top + boxSize), Offset(left + cornerLength, top + boxSize), cornerStroke)
        drawLine(cornerColor, Offset(left, top + boxSize), Offset(left, top + boxSize - cornerLength), cornerStroke)

        // Bottom-right corner
        drawLine(cornerColor, Offset(left + boxSize, top + boxSize), Offset(left + boxSize - cornerLength, top + boxSize), cornerStroke)
        drawLine(cornerColor, Offset(left + boxSize, top + boxSize), Offset(left + boxSize, top + boxSize - cornerLength), cornerStroke)
    }
}
