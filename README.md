# Jules

A [Kotlin Multiplatform](https://kotlinlang.org/docs/multiplatform.html) client app for the [Jules API](https://jules.google/docs/api/reference/overview/), targeting Android, iOS, Desktop (JVM), and Web (JS + WasmJS).

## Platforms

| Platform | Target | Artifact |
|----------|--------|----------|
| Android | `androidTarget` | `.apk` |
| iOS | `iosArm64`, `iosSimulatorArm64` | XCFramework |
| Desktop (Linux) | `jvm` | `.deb` |
| Desktop (Windows) | `jvm` | `.msi` |
| Desktop (macOS) | `jvm` | `.dmg` |
| Web (Wasm) | `wasmJs` | Wasm bundle |
| Web (JS) | `js` | JS bundle |

## Modules

| Module | Description |
|--------|-------------|
| `shared/` | KMP SDK — Jules API models and HTTP client |
| `composeApp/` | Compose Multiplatform UI |
| `server/` | Ktor server |
| `iosApp/` | iOS entry point (Swift) |

## Getting Started

### Prerequisites

- JDK 17+
- Android SDK (for Android builds)
- Xcode (for iOS builds, macOS only)

### API Key

Get your Jules API key from [jules.google.com/settings](https://jules.google.com/settings).

Set it as an environment variable — **never commit it**:

```bash
export JULES_API_KEY="your-api-key-here"
```

### Build & Run

```bash
# Android
./gradlew :composeApp:assembleDebug

# Desktop (run locally)
./gradlew :composeApp:run

# Web (Wasm, dev server)
./gradlew :composeApp:wasmJsBrowserDevelopmentRun

# Web (JS, dev server)
./gradlew :composeApp:jsBrowserDevelopmentRun

# Server
./gradlew :server:run

# Shared module compile check
./gradlew :shared:compileKotlinJvm
```

## Tech Stack

- **Kotlin** `2.3.20`
- **Compose Multiplatform** `1.10.3`
- **Ktor** `3.4.1` — HTTP client (shared SDK) and server
- **kotlinx.coroutines** `1.10.2`
- **kotlinx.serialization** — JSON models

## CI

GitHub Actions builds all platform artifacts on every push/PR to `main`. See [`.github/workflows/build.yml`](.github/workflows/build.yml).

## Docs

- [`JULES_API.md`](JULES_API.md) — Full Jules REST API reference
- [`AGENTS.md`](AGENTS.md) — Guidelines for AI agents working in this repo
