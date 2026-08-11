# AGENT HANDOFF — GB-M0-R2

Tool: Codex
Role: Implementation driver
Task ID: `GB-M0-R2`
Branch: `feature/gb-m0-feasibility`
Sub-agents/agent teams: FORBIDDEN

## Goal
Implement an ANCS client probe on Galaxy Watch6 that discovers iPhone ANCS, subscribes safely, parses notification events, fetches selected attributes, and exposes available actions in diagnostic UI/logs.

## Sources of truth
Use Apple's ANCS specification. Do not invent undocumented action semantics.

## Required behavior
- discover ANCS service and three standard characteristics
- authorization/error handling
- parse Notification Source events
- request attributes through Control Point / parse Data Source stream
- surface event flags and positive/negative action availability
- allow a diagnostic action only when ANCS says it is available
- robust stream reassembly/bounds checks
- redact or minimize notification content in persistent logs

## Acceptance
Build/tests may pass in software, but final status remains `PARTIAL — HARDWARE_REQUIRED` until real notification and action tests pass on iPhone 17e + Watch6.
