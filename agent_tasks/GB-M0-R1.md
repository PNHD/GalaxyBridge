# AGENT HANDOFF — GB-M0-R1

Tool: Codex
Role: Implementation driver
Repo: `PNHD/GalaxyBridge`
Branch: `feature/gb-m0-feasibility`
Task ID: `GB-M0-R1`
Write permission: YES, only task-owned files
Commit permission: YES, max 1 focused commit for first PoC
Sub-agents/agent teams: FORBIDDEN

## Goal
Create the minimal iOS + Wear OS project scaffold needed to prove bidirectional BLE/GATT transport on a real iPhone 17e and Galaxy Watch6.

## Required pre-state
- print CWD, branch, HEAD
- working tree clean
- read README + docs/CURRENT_WORK.md + docs/DECISIONS.md + docs/M0_TEST_PLAN.md
- do not start if the repo state differs materially; report BLOCKED

## Implementation constraints
- iOS: native Swift/SwiftUI + CoreBluetooth
- Wear OS: native Kotlin; minimal UI only
- no cloud/backend
- no Data Layer dependency
- define a small versioned transport envelope with protocolVersion, sessionId, seq, type, timestamp, payload
- logs must make sequence/reconnect behavior auditable
- no polished design system

## Acceptance
Software-side completion requires:
- both projects build
- explicit BLE roles documented and implemented
- round-trip ping/ack code path
- reconnect/state logs
- test instructions for real hardware

Final task status must remain `PARTIAL — HARDWARE_REQUIRED` until owner runs the physical test matrix.

## Return
- status: PASS/PARTIAL/BLOCKED/FAILED
- commit SHA
- files changed
- build/test commands + outputs
- known limitations
- exact owner test steps
- no next milestone automatically
