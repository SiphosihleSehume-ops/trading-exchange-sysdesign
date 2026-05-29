# ⚡ National Grid Trading Exchange — Java OOP Elite Assessment

## Overview

In this assessment you will design and implement a **National Grid Trading Exchange System** in Java.

The system simulates an energy commodities trading platform where:

- **Traders** place buy and sell **Orders** on **Markets**
- **Orders** are matched by a **MatchingEngine** using price-time priority
- **Portfolios** track asset holdings and realised profit/loss
- **RiskEngine** enforces position limits and margin requirements before orders are accepted
- **AuditLog** records every state-changing event immutably
- **Settlement** processes matched trades and transfers funds/assets between portfolios
- **MarketDataFeed** publishes price updates to subscribed **MarketObserver** listeners
- A **CircuitBreaker** halts trading on a market when price movement exceeds a configurable threshold

The **test files are provided**. Your task is to implement every class, interface, abstract class, and enum from scratch. **No skeleton is provided.**

---

## Principles You Must Apply

| Principle | Where |
|---|---|
| Encapsulation | All model classes |
| Inheritance & Polymorphism | `Order` hierarchy, `BankSystem`-style service hierarchy |
| Abstraction (abstract classes + interfaces) | `Order`, `MatchingStrategy`, `MarketObserver`, `RiskRule` |
| Defensive Programming & Defensive Copying | Every collection getter |
| Observer Pattern | `MarketDataFeed` → `MarketObserver` |
| Strategy Pattern | `MatchingStrategy` (price-time vs pro-rata) |
| Chain of Responsibility | `RiskEngine` processes a chain of `RiskRule` objects |
| Immutability | `TradeRecord`, `AuditEntry` must be fully immutable |
| Comparable / Comparator | `Order` priority in `PriorityQueue` |
| Enum | `OrderSide`, `OrderStatus`, `AssetType`, `CircuitBreakerState` |

---

## Learning Outcomes

- Expert-level OOP in Java
- Design patterns (Observer, Strategy, Chain of Responsibility)
- Immutable value objects
- `PriorityQueue` with custom `Comparator`
- Generic types (basic usage)
- Enums with fields and methods
- Bidirectional collection mappings
- Defensive copying at every layer
- Full JUnit 5 test coverage driven by provided tests

---

## Time Limit

**5 hours**

---

## Assessment Structure

| Component      | Weight | Recommended Time |
|----------------|--------|------------------|
| Implementation | 100%   | 5 hours          |

---

## Scoring

```
Coding Score = (tests passed / total tests) × 100%
Final Score  = Coding Score
```

**Pass mark: 70% or higher**

---

## Project Structure

```
national-grid-exchange/
├── pom.xml
│
└── src/
    ├── main/
    │   └── java/
    │       └── za/
    │           └── co/
    │               └── eliteproject/
    │                   ├── Main.java
    │                   │
    │                   ├── enums/
    │                   │   ├── OrderSide.java
    │                   │   ├── OrderStatus.java
    │                   │   ├── AssetType.java
    │                   │   └── CircuitBreakerState.java
    │                   │
    │                   ├── model/
    │                   │   ├── Trader.java
    │                   │   ├── Order.java
    │                   │   ├── LimitOrder.java
    │                   │   ├── MarketOrder.java
    │                   │   ├── Portfolio.java
    │                   │   ├── Asset.java
    │                   │   ├── TradeRecord.java
    │                   │   ├── AuditEntry.java
    │                   │   └── Market.java
    │                   │
    │                   ├── service/
    │                   │   ├── MatchingEngine.java
    │                   │   ├── RiskEngine.java
    │                   │   ├── Settlement.java
    │                   │   ├── AuditLog.java
    │                   │   ├── MarketDataFeed.java
    │                   │   └── CircuitBreaker.java
    │                   │
    │                   ├── strategy/
    │                   │   ├── MatchingStrategy.java
    │                   │   ├── PriceTimeMatchingStrategy.java
    │                   │   └── ProRataMatchingStrategy.java
    │                   │
    │                   ├── risk/
    │                   │   ├── RiskRule.java
    │                   │   ├── PositionLimitRule.java
    │                   │   ├── MarginRequirementRule.java
    │                   │   └── BlacklistRule.java
    │                   │
    │                   └── observer/
    │                       └── MarketObserver.java
    │
    └── test/
        └── java/
            └── za/
                └── co/
                    └── eliteproject/
                        ├── TraderTest.java
                        ├── OrderTest.java
                        ├── PortfolioTest.java
                        ├── MarketTest.java
                        ├── MatchingEngineTest.java
                        ├── RiskEngineTest.java
                        ├── SettlementTest.java
                        ├── AuditLogTest.java
                        ├── MarketDataFeedTest.java
                        └── CircuitBreakerTest.java
```

