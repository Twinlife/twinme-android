# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /home/chris/android/android-studio-sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
-renamesourcefileattribute SourceFile

# The support library contains references to newer platform versions.
# Don't warn about those in case this app is linking against an older
# platform version.  We know about them, and they are safe.
-dontwarn android.support.**

# keep setters in Views so that animations can still work.
# see http://proguard.sourceforge.net/manual/examples.html#beans
-keepclassmembers public class * extends android.view.View {
   void set*(***);
   *** get*();
}

# We want to keep methods in Activity that could be used in the XML attribute onClick
-keepclassmembers class * extends android.app.Activity {
   public void *(android.view.View);
}

# For enumeration classes, see http://proguard.sourceforge.net/manual/examples.html#enumerations
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}

# WebView
-keep class mobi.skred.app.R$raw {
    *;
}

# WebRTC
-keep class org.webrtc.** {
    *;
}

# libwebsockets
-keep class org.libwebsockets.ConnectionStats {
    *;
}
-keep class org.libwebsockets.Container {
    *;
}
-keep class org.libwebsockets.ErrorCategory {
    *;
}
-keep class org.libwebsockets.Observer {
    *;
}
-keep class org.libwebsockets.Session {
    *;
}
-keep class org.libwebsockets.SocketProxyDescriptor {
    *;
}

# Firebase
-keep class com.google.android.gms.** {
    *;
}
-dontwarn com.google.android.gms.**

-keep class com.google.firebase.** {
    *;
}
-dontwarn com.google.firebase.**

# SQLCipher

-keep class net.sqlcipher.CursorWindow {
    *;
}
-keep class net.sqlcipher.InvalidRowColumnException {
    *;
}
-keep class net.sqlcipher.UnknownTypeException {
    *;
}
-keep class net.sqlcipher.database.** {
    *;
}
  
-keep class org.twinlife.twinlife.crypto.CryptoKey {
    *;
}

-keep class org.twinlife.twinlife.crypto.CryptoBox {
    *;
}
