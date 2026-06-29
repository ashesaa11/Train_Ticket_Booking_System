# 畅通无阻 — 火车票预订系统

一款基于 Android 原生 Kotlin + Jetpack Compose 的火车票预订应用，支持车次查询、在线购票、订单管理、AI 智能助手等功能。

---

## 功能概览

- **车次查询**：按出发站/到达站/日期搜索，15天内车次
- **在线购票**：选座 → 选乘客 → 确认 → 支付密码验证，完整购票流程
- **订单管理**：订单列表（全部/未出行/已出行/已退票）、订单详情、退票（按距出发时间阶梯费率）
- **AI 智能助手**：基于 LLM function calling，支持自然语言查车次、购票、退票
- **常用乘客**：管理乘车人信息
- **个人信息**：昵称编辑、支付密码修改

---

## 技术栈

| 类别 | 技术 |
|------|------|
| 语言 | Kotlin |
| UI | Jetpack Compose + Material 3 |
| 导航 | Navigation Compose (v2.9.0) |
| 数据库 | Room (v2.7.2) + KSP |
| 本地存储 | DataStore Preferences |
| AI | HttpURLConnection → OpenAI 兼容 API (function calling) |
| JSON | org.json (内置) |
| 构建 | Gradle 9.4.1, AGP |

## 环境配置

| 项目 | 值 |
|------|-----|
| compileSdk | 36 |
| minSdk | 24 |
| targetSdk | 36 |
| Java | 11 (target), JDK 21 (build) |
| Kotlin | 2.2.10 |
| KSP | 2.2.10-2.0.2 |
| Gradle | 9.4.1 |

## 构建与运行

```bash
# 设置 JDK 21
export JAVA_HOME="/path/to/Android Studio/jbr"

# 编译 Debug APK
./gradlew assembleDebug

# 安装到设备
adb install -r app/build/outputs/apk/debug/app-debug.apk

# 运行单元测试
./gradlew test
```

---

## 项目架构

```
UI (Compose Screens)
  ↓
ViewModel (StateFlow)
  ↓
Repository
  ↓
Room DAO → SQLite
```

```
AI Chat → LLMClient (HttpURLConnection) → OpenAI-compatible API
        → FunctionCallHandler → StationRepo / OrderRepo / PassengerRepo
```

### 包结构

| 包 | 说明 |
|----|------|
| `data/entity/` | 9 个实体：User, Station, Train, TrainStop, SeatType, Passenger, TrainOrder, OrderItem, ChatHistory |
| `data/dao/` | 7 个 DAO：UserDao, StationDao, TrainDao, SeatTypeDao, PassengerDao, OrderDao, ChatHistoryDao |
| `data/repository/` | 5 个 Repository：User, Station, Train, Passenger, Order |
| `data/seed/` | SeedData：程序化生成 541 趟车次，28 个站点 |
| `data/ApiConfigStore.kt` | DataStore 存储 AI API 配置（url, key, model） |
| `ai/` | LLMClient (HttpURLConnection), FunctionCallHandler (5 tools) |
| `ui/auth/` | LoginScreen + LoginViewModel (手机号+密码，SharedFlow) |
| `ui/home/` | HomeScreen (搜索卡片、热门路线、DatePicker) |
| `ui/train/` | TrainList, SeatSelect, PassengerSelect |
| `ui/order/` | OrderConfirm, Payment, OrderList, OrderDetail |
| `ui/profile/` | Profile, PassengerManage, DataManage |
| `ui/ai/` | AIChatScreen + AIChatViewModel |
| `ui/navigation/` | AppNavigation (NavHost + bottom tabs + 悬浮AI按钮) |
| `ui/theme/` | Color.kt, Theme.kt, Type.kt |
| `util/` | DateTimeUtil, PriceCalculator |

### 导航路由

```
LOGIN → HOME (MainScreen: 首页/订单/我的)
HOME → TrainList → SeatSelect → PassengerSelect → OrderConfirm → Payment → OrderDetail
Profile → PassengerManage / DataManage / AIChat
```

