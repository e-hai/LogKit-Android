# AGENTS.md - Developer Guide for AI Agents (Android Repository)

Welcome to **LogKit Android**! This file serves as the definitive reference manual and coding guidelines for AI Agents (such as Antigravity, AGY, Cursor, Copilot, etc.) working on this repository.

> 💡 **Notice**: The iOS Swift codebase is maintained in a dedicated independent repository: [LogKit-iOS](https://github.com/e-hai/LogKit-iOS).

---

## 🎯 Project Mission & Architectural Principles

**LogKit Android** is a pure Kotlin, Jetpack Compose-native logger for Android with an in-app inspection UI console.

### Key Architectural Guidelines
1. **100% Pure Kotlin & Idiomatic**: No legacy Java code, no obsolete helpers, SAM functional interfaces where appropriate.
2. **Minimalist 10-File Architecture**: Highly cohesive components without unnecessary abstraction layers.
3. **Jetpack Compose (Material3) Console**: Dark (`#121212`) & Light (`#F5F5F5`) dual-theme console.
4. **Gradle Version Catalog**: Dependencies managed via `gradle/libs.versions.toml`.

---

## 📐 Component Architecture

```
log/src/main/java/com/kit/log/
├── LogKit.kt                         # Unified Kotlin Singleton Facade & API Entry Point
├── LoggerPrinter.kt                  # Core Formatting & Dispatch Engine (ThreadLocal Tag Isolation)
├── LogAdapter.kt                     # Functional Interface (fun interface) & Factory Constructors
├── Utils.kt                          # StackTrace Offsets & Log Level Converters
├── strategy/
│   ├── FormatStrategy.kt             # Output Formatting Strategy Interface
│   ├── LogStrategy.kt                # Log Output Strategy Interface & Logcat Default
│   ├── PrettyFormatStrategy.kt       # Box Border, Thread & StackTrace Formatter (4KB Chunking)
│   ├── CsvFormatStrategy.kt          # CSV Storage Formatter for Disk Persistence
│   └── DiskLogWriteReadStrategy.kt   # Background HandlerThread Disk Persistence (500KB Split, 3000-line Reverse Read)
└── ui/
    ├── LogActivity.kt                # ComponentActivity Host with Edge-to-Edge Window
    ├── LogScreen.kt                  # Material3 Compose Log Inspection Console (Full Feature Set)
    └── LogViewModel.kt               # ViewModel with Multi-Tag Filter Flow & Level Calculations
```

---

## 💡 UX & UI Standards (Strict Preservation)

When extending or modifying `LogScreen.kt`, AI Agents MUST strictly preserve:
1. **Collapsible Tag Search & Multi-Tag Selection**:
   - Inline collapsible search bar triggered by `🔍`.
   - Tags sorted alphabetically (natural dictionary order, NO dynamic frequency reordering).
   - Matched keyword text highlighted in yellow background (`Color(0xFFFFD54F)`).
   - Multi-tag set filtering with active chips row (`[Tag ✕]`) and clear button.
2. **Level Priority Selection**:
   - Short-press: Multi-select toggle.
   - Long-press: **Solo Mode** (exclusive filter to only view that specific level).
   - Display counts next to levels (`E (3)`, `W (1)`).
3. **Dual Themes (Light & Dark)**:
   - Dynamic `☀️` / `🌙` toggle with `WindowInsetsControllerCompat` status bar icon sync.
4. **Manual Collapse Button**:
   - Header collapsed explicitly via top-right `▲` button (NO scroll auto-collapse).
   - Floating action button on top-right to uncollapse.
5. **Click-to-Copy**:
   - Tap any log card to copy formatted log entry to the system clipboard with toast feedback.

---

## 🛠 Build & Verification Commands

AI Agents MUST run the build verification command after modifying code:

```bash
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
./gradlew assembleDebug
```
