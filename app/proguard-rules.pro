# Kotlinx Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keep,includedescriptorclasses class com.lvsmsmch.deckbuilder.**$$serializer { *; }
-keepclassmembers class com.lvsmsmch.deckbuilder.** {
    *** Companion;
}
-keepclasseswithmembers class com.lvsmsmch.deckbuilder.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# DTOs accessed reflectively by Retrofit / Serialization
-keep class com.lvsmsmch.deckbuilder.data.network.dto.** { *; }

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**

# Strip log calls in release
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
}