## 数据模型

### 实体关系

- **User**：手机号（主键）、密码哈希、昵称、支付密码
- **Station**：站名、城市、编码（28 个站）
- **Train**：车次号（如 G123）、类型（G/D/K）、出发/到达站 ID、历时
- **TrainStop**：车次停靠站，含到站/发车时间、站序
- **SeatType**：座位类型、价格、总票数、每日已售 JSON
- **Passenger**：姓名、身份证号、乘客类型（成人/儿童/学生）
- **TrainOrder**：订单状态、总价、`originalOrderId`（改签链路，功能已移除保留字段）
- **OrderItem**：订单中的每张票（乘客、座位类型、价格）
- **ChatHistory**：AI 对话记录

### 座位库存

`SeatType.dailySold` 为 JSON 字段，格式：`{"2026-06-29": 5, "2026-06-30": 3}`。`getAvailableSeats()` = totalCount - soldCount。

---

## AI 模块详解

### API 调用

`LLMClient` 使用 `HttpURLConnection` 调用 OpenAI 兼容 API，支持 function calling：

```kotlin
// 请求体包含 tools 和 tool_choice: "auto"
val body = JSONObject().apply {
    put("model", model)
    put("messages", [...])
    put("tools", [...])
    put("tool_choice", "auto")
}
```

### 工具列表 (5 tools)

| 工具名 | 功能 | 参数 |
|--------|------|------|
| `search_trains` | 查询车次 | from, to, date |
| `book_ticket` | 预订车票 | train_id, seat_type, date, passenger_names |
| `refund_ticket` | 退订车票 | order_id |
| `list_passengers` | 列出常用乘客 | 无 |
| `list_orders` | 列出用户订单 | 无 |

### 系统提示词

系统提示词注入当前日期，强调：
- 必须使用 function_call 而非文本伪造
- 购票前先调 list_passengers
- 退票前先调 list_orders
- 纯文本输出，禁止 Markdown/emoji

### 对话上下文管理

- 每次进入 AI 页面清空历史
- 不保存工具执行状态消息到历史
- 限制上下文为 system + 最近 20 条消息

### 工具状态映射

UI 端将工具名映射为用户友好标签：

| 工具名 | 显示 |
|--------|------|
| search_trains | 查询车次中 |
| book_ticket | 购票中 |
| refund_ticket | 退票中 |
| list_passengers | 查询乘客中 |
| list_orders | 查询订单中 |

---

## 业务规则

### 退票费率

| 距出发时间 | 手续费 |
|-----------|--------|
| > 48 小时 | 免费 |
| 24 ~ 48 小时 | 5% |
| < 24 小时 | 10% |

### 支付密码

- 6 位数字
- 首次登录提示设置
- 购票时验证
- 可在「我的」中修改

### 车次分布

`TrainRepository.search()` 使用确定性哈希 `hash(trainId + date) % 100 < 70` 决定车次在特定日期的可用性，同一路线不同日期看到不同车次。每个站点对至少保证 3 趟车。

### 数据管理

仅开发者账号（13800000000）可见数据管理入口，可添加站点和车次。

### 日期窗口

日期选择器限制今天起 15 天内，使用 Material 3 的 `SelectableDates`。

---

## 开发者入口

- 手机号：`13800000000`
- 密码：`123456`

---

## 注意事项

- Theme 名称：`Train_Ticket_Booking_SystemTheme`（含下划线）
- `padding()` 不支持负值，用 `offset()` 替代
- 导航使用 `popUpTo + launchSingleTop` 避免重复栈
- 登录状态管理用 `SharedFlow(replay=0)` 而非计数器
- `INTERNET` 权限 + `usesCleartextTraffic=true` 已在 AndroidManifest 配置
- KSP 版本格式：Kotlin 2.2.x 使用 `2.2.10-2.0.2`
- `android.disallowKotlinSourceSets=false` 需在 gradle.properties 设置

---

## Git 远程

```
git@github.com:ashesaa11/Train_Ticket_Booking_System.git
```

## 许可证

MIT
