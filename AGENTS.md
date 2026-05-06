# AGENTS.md

Guidelines for AI agents (Jules, Kiro, Copilot, etc.) working in this repository.

## Project Overview

This is a **Kotlin Multiplatform (KMP)** project that builds a Jules API client app targeting Android, iOS, Desktop (JVM), Web (JS + WasmJS), and Server.

| Module | Purpose |
|--------|---------|
| `shared/` | KMP SDK — models, API client, shared business logic |
| `composeApp/` | Compose Multiplatform UI (Android, iOS, Desktop, Web) |
| `server/` | Ktor server application |
| `iosApp/` | iOS entry point (Swift/Xcode) |

## Jules API

Base URL: `https://jules.googleapis.com/v1alpha`  
Auth header: `x-goog-api-key: <key>`  
Full reference: [`JULES_API.md`](./JULES_API.md)

Core resources: **Sessions**, **Activities**, **Sources**

## Architecture

### SDK (`shared/`)

- All API models live in `shared/src/commonMain/kotlin/.../sdk/models/`
- The API client lives in `shared/src/commonMain/kotlin/.../sdk/JulesApiClient.kt`
- Platform-specific Ktor engines are declared in `shared/build.gradle.kts` source sets — no platform source files needed for the HTTP layer
- Use `kotlinx.serialization` for all JSON; annotate models with `@Serializable`

### UI (`composeApp/`)

- Shared UI in `composeApp/src/commonMain/`
- ViewModels use `androidx.lifecycle.ViewModel` + `viewModelScope`
- State is exposed as `StateFlow`; screens observe via `collectAsState()`
- Navigation is handled with a sealed `Screen` interface in the ViewModel — no navigation library

## Code Conventions

- **Minimal code**: only write what directly solves the requirement; no speculative abstractions
- **Immutable models**: use `data class` with `val` fields and sensible defaults for optional fields
- **Error handling**: use `runCatching` at the ViewModel layer; propagate `error: String?` in UI state
- **Coroutines**: all network calls are `suspend`; launch from `viewModelScope`
- **No hardcoded secrets**: API keys come from config/environment, never committed

## Build Commands

```bash
# Android
./gradlew :composeApp:assembleDebug

# Desktop
./gradlew :composeApp:run

# Web (Wasm)
./gradlew :composeApp:wasmJsBrowserDevelopmentRun

# Web (JS)
./gradlew :composeApp:jsBrowserDevelopmentRun

# Server
./gradlew :server:run

# Shared module (JVM compile check)
./gradlew :shared:compileKotlinJvm
```

## Key Files

| File | Description |
|------|-------------|
| `gradle/libs.versions.toml` | All dependency versions and library aliases |
| `shared/build.gradle.kts` | KMP targets + Ktor engine per platform |
| `build.gradle.kts` | Root — plugin declarations only |
| `JULES_API.md` | Full Jules REST API reference |

## Adding Dependencies

Always add to `gradle/libs.versions.toml` first, then reference via `libs.*` alias in the relevant `build.gradle.kts`. Never use string literals for dependency coordinates in build files.

## What Not to Do

- Do not add navigation libraries (Decompose, Voyager, etc.) without discussion — current nav is intentionally simple
- Do not add DI frameworks without discussion
- Do not commit API keys or secrets
- Do not modify `iosApp/` Xcode project files unless the task explicitly requires it
- Do not use `git push --force` or `git reset --hard` without explicit instruction
