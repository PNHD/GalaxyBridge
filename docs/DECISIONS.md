# DECISIONS

## D-001 — Feasibility before UI
Status: ACCEPTED

No polished product UI until M0 proves real transports.

## D-002 — Wear OS Data Layer is not a dependency
Status: ACCEPTED

GalaxyBridge uses an app-owned Bluetooth/network protocol because an iPhone companion cannot be treated like the normal Android paired-phone Data Layer path.

## D-003 — ANCS for iOS notification access
Status: RESEARCH-ACCEPTED / HARDWARE-PENDING

Watch6 is intended to act as the GATT client (Notification Consumer) against iOS ANCS.

## D-004 — Do not mistake Core Bluetooth BR/EDR support for RFCOMM access
Status: ACCEPTED

Apple's Core Bluetooth support for Bluetooth Classic exposes GATT over BR/EDR. It is not a general arbitrary RFCOMM/SPP socket API. Therefore Buds2 Pro direct control from iOS remains unproven unless SM-R510 exposes the needed management protocol through GATT.

## D-005 — Watch6 is the preferred SPP bridge fallback
Status: RESEARCH-ACCEPTED / HARDWARE-PENDING

Android/Wear OS exposes RFCOMM BluetoothSocket APIs, so Watch6 is the best candidate to connect to the Buds proprietary SPP management channel while iPhone retains the system audio connection.

## D-006 — GalaxyBudsClient is protocol evidence, not source code to copy
Status: ACCEPTED

GalaxyBudsClient is GPLv3. Use it to understand device behavior/protocol concepts and independently implement the required protocol unless GalaxyBridge intentionally adopts a GPL-compatible distribution model.

## D-007 — Health distribution constraint
Status: ACCEPTED

Samsung Health Sensor SDK can be developed/tested using Health Platform developer mode. Public distribution requires Samsung partner approval and registered package/signing SHA-256.

## D-008 — Physical evidence outranks software-only evidence
Status: ACCEPTED

Real iPhone/Watch/Buds tests are the acceptance authority for transport and device behavior.
