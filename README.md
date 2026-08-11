# LogKit

**LogKit** is a powerful, Kotlin-first, Jetpack Compose-native, pretty and customizable logger for Android with an integrated mobile log-inspection UI console.

---

## 📐 Architecture Overview

```mermaid
graph TD
    subgraph App ["App Client / Entry Points"]
        A1["LogKit (Unified Kotlin Singleton)"]
    end

    subgraph Core ["Core Dispatcher"]
        B["LoggerPrinter (ThreadLocal Tag & Dispatcher)"]
        B1["JSON / XML Formatter"]
    end

    subgraph Adapters ["Log Adapters"]
        C1["AndroidLogAdapter"]
        C2["DiskLogAdapter"]
    end

    subgraph Strategies ["Strategies Layer"]
        D1["PrettyFormatStrategy\n(Borders, Thread, StackTrace)"]
        D2["CsvFormatStrategy\n(Timestamp, Level, Tag, Msg)"]
        E1["LogcatLogStrategy"]
        E2["DiskLogWriteReadStrategy\n(HandlerThread Async, 500KB Split)"]
    end

    subgraph UI ["Compose In-App Console UI"]
        F1["LogActivity (ComponentActivity)"]
        F2["LogViewModel (StateFlow & Coroutines)"]
        F3["LogScreen (Material3 Compose)"]
        F4["TagSelectionDialog (Tag Search)"]
        F5["Priority Filter (V/D/I/W/E) & Highlight"]
    end

    A1 --> B
    B --> B1
    B --> C1
    B --> C2
    C1 --> D1
    C2 --> D2
    D1 --> E1
    D2 --> E2
    E2 -.->|Asynchronous CSV Persistence| F2
    F1 --> F3
    F2 --> F3
    F3 --> F4
    F3 --> F5
```

---

## ✨ Features

- 🚀 **Kotlin & Jetpack Compose Native**: Pure Kotlin implementation with a dark-themed Material3 Compose log console.
- 🎨 **Pretty Output**: Beautiful logcat formatting with thread info, stack traces, and clean borders.
- 📁 **Async Disk Persistence**: Asynchronous `HandlerThread` file persistence with automatic 500KB file splitting and retrieval of recent 3000 log lines.
- 🎯 **Priority Multi-Select Filter**: Interactive `V`, `D`, `I`, `W`, `E` priority filters with brand color badges.
- 🔍 **Tag Search & Highlighting**: Dedicated grid-based Tag search dialog with real-time keyword background highlighting (`Color(0xFFFFD54F)`).
- 📦 **Gradle Version Catalog**: Built with modern `libs.versions.toml` dependency management.

---

## 🛠 Setup

Initialize LogKit in your Application or Activity:

```kotlin
// Initialize both Android Logcat and Disk Logging with Compose UI support
LogKit.initAllLog(context)

// Or initialize only Android Logcat
LogKit.initOnlyAndroidLog()
```

---

## 💡 Usage

```kotlin
// Basic logging
LogKit.d("Hello LogKit")
LogKit.d("NETWORK", "Fetching user data...")
LogKit.i("TAG", "Info message")
LogKit.w("TAG", "Warning message")
LogKit.e("TAG", "Error message")

// Tag chain support
LogKit.t("CUSTOM_TAG").d("Chain tag message")

// Format JSON / XML
LogKit.json("{\"user\": \"LogKit\", \"status\": \"active\"}")
LogKit.xml("<config><item name=\"debug\">true</item></config>")
```

### Open In-App Log Inspection Console UI

```kotlin
LogKit.showLogUi(activity)
```

---

## 📄 License
Apache License 2.0
