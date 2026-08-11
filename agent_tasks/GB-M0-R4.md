# AGENT HANDOFF — GB-M0-R4

Tool: Research first; Codex only after protocol inventory
Role: Critical feasibility spike
Task ID: `GB-M0-R4`
Branch: `feature/gb-m0-feasibility`
Sub-agents/agent teams: FORBIDDEN

## Goal
Determine whether Galaxy Buds2 Pro can be managed while audio remains connected to iPhone, preferring direct iPhone GATT only if genuinely available, otherwise using Galaxy Watch6 as RFCOMM/SPP bridge.

## Phase A — Read-only research
- inspect current GalaxyBudsClient protocol architecture and SM-R510 model support
- identify the exact service UUID/channel acquisition path used for Buds2 Pro
- enumerate non-destructive status messages and ANC command/state messages
- document framing/checksum behavior
- do not copy GPL source into project code

## Phase B — iPhone service inventory
- inventory CoreBluetooth-visible Buds services/characteristics
- explicitly classify direct path as PASS or NO_USABLE_GATT
- do not call Core Bluetooth Classic support an RFCOMM API

## Phase C — Watch RFCOMM PoC
- use Android Bluetooth RFCOMM APIs
- connect read-only first
- obtain/decode status
- only then implement one ANC toggle

## Critical topology gate
With Buds streaming audio from iPhone:
- Watch management socket connects and stays connected
- battery/status can be read
- ANC toggle changes acoustic behavior and returned state
- iPhone audio is not broken

Do not flash firmware, reset Buds, or send unknown/destructive messages.

## Return
- PASS/PARTIAL/BLOCKED/FAILED
- exact service UUID/channel evidence
- packet traces with sensitive identifiers redacted
- device firmware version
- simultaneous-topology result
- recommendation: DIRECT_IOS_GATT / WATCH_RFCOMM_BRIDGE / BLOCKED
