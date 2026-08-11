---
name: galaxybridge-health-bridge
description: Use for Galaxy Watch6 health/sensor capability work, Samsung Health Sensor SDK, Wear OS Health Services, transfer of normalized measurements to iPhone, or Apple HealthKit authorization/writes. Do not use to claim Samsung Health Monitor medical parity.
---

# GalaxyBridge Health Bridge

## Goal

Prove a truthful minimum path:

Galaxy Watch6 measurement → GalaxyBridge Watch app → GalaxyBridge transport → iPhone app → HealthKit write.

R3 minimum target is heart rate. Extend to steps/workouts, SpO2, BIA, ECG/raw sensor access or skin temperature only after capability and SDK constraints are verified on the real Watch6.

## Source hierarchy

Before implementation, check current:
1. Samsung Developer Health Sensor SDK documentation for Samsung-specific sensor access and distribution/developer-mode constraints.
2. Android/Wear OS Health Services documentation for standard Wear health/workout data.
3. Apple HealthKit documentation for iPhone authorization, data types, units and writes.

Do not infer capability from another watch model or an old SDK version.

## Capability boundaries

- Distinguish `sensor/raw-data access`, `health/fitness metric`, and `regulated/certified medical feature`.
- Access to an ECG/BIA/SpO2-related sensor API does not prove Samsung Health Monitor workflow parity or regulatory equivalence.
- No medical diagnosis or certification claims.
- Treat unsupported/permission-denied/partner-restricted states as explicit product limitations, not failures to hide.

## HealthKit rules

- Check HealthKit availability.
- Request only the exact data types required by the feature.
- Distinguish read privacy behavior from write/share authorization.
- Normalize units and timestamps before creating samples.
- A write is successful only after HealthKit reports save success; do not infer persistence from UI state.
- Never fabricate values to make an end-to-end test pass. Test fixtures must be clearly marked and must not be mistaken for physical measurements.

## Transport/data integrity

Every transferred measurement should carry enough metadata to audit:
- metric/type
- value and unit
- measurement timestamp
- source/device/session identity at the minimum necessary granularity
- quality/status flags exposed by the source API when relevant

Avoid storing raw health data in verbose diagnostic logs.

## Validation gate

Software tests may validate serialization/unit conversion and HealthKit construction, but the Watch sensor path requires real-device evidence.

Until real Watch6 + iPhone evidence proves the path, final status is at most:
`PARTIAL — HARDWARE_REQUIRED`

Finish with `docs/AGENT_HANDOFF.md`, then STOP.
