# CURRENT WORK

## Project
GalaxyBridge

## Current milestone
`GB-M0 — Hardware Feasibility`

## Current task queue

### GB-M0-R1 — iPhone ↔ Watch6 BLE Transport PoC
Status: READY
Owner: Codex implementation
Reviewer: Claude Desktop / Sonnet High / read-only
Gate: real iPhone 17e + Galaxy Watch6
Tracking: issue #1

Must prove:
- discovery/pairing path
- encrypted bidirectional application payload
- reconnect after app/device interruption
- foreground behavior
- background behavior and limitations
- measured latency and reconnect time
- no reliance on Wear OS Data Layer

### GB-M0-R2 — Watch6 ANCS PoC
Status: BLOCKED_BY_R1_TRANSPORT_SETUP only for shared project scaffold; ANCS research can run independently
Owner: Codex implementation
Reviewer: Claude Desktop / Sonnet High / read-only
Gate: real iPhone 17e + Galaxy Watch6

Must prove:
- Watch sees ANCS service when authorized
- notification add/modify/remove events
- attribute retrieval (app/title/message where exposed)
- available positive/negative actions
- at least one real notification action round-trip
- incoming-call category observation; answer/decline only counted if iOS exposes and hardware test passes

### GB-M0-R3 — Watch6 Health Sensor PoC
Status: READY_FOR_RESEARCH; implementation after base Wear app exists
Owner: Codex implementation
Reviewer: Claude Desktop / Sonnet High / read-only
Gate: real Galaxy Watch6; Samsung Health Platform developer mode during development

Must prove capability detection and at minimum:
- heart rate acquisition
- transfer to iPhone
- HealthKit write after explicit user permission

Extended probes:
- steps/workout via Wear OS Health Services
- SpO2
- BIA
- raw ECG
- skin temperature

Distribution note: Samsung Health Sensor SDK public distribution requires Samsung partner registration; developer mode is testing/debugging only.

### GB-M0-R4 — Buds2 Pro Transport Critical Spike
Status: PARTIAL — HARDWARE_REQUIRED / DIAGNOSTIC IMPLEMENTATION READY_FOR_OWNER_BUILD
Owner: Research first, then Codex PoC
Reviewer: Claude Desktop / Sonnet High / read-only
Gate: real Buds2 Pro + Watch6 + iPhone simultaneously
Tracking: issue #2

Verified research baseline:
- upstream snapshot: `timschneeb/GalaxyBudsClient@dce4735d76cd16abb818cdd96bf458efd4abef47`
- `Buds2ProDeviceSpec` selects `Uuids.SppNew`
- exact first candidate service UUID: `2e73a4ad-332d-41fc-90e2-16bef06523f2`
- modern SPP framing evidence includes SOM/header/message-id/payload/CRC16-CCITT/EOM
- direct iPhone path remains unproven until actual GATT inventory

Decision tree:
1. Inventory all GATT services/characteristics exposed by SM-R510 to iOS.
2. If management protocol is available over GATT, test direct iPhone path.
3. Otherwise test Watch6 RFCOMM/SPP client path using `2e73a4ad-332d-41fc-90e2-16bef06523f2`, verified via SDP/device evidence.
4. With Buds audio connected to iPhone, establish management connection from Watch6.
5. Record read-only incoming bytes with a strict memory bound and no command writes.
6. Only after PM review of independently understood packet evidence, add one non-destructive status query.
7. ANC reads/writes remain outside R4 implementation authority.

The milestone cannot pass without step 4 proving simultaneous iPhone audio + Watch management is viable.

Implemented diagnostic surface:
- iOS CoreBluetooth scan, explicit selection, complete service/characteristic inventory, safe reads/subscriptions, and redacted report export
- Watch6 bonded-device inventory, explicit selection, public SDP-resolved RFCOMM connection, read-only input, bounded raw-byte log, lifecycle/error report, and manual disconnect
- exact physical topology instructions in `docs/GB-M0-R4_OWNER_RUNBOOK.md`
- no status query because no exact read-only message ID/packet is independently justified by the current repository evidence

Current architecture classification: `PENDING — HARDWARE_REQUIRED`; none of `DIRECT_IOS_GATT`, `WATCH_RFCOMM_BRIDGE`, or `BLOCKED` has been selected.

## Stop conditions
- Do not start polished UI before M0 transport gates pass.
- Do not claim Watch notification parity from ANCS discovery alone.
- Do not claim Buds parity from protocol source inspection alone.
- Do not flash Buds firmware during M0.
- Do not copy GPLv3 implementation code into a closed-source codebase.
- Stop and report BLOCKED if a required platform API/profile cannot be accessed.
- Owner physical-device evidence overrides simulator/emulator/agent claims.

## Next PM action
1. Implement GB-M0-R1 minimal iOS/Wear scaffold.
2. In parallel, prepare GB-M0-R4 iPhone GATT inventory + Watch RFCOMM diagnostic probes.
3. Do not start R2/R3 product integration until the shared scaffold exists.
