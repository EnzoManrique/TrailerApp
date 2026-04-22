# Design: Firebase Real-Time Sync for TrailerStockApp

## Technical Approach

TrailerStockApp currently operates as 100% offline-first with Room/SQLite. To support multi-device sync (two physical devices), we implement a **hybrid local-first + cloud sync** architecture. The system maintains Room as the source of truth for instant UI updates, batches mutations for Firestore upload via WorkManager, and uses Last-Write-Wins (LWW) with entity-specific conflict resolution for inventory transactions. Offline capability remains uninterrupted; sync happens in the background.

**Key strategy**: Optimistic updates to Room → queue to SyncOperation → WorkManager background job → batch push to Firestore. Incoming Firestore changes trigger listeners → ConflictResolver → apply to Room → notify UI.

---

## Architecture Decisions

### Decision 1: Hybrid Local-First with Firestore

| Aspect | Choice | Alternatives | Rationale |
|--------|--------|--------------|-----------|
| **Primary Store** | Room (SQLite) | Pure Firestore | Instant UI updates, offline-first. Firestore is eventual-consistency backup. |
| **Write Path** | Room first, queue sync | Firestore first | User sees update immediately; network issues don't block UI. Retry logic handles failures. |
| **Read Path** | Room with Firestore listener | Firestore only | Eliminates latency; listeners keep Room in sync asynchronously. |

**Rationale**: MVVM + Compose bindings expect synchronous Flow emissions. Room satisfies this; Firestore listeners fill Room, not vice versa. Offline users never blocked.

---

### Decision 2: Conflict Resolution Strategy

| Entity Type | Strategy | Example | Why |
|-------------|----------|---------|-----|
| **Producto** (prices, metadata) | Last-Write-Wins (LWW) | Device A: price $5 @ 10:00, Device B: price $6 @ 10:05 → B wins | Humans rarely conflict on metadata; recency = authority. |
| **Stock (inventory delta)** | Aggregate (sum deltas) | Device A: -5 units @ 10:00, Device B: -3 units @ 10:05 → stock -= 8 total | Stock is cumulative; both moves are real. Single LWW loses data. |
| **Venta (transactions)** | Append-only, deviceId FK | Two devices create Venta separately → both persisted in Firestore | Transactions are immutable events; no conflict possible. |

**Rationale**: LWW works for attributes (prices). Stock requires aggregate because each device operates independently and both movements are legitimate. Venta is immutable event log.

---

### Decision 3: SyncManager as Orchestrator

| Responsibility | Implementation | Why |
|---|---|---|
| **Queue management** | Batch pending SyncOperations every 30s | Reduce API calls; max 100 ops/batch to stay under Firestore limits. |
| **Retry with backoff** | Exponential backoff: 1s, 2s, 4s, 8s... (max 3 retries, 24h TTL) | Handle transient failures; don't spam on persistent errors. |
| **Listener registration** | Single global listener per entity → Room update | One listener per table type; avoids duplicate callbacks. |
| **Offline detection** | WorkManager respects network constraints | No battery drain during airplane mode; KEEP policy for battery saver. |

**Rationale**: Centralized orchestrator prevents duplicate sync logic. WorkManager integration ensures reliability—system reschedules if process dies.

---

### Decision 4: StockService Mutation Centralization

| Old Pattern | New Pattern | Why |
|---|---|---|
| `ProductoRepository.descontarStock(id, qty)` | `StockService.recordMovement(productId, delta, deviceId)` | Single entry point for stock changes. Both Producto AND StockMovement table updated atomically. Audit trail automatic. |
| Direct `update()` calls | `StockService` wrapper | Ensures deviceId + timestamp always logged. Enables conflict resolution (know which device changed what). |

**Rationale**: Stock is high-risk (inventory accuracy). Centralizing mutations ensures consistency. StockMovement table provides audit trail and enables aggregate conflict resolution.

---

### Decision 5: DeviceIdProvider for Tiebreaker

| Component | Responsibility | Why |
|---|---|---|
| **DeviceIdProvider** | UUID generated once on first launch; stored in DataStore Preferences | Identifies which device originated a change (used in conflict resolution). Survives app reinstall if DataStore backup enabled; hidden from user. |

