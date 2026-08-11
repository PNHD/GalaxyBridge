# Agent Skills Audit — GB-M0

Research date: 2026-08-11

Purpose: choose only skills/resources that improve GalaxyBridge M0 without bloating agent context, adding unrelated dependencies, or importing licensing risk.

## Decision policy

- `INSTALL_LOCAL`: create a GalaxyBridge repo-scoped skill based on current primary docs and project constraints.
- `REFERENCE_ONLY`: useful external material, but do not vendor/copy it.
- `DEFER`: relevant later, not for M0.
- `REJECT`: wrong stack/scope or unsafe/noisy for this project.
- No automatic third-party skill updates. Re-audit source, behavior and license before changing an installed skill.

## Installed repo-scoped skills

### galaxybridge-ble-feasibility — INSTALL_LOCAL
Targets R1/R2 and the iOS portion of R4.

Why:
- CoreBluetooth BLE/GATT role selection, permissions, MTU/write limits, reconnect/background behavior are critical M0 failure points.
- Wear OS Bluetooth behavior must be validated against current Android APIs rather than assumptions.

Primary references:
- Apple Core Bluetooth documentation/specifications
- Apple ANCS specification for R2
- Android Bluetooth documentation
- official `android/skills` and Android documentation tooling

### galaxybridge-buds-protocol — INSTALL_LOCAL
Targets R4.

Pinned interoperability research:
- `timschneeb/GalaxyBudsClient@dce4735d76cd16abb818cdd96bf458efd4abef47`
- SM-R510 maps to `SppNew`
- first RFCOMM service UUID candidate: `2e73a4ad-332d-41fc-90e2-16bef06523f2`

Boundary:
- source is useful for interface/wire research, but implementation code is not vendored.
- read-only hardware probing precedes any known-safe write.

### galaxybridge-health-bridge — INSTALL_LOCAL
Targets R3.

Primary references:
- Samsung Health Sensor SDK documentation
- Wear OS Health Services documentation
- Apple HealthKit documentation

Boundary:
- raw/capability sensor access is not equivalent to Samsung Health Monitor medical/certified functionality.
- no medical claims.

## External skill/resource audit

### `dpearson2699/swift-ios-skills` / `core-bluetooth` — REFERENCE_ONLY
Actual SKILL.md was inspected, not only its title. It covers central/peripheral GATT, scan/connect/discovery, read/write/notify, write-flow control, background BLE and state restoration. Its extended BLE reference also covers reconnection, parsing, L2CAP and peripheral request handling.

Reason not vendored: repository license is PolyForm Perimeter 1.0.0, not a simple permissive MIT/Apache license. GalaxyBridge instead keeps a clean project-local workflow grounded in primary platform docs.

### `dpearson2699/swift-ios-skills` / `healthkit` — REFERENCE_ONLY
Actual file was inspected. Good coverage of HealthKit capabilities/entitlements, authorization privacy semantics, sample writes, queries and device-only background behavior. Same license boundary as above.

### `dpearson2699/swift-ios-skills` / `background-processing` — DEFER
Useful for BGTaskScheduler/background jobs, but it is not a substitute for CoreBluetooth background semantics. Installing it during M0 could encourage the wrong mechanism for persistent BLE behavior.

### `dpearson2699/swift-ios-skills` / `accessorysetupkit` — DEFER
Potentially useful for a later privacy-preserving accessory setup flow. Do not assume Samsung Watch/Buds can use it until the physical pairing architecture is known.

### `dpearson2699/swift-ios-skills` / `audioaccessorykit` — DEFER
Potentially relevant to future audio-accessory integration, but it does not establish arbitrary Buds2 Pro Samsung management protocol access. R4 transport feasibility comes first.

### official `android/skills` — REFERENCE_ONLY / selective later install
Repository is official Android guidance and Apache-2.0. Current catalog contains Android CLI, testing, Wear Compose Material 3 and other targeted skills, but no dedicated Bluetooth/RFCOMM skill was found in the current catalog.

Useful now:
- `devtools/android-cli`: authoritative Android docs search, SDK/device/run tooling when available on the developer machine.

Deferred:
- `testing/testing-setup`: actual skill is broad and can introduce Hilt/Jacoco/Robolectric/screenshot infrastructure. M0 needs focused protocol/state tests plus real-device gates, not a large test-stack migration.
- Wear Compose/UI skills: defer until product UX work.

### `openai/skills` / `security-best-practices` — REJECT for current code stack
Actual skill currently scopes its detailed guidance to Python, JavaScript/TypeScript and Go, not Swift/Kotlin. Do not install merely because it is first-party.

### generic React Native / Flutter mobile skills — REJECT
GalaxyBridge is native Swift/SwiftUI + Kotlin/Wear OS. Cross-platform framework skills add wrong-stack guidance.

### generic mobile QA/Appium skills — DEFER
Potentially useful after core transport works. M0's highest-value validation is physical Bluetooth topology and hardware evidence, which the project test plan already defines more specifically.

## skills.sh policy

skills.sh is useful for discovery and popularity signals, not authority. Its own documentation says install ranking is based on anonymous install telemetry and that listed skills are routinely audited but quality/security cannot be guaranteed. Every external skill must therefore be inspected before adoption.

## Agent-specific locations

- Codex: `.agents/skills/<skill>/SKILL.md`
- Claude Code: `.claude/skills/<skill>/SKILL.md`
- Repository operating contract: `AGENTS.md`
- Claude-specific contract: `CLAUDE.md`

The GalaxyBridge skills intentionally stay concise so automatic skill discovery does not crowd the agent context.
