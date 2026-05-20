# Default ProGuard rules. Release minification is currently disabled so this file
# is a placeholder — keep it around for when shrinking gets turned on.
-dontwarn org.bouncycastle.jsse.**
-dontwarn org.conscrypt.**
-dontwarn org.openjsse.**

# kotlinx.serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keep,includedescriptorclasses class com.patchfox.mise.**$$serializer { *; }
-keepclassmembers class com.patchfox.mise.** {
    *** Companion;
}
-keepclasseswithmembers class com.patchfox.mise.** {
    kotlinx.serialization.KSerializer serializer(...);
}
