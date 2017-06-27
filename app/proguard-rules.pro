# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Users/elemenzhang/Library/Android/sdk/tools/proguard/proguard-android.txt
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
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
-dontoptimize

-keepattributes *Annotation*
-keepclassmembers class ** {
    @org.greenrobot.eventbus.Subscribe <methods>;
}
-keep enum org.greenrobot.eventbus.ThreadMode { *; }

# Only required if you use AsyncExecutor
-keepclassmembers class * extends org.greenrobot.eventbus.util.ThrowableFailureEvent {
    <init>(Java.lang.Throwable);
}

-keep class de.greenrobot.** { *; }

-dontwarn de.greenrobot.**

-keep class freemarker.** { *; }

-dontwarn freemarker.**

-keep class com.manjay.housebox.greendao.** {*;}

-keepclassmembers class * extends de.greenrobot.dao.AbstractDao {

    public static Java.lang.String TABLENAME;

}

# Joda Time 2.3

-dontwarn org.joda.convert.**
-dontwarn org.joda.time.**
-keep class org.joda.time.** { *; }
-keep interface org.joda.time.** { *; }

# ez-vcard
-dontwarn com.fasterxml.jackson.**		# Jackson JSON Processor (for jCards) not used
-dontwarn freemarker.**				# freemarker templating library (for creating hCards) not used
-dontwarn org.jsoup.**				# jsoup library (for hCard parsing) not used
-dontwarn sun.misc.Perf
-dontwarn ezvcard.io.json.JCardModule
-keep class ezvcard.property.** { *; }		# keep all VCard properties (created at runtime)
-keep class ezvcard.** { *; }

# ButterKnife
-keep class butterknife.** { *; }
-dontwarn butterknife.internal.**
-keep class **$$ViewBinder { *; }
-keepclasseswithmembernames class * {
    @butterknife.* <fields>;
}
-keepclasseswithmembernames class * {
    @butterknife.* <methods>;
}