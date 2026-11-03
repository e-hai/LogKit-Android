# AGENTS.md - Developer Guide for AI Agents (Android Repository)

Welcome to **LogKit Android**! This file serves as the definitive reference manual and coding guidelines for AI Agents (such as Antigravity, AGY, Cursor, Copilot, etc.) working on this Android repository.

> 💡 **Notice**: The iOS Swift codebase is maintained in a dedicated independent repository: [LogKit-iOS](https://github.com/e-hai/LogKit-iOS).

---

## 🎯 Project Mission & Vision

**LogKit** is a powerful, Kotlin-first, Jetpack Compose-native logger for Android with an in-app inspection UI console.

### Key Architectural Principles
1. **100% Pure Kotlin & Idiomatic**: No legacy Java code, no obsolete helpers, no redundant interface wrappers.
2. **Minimalist 10-File Architecture**: Compact, highly maintainable codebase without unnecessary abstraction layers.
3. **Jetpack Compose Native UI**: Dark & Light dual-theme Material3 Compose console for mobile debugging.
4. **Gradle Version Catalog**: All dependencies and AGP/Kotlin plugins are managed via `gradle/libs.versions.toml`.

---

## 📐 Architecture & Core Components

```
log/src/main/java/com/kit/log/
├── LogKit.kt                         # Unified Kotlin Singleton Facade & API Entry Point
├── LoggerPrinter.kt                  # Core Formatting & Dispatch Engine
├── LogAdapter.kt                     # Functional Interface (fun interface) & Factory Constructors
├── Utils.kt                          # StackTrace Offsets & Log Level Converters
├── strategy/
│   ├── FormatStrategy.kt             # Output Formatting Strategy Interface
│   ├── LogStrategy.kt                # Log Output Strategy Interface & Logcat Default
│   ├── PrettyFormatStrategy.kt       # Pretty Logcat Box Border & Thread/StackTrace Formatter
│   ├── CsvFormatStrategy.kt          # CSV Storage Formatter for Disk Persistence
│   └── DiskLogWriteReadStrategy.kt   # Async Background HandlerThread Disk Persistence (500KB Split)
└── ui/
    ├── LogActivity.kt                # ComponentActivity Host with Edge-to-Edge Window
    ├── LogScreen.kt                  # Material3 Compose Log Inspection Console
    └── LogViewModel.kt               # ViewModel & LogInfo Data Structure
```

---

## 💡 Key UI & Interactive Features

When extending or modifying `LogScreen.kt`, strictly preserve the following user-centric UX designs:

1. **Priority Level Multi-Select Filter**:
   - Interactive `ALL`, `V`, `D`, `I`, `W`, `E` chips with explicit count badge `(X/5)`.
   - Distinct brand colors for each priority (`VERBOSE` gray, `DEBUG` blue, `INFO` green, `WARN` orange, `ERROR` red).
2. **Tag Search & Highlight**:
   - Grid-based `TagSelectionDialog` with real-time text matching and yellow background highlight (`Color(0xFFFFD54F)`).
3. **Dual Themes (Light & Dark)**:
   - Dynamic `☀️` / `🌙` theme toggle supporting high-contrast Dark (`#121212`) and Light (`#F5F5F5`) palettes.
4. **Collapsible Header on Scroll**:
   - Auto-collapsing TopAppBar on list scroll to unleash **100% full-screen Log viewing area**.
5. **Immersive Edge-to-Edge Status Bar**:
   - WindowInsets-aware `statusBarsPadding()` and `WindowInsetsControllerCompat` status bar icon color sync.

---

## 🛠 Build & Verification Commands

AI Agents MUST run the build verification command after modifying code:

```bash
# Set JAVA_HOME and compile the debug APK
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
./gradlew assembleDebug
```

---

## 🚨 Guidelines for AI Agents

- **No Over-Engineering**: Do NOT introduce extra single-implementation interfaces (like the old `Printer.kt`).
- **Use Version Catalog**: Add dependencies ONLY to `gradle/libs.versions.toml`. Do NOT hardcode library versions in `.gradle` files.
- **Maintain Touch Target Ergonomics**: Ensure button touch targets remain comfortable (minimum 36-48dp) and do not compromise touch UX when adjusting density.
