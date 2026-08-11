# MILESTONES

## GB-M0 — Hardware Feasibility
Exit only when critical transports have real-device evidence.

- R1 iPhone ↔ Watch6 BLE/GATT transport
- R2 ANCS notification bridge
- R3 health sensor → iPhone/HealthKit minimum path
- R4 Buds2 Pro management transport

## GB-M1 — Companion Core
- device onboarding
- Watch connection lifecycle
- connection diagnostics
- notification presentation/actions
- media/call experiments where platform permits

## GB-M2 — Health Bridge
- capability matrix
- heart rate/activity/workouts
- supported HealthKit mapping
- optional Samsung sensor extensions
- sleep feasibility investigation
- explicit distinction between wellness data and regulated Samsung Health Monitor functions

## GB-M3 — Buds2 Pro Manager
Subject to M0-R4 PASS.

Candidate controls to validate by real packets:
- left/right/case battery where reported
- ANC
- Ambient mode / ambient volume
- noise control levels
- EQ
- touch lock / touch settings
- touch-and-hold noise controls
- conversation detection
- seamless connection setting
- fit test
- Find My Earbuds command path
- spatial audio/head-tracking feasibility
- firmware metadata (read-only initially)

## GB-M4 — Reliability
- background/reconnect behavior
- state restoration
- transport retry policy
- battery/CPU profiling
- crash recovery
- diagnostics export

## GB-M5 — Product UX
Only after behavior is stable.

- iPhone device cards
- Watch controls/status
- Buds quick controls
- Health sync settings
- diagnostics and unsupported-feature messaging

## GB-M6 — Personal Release / Distribution Decision
- signed iOS build
- signed Wear OS build
- installation/update process
- regression matrix
- decide personal sideload vs public distribution
- Samsung Health Sensor SDK partnership decision if public distribution is desired
- App Store review constraints assessment