**Rationale**: Each device needs immutable identity for determining conflict winners and audit trails. UUID is industry-standard; DataStore survives app lifecycle.

---

## Data Flow

### Architecture Diagram

```
    ┌─────────────────────────────────────────┐
    │         UI Layer (Jetpack Compose)      │
    │  ProductsScreen, VentasScreen, etc.    │
    └──────────────┬──────────────────────────┘
                   │
                   ▼
    ┌─────────────────────────────────────────┐
    │      ViewModel + State Management       │
    │   (Flows from Room, reactive updates)   │
    └──────────────┬──────────────────────────┘
                   │
    ┌──────────────┴──────────────┐
    ▼                             ▼
┌──────────────┐           ┌────────────────────┐
│ ProductoRepo │           │ SyncManager        │
│  (Room CRUD) │           │ (Orchestrator)     │
└──────────────┘           └────────────────────┘
    │                             │
    ├─ RoomDatabase ──────────┐   ├─ SyncOperation DAO ──┐
    │                        │   │                      │
    ▼                        ▼   ▼                      ▼
┌─────────────────────────────────────────────┐
│  AppDatabase (Room / SQLite)                │
│  - Producto (+ syncedAt, deviceId)          │
│  - Venta (+ syncedAt, deviceId)             │
│  - SyncOperation (queue)                    │
│  - StockMovement (audit trail)              │
└─────────────────────────────────────────────┘
    │
    │  (SyncManager polls every 30s)
    │
    ▼
┌─────────────────────────────────────────────┐
│  WorkManager Job                            │
│  - Batch pull: fetch new Firestore docs     │
│  - Batch push: send queued SyncOperations   │
└─────────────────────────────────────────────┘
    │
    ├─ ConflictResolver
    │  (Compare local vs remote, apply rules)
    │
    ▼
┌─────────────────────────────────────────────┐
│  Firebase Firestore (Remote)                │
│  - productos/{id}                           │
│  - stock_movements/{year}/{month}/{id}      │
│  - sync_log/{deviceId}/{opId}               │
│  - Listeners: notify on remote changes      │
└─────────────────────────────────────────────┘
```

---

### Push Flow (Local → Firestore)

```
1. User taps "Create Sale" in UI
   ↓
2. ViewModel calls VentaRepository.insert(venta)
   ↓
3. Repository inserts to Room (optimistic: user sees immediately)
   ↓
4. Repository calls SyncManager.queueChange(SyncOperation)
   ↓
5. SyncOperation inserted to Room (queued, synced=false)
   ↓
6. WorkManager triggers every 30s
   ↓
7. SyncManager.sync() queries Room for synced=false, synced=null operations
   ↓
8. Batches up to 100 ops (Group by entityType for consistency)
   ↓
9. FirebaseRepository.push(batch) uploads to Firestore
   ↓
10. On HTTP 200:
    - Mark all SyncOperations: synced=true, syncedAt=now
    - Log to sync_log/{deviceId}/ for audit
    ↓
11. On HTTP failure (network, 5xx, etc.):
    - Increment retryCount
    - If retryCount < 3: reschedule WorkManager with backoff
    - If retryCount >= 3: mark failureReason, alert user
    - TTL: operations >24h old auto-deleted to prevent queue bloat
```

---

### Pull Flow (Firestore → Local)

```
1. Firestore listener detects document change in productos/{id}
   ↓
2. FirebaseRepository.observeChanges(listener) notifies ConflictResolver
   ↓
3. ConflictResolver.resolve(remoteDoc, localDoc)
   - Compare: remote.syncedAt vs local.syncedAt (LWW for Producto)
   - Compare: remote.deviceId vs local.deviceId (tiebreaker if equal time)
   - Apply rule: aggregate if stock, LWW if price
   ↓
4. Execute Room transaction:
   - Update Producto (if precio changed, add audit entry to SyncLog)
   - Update StockMovement (if stock changed)
   ↓
5. Room Flow<Producto> emits new value
   ↓
6. ViewModel receives update
   ↓
7. UI recompose shows new data
   ↓
8. Optional: Show toast if conflict detected ("Updated from Device-B")
```

---

### Cold Start Flow (App Launch)

