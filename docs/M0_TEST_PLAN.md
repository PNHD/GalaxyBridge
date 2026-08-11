# GB-M0 REAL-HARDWARE TEST PLAN

## Test devices
- iPhone 17e
- Galaxy Watch6
- Galaxy Buds2 Pro SM-R510

Record exact OS/firmware/build numbers before every acceptance run.

## Evidence contract
For each test capture:
- timestamp
- device/OS/firmware versions
- app commit SHA
- steps performed
- raw app logs
- observed device behavior
- PASS/PARTIAL/BLOCKED/FAILED
- short screen recording when behavior is visual

## R1 — BLE transport
1. Cold-start both apps.
2. Discover and pair/authorize.
3. iPhone → Watch payload with monotonically increasing sequence.
4. Watch → iPhone acknowledgement and reverse payload.
5. Measure 100 ping/ack samples.
6. Kill/relaunch foreground app cases.
7. Lock iPhone; test background delivery/reconnect.
8. Move devices out of range, return, verify recovery.
9. Reboot Watch and iPhone separately; verify expected recovery path.

PASS requires reliable bidirectional real-device transport and documented background limitations.

## R2 — ANCS
1. Pair/authorize Watch as ANCS consumer.
2. Confirm service discovery and characteristics.
3. Trigger notifications from at least Messages, Gmail and one third-party messaging app.
4. Verify add/update/remove events.
5. Fetch title/message/app identifier where iOS exposes them.
6. Record EventFlags and available actions.
7. Execute at least one exposed action and verify iPhone-side result.
8. Trigger incoming call and record category/actions.

Do not count answer/decline support unless actually exposed and verified on device.

## R3 — Health
1. Enable Samsung Health Platform developer mode for development test only.
2. Capability-check trackers.
3. Acquire heart rate.
4. Send normalized sample to iPhone.
5. Request HealthKit authorization.
6. Write a clearly tagged test sample and read it back where permitted.
7. Probe other target tracker capabilities without assuming support.

## R4 — Buds critical spike
### A. iPhone inventory
1. Pair Buds2 Pro normally with iPhone for audio.
2. Inventory CoreBluetooth-visible services/characteristics.
3. Determine whether any usable Samsung management GATT service exists.
4. Do not infer RFCOMM access from BR/EDR pairing.

### B. Watch RFCOMM inventory
1. Pair/authorize Watch for Bluetooth device discovery as required.
2. Discover Buds bonded/visible state and SDP UUIDs.
3. Identify candidate SPP service record.
4. Open RFCOMM socket.
5. Decode status packets without sending destructive commands.

### C. Simultaneous topology gate
1. Keep Buds audio actively streaming from iPhone.
2. Open/hold Watch management connection.
3. Read stable status/battery.
4. Toggle ANC once.
5. Verify acoustic mode changed.
6. Verify returned state changed.
7. Confirm iPhone audio remains usable.

PASS only if simultaneous topology works. If it fails, capture exact failure mode before trying alternate connection ownership.

## Safety
- No firmware flashing/downgrading in M0.
- No factory-reset commands unless separately approved.
- Prefer read-only protocol discovery before control writes.