---

## Running Tests

```bash
mvn clean compile test
```

---

## Implementation Steps

> Work through steps in order. Dependencies run deep — skipping ahead will cause compile failures.

---

### Step 1 — Implement Enums

**Package:** `za.co.wethinkcode.enums`

These four enums are used system-wide. Each must have fields and at least one method where noted.

---

#### `OrderSide`

Represents which side of the market an order is on.

| Constant | Description        |
|----------|--------------------|
| `BUY`    | A buy order        |
| `SELL`   | A sell order       |

| Method       | Return Type | Description                              |
|--------------|-------------|------------------------------------------|
| `opposite()` | `OrderSide` | Returns `BUY` if called on `SELL`, and vice versa |

---

#### `OrderStatus`

| Constant      | Description                            |
|---------------|----------------------------------------|
| `PENDING`     | Submitted, not yet processed           |
| `PARTIAL`     | Partially filled                       |
| `FILLED`      | Fully filled                           |
| `CANCELLED`   | Cancelled before fill                  |
| `REJECTED`    | Rejected by the risk engine            |

| Method        | Return Type | Description                                         |
|---------------|-------------|-----------------------------------------------------|
| `isTerminal()`| `boolean`   | Returns `true` if status is `FILLED`, `CANCELLED`, or `REJECTED` |

---

#### `AssetType`

| Constant     | Field: `String label`   |
|--------------|-------------------------|
| `ELECTRICITY`| `"Electricity (MWh)"`   |
| `GAS`        | `"Natural Gas (MMBtu)"` |
| `CARBON`     | `"Carbon Credits (t)"`  |
| `COAL`       | `"Coal (tonne)"`        |

Constructor must accept and store `label`. Provide `getLabel()`.

---

#### `CircuitBreakerState`

| Constant  | Description                        |
|-----------|------------------------------------|
| `CLOSED`  | Normal — trading allowed           |
| `OPEN`    | Halted — no new orders accepted    |
| `TESTING` | One test order allowed through     |

| Method       | Return Type | Description                                           |
|--------------|-------------|-------------------------------------------------------|
| `allowsTrading()` | `boolean` | `true` for `CLOSED` and `TESTING`, `false` for `OPEN` |

---

### Step 2 — Implement `Asset`

**File:** `model/Asset.java`

Represents a tradeable commodity holding.

#### Fields

| Field       | Type        | Access  | Description                  |
|-------------|-------------|---------|------------------------------|
| `assetType` | `AssetType` | private | The type of commodity        |
| `quantity`  | `double`    | private | Units held (can be fractional)|
| `avgCost`   | `double`    | private | Volume-weighted average cost |

#### Constructor

| Signature                                          | Access |
|----------------------------------------------------|--------|
| `Asset(AssetType assetType, double quantity, double avgCost)` | public |

#### Methods

| Method              | Return Type | Description                                                             |
|---------------------|-------------|-------------------------------------------------------------------------|
| `getAssetType()`    | `AssetType` | Returns asset type                                                      |
| `getQuantity()`     | `double`    | Returns quantity                                                        |
| `getAvgCost()`      | `double`    | Returns average cost                                                    |
| `add(double quantity, double price)` | `void` | Increases holding; recalculates `avgCost` using VWAP formula |
| `reduce(double quantity)` | `boolean` | Reduces holding; returns `false` if quantity would go below zero |
| `getMarketValue(double currentPrice)` | `double` | Returns `quantity * currentPrice`                         |
| `getUnrealisedPnL(double currentPrice)` | `double` | Returns `(currentPrice - avgCost) * quantity`            |
| `toString()`        | `String`    | Asset summary                                                           |

#### VWAP Formula

```
newAvgCost = ((oldQuantity * oldAvgCost) + (addedQuantity * addedPrice))
             / (oldQuantity + addedQuantity)
```

#### Validation Rules

| Rule                                  |
|---------------------------------------|
| Initial quantity cannot be negative   |
| Initial avgCost cannot be negative    |
| `add()` quantity and price must be positive |
| `reduce()` quantity must be positive  |

---

### Step 3 — Implement `Portfolio`

**File:** `model/Portfolio.java`

A `Portfolio` belongs to a `Trader` and holds cash and asset positions.

#### Fields

