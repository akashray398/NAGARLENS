# NAGARLENS Release ProGuard Rules
# Keep Hilt generated classes
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager
-keepclassmembers class * {
    @javax.inject.Inject <init>(...);
}
