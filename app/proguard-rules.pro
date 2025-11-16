# Add project specific ProGuard rules here.
-keep class org.tensorflow.** { *; }
-keep class com.google.ar.** { *; }
-keepclassmembers class * {
    @org.tensorflow.lite.annotations.* <methods>;
}
