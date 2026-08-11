# GB-M0-R4 OWNER HARDWARE RUNBOOK

## Current boundary

Status before this run: `PARTIAL — HARDWARE_REQUIRED`.

This diagnostic sends no RFCOMM command bytes and performs no unknown GATT writes. It cannot read status on demand. Do not infer `WATCH_RFCOMM_BRIDGE` from an app build, a socket object, or an emulator result.

## Record before testing

- test timestamp and timezone
- GalaxyBridge commit SHA
- iPhone model and exact iOS build
- Galaxy Watch6 model and exact Wear OS / One UI Watch build
- Buds2 Pro model (`SM-R510`) and exact firmware build
- whether the Buds were already bonded to both iPhone and Watch

Do not include Bluetooth addresses, Apple IDs, Samsung account identifiers, or unrelated device identifiers in returned evidence.

## Build and install

### iPhone diagnostic

1. On a Mac with current Xcode, open `ios/GalaxyBridgeBudsDiagnostics/GalaxyBridgeBudsDiagnostics.xcodeproj`.
2. Select the `GalaxyBridgeBudsDiagnostics` scheme and the physical iPhone.
3. Set the owner's development team/signing identity locally; do not commit signing identifiers.
4. Run the app on the iPhone and grant Bluetooth permission.

### Watch6 diagnostic

1. Open `wear/` in Android Studio with JDK 17 and Android SDK 35 installed.
2. Build the `app` debug variant and deploy it to the physical Galaxy Watch6.
3. Grant Nearby devices / Bluetooth connection permission when prompted.
4. Do not test acceptance on an emulator.

## Part A — iOS GATT inventory

1. Put both earbuds in-ear or otherwise keep the target powered and visible without resetting them.
2. Confirm normal iPhone audio pairing remains intact.
3. Open **Buds GATT Inventory** and tap **Scan**.
4. Wait at least 30 seconds. If no Buds-like peripheral appears, stop the scan and record that exact result; do not repeatedly reconnect or reset the Buds.
5. If a likely target appears, verify its displayed product name and select it explicitly. Do not select an ambiguous peripheral.
6. Wait for service and characteristic discovery to settle.
7. Review the full service/characteristic inventory. The app may read only characteristics advertising `read` and subscribe only to `notify`/`indicate`; it performs no characteristic value writes.
8. Set the evidence classification to exactly one of:
   - `DIRECT_IOS_GATT_CANDIDATE` only when a plausible management GATT surface is physically observed;
   - `NO_USABLE_GATT_OBSERVED` when a completed physical inventory exposes no usable management surface;
   - `HARDWARE_REQUIRED` when the scan/inventory is incomplete or ambiguous.
9. Tap **Copy** or **Export** and save the redacted report.

CoreBluetooth discovery does not prove arbitrary RFCOMM/SPP access.

## Part B — critical simultaneous topology test

Prepare bonding before the timed run: the Buds must appear in the Watch's bonded-device list, but the iPhone must own the active audio connection when the run starts.

1. Close both diagnostic apps.
2. Connect the Buds to the iPhone and ensure the Watch is not the active audio source.
3. Start a continuous music track on the iPhone.
4. Listen through the Buds for at least 30 seconds and record that audio is stable.
5. Start one continuous screen recording covering the iPhone playback state and another covering the Watch probe if practical.
6. On Watch6, open **Buds RFCOMM Probe** and tap **Refresh bonded devices**.
7. Verify the selected row says `LIKELY SM-R510` and matches the expected name, Bluetooth type, bond state, masked address suffix, and cached UUID metadata. If identification is ambiguous, stop without connecting.
8. Select the target explicitly.
9. While iPhone music is still playing, tap **Connect** once. Do not run a rapid retry loop.
10. Observe the Watch report and the iPhone/Buds behavior for 60 seconds without sending any other Buds control.
11. Record whether RFCOMM established, its connection time, any exception, total bytes received, and bounded raw bytes.
12. Record whether iPhone audio continued uninterrupted and whether the Buds switched source unexpectedly.
13. Tap **Disconnect** once.
14. Continue listening for 30 seconds and record whether iPhone audio remains operational.
15. Tap **Copy report** or **Share report** and save the redacted Watch report.

No status request exists in this R4 implementation, so a valid status response cannot yet be claimed.

## Evidence the owner must return

- the completed pre-test device/OS/firmware/build record
- exported iOS GATT report
- exported Watch RFCOMM report
- screen recordings or timestamped observation notes
- exact answer to each question:
  - A. Did Watch RFCOMM connect?
  - B. Did iPhone audio continue uninterrupted?
  - C. Did the Buds switch Bluetooth source unexpectedly?
  - D. Did Watch receive any bytes? If yes, how many and what bounded hex was logged?
  - E. Was a status query sent and validated? Expected answer for this build: `NO — NOT IMPLEMENTED`.
  - F. After Watch disconnect, did iPhone audio remain operational?
- any exception type/message after confirming the report redacted Bluetooth addresses

## Classification gate

Do not select `DIRECT_IOS_GATT`, `WATCH_RFCOMM_BRIDGE`, or `BLOCKED` until the physical evidence is reviewed. In particular, `WATCH_RFCOMM_BRIDGE` requires simultaneous iPhone A2DP audio plus a stable Watch RFCOMM management connection on the owner's hardware.
