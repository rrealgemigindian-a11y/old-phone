-keep class com.kasari.update.** { *; }
-dontwarn com.kasari.update.**
-dontwarn kotlin.**
-dontwarn okhttp3.**
-dontwarn okio.**

-keepclassmembers class * extends android.app.Service { *; }
-keepclassmembers class * extends android.content.BroadcastReceiver { *; }

# Keep Google Play Services
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.android.gms.**