# NAGARLENS — AI Engine Runtime & Execution Guide

> **Target Components:** Backend Vision Engine (`backend/app/ml`) & Android AI Client (`android/app/src/main/java/com/nagarlens/app/core/ml`)  

---

## 1. Backend AI Execution Commands

To execute backend unit tests for image preprocessing, taxonomy, NMS postprocessing, severity assessment, and API routes:

```cmd
cd D:\NAGARLENS\$HOME\Projects\NAGARLENS\backend
set PYTHONPATH=.
pytest tests/unit
```

---

## 2. Android AI Execution Commands

To execute Android unit tests including AI assessment contract tests, model fallback tests, and hasher integrity verification:

```cmd
cd D:\NAGARLENS\$HOME\Projects\NAGARLENS\android
set ANDROID_PREFS_ROOT=
gradlew.bat testDebugUnitTest
```

To install and launch on connected device:

```cmd
gradlew.bat installDebug
adb shell am start -n com.nagarlens.app/.MainActivity
```