| Field             | Type                           | Access  | Description                          |
|-------------------|--------------------------------|---------|--------------------------------------|
| `portfolioId`     | `int`                          | private | Unique ID                            |
| `cashBalance`     | `double`                       | private | Available cash                       |
| `reservedCash`    | `double`                       | private | Cash locked against open buy orders  |
| `assets`          | `HashMap<AssetType, Asset>`    | private | Current holdings by asset type       |
| `realisedPnL`     | `double`                       | private | Total realised profit/loss           |

#### Constructor

| Signature                                          | Access |
|----------------------------------------------------|--------|
| `Portfolio(int portfolioId, double initialCash)`   | public |

#### Methods

| Method                                        | Return Type             | Access | Description                                                                 |
|-----------------------------------------------|-------------------------|--------|-----------------------------------------------------------------------------|
| `getPortfolioId()`                            | `int`                   | public | Returns ID                                                                  |
| `getCashBalance()`                            | `double`                | public | Returns available cash (excludes reserved)                                  |
| `getReservedCash()`                           | `double`                | public | Returns reserved cash                                                       |
| `getTotalCash()`                              | `double`                | public | Returns `cashBalance + reservedCash`                                        |
| `reserveCash(double amount)`                  | `boolean`               | public | Moves cash from available to reserved; returns `false` if insufficient      |
| `releaseCash(double amount)`                  | `void`                  | public | Moves cash from reserved back to available                                  |
| `deductReservedCash(double amount)`           | `boolean`               | public | Permanently removes from reserved (used at settlement); returns `false` if insufficient |
| `creditCash(double amount)`                   | `void`                  | public | Adds to available cash balance                                              |
| `addAsset(AssetType, double qty, double price)`| `void`                 | public | Adds or updates asset holding using VWAP                                    |
| `reduceAsset(AssetType, double qty)`          | `boolean`               | public | Reduces asset holding; returns `false` if insufficient                      |
| `getAsset(AssetType)`                         | `Asset`                 | public | Returns `Asset` or `null` if not held                                       |
| `getAllAssets()`                              | `Map<AssetType, Asset>` | public | Defensive copy                                                              |
| `getRealisedPnL()`                            | `double`                | public | Returns total realised PnL                                                  |
| `recordRealisedPnL(double amount)`            | `void`                  | public | Adds amount (positive or negative) to `realisedPnL`                        |
| `getTotalValue(Map<AssetType, Double> prices)`| `double`                | public | Cash + market value of all assets at given prices                           |
| `toString()`                                  | `String`                | public | Portfolio summary                                                           |

#### Validation Rules

| Rule                                      |
|-------------------------------------------|
| Initial cash cannot be negative           |
| All cash amounts in methods must be positive |
| Cannot reduce an asset not held           |

---

### Step 4 — Implement `Trader`

**File:** `model/Trader.java`

#### Fields

| Field          | Type        | Access  | Description                      |
|----------------|-------------|---------|----------------------------------|
| `traderId`     | `int`       | private | Unique ID                        |
| `username`     | `String`    | private | Trading username                 |
| `portfolio`    | `Portfolio` | private | Trader's portfolio               |
| `isBlacklisted`| `boolean`   | private | Blacklist flag, default `false`  |
| `openOrders`   | `List<Order>` | private | Orders not yet in terminal status |

#### Constructor

| Signature                                         | Access |
|---------------------------------------------------|--------|
| `Trader(int traderId, String username, Portfolio portfolio)` | public |

#### Methods

| Method                    | Return Type   | Access | Description                                           |
|---------------------------|---------------|--------|-------------------------------------------------------|
| `getTraderId()`           | `int`         | public | Returns trader ID                                     |
| `getUsername()`           | `String`      | public | Returns username                                      |
| `getPortfolio()`          | `Portfolio`   | public | Returns portfolio                                     |
| `addOpenOrder(Order)`     | `void`        | public | Adds order to open orders list                        |
| `removeOpenOrder(int orderId)` | `boolean` | public | Removes order by ID; returns `true` if found         |
| `getOpenOrders()`         | `List<Order>` | public | Defensive copy                                        |
| `blacklist()`             | `void`        | public | Blacklists trader                                     |
| `isBlacklisted()`         | `boolean`     | public | Returns blacklist status                              |
| `getTotalExposure()`      | `double`      | public | Sum of `quantity * limitPrice` across all open BUY `LimitOrder`s |
| `toString()`              | `String`      | public | Trader summary                                        |

#### Validation Rules

| Rule                              |
|-----------------------------------|
| Username cannot be blank          |
| Portfolio cannot be `null`        |

---

### Step 5 — Implement `Order` (Abstract) and Subclasses

