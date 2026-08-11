# GalaxyBridge Agent Contract

This file is repository-wide source-of-truth guidance for coding agents.

## Read before work

Read the relevant task/issue plus, at minimum:
- `README.md`
- `docs/CURRENT_WORK.md`
- `docs/DECISIONS.md`
- `docs/M0_TEST_PLAN.md`
- the matching file under `agent_tasks/`
- `docs/AGENT_HANDOFF.md`

If these disagree with an older prompt or comment, stop and report the conflict instead of guessing.

## Work discipline

- One task, one writer, one declared branch/worktree at a time.
- Verify repository, branch, HEAD and `git status --short` before edits.
- Do not merge unless the owner/ChatGPT PM explicitly instructs it.
- Do not start the next task or milestone automatically.
- Do not rewrite unrelated files or add speculative architecture.
- Prefer minimal diagnostic code during `GB-M0`; polished product UI is out of scope.
- Run only tests/builds that actually exist; report unavailable validation plainly.

## Evidence rules

- Real-device evidence outranks emulator, mock, build and agent claims.
- Hardware-dependent work may be at most `PARTIAL — HARDWARE_REQUIRED` until the owner performs the prescribed physical test.
- Never fabricate Bluetooth state, sensor output, notification actions, codec support, background behavior or hardware results.
- Record exact commands actually run and exact failures.

## Platform research

- Platform/API behavior that may have changed must be checked against current primary documentation before implementation.
- For Apple behavior, prefer Apple developer documentation/specifications.
- For Android/Wear OS behavior, prefer `developer.android.com`, official Android skills/CLI and official samples.
- For Samsung health capabilities, prefer Samsung Developer documentation.
- Third-party repos are interoperability/research references, not authority over current OS behavior.

## Bluetooth / Buds safety

- Do not use Wear OS Data Layer as the iPhone↔Watch transport.
- Do not treat CoreBluetooth as arbitrary RFCOMM/SPP access.
- For Buds2 Pro: read-only first. No firmware flashing/downgrade, factory/pairing reset, FOTA writes, fuzzing, brute-force message IDs or unknown/destructive writes.
- Do not copy GPL or other restrictively licensed implementation code into GalaxyBridge. Interface facts may be documented; implementation must respect the project license strategy.

## Agent skills

Codex repo skills live under `.agents/skills/`; Claude project skills live under `.claude/skills/`. Use the matching GalaxyBridge skill when the task concerns BLE transport, Buds protocol or health bridging.

## PM handoff

Every implementation or review task must finish with the complete handoff format in `docs/AGENT_HANDOFF.md`, then STOP.
