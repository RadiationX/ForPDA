-optimizationpasses 5
-dontskipnonpubliclibraryclassmembers
-allowaccessmodification
-dontpreverify
-repackageclasses ''
-adaptclassstrings


-dontnote **
-dontwarn forpdateam.ru.forpda.**

-keep class ru.forpdateam.forpda.** { *; }
-keep class forpdateam.ru.forpda.** { *; }

-keepclassmembers class ru.forpdateam.forpda.** { *; }
-keepclassmembers class forpdateam.ru.forpda.** { *; }

-keepclassmembers enum forpdateam.ru.forpda.** { *; }
-keepclassmembers enum ru.forpdateam.forpda.** { *; }

-keepattributes SourceFile,LineNumberTable

# okio
-keep class sun.misc.Unsafe { *; }
-dontwarn java.nio.file.*
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement
-dontwarn okio.**
-dontnote okio.**


# OkHttp
-keepattributes Signature
-keepattributes *Annotation*
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-dontwarn okhttp3.**

-keep public class androidx.browser.customtabs.CustomTabsService

# -keep сlass com.lapism.searchview.** { *; }

-keep class io.realm.annotations.RealmModule
-keep @io.realm.annotations.RealmModule class *
-keep class io.realm.internal.Keep
-keep @io.realm.internal.Keep class *
-dontwarn javax.**
-dontwarn io.realm.**

-keep public class * extends io.realm.RealmObject { *; }
-keepnames public class * extends io.realm.RealmObject

-keep class **.R
-keep class **.R$* {
    <fields>;
}

-keep public class com.unnamed.b.atv.model.TreeNode
-keep public class * extends com.unnamed.b.atv.model.TreeNode { *; }
-keep public class com.unnamed.b.atv.model.TreeNode.BaseNodeViewHolder
-keep public class * extends com.unnamed.b.atv.model.TreeNode.BaseNodeViewHolder { *; }

# В search fragment юзается с рефлексией, поэтому нужно исключить
-keep public class androidx.swiperefreshlayout.widget.SwipeRefreshLayout { *; }