**Files:** `model/Order.java`, `model/LimitOrder.java`, `model/MarketOrder.java`

---

#### `Order` (Abstract)

| Field        | Type          | Access    | Description                                |
|--------------|---------------|-----------|--------------------------------------------|
| `orderId`    | `int`         | protected | Unique order ID                            |
| `trader`     | `Trader`      | protected | Submitting trader                          |
| `assetType`  | `AssetType`   | protected | Asset being traded                         |
| `side`       | `OrderSide`   | protected | BUY or SELL                                |
| `quantity`   | `double`      | protected | Total quantity requested                   |
| `filledQty`  | `double`      | protected | Quantity filled so far; starts at `0`      |
| `status`     | `OrderStatus` | protected | Current status; starts as `PENDING`        |
| `timestamp`  | `long`        | protected | `System.currentTimeMillis()` at creation   |

Constructor: `protected Order(int orderId, Trader trader, AssetType assetType, OrderSide side, double quantity)`

##### Abstract Methods

| Method              | Return Type | Description                                           |
|---------------------|-------------|-------------------------------------------------------|
| `getOrderType()`    | `String`    | Returns `"LIMIT"` or `"MARKET"`                       |
| `canMatchAt(double price)` | `boolean` | Whether this order can be matched at the given price |

##### Concrete Methods

| Method                      | Return Type   | Access | Description                                                      |
|-----------------------------|---------------|--------|------------------------------------------------------------------|
| `getOrderId()`              | `int`         | public | Returns order ID                                                 |
| `getTrader()`               | `Trader`      | public | Returns trader                                                   |
| `getAssetType()`            | `AssetType`   | public | Returns asset type                                               |
| `getSide()`                 | `OrderSide`   | public | Returns side                                                     |
| `getQuantity()`             | `double`      | public | Returns total quantity                                           |
| `getFilledQty()`            | `double`      | public | Returns filled quantity                                          |
| `getRemainingQty()`         | `double`      | public | Returns `quantity - filledQty`                                   |
| `getStatus()`               | `OrderStatus` | public | Returns status                                                   |
| `getTimestamp()`            | `long`        | public | Returns timestamp                                                |
| `fill(double qty)`          | `void`        | public | Adds `qty` to `filledQty`; updates status to `PARTIAL` or `FILLED` |
| `cancel()`                  | `void`        | public | Sets status to `CANCELLED` if not already terminal              |
| `reject()`                  | `void`        | public | Sets status to `REJECTED` if not already terminal               |
| `toString()`                | `String`      | public | Order summary                                                    |

##### Validation Rules

| Rule                              |
|-----------------------------------|
| Quantity must be positive         |
| Trader cannot be `null`           |
| Cannot fill more than remaining   |
| Cannot change status if terminal  |

---

#### `LimitOrder` extends `Order`

| Field        | Type     | Access  | Description           |
|--------------|----------|---------|-----------------------|
| `limitPrice` | `double` | private | Maximum/minimum price |

Constructor: `public LimitOrder(int orderId, Trader trader, AssetType assetType, OrderSide side, double quantity, double limitPrice)`

| Method             | Notes                                                                              |
|--------------------|------------------------------------------------------------------------------------|
| `getLimitPrice()`  | Returns `limitPrice`                                                               |
| `getOrderType()`   | Returns `"LIMIT"`                                                                  |
| `canMatchAt(double price)` | BUY: `price <= limitPrice`; SELL: `price >= limitPrice`               |

`LimitOrder` **must implement `Comparable<LimitOrder>`**:
- BUY orders: higher price ranks first (descending price); ties broken by earlier timestamp
- SELL orders: lower price ranks first (ascending price); ties broken by earlier timestamp

> **Note:** The `Comparable` implementation must work correctly for both sides. `MatchingEngine` will use separate `PriorityQueue` instances for buys and sells and will handle side-awareness externally via `Comparator`.

#### Validation Rule

| Rule                           |
|--------------------------------|
| Limit price must be positive   |

---

#### `MarketOrder` extends `Order`

Constructor: `public MarketOrder(int orderId, Trader trader, AssetType assetType, OrderSide side, double quantity)`

| Method             | Notes                                      |
|--------------------|--------------------------------------------|
| `getOrderType()`   | Returns `"MARKET"`                         |
| `canMatchAt(double price)` | Always returns `true` — matches at any price |

---

### Step 6 — Implement `TradeRecord` (Immutable)

**File:** `model/TradeRecord.java`

A `TradeRecord` is created every time two orders are matched. It must be **fully immutable** — no setters, all fields `final`.

