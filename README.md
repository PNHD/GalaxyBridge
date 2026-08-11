# GalaxyBridge

Make Galaxy Watch6 and Galaxy Buds2 Pro first-class accessories on iPhone by reproducing every technically accessible Samsung feature and explicitly documenting platform-level blockers.

## Current phase

`GB-M0 — Hardware Feasibility`

No product UI work is allowed before transport feasibility is proven on real hardware.

## Target hardware

- iPhone 17e / current iOS
- Samsung Galaxy Watch6 / Wear OS
- Samsung Galaxy Buds2 Pro (SM-R510)

## Product architecture under investigation

1. **Primary:** iPhone app ↔ Galaxy Watch6 app over BLE/GATT.
2. **Notifications:** Watch6 acts as an Apple Notification Center Service (ANCS) client.
3. **Health:** Watch6 reads sensors locally, sends normalized measurements to iPhone, iPhone writes supported data to HealthKit.
4. **Buds preferred path:** direct iPhone control only if the Buds expose a usable GATT management surface.
5. **Buds fallback path:** Watch6 opens Samsung SPP/RFCOMM management connection to Buds2 Pro and relays commands/state to iPhone over BLE.

## Critical rule

A build, emulator, mock payload, accepted packet, or UI state is **not** feature success. Real-device evidence is required.

See `docs/CURRENT_WORK.md`.
