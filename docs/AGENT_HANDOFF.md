# PM HANDOFF — REQUIRED

Use this exact structure at the end of every implementation/review task. Do not merely say `done`.

## TASK ID

## FINAL STATUS
Use exactly one when applicable:
- `PASS`
- `PARTIAL — HARDWARE_REQUIRED`
- `CHANGES_REQUIRED`
- `BLOCKED`
- `FAILED`

## REPOSITORY

## BRANCH

## BASELINE HEAD

## FINAL HEAD SHA

## COMMITS CREATED
For each commit:
- SHA
- message

## FILES CHANGED
Exact repository paths.

## WHAT WAS IMPLEMENTED / REVIEWED
Concrete scope only.

## ARCHITECTURE / TECHNICAL DECISIONS
- decisions made
- alternatives rejected
- rationale

## COMMANDS ACTUALLY RUN
Exact commands. Never list commands that were not executed.

## BUILD RESULTS
Exact pass/fail/skipped/unavailable evidence.

## TEST RESULTS
Counts and failures where available.

## UNVALIDATED ITEMS
Anything requiring another OS/tool/device or inferred rather than verified.

## HARDWARE EVIDENCE
- what was physically tested
- what was not physically tested
- device/OS/firmware details when known

Never fabricate physical-device evidence.

## KNOWN ISSUES / RISKS

## REVIEWER ATTENTION
Exact files/functions/claims the PM or reviewer should inspect carefully.

## OWNER ACTION REQUIRED
Exact owner steps, if any.

## NEXT RECOMMENDED TASK
Recommendation only. Do not start it.

## GIT STATUS
Include `git status --short` or equivalent and state whether the working tree is clean.

---

After this handoff: **STOP**.

Do not merge. Do not change milestone. Do not begin the next task. Do not promote hardware-dependent functionality to PASS without owner evidence.