#### Fields (all `private final`)

| Field         | Type        | Description                          |
|---------------|-------------|--------------------------------------|
| `tradeId`     | `int`       | Unique trade ID                      |
| `buyOrderId`  | `int`       | ID of the matched buy order          |
| `sellOrderId` | `int`       | ID of the matched sell order         |
| `assetType`   | `AssetType` | Asset traded                         |
| `quantity`    | `double`    | Quantity matched                     |
| `price`       | `double`    | Execution price                      |
| `timestamp`   | `long`      | `System.currentTimeMillis()` at creation |

#### Constructor

`public TradeRecord(int tradeId, int buyOrderId, int sellOrderId, AssetType assetType, double quantity, double price)`

All getters, no setters. `toString()` must include all fields.

#### Validation Rules

| Rule                                  |
|---------------------------------------|
| Quantity must be positive             |
| Price must be positive                |
| Buy and sell order IDs cannot be equal|

---

### Step 7 — Implement `AuditEntry` (Immutable) and `AuditLog`

**Files:** `model/AuditEntry.java`, `service/AuditLog.java`

---

#### `AuditEntry` (Immutable)

All fields `private final`. No setters.

| Field       | Type     | Description                                   |
|-------------|----------|-----------------------------------------------|
| `entryId`   | `int`    | Unique entry ID                               |
| `eventType` | `String` | e.g. `"ORDER_SUBMITTED"`, `"TRADE_EXECUTED"`, `"ORDER_REJECTED"` |
| `details`   | `String` | Human-readable description                    |
| `timestamp` | `long`   | `System.currentTimeMillis()` at creation      |

Constructor: `public AuditEntry(int entryId, String eventType, String details)`

All getters, `toString()`.

#### Validation Rules

| Rule                          |
|-------------------------------|
| Event type cannot be blank    |
| Details cannot be blank       |

---

#### `AuditLog`

A **thread-safe, append-only** log of `AuditEntry` records.

| Field     | Type               | Access  | Description         |
|-----------|--------------------|---------|---------------------|
| `entries` | `List<AuditEntry>` | private | All audit entries   |
| `nextId`  | `int`              | private | Auto-incrementing ID|

Constructor: `public AuditLog()`

| Method                                      | Return Type        | Access | Description                                             |
|---------------------------------------------|--------------------|--------|---------------------------------------------------------|
| `log(String eventType, String details)`     | `AuditEntry`       | public | Creates, stores, and returns a new `AuditEntry`         |
| `getEntries()`                              | `List<AuditEntry>` | public | Defensive copy                                          |
| `getEntriesByType(String eventType)`        | `List<AuditEntry>` | public | Filtered defensive copy                                 |
| `getTotalEntries()`                         | `int`              | public | Total count                                             |
| `getLatestEntry()`                          | `AuditEntry`       | public | Most recently added entry, or `null` if empty           |

---

### Step 8 — Implement `Market`

**File:** `model/Market.java`

A `Market` is a venue for trading a single `AssetType`.

#### Fields

| Field             | Type              | Access  | Description                                 |
|-------------------|-------------------|---------|---------------------------------------------|
| `marketId`        | `String`          | private | Unique market ID (e.g. `"ELEC-ZA"`)         |
| `assetType`       | `AssetType`       | private | The asset traded here                       |
| `lastTradePrice`  | `double`          | private | Price of the most recent executed trade     |
| `openingPrice`    | `double`          | private | Price at market open; set once              |
| `isOpen`          | `boolean`         | private | Whether the market is accepting orders      |
| `orders`          | `List<Order>`     | private | All orders submitted to this market         |
| `tradeHistory`    | `List<TradeRecord>` | private | All executed trades                        |

#### Constructor

`public Market(String marketId, AssetType assetType, double openingPrice)`

#### Methods

| Method                        | Return Type        | Access | Description                                            |
|-------------------------------|--------------------|--------|--------------------------------------------------------|
| `getMarketId()`               | `String`           | public | Returns market ID                                      |
| `getAssetType()`              | `AssetType`        | public | Returns asset type                                     |
| `getLastTradePrice()`         | `double`           | public | Returns last trade price                               |
| `getOpeningPrice()`           | `double`           | public | Returns opening price                                  |
| `isOpen()`                    | `boolean`          | public | Returns whether market is open                         |
| `open()`                      | `void`             | public | Opens market                                           |
| `close()`                     | `void`             | public | Closes market                                          |
| `submitOrder(Order)`          | `void`             | public | Adds order; throws if market is closed                 |
| `recordTrade(TradeRecord)`    | `void`             | public | Appends trade; updates `lastTradePrice`                |
| `getOrders()`                 | `List<Order>`      | public | Defensive copy                                         |
| `getTradeHistory()`           | `List<TradeRecord>`| public | Defensive copy                                         |
| `getTotalVolume()`            | `double`           | public | Sum of quantities across all `TradeRecord`s            |
| `getPriceMovement()`          | `double`           | public | `((lastTradePrice - openingPrice) / openingPrice) * 100` as a percentage |
| `toString()`                  | `String`           | public | Market summary                                         |

