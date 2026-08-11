# BUDS2 PRO PROTOCOL RESEARCH NOTES

## Device
Galaxy Buds2 Pro — SM-R510

## Upstream evidence snapshot
Verified against `timschneeb/GalaxyBudsClient` commit:

`dce4735d76cd16abb818cdd96bf458efd4abef47`

Pin this commit in future forensic comparisons so upstream changes do not silently rewrite the M0 baseline.

## Transport
GalaxyBudsClient models Galaxy Buds configuration as proprietary binary data over Bluetooth SPP/RFCOMM, separate from normal audio profiles.

At the pinned snapshot, `Buds2ProDeviceSpec`:
- identifies the model as `Models.Buds2Pro`
- uses base name `Buds2 Pro`
- selects `Uuids.SppNew` as `ServiceUuid`
- advertises explicit feature rules for ANC, Ambient Sound, Noise Control, conversation detection, case battery, seamless connection, spatial sensor/head tracking, fit test, advanced touch lock and other families

`Uuids.SppNew` is defined as:

`2e73a4ad-332d-41fc-90e2-16bef06523f2`

This is the first exact RFCOMM service UUID candidate for the Watch6 hardware probe. The RFCOMM channel MUST be resolved through SDP/service discovery from the UUID rather than guessed or hard-coded.

## High-value command/state families
Research targets for SM-R510/current firmware include:

- status / extended status
- ANC / noise-control state and control
- noise controls with one earbud
- Ambient Sound / ambient customization
- touch-and-hold noise controls
- conversation detection
- case battery / charging state
- seamless connection
- equalizer
- touch lock / advanced touch settings
- fit test
- spatial sensor / head tracking
- Find My Earbuds-style ringing path
- firmware metadata/update families — READ-ONLY in M0

Feature declarations in an open-source client are protocol evidence only. They are NOT GalaxyBridge acceptance evidence until verified against the owner's physical Buds2 Pro and current firmware.

## Framing evidence
At the pinned snapshot, `SppMessage` models packets with:

- start-of-message byte
- two-byte modern header for non-legacy devices
- one-byte message ID
- payload
- two-byte CRC
- end-of-message byte

The encoder computes `crc16_ccitt(messageId, payload)`. The decoder validates size, CRC, SOM and EOM, and includes fragmentation/request-response bits in the modern header.

GalaxyBridge MUST implement only the minimum framing needed for read-only discovery first. Do not copy GPL implementation code. Build a clean-room protocol layer from necessary interface facts plus captured wire/device behavior.

## R4 hardware sequence
1. Inventory all CoreBluetooth-visible services/characteristics from SM-R510 on iPhone.
2. After complete physical evidence, classify the iOS result as `DIRECT_IOS_GATT_CANDIDATE`, `NO_USABLE_GATT_OBSERVED`, or `HARDWARE_REQUIRED`.
3. On Watch6, query bonded/visible device UUIDs and resolve `2e73a4ad-332d-41fc-90e2-16bef06523f2` through SDP.
4. Open RFCOMM read-only first.
5. Capture bounded incoming bytes without sending any command.
6. Keep Buds actively streaming audio from iPhone while Watch holds management connection.
7. Only after a separate PM review establishes a high-confidence, non-destructive status packet may one status query be implemented.

## R4 diagnostic implementation

The iOS diagnostic independently implements CoreBluetooth discovery. It scans without a service filter because the physical GATT surface is unknown, requires explicit owner selection, discovers all services and characteristics, reads only characteristics advertising `read`, subscribes only to `notify`/`indicate`, records per-characteristic properties and peripheral write-size limits, bounds captured values, and exports a report without the CoreBluetooth peripheral identifier.

The Watch6 diagnostic enumerates bonded devices after `BLUETOOTH_CONNECT` permission, requires explicit owner selection, and calls Android's public `createRfcommSocketToServiceRecord(UUID)` API with `2e73a4ad-332d-41fc-90e2-16bef06523f2`. SDP resolves the channel; no channel number is present in code. The probe obtains only the socket input stream, retains at most 4096 recent bytes, tracks total bytes, and has no automatic reconnect.

No status query is implemented. The current research documents framing shape but not a sufficiently justified exact read-only message ID and packet construction. Therefore `commands_sent` remains exactly zero and no protocol parser is claimed.

## Safety
- no firmware flash/downgrade
- no factory reset
- no unknown message IDs
- no destructive writes
- no status or ANC command in the R4 diagnostic implementation
- no automatic reconnect loop
- redact device addresses/identifiers in committed traces

## License boundary
GalaxyBudsClient is GPLv3. Do not paste/port its implementation into a proprietary source tree. Decide early whether GalaxyBridge will be GPL-compatible or use an independently implemented protocol layer based on observed wire behavior and necessary interface facts.
