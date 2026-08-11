# BUDS2 PRO PROTOCOL RESEARCH NOTES

## Device
Galaxy Buds2 Pro — SM-R510

## Transport
GalaxyBudsClient describes Galaxy Buds configuration as proprietary binary data over Bluetooth SPP/RFCOMM, separate from A2DP audio.

For `Buds2ProDeviceSpec`, the project selects `Uuids.SppNew`. Current source defines:

`2e73a4ad-332d-41fc-90e2-16bef06523f2`

This is now the first exact RFCOMM service UUID candidate for the Watch6 hardware probe. The RFCOMM channel should be resolved through SDP from the service UUID rather than assumed/hardcoded.

## High-value command/state families observed in GalaxyBudsClient protocol enums
These are research targets only; support must be confirmed against SM-R510/current firmware.

- status / extended status
- ANC with one earbud
- noise control update/control
- touch-and-hold noise controls
- conversation detection and duration
- spatial audio
- ambient amplification/mode/volume
- noise reduction level
- equalizer
- touchpad lock and touch settings
- battery type/status-related messages
- fit test
- Find My Earbuds start/stop
- self-test
- seamless connection
- FOTA command family
- spatial sensor/control data
- adaptive volume family

## Framing
GalaxyBudsClient's protocol implementation uses framed messages with a start marker/header, message identifier, payload, CRC16-CCITT and end marker. A clean-room implementation should be driven by captured packets and device behavior, not copied GPL source.

## License boundary
GalaxyBudsClient is GPLv3. Do not paste/port its implementation into a proprietary source tree. Decide early whether GalaxyBridge will be GPL-compatible or use an independently implemented protocol layer based on observed wire behavior and necessary interface facts.