#### Validation Rules

| Rule                                              |
|---------------------------------------------------|
| Opening price must be positive                    |
| Market ID cannot be blank                         |
| Orders cannot be submitted to a closed market     |

---

### Step 9 — Implement `MatchingStrategy` (Interface) and Implementations

**Package:** `strategy`

---

#### `MatchingStrategy` (Interface)

```java
List<TradeRecord> match(PriorityQueue<LimitOrder> buyQueue,
                        PriorityQueue<LimitOrder> sellQueue,
                        int tradeIdStart);
```

Returns a list of `TradeRecord`s generated by matching the queues.

---

#### `PriceTimeMatchingStrategy`

Implements the standard **price-time priority** algorithm:

1. Peek at the best buy (highest price) and best sell (lowest price).
2. If `bestBuy.limitPrice >= bestSell.limitPrice`, a match exists.
3. Execution price is the **sell order's limit price** (sell side sets the price).
4. Matched quantity is `min(buy.remainingQty(), sell.remainingQty())`.
5. Call `fill()` on both orders for the matched quantity.
6. Create a `TradeRecord` and add it to results.
7. Remove fully filled orders from their respective queues.
8. Repeat until no match is possible.

---

#### `ProRataMatchingStrategy`

Implements **pro-rata** allocation — when multiple sell orders sit at the same best price, quantity is distributed proportionally by their remaining size:

1. Find the best buy order (highest limit price).
2. Collect all sell orders whose `limitPrice <= buy.limitPrice` — these are the eligible sellers.
3. Distribute the buy's remaining quantity across eligible sellers **in proportion to their remaining quantities**.
4. Each seller receives: `floor((sellerRemainingQty / totalEligibleQty) * buyRemainingQty)`.
5. Any rounding remainder goes to the seller with the largest remaining quantity.
6. Create one `TradeRecord` per seller actually filled.
7. Remove fully filled orders.

---

### Step 10 — Implement `RiskRule` (Interface) and Implementations

**Package:** `risk`

---

#### `RiskRule` (Interface)

```java
boolean evaluate(Order order, Market market);
String getRuleName();
```

Returns `true` if the order **passes** the rule, `false` if it should be rejected.

---

#### `PositionLimitRule`

| Field            | Type     | Description                               |
|------------------|----------|-------------------------------------------|
| `maxPositionValue` | `double` | Maximum total exposure a trader may hold |

Constructor: `public PositionLimitRule(double maxPositionValue)`

Rule logic: A BUY `LimitOrder` is rejected if `trader.getTotalExposure() + (order.getQuantity() * limitPrice) > maxPositionValue`. `MarketOrder`s always pass this rule (price unknown).

---

#### `MarginRequirementRule`

| Field             | Type     | Description                                    |
|-------------------|----------|------------------------------------------------|
| `marginFraction`  | `double` | Fraction of order value that must be in cash (e.g. `0.2` = 20%) |

Constructor: `public MarginRequirementRule(double marginFraction)`

Rule logic: A BUY `LimitOrder` is rejected if `trader.getPortfolio().getCashBalance() < order.getQuantity() * limitPrice * marginFraction`. SELL orders and `MarketOrder`s always pass.

---

#### `BlacklistRule`

No fields beyond `getRuleName()`. Rejects any order from a blacklisted trader.

---

### Step 11 — Implement `RiskEngine`

**File:** `service/RiskEngine.java`

Implements **Chain of Responsibility** — processes a list of `RiskRule` objects in sequence.

#### Fields

| Field   | Type            | Access  |
|---------|-----------------|---------|
| `rules` | `List<RiskRule>`| private |

Constructor: `public RiskEngine()`

| Method                    | Return Type | Description                                                                  |
|---------------------------|-------------|------------------------------------------------------------------------------|
| `addRule(RiskRule)`       | `void`      | Appends a rule to the chain                                                  |
| `getRules()`              | `List<RiskRule>` | Defensive copy                                                          |
| `evaluate(Order, Market)` | `boolean`   | Runs all rules in order; returns `false` (and stops) on first rule failure   |
| `getFailingRule(Order, Market)` | `RiskRule` | Returns the first rule that rejects the order, or `null` if all pass  |

