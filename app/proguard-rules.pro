# Add project specific ProGuard rules here.
-keepattributes *Annotation*
-keep class com.gptclone.app.database.** { *; }
-keep class com.gptclone.app.model.** { *; }
-dontwarn io.noties.markwon.**
-dontwarn io.noties.prism4j.**
