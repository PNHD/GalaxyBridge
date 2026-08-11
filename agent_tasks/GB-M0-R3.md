# AGENT HANDOFF — GB-M0-R3

Tool: Codex
Role: Implementation driver
Task ID: `GB-M0-R3`
Branch: `feature/gb-m0-feasibility`
Sub-agents/agent teams: FORBIDDEN

## Goal
Prove Galaxy Watch6 heart-rate measurement → GalaxyBridge transport → iPhone HealthKit write.

## Constraints
- capability-check before tracker use
- Samsung Health Sensor SDK developer mode is development-only
- request minimum permissions
- HealthKit permissions must be explicit
- measurements are wellness/fitness data; no medical claims
- do not fabricate unsupported Watch6 tracker capabilities

## Acceptance
- Watch capability detection implemented
- heart rate captured on hardware
- sample crosses app transport
- authorized HealthKit write succeeds and is auditable
- unsupported/denied states handled

Final status remains hardware-dependent.
