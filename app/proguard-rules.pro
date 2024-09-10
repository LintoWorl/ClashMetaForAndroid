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

-dontobfuscate

-assumenosideeffects class kotlin.jvm.internal.Intrinsics {
    public static void checkNotNull(...);
    public static void checkExpressionValueIsNotNull(...);
    public static void checkNotNullExpressionValue(...);
    public static void checkReturnedValueIsNotNull(...);
    public static void checkFieldIsNotNull(...);
    public static void checkParameterIsNotNull(...);
    public static void checkNotNullParameter(...);
}

# Kotlin Coroutine
# Allow R8 to optimize away the FastServiceLoader.
# Together with ServiceLoader optimization in R8
# this results in direct instantiation when loading Dispatchers.Main
-assumenosideeffects class kotlinx.coroutines.internal.MainDispatcherLoader {
    boolean FAST_SERVICE_LOADER_ENABLED return false;
}

-assumenosideeffects class kotlinx.coroutines.internal.FastServiceLoaderKt {
    boolean ANDROID_DETECTED return true;
}

-keep class kotlinx.coroutines.android.AndroidDispatcherFactory {*;}

# Disable support for "Missing Main Dispatcher", since we always have Android main dispatcher
-assumenosideeffects class kotlinx.coroutines.internal.MainDispatchersKt {
    boolean SUPPORT_MISSING return false;
}

# Statically turn off all debugging facilities and assertions
-assumenosideeffects class kotlinx.coroutines.DebugKt {
    boolean getASSERTIONS_ENABLED() return false;
    boolean getDEBUG() return false;
    boolean getRECOVER_STACK_TRACES() return false;
}

-keep public class * extends android.app.Activity
-keep public class * extends androidx.fragment.app.FragmentActivity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.preference.Preference
-keep public class * extends androidx.fragment.app.Fragment

-dontwarn android.support.**
 -keep class android.support.** { *; }


-keepclassmembers public class * extends android.view.View {
   void set*(***);
   *** get*();
}

# We want to keep methods in Activity that could be used in the XML attribute onClick
-keepclassmembers class * extends android.app.Activity {
   public void *(android.view.View);
}


-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}



-keepclassmembers class **.R$* {
    public static <fields>;
}

-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}

# keep all serializable class
-keep class * implements java.io.Serializable {*;}

-keep class app.hw.network.model.**{*;}

