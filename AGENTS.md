# AGENTS.md

This file provides guidance to the AI agent when working with code in this repository.

## Build / Test / Lint

- **Build**: `./gradlew assembleDebug`
- **Unit tests**: `./gradlew test`
- **Instrumented tests**: `./gradlew connectedAndroidTest`
- No custom lint or static analysis configured.

## Tech Stack

- Kotlin + Jetpack Compose + Material 3
- Dependencies managed via Gradle version catalog at `gradle/libs.versions.toml`
- compileSdk uses `release(36)` syntax (AGP 9.x), not a plain integer
- Java 11 source/target compatibility

## Gotchas

- Theme composable is named `Train_Ticket_Booking_SystemTheme` (underscores), not PascalCase.
- Gradle wrapper and wrapper batch script are checked in; never commit `local.properties`.
