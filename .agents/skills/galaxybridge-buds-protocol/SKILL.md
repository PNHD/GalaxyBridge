---
name: galaxybridge-buds-protocol
description: Use for Galaxy Buds2 Pro SM-R510 protocol research, iOS GATT inventory, Wear OS RFCOMM/SPP probing, Samsung management packet framing, battery/status/ANC feasibility or simultaneous iPhone-audio plus Watch-management topology. Never use for destructive firmware/reset experiments.
---

# GalaxyBridge Buds2 Pro Protocol

## Fixed research baseline

Target: Galaxy Buds2 Pro `SM-R510`.

Pinned interoperability reference already inspected:
`timschneeb/GalaxyBudsClient@dce4735d76cd16abb818cdd96bf458efd4abef47`

Observed interface facts:
- Buds2 Pro selects the `SppNew` service family in that reference.
- first RFCOMM service UUID candidate: `2e73a4ad-332d-41fc-90e2-16bef06523f2`
- packet family uses framed binary messages with a start marker/header, message ID, payload, CRC16-CCITT and end marker.

These are research inputs, not physical-device proof.

## License / clean implementation boundary

- Do not copy/port GalaxyBudsClient implementation code into GalaxyBridge.
- Use independently implemented behavior based on necessary interface facts, current platform APIs and observed device wire behavior.
- If a future task proposes reusing external implementation code, stop and make the license strategy explicit first.

## Required decision tree

1. On iPhone, inventory every CoreBluetooth-visible Buds service/characteristic without unknown writes.
2. If a credible Samsung management GATT surface exists, classify only as `DIRECT_IOS_GATT_CANDIDATE` until hardware commands prove it.
3. Otherwise, on Watch6 use public Android RFCOMM APIs and SDP/service-UUID resolution. Do not hard-code an RFCOMM channel.
4. Connect read-only first and log bounded connection/error/raw-byte evidence.
5. Only after a specific non-destructive status request is independently understood may it be transmitted.
6. Configuration writes such as ANC require a later explicit gate after read/status stability.

## Critical physical topology gate

The Watch bridge is not viable until a real test demonstrates simultaneously:
- Buds2 Pro actively streams audio from iPhone; and
- Watch6 holds the management RFCOMM connection; and
- audio remains usable while management/status traffic works.

Source inspection, socket construction or emulator success is not this gate.

## Absolute safety stops

Never perform during M0:
- firmware flashing/downgrade/FOTA writes
- factory or pairing reset
- EEPROM/debug writes
- unknown command writes
- brute-force message IDs
- fuzzing
- rapid reconnect loops that could destabilize the accessory

If only an unsafe or unknown write could advance research, stop and report `BLOCKED`.

## Evidence

Record exact device model/firmware when available, connection state, errors, bytes received, whether iPhone audio dropped/switched and whether any decoded response is stable across repeated safe reads.

Until physical evidence exists, final status is at most `PARTIAL — HARDWARE_REQUIRED`.
Finish with `docs/AGENT_HANDOFF.md`, then STOP.
