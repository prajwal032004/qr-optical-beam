# ML Kit discovers its components by class name from manifest <meta-data> (via
# MlKitComponentDiscoveryService). R8 cannot see that reflection, so without these rules the
# registrars are renamed/stripped and BarcodeScanning.getClient() crashes with an NPE in
# release builds as soon as the Receive screen opens the camera.
-keep class * implements com.google.firebase.components.ComponentRegistrar { *; }
-keep class com.google.mlkit.** { *; }
-keep class com.google.android.gms.internal.mlkit_vision_barcode** { *; }
-keep class com.google.android.gms.internal.mlkit_vision_common** { *; }
-dontwarn com.google.mlkit.**

# Readable crash stack traces (the mapping file restores names).
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
