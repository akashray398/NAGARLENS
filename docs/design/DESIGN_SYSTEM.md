# Design System & Field UI Guidelines

> **System:** NAGARLENS Infrastructure Intelligence Network  
> **UI Framework:** Jetpack Compose (Android Material 3)  
> **Environment Context:** Outdoor Field Inspection, Direct Sunlight, High Motion, One-Handed Operation  
> **Document Version:** 1.0.0  
> **Status:** Baseline  

---

## 1. Design Philosophy for Field Infrastructure Tools

Field inspectors operating in urban environments work under harsh real-world conditions:
- Direct, intense sunlight causing screen glare.
- Rainy or wet weather requiring physical ruggedness or glove usage.
- High physical mobility (walking along drain lines, standing near traffic).
- Quick capture requirements requiring minimal taps and clear feedback.

The NAGARLENS Design System synthesizes **Material Design 3** with specialized **Field Ergonomics**.

---

## 2. Color Palette & High Contrast Outdoor Modes

The color system uses high-contrast roles to remain legible under direct sunlight.

### 2.1 Theme Palette Roles

| Token Name | Hex Code | Material 3 Role | Outdoor Field Purpose |
| :--- | :--- | :--- | :--- |
| **`NagarNavy`** | `#0B192C` | `Primary` | Core app bar, primary structural surfaces |
| **`NagarBlue`** | `#1E3E62` | `PrimaryContainer` | Interactive card backgrounds & active filters |
| **`SignalAmber`** | `#FF6500` | `Secondary` / Accent | Defect warning badges, high-severity indicators |
| **`AlertRed`** | `#D9534F` | `Error` | Critical drain blockage alerts, integrity failure |
| **`SafetyGreen`**| `#28A745` | `Tertiary` | Clear flow state, verified asset status |
| **`FieldSurface`**| `#F4F6F8` | `Background` | High contrast light neutral surface |
| **`FieldText`** | `#111827` | `OnBackground` | Deep black high-contrast body text |

### 2.2 Contrast & Glare Rule
All text elements in field capture screens MUST maintain a minimum contrast ratio of **7:1** against their background surface, exceeding standard WCAG AA requirements to combat outdoor screen glare.

---

## 3. Typography & Field Touch Ergonomics

### 3.1 Touch Targets & One-Handed Zones
- **Minimum Touch Target Size:** `56dp x 56dp` for core action buttons (e.g., Camera Shutter, Save Observation, Retry Sync).
- **Primary Action Positioning:** All critical action controls MUST be placed in the lower third of the screen (Thumb Zone) to enable one-handed operation while holding a field tablet or phone.

```text
+-----------------------------------+
|  Secondary Info / Map View        |
|  (Upper Zone - Viewing Only)      |
|                                   |
+-----------------------------------+
|  Asset Details / Observations     |
|  (Middle Zone - Scrollable Info)  |
|                                   |
+-----------------------------------+
|  [ CAPTURE EVIDENCE ]            | <-- Thumb Zone (Lower Third)
|  [ SAVE & QUEUE ]                 |     Min Touch Height: 56dp
+-----------------------------------+
```

---

## 4. Custom Field Components

### 4.1 Asset Health Status Badges
Status indicators must combine clear color, icon, and text to remain unambiguous for colorblind inspectors:

- **CRITICAL_BLOCKAGE:** Red badge + Warning Icon (`ic_warning`) + Text `CRITICAL`
- **PARTIAL_SILTATION:** Amber badge + Info Icon (`ic_info`) + Text `MODERATE`
- **NORMAL_FLOW:** Green badge + Check Icon (`ic_check_circle`) + Text `CLEAR`
- **UNVERIFIED:** Gray badge + Question Icon (`ic_help`) + Text `UNVERIFIED`

### 4.2 Offline Sync Status Banner
A permanent, non-intrusive status bar indicator at the top of the screen displays real-world connectivity and queue status:

- **Online & Synced:** Subtle indicator `All observations uploaded`
- **Offline Mode:** High-visibility banner `Offline — 4 observations queued locally`
- **Syncing in Progress:** Progress bar with active byte stream status `Uploading 2/4 (45%)`

---

## 5. Camera Capture UX Guidance

1. **Overlay Framing Grid:** Display a subtle 3x3 framing grid with a centered target reticle to guide the inspector in centering the drain inlet or manhole structure.
2. **Instant Spatial Feedback:** Display live GNSS lock state (`GPS: Precise ±2.1m`) directly on the camera viewfinder interface before allowing shutter click.
3. **Audio & Haptic Feedback:** Provide a distinct haptic pulse and audible click when an observation is captured and hashed successfully.
