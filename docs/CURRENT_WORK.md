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
Status: HIGHEST RISK / READY
Owner: Research first, then Codex PoC
Reviewer: Claude Desktop / Sonnet High / read-only
Gate: real Buds2 Pro + Watch6 + iPhone simultaneously

Decision tree:
1. Inventory all GATT services/characteristics exposed by SM-R510 to iOS.
2. If management protocol is available over GATT, test direct iPhone path.
3. Otherwise test Watch6 RFCOMM/SPP client path using first known SM-R510 candidate service UUID `2e73a4ad-332d-41fc-90e2-16bef06523f2`, then verify via SDP/device evidence.
4. With Buds audio connected to iPhone, establish management connection from Watch6.
5. Read battery/status.
6. Read ANC state.
7. Toggle ANC and verify acoustically + via returned state.

The milestone cannot pass without step 4 proving simultaneous iPhone audio + Watch management is viable.

## Stop conditions
- Do not start polished UI before M0 transport gates pass.
- Do not claim Watch notification parity from ANCS discovery alone.
- Do not claim Buds parity from protocol source inspection alone.
- Do not flash Buds firmware during M0.
- Do not copy GPLv3 implementation code into a closed-source codebase.
- Stop and report BLOCKED if a required platform API/profile cannot be accessed.
- Owner physical-device evidence overrides simulator/emulator/agent claims.

## Next PM action
Run GB-M0-R4 protocol inventory in parallel with GB-M0-R1 architecture scaffold, then prepare real-device test build.
