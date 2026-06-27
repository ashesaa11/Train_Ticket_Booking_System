# AGENTS.md

This file provides guidance to the AI agent when working with code in this repository.

## Build / Test / Lint

- **Build**: `./gradlew assembleDebug` (需要先设置JAVA_HOME)
- **Unit tests**: `./gradlew test`
- **Instrumented tests**: `./gradlew connectedAndroidTest`
- No custom lint or static analysis configured.

## Environment Setup

- **JAVA_HOME**: `D:\Program Files\Android\Android Studio\jbr` (JDK 21)
- **Build command**: `JAVA_HOME="/d/Program Files/Android/Android Studio/jbr" ./gradlew assembleDebug`
- **Git remote**: `git@github.com:ashesaa11/Train_Ticket_Booking_System.git` (SSH over proxy)
- **SSH proxy**: `~/.ssh/config` routes github.com via `connect -H 127.0.0.1:7897`
- **Commit messages**: 中文
- **Commit strategy**: 仅在用户明确要求或确认稳定后提交+推送
- **Gradle offline**: 如无法下载Gradle发行版，手动放zip到 `~/.gradle/wrapper/dists/gradle-9.4.1-bin/arn2x92ynaizyzdaamcbpbhtj/`

## Tech Stack

- Kotlin + Jetpack Compose + Material 3
- Room Database (v2.7.2, KSP processor, destructive migration)
- DataStore Preferences (API config storage)
- Navigation Compose (v2.9.0)
- HttpURLConnection (LLM API calls, zero extra dep)
- JSONObject/JSONArray (org.json, built-in)
- compileSdk 36, minSdk 24, Java 11

## Gotchas

- Theme composable: `Train_Ticket_Booking_SystemTheme` (underscores)
- KSP version: `2.2.10-2.0.2` for Kotlin 2.2.10 (format changed in 2.2.x, NOT 1.0.x)
- `android.disallowKotlinSourceSets=false` required in gradle.properties
- `INTERNET` permission + `usesCleartextTraffic=true` required in AndroidManifest
- Compose `padding()` 不支持负值，用 `offset()` 替代
- 导航用 `navigate(HOME) { popUpTo(LOGIN, inclusive=false); launchSingleTop=true }` 避免重复栈
- 登录状态管理用 `SharedFlow(replay=0)` 而非计数器，避免 Composition 重建时LaunchedEffect重放

## Project Architecture

### Layers
```
UI (Compose Screens) → ViewModel → Repository → Room DAO → SQLite
AI Chat → LLMClient (HttpURLConnection) → OpenAI API
        → FunctionCallHandler → StationRepo / OrderRepo / PassengerRepo
```

### Key Packages
| Package | Content |
|---------|---------|
| `data/entity/` | User, Station, Train, TrainStop, SeatType, Passenger, TrainOrder, OrderItem, ChatHistory (9 entities) |
| `data/dao/` | UserDao, StationDao, TrainDao, SeatTypeDao, PassengerDao, OrderDao, ChatHistoryDao (7 DAOs) |
| `data/repository/` | UserRepository, StationRepository, TrainRepository, PassengerRepository, OrderRepository (5 repos) |
| `data/seed/` | SeedData: 28 stations, 68 trains |
| `data/ApiConfigStore.kt` | DataStore KV for AI API config (url, key, model) |
| `ai/` | LLMClient (HttpURLConnection), FunctionCallHandler (4 tools) |
| `ui/auth/` | LoginScreen + LoginViewModel (phone+pwd, SharedFlow events) |
| `ui/home/` | HomeScreen (hero banner, search card, hot routes) |
| `ui/train/` | TrainList, SeatSelect, PassengerSelect |
| `ui/order/` | OrderConfirm, Payment, OrderList, OrderDetail |
| `ui/profile/` | Profile, PassengerManage, DataManage |
| `ui/ai/` | AIChatScreen + AIChatViewModel |
| `ui/navigation/` | AppNavigation (NavHost + bottom tabs) |
| `util/` | DateTimeUtil, PriceCalculator |

### Navigation
```
LOGIN → HOME (MainScreen: 首页/订单/我的 底部Tab)
HOME → TrainList → SeatSelect → PassengerSelect → OrderConfirm → Payment → OrderDetail
Profile → PassengerManage / DataManage / AIChat
```

### Business Rules
- Refund: >48h free, 24-48h 5%, <24h 10%
- Reschedule: >48h free, 24-48h 5%, <24h 15%, one-time only
- Seat inventory: JSON `dailySold` field, `{"2026-06-27": 5, ...}`
- Payment password: 6-digit, first-login prompt to set, changeable in Profile
- Nickname: default "用户" + phone last 4 digits, editable in Profile

### AI Module (LLM-powered)
- **API**: OpenAI-compatible format (supports DeepSeek, Qwen, etc.)
- **Config**: stored in DataStore, configured via settings icon in chat page
- **4 Tools**: search_trains, book_ticket, refund_ticket, list_passengers
- **Chat history**: persisted in `chat_history` table per user
- **System prompt**: Chinese-only, no emoji, no markdown, use passenger list for booking

## Current State

### Completed
- [x] Phone+pwd registration/login with SharedFlow event pattern
- [x] OTA-style blue theme (#1A73E8)
- [x] Buy flow (search → seat → passenger → confirm → payment)
- [x] Order management (list with tabs, detail, refund)
- [x] Profile (nickname edit, payment pwd change, passenger mgmt)
- [x] AI chatbot (LLM with function calling, history persistence)
- [x] Developer entry (13800000000/123456, quick login)
- [x] 28 stations, 68 trains seed data

### Known Issues / Design Decisions
- Reschedule flow not fully implemented (dialog placeholder only)
- Bottom nav tabs (订单/我的) navigate via outer NavController, not inner NavHost
- Login page not popped from backstack; back from HOME goes to LOGIN
- DataManageScreen: stations use dropdown, train type is dropdown
- API config default points to DeepSeek; user must provide own key