```
1. MainActivity.onCreate() calls SyncManager.initialize()
   ↓
2. SyncManager checks last_sync_timestamp in Room
   ↓
3. If timestamp > 5 minutes old OR null:
   - Pull full product list from Firestore (paginated: 100/page)
   - First batch: all Productos modified in last 30 days
   - Iterate: load next 100 until caught up
   ↓
4. For each remote doc, ConflictResolver applies rules
   ↓
5. Insert/update Room with resolved state
   ↓
6. Set last_sync_timestamp = now
   ↓
7. Register global Firestore listener on productos/
   ↓
8. UI transitions from "Loading..." to normal product list
```

---

## File Changes & Migrations

### Room Migration (v10 → v11)

**New Columns on Existing Tables:**

| Table | Columns Added | Type | Default | Purpose |
|-------|---|---|---|---|
| **Producto** | `synced_at` | LONG | 0 | Timestamp of last successful sync to Firestore |
| | `device_id` | TEXT | "" | UUID of device that owns this change (tiebreaker) |
| | `remote_id` | TEXT | NULL | Firestore document ID (maps local ↔ remote) |
| **Venta** | `synced_at` | LONG | 0 | Sync timestamp |
| | `device_id` | TEXT | "" | Originating device |
| | `remote_id` | TEXT | NULL | Firestore doc ID |
| **VentaDetalle** | `synced_at` | LONG | 0 | Sync timestamp |

**New Tables:**

| Table | Columns | Purpose |
|-------|---------|---------|
| **SyncOperation** | `id` (PK), `entityType` (TEXT), `entityId` (LONG), `operation` (TEXT: INSERT/UPDATE/DELETE), `changes` (TEXT: JSON), `timestamp` (LONG), `deviceId` (TEXT), `synced` (BOOLEAN), `retryCount` (INT), `syncedAt` (LONG), `failureReason` (TEXT) | Queue of pending mutations for Firestore. One row per atomic change. |
| **StockMovement** | `id` (PK), `productId` (LONG FK), `delta` (INT: +/- quantity), `deviceId` (TEXT), `timestamp` (LONG), `syncedAt` (LONG), `reconciled` (BOOLEAN) | Immutable audit trail of stock changes. Enables aggregate conflict resolution. |

---

## Firestore Schema

```
productos/{id}/
  - name: String
  - description: String
  - precioCosto: Double
  - precioLista: Double
  - precioMayorista: Double
  - stockActual: Int (computed from StockMovement deltas)
  - stockMinimo: Int
  - categoriaId: Int
  - eliminado: Boolean
  - deviceId: String (last writer)
  - syncedAt: Timestamp (server write time)
  - lastModifiedBy: String (for audit)

stock_movements/{year}/{month}/{id}/
  - productId: Long
  - delta: Int
  - deviceId: String
  - timestamp: Timestamp
  - reconciled: Boolean (used for aggregation logic)

sync_log/{deviceId}/{opId}/
  - entityType: String
  - entityId: Long
  - operation: String (INSERT/UPDATE/DELETE)
  - changes: Map<String, Any> (what changed)
  - timestamp: Timestamp
  - resolved: Boolean
  - winningChange: Map (used in conflict resolution audit)
```

---

## Component Breakdown

### SyncManager (Orchestrator)
**File**: `data/sync/SyncManager.kt`

Orchestrates all sync operations. Runs in WorkManager background job (30s periodic). Manages SyncOperation queue, coordinates push/pull, applies retry logic with exponential backoff. Registers global Firestore listeners. Exposes methods: `queueChange()`, `sync()`, `initialize()`. Dependencies: FirebaseRepository, Room DAOs, DeviceIdProvider, ConflictResolver. Thread model: suspend functions in Coroutine scope; all IO non-blocking.

---

### ConflictResolver (Decision Maker)
**File**: `data/sync/ConflictResolver.kt`

Takes (localDoc, remoteDoc, timestamp, deviceId) and applies entity-specific rules. For Producto: compares syncedAt (LWW); if tied, uses deviceId as tiebreaker (lexicographic). For Stock: aggregates deltas from StockMovement. Logs resolution to sync_log for audit. Returns winning state to apply to Room.

---

### StockService (Mutation Centralizer)
**File**: `domain/service/StockService.kt`

