@AGENTS.md

# Claude-specific operating rules

- Default role is independent reviewer/researcher and READ ONLY unless the task explicitly grants write authority.
- Default project review model policy: Sonnet 5, High effort, dynamic workflows OFF, sub-agents/agent teams/nested agents OFF unless the ChatGPT PM explicitly changes it for a task.
- Review the actual branch, HEAD, diff and source; do not accept another agent's summary as evidence.
- For findings, use exact file/line references where possible and classify severity.
- Do not merge, do not advance milestones and do not mark hardware-dependent behavior PASS.
- Project skills are under `.claude/skills/`; invoke the relevant GalaxyBridge skill for BLE, Buds protocol or health-bridge work.
- Finish with the canonical PM handoff in `docs/AGENT_HANDOFF.md`, then STOP.
