# NAGARLENS Production Release ProGuard / R8 Rules

# Keep Hilt generated classes
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager
-keepclassmembers class * {
    @javax.inject.Inject <init>(...);
}

# Keep Room Database Entities and DAOs
-keep class androidx.room.RoomDatabase
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Dao interface *
-keep class com.nagarlens.app.core.database.** { *; }

# Keep Domain Data Models and Serialization Enums
-keep class com.nagarlens.app.core.model.** { *; }

# Keep CameraX and Location Services
-keep class androidx.camera.** { *; }
-keep class com.google.android.gms.location.** { *; }