Replaces direct calls to `ProductoRepository.descontarStock()`, `updateStock()`, `restock()`. New method: `recordMovement(productId, delta, deviceId)`. Atomically: inserts to StockMovement (audit), updates Producto.stockActual, queues SyncOperation. Ensures deviceId + timestamp always logged. Used by VentaRepository when creating/editing sales.

---

### FirebaseRepository (Remote Datasource)
**File**: `data/firestore/FirebaseRepository.kt`

Interface to Firestore. Methods: `push(batch: List<SyncOperation>)` → uploads batch to Firestore & sync_log; `pull(since: Timestamp)` → fetches changed docs from productos/ since timestamp; `observeChanges(listener: ChangeListener)` → registers global listener on productos/. Handles Firestore exceptions, retries on transient errors.

---

### DeviceIdProvider (Tiebreaker)
**File**: `data/local/DeviceIdProvider.kt`

Generates UUID on first app launch; persists to DataStore Preferences (key: "device_id"). On subsequent launches, retrieves same UUID. Used throughout: every SyncOperation, every Room mutation gets deviceId appended. Survivor of app reinstall if DataStore backup enabled (Firebase Cloud Backup).

---

## Testing Strategy

| Layer | What | How | Acceptance |
|-------|------|-----|-----------|
| **Unit** | ConflictResolver LWW logic | Two Producto instances, different syncedAt → verify correct winner returned | Pass 100/100 scenarios |
| | ConflictResolver aggregate logic | Multiple StockMovement deltas → sum correctly | ✓ |
| | StockService.recordMovement() | Insert → verify Producto updated AND StockMovement inserted AND SyncOperation queued | ✓ |
| **Integration** | SyncManager push with mock Firestore | Queue 50 SyncOps → trigger sync → verify all marked synced=true and batch upload called | No flakes in 10 runs |
| | SyncManager pull with listener | Insert remote doc to mock Firestore → listener fires → Room updated | ✓ |
| | Room migration v10→v11 | Create v10 DB, migrate, verify new columns exist and default correctly | ✓ |
| **E2E** | Two-device sync | Emulator A: create product → Emulator B should see it within 60s | Pass 5/5 times |
| | Offline then reconnect | Disable network on Emulator A, perform 10 ops, enable network → verify all 10 synced within 2 min | ✓ |
| | Conflict resolution | Device A & B modify same Producto prices simultaneously → verify LWW applied, loser's change in sync_log | ✓ |

---

## Phased Deployment

| Phase | Timeline | Deliverables | Milestones |
|-------|----------|--------------|-----------|
| **Phase 1: Schema** | Week 1 | Migration 10→11, new tables, DAOs | Room compile clean, migration tested |
| **Phase 2: SyncManager** | Week 2 | SyncManager core, FirebaseRepository push/pull | Background job schedules, batch upload works |
| **Phase 3: Conflict Resolver** | Week 3 | ConflictResolver logic, StockService, DeviceIdProvider, listener registration | Multi-device sync tested in emulator |
| **Phase 4: Testing & UI** | Week 4 | E2E tests, offline scenarios, UI badges ("syncing", "offline", "conflict"), error handling, launch to production | All test scenarios pass, user sees accurate state |

---

## Open Questions

- [ ] **Firestore Rules**: Should we enforce per-user/device access or allow all authenticated users to read/write? (Proposal: all authenticated for MVP; revisit in Phase 4)
- [ ] **Stock reconciliation**: If two devices create concurrent stock movements, should we run periodic reconciliation query on Firestore to verify sums? (Proposal: defer to Phase 5; monitor drift in logs)
- [ ] **Data cleanup**: TTL policy for sync_log entries >90 days? (Proposal: manual cleanup via admin script for now)
- [ ] **Metrics**: Should we log sync metrics (avg batch size, retry rate, conflict rate) to Firestore analytics? (Proposal: Phase 5 for observability)

---

## Summary

This design provides **offline-first, hybrid local-first + cloud sync** with **conflict resolution** tailored to TrailerStockApp's inventory domain. Room remains the primary store for instant UI updates; Firestore is the source of truth for multi-device state. SyncManager orchestrates background jobs; ConflictResolver applies domain rules (LWW for metadata, aggregate for stock). StockService centralizes mutations to ensure audit trails. Testing spans unit (rules), integration (SyncManager + Room), and E2E (two-device scenarios). Phased 4-week rollout minimizes risk.