---

### Step 12 — Implement `MarketObserver` and `MarketDataFeed`

---

#### `MarketObserver` (Interface)

**File:** `observer/MarketObserver.java`

```java
void onPriceUpdate(String marketId, AssetType assetType, double newPrice, double previousPrice);
void onMarketStatusChange(String marketId, boolean isOpen);
void onTradeExecuted(TradeRecord trade);
```

---

#### `MarketDataFeed`

**File:** `service/MarketDataFeed.java`

Manages subscriptions and broadcasts events to all registered `MarketObserver`s.

| Field         | Type                          | Access  |
|---------------|-------------------------------|---------|
| `observers`   | `List<MarketObserver>`        | private |
| `priceCache`  | `HashMap<String, Double>`     | private | Last known price per marketId |

Constructor: `public MarketDataFeed()`

| Method                                                      | Return Type | Description                                                 |
|-------------------------------------------------------------|-------------|-------------------------------------------------------------|
| `subscribe(MarketObserver)`                                 | `void`      | Registers observer; no duplicates                           |
| `unsubscribe(MarketObserver)`                               | `boolean`   | Removes observer; returns `true` if found                   |
| `publishPriceUpdate(String marketId, AssetType, double newPrice)` | `void` | Notifies all observers; updates `priceCache`          |
| `publishMarketStatusChange(String marketId, boolean isOpen)`| `void`      | Notifies all observers                                      |
| `publishTradeExecuted(TradeRecord)`                         | `void`      | Notifies all observers                                      |
| `getLastPrice(String marketId)`                             | `double`    | Returns last cached price, or `-1` if unknown               |
| `getObserverCount()`                                        | `int`       | Returns number of subscribed observers                      |

---

### Step 13 — Implement `CircuitBreaker`

**File:** `service/CircuitBreaker.java`

The `CircuitBreaker` monitors a market's price movement and halts trading if it exceeds a threshold.

#### Fields

| Field              | Type                  | Access  | Description                                    |
|--------------------|-----------------------|---------|------------------------------------------------|
| `market`           | `Market`              | private | The market being monitored                     |
| `threshold`        | `double`              | private | % price movement that triggers the breaker     |
| `state`            | `CircuitBreakerState` | private | Current state; starts as `CLOSED`              |
| `tripCount`        | `int`                 | private | Number of times the breaker has tripped        |

Constructor: `public CircuitBreaker(Market market, double threshold)`

| Method                  | Return Type          | Description                                                                      |
|-------------------------|----------------------|----------------------------------------------------------------------------------|
| `getState()`            | `CircuitBreakerState`| Returns current state                                                            |
| `getTripCount()`        | `int`                | Returns how many times it has tripped                                            |
| `getThreshold()`        | `double`             | Returns threshold                                                                |
| `check()`               | `void`               | Evaluates `market.getPriceMovement()`; trips to `OPEN` if `abs(movement) >= threshold` |
| `reset()`               | `void`               | Moves from `OPEN` → `TESTING`                                                    |
| `confirmStable()`       | `void`               | Moves from `TESTING` → `CLOSED`                                                  |
| `allowsTrading()`       | `boolean`            | Delegates to `state.allowsTrading()`                                             |
| `toString()`            | `String`             | Summary                                                                          |

#### Validation Rules

| Rule                               |
|------------------------------------|
| Threshold must be between 1 and 100|
| `reset()` only valid from `OPEN`   |
| `confirmStable()` only valid from `TESTING` |

---

### Step 14 — Implement `MatchingEngine`

**File:** `service/MatchingEngine.java`

The `MatchingEngine` orchestrates order books per market, runs risk checks, and executes matches.

#### Fields

| Field              | Type                                               | Access  | Description                              |
|--------------------|----------------------------------------------------|---------|------------------------------------------|
| `markets`          | `HashMap<String, Market>`                          | private | Registered markets by ID                 |
| `buyQueues`        | `HashMap<String, PriorityQueue<LimitOrder>>`       | private | Buy order books per market               |
| `sellQueues`       | `HashMap<String, PriorityQueue<LimitOrder>>`       | private | Sell order books per market              |
| `riskEngine`       | `RiskEngine`                                       | private | Pre-trade risk checking                  |
| `matchingStrategy` | `MatchingStrategy`                                 | private | Pluggable matching algorithm             |
| `auditLog`         | `AuditLog`                                         | private | Audit trail                              |
| `dataFeed`         | `MarketDataFeed`                                   | private | Price/event publisher                    |
| `tradeCounter`     | `int`                                              | private | Auto-incrementing trade ID               |

