---
name: galaxybridge-ble-feasibility
description: Use for GalaxyBridge iPhone↔Galaxy Watch6 BLE/GATT transport, CoreBluetooth service inventory, ANCS transport plumbing, Bluetooth permissions, MTU/write-flow, reconnect or background BLE claims. Do not use for product UI styling.
---

# GalaxyBridge BLE Feasibility

## First actions

1. Read `AGENTS.md`, `docs/CURRENT_WORK.md`, `docs/DECISIONS.md`, `docs/M0_TEST_PLAN.md` and the active task packet.
2. Report repo, branch, HEAD and working-tree status before editing.
3. Verify unstable Apple/Android Bluetooth API behavior against current primary docs before relying on it.

## Architecture rules

- Do not use Wear OS Data Layer as the iPhone↔Watch transport.
- Explicitly justify GATT central/client and peripheral/server roles from platform capability, background/reconnect needs and real-device constraints.
- Do not confuse CoreBluetooth GATT support with arbitrary Bluetooth Classic RFCOMM/SPP access.
- Use fixed/versioned service and characteristic UUIDs owned by GalaxyBridge for the Watch bridge.
- Prefer platform pairing/bonding/encryption; do not invent custom crypto for M0.

## GATT correctness checklist

- Gate operations on Bluetooth powered/authorized state.
- Retain connection objects correctly.
- Discover services/characteristics only after connection callbacks.
- Check characteristic properties before read/write/notify.
- Respect maximum write length and flow control.
- If application frames exceed one ATT payload, implement bounded fragmentation/reassembly with timeout and malformed-frame rejection.
- Record session IDs and sequence numbers; reject stale/duplicate frames deterministically.
- Separate transport-connected from application-ready state.
- Reconnect must create/reset session state intentionally; never reuse stale sequence state by accident.

## Background/relaunch claims

- Treat iOS and Wear OS background behavior as OS-managed and constrained.
- Document state restoration/reconnect capabilities separately from continuous background execution.
- Do not claim reliable background scanning, advertising, relaunch or long-lived connection until current docs and hardware evidence support the exact claim.

## ANCS boundary

For R2, Apple ANCS is an iPhone accessory notification service, not GalaxyBridge's custom transport. Parse streams defensively, request only necessary attributes, minimize persisted notification content, and only execute actions that ANCS explicitly advertises.

## Validation gate

Software build/unit tests can validate framing/state logic but cannot prove physical transport. Until owner hardware testing succeeds, final status is at most:

`PARTIAL — HARDWARE_REQUIRED`

Finish with `docs/AGENT_HANDOFF.md`, then STOP.
