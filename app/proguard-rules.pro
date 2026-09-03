# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# --- R8 keep rules for third-party libraries used by FiberApp ---
# Play Services Maps ships its own consumer rules, but keep this as a defensive
# backstop since some internal bridging classes are accessed reflectively.
-keep class com.google.android.gms.maps.** { *; }
-keep interface com.google.android.gms.maps.** { *; }

# KML / GeoJSON parsing (android-maps-utils) - keep data model classes intact.
-keep class com.google.maps.android.data.** { *; }
-keep class com.google.maps.android.data.kml.** { *; }
-keep class com.google.maps.android.data.geojson.** { *; }

# Third-party swipe UI library (unmaintained, no published consumer rules).
-keep class kaufland.com.swipelibrary.** { *; }