Constructor: `public MatchingEngine(RiskEngine riskEngine, MatchingStrategy strategy, AuditLog auditLog, MarketDataFeed dataFeed)`

| Method                              | Return Type        | Description                                                                                              |
|-------------------------------------|--------------------|----------------------------------------------------------------------------------------------------------|
| `registerMarket(Market)`            | `void`             | Adds market; initialises buy/sell queues with correct `Comparator`                                       |
| `submitOrder(Order)`                | `List<TradeRecord>`| Runs risk checks; rejects if failed; adds to order book; runs matching; returns list of generated trades |
| `cancelOrder(int orderId, String marketId)` | `boolean`  | Cancels order if found and not terminal; releases reserved cash if BUY `LimitOrder`                     |
| `getOrderBook(String marketId, OrderSide)` | `List<LimitOrder>` | Returns defensive copy of the appropriate queue as sorted list                                |
| `getMarket(String marketId)`        | `Market`           | Returns market or `null`                                                                                 |
| `getAllMarkets()`                   | `List<Market>`     | Defensive copy                                                                                           |
| `setMatchingStrategy(MatchingStrategy)` | `void`         | Swaps the matching strategy at runtime                                                                   |

#### Buy Queue `Comparator`

Higher limit price first; ties resolved by earlier timestamp (ascending).

#### Sell Queue `Comparator`

Lower limit price first; ties resolved by earlier timestamp (ascending).

#### `submitOrder` Detailed Logic

1. Look up the market; throw `IllegalArgumentException` if not found.
2. Check `market.isOpen()`; throw `IllegalStateException` if closed.
3. Run `riskEngine.evaluate(order, market)`; if `false`, call `order.reject()`, log `"ORDER_REJECTED"`, return empty list.
4. If BUY `LimitOrder`, call `portfolio.reserveCash(quantity * limitPrice)`; if `false`, reject order.
5. Add `LimitOrder`s to the appropriate queue. `MarketOrder`s are matched immediately without entering the queue.
6. Call `market.submitOrder(order)`.
7. Run `matchingStrategy.match(buyQueue, sellQueue, tradeCounter)`.
8. For each `TradeRecord` returned: call `market.recordTrade(trade)`, publish to `dataFeed`, log `"TRADE_EXECUTED"`, increment `tradeCounter`.
9. Log `"ORDER_SUBMITTED"` for successful submissions.
10. Return list of `TradeRecord`s.

---

### Step 15 — Implement `Settlement`

**File:** `service/Settlement.java`

`Settlement` processes `TradeRecord`s and updates the portfolios of the buyer and seller.

#### Fields

| Field       | Type      | Access  | Description          |
|-------------|-----------|---------|----------------------|
| `auditLog`  | `AuditLog`| private | For logging          |
| `settled`   | `List<TradeRecord>` | private | Already settled trades |

Constructor: `public Settlement(AuditLog auditLog)`

| Method                                                       | Return Type | Description                                                                   |
|--------------------------------------------------------------|-------------|-------------------------------------------------------------------------------|
| `settle(TradeRecord, Trader buyer, Trader seller)`           | `boolean`   | Executes settlement; returns `false` if preconditions fail                    |
| `getSettledTrades()`                                         | `List<TradeRecord>` | Defensive copy                                                          |
| `getTotalSettledValue()`                                     | `double`    | Sum of `quantity * price` across settled trades                               |
| `isAlreadySettled(int tradeId)`                              | `boolean`   | Checks if a trade ID is in the settled list                                   |

#### `settle()` Logic

1. Check `isAlreadySettled(tradeId)`; return `false` if already done.
2. Calculate `tradeValue = trade.getQuantity() * trade.getPrice()`.
3. Call `buyer.getPortfolio().deductReservedCash(tradeValue)`; return `false` if insufficient.
4. Call `seller.getPortfolio().reduceAsset(trade.getAssetType(), trade.getQuantity())`; return `false` if insufficient.
5. Call `seller.getPortfolio().creditCash(tradeValue)`.
6. Call `buyer.getPortfolio().addAsset(trade.getAssetType(), trade.getQuantity(), trade.getPrice())`.
7. Compute realised PnL for seller: `(trade.getPrice() - sellerAvgCost) * trade.getQuantity()` and record it.
8. Add trade to `settled` list.
9. Log `"TRADE_SETTLED"`.
10. Return `true`.

---
