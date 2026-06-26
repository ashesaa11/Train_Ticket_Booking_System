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
- KSP version for Kotlin 2.2.10 is `2.2.10-2.0.2` (not 1.0.x format).
- `android.disallowKotlinSourceSets=false` required in gradle.properties for Room + KSP with AGP 9.x.
- JAVA_HOME must be set before `./gradlew` (Android Studio bundled JDK at `D:\Program Files\Android\Android Studio\jbr`).
- Gradle wrapper validates against `services.gradle.org`; if offline, set `validateDistributionUrl=false`.

## Project Architecture

### Layers
```
UI (Compose Screens) → ViewModel → Repository → Room DAO → SQLite
```

### Key Packages
- `data/entity/` - 8 Room entities (User, Station, Train, TrainStop, SeatType, Passenger, TrainOrder, OrderItem)
- `data/dao/` - 6 DAO interfaces
- `data/repository/` - 5 Repositories (User, Station, Train, Passenger, Order)
- `data/seed/` - SeedData: 20 stations, 15 trains with stops and seat types
- `ai/` - IntentParser (regex rules), ServiceRouter, IntentType enum
- `ui/navigation/` - AppNavigation: NavHost with bottom tabs + buy flow
- `ui/auth/`, `ui/home/`, `ui/train/`, `ui/order/`, `ui/profile/`, `ui/ai/`
- `util/` - DateTimeUtil, PriceCalculator

### Navigation Routes
Login → Home(首页/订单/我的) → Buy flow (TrainList → SeatSelect → PassengerSelect → OrderConfirm → Payment → OrderDetail)
Profile → PassengerManage / DataManage / AIChat

### Business Rules
- Refund: >48h free, 24-48h 5%, <24h 10%
- Reschedule: >48h free, 24-48h 5%, <24h 15%, one-time only
- Seat inventory tracked via JSON dailySold field

## Development Progress
- [x] Phase 1: Room database + seed data
- [x] Phase 2: Repository layer + utilities
- [x] Phase 3: Login with SMS simulation
- [x] Phase 4: Navigation + HomeScreen
- [x] Phase 5: Buy flow (search, seat, passenger, confirm, payment)
- [x] Phase 6: Order management (list, detail, refund, reschedule)
- [x] Phase 7: Profile + Passenger management + Data management
- [x] Phase 8: AI assistant (local rule engine)
- [x] Phase 9: Final build verification
