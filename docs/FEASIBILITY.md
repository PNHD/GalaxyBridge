# FEASIBILITY BASELINE — 2026-08-11

## Watch6 ↔ iPhone

### Strong path: BLE/GATT application transport
A custom Wear OS app and iOS app can implement an app-owned GATT protocol. Background behavior must be measured rather than assumed.

### Strong path: iOS notifications via ANCS
Apple ANCS is explicitly designed so a Bluetooth accessory acting as a GATT client can receive iOS Notification Center events, retrieve notification/app attributes, and perform predetermined notification actions when offered by iOS.

ANCS UUIDs:
- Service: `7905F431-B5CE-4E99-A40F-4B1E122D00D0`
- Notification Source: `9FBF120D-6301-42D9-8C58-25E699A21DBD`
- Control Point: `69D1D8F3-45E1-49A8-9821-9BBDFDAAD9D9`
- Data Source: `22EAC6E9-24D6-4BB5-BE44-B36ACE7C7BFB`

ANCS is not guaranteed to always be published, and access requires authorization. Real-device lifecycle testing is mandatory.

## Watch6 health

Samsung Health Sensor SDK supports Galaxy Watch4 and later and currently exposes capabilities including accelerometer, heart rate/IBI, PPG, skin temperature, BIA/MF-BIA, ECG, and SpO2 subject to device/software capability checks.

Development/testing can use Health Platform developer mode. Public distribution requires Samsung partnership/registration.

## Buds2 Pro

### Known protocol shape
GalaxyBudsClient's reverse-engineering documents Galaxy Buds audio over A2DP and proprietary configuration/firmware/commands over SPP/RFCOMM.

### Important iOS correction
Core Bluetooth 'Classic' support does not mean arbitrary RFCOMM sockets. Apple's WWDC19 material describes this feature as transparent **GATT over BR/EDR**. Therefore the direct iPhone manager path remains conditional on discovering a suitable Buds GATT service.

### Watch bridge hypothesis
Android exposes RFCOMM BluetoothSocket APIs and service-record based connections. A Wear OS app therefore has a plausible low-level transport to the Buds management channel.

Unknown requiring hardware proof:
- exact SM-R510 RFCOMM service UUID/channel on current firmware
- whether Watch6 can establish management SPP while iPhone simultaneously owns A2DP/HFP
- whether current Buds2 Pro firmware accepts all desired command IDs in that topology

## Current Buds2 Pro firmware research baseline
The public Galaxy Buds firmware archive lists SM-R510 builds through `R510XXU0AZD1` (April 2026). Do not assume protocol behavior from older firmware alone.

## Research sources

Primary/near-primary references used for baseline:
- Apple Developer — ANCS specification
- Apple Developer — WWDC19 “What's New in Core Bluetooth” / Using Core Bluetooth Classic
- Android Developers — BluetoothSocket / createRfcommSocketToServiceRecord
- Samsung Developer — Samsung Health Sensor SDK overview, process, developer mode
- timschneeb/GalaxyBudsClient — protocol reverse-engineering implementation, GPLv3
- timschneeb/galaxy-buds-firmware-archive — SM-R510 firmware history
