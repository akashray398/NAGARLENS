# NAGARLENS — Android Build & Emulator Verification Guide

> **Target Module**: `:app` (`android/app`)  
> **Application ID**: `com.nagarlens.app`  
> **Launcher Activity**: `com.nagarlens.app.MainActivity`  
> **Min SDK**: 26 | **Compile SDK**: 35  

---

## 1. Environment & Build Requirements

- **JDK**: Java 17 (or Java 21)
- **Android SDK**: Android 15 (API Level 35) / Android 14 (API Level 34)
- **Build Tool**: Gradle Wrapper (`gradlew.bat` / `./gradlew`)

---

## 2. Mandatory Local Build Commands

### Step A — Clean & Compile Debug Variant
In Windows `cmd` shell (environment variable `ANDROID_PREFS_ROOT` must be cleared if present):

```cmd
cd D:\NAGARLENS\$HOME\Projects\NAGARLENS\android
set ANDROID_PREFS_ROOT=
gradlew.bat assembleDebug
```

### Step B — Execute Complete Unit Test Suite
Run all 51+ unit and migration tests:

```cmd
gradlew.bat testDebugUnitTest
```

### Step C — Install APK on Connected Device / Emulator

```cmd
gradlew.bat installDebug
```

Or via direct ADB:

```cmd
adb devices
adb install -r app\build\outputs\apk\debug\app-debug.apk
```

### Step D — Launch NAGARLENS Main Activity

```cmd
adb shell am start -n com.nagarlens.app/.MainActivity
```

---

## 3. Generated Build Artifact Location

- **APK File**: `app-debug.apk`
- **Output Path**: `D:\NAGARLENS\$HOME\Projects\NAGARLENS\android\app\build\outputs\apk\debug\app-debug.apk`
- **Application ID**: `com.nagarlens.app`

---

## 4. Android Studio & Emulator Launch Steps

1. Open **Android Studio**.
2. Select **Open** and choose directory `D:\NAGARLENS\$HOME\Projects\NAGARLENS\android`.
3. Open **Device Manager** (`Tools -> Device Manager`).
4. Select or create an Android Virtual Device (AVD) running **Android 14 (API 34)** or **Android 15 (API 35)**.
5. Click **Play** to start the emulator.
6. Run `gradlew.bat installDebug` or press **Run 'app'** (`Shift + F10`) inside Android Studio.
