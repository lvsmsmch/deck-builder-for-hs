# Project rules for Claude Code

You are working autonomously on this project. The user is not watching in real-time — they'll check back later.

## Progress discipline

- After every meaningful unit of work (a phase, a feature, a fix): update `PROGRESS.md` in the repo root, then `git commit -am "phase N: <summary>"`.
- `PROGRESS.md` format: a checklist of phases with `[x]` done, `[ ]` pending, `[~]` in progress. Brief notes allowed.
- At session start: first command is `cat PROGRESS.md` (or create it if absent). Then `cat PLAN.md` if exists. Resume from first `[ ]` or `[~]` line.

## Autonomy via CONTINUE

Before ending your turn, check `PROGRESS.md`.

If `PROGRESS.md` has any `[ ]` or `[~]` items AND you are not asking the user a question:

1. Update `PROGRESS.md` (mark current phase done)
2. `git add -A && git commit -m "phase N: <summary>"`
3. End your turn with a line containing exactly: `<<CONTINUE>>`

The backend will reset your context and re-invoke you.

- If you need user input (ambiguous requirement, external resource, judgment call): ask normally, no marker.
- If all phases in `PROGRESS.md` are `[x]`: say "all done" (or equivalent), no marker. Work is finished.
- `CONTINUE` is forbidden if you did not commit this turn. Silently ending a turn with unchecked phases and no question is a BUG — always emit `CONTINUE` or ask.

## When to stop and ask

- Ambiguous requirement (pick colors, pick API, pick flow)
- External resource needed (API key, credential, design asset)
- Build error that needs human judgment

Emit a plain question, no `CONTINUE` marker.

## Build

- Android: use existing `gradlew` in repo. Debug builds only unless asked.
- Don't run tests unless the user asks.

## Git hygiene

- Small frequent commits beat large ones.
- Never commit secrets, `.env` files, local keystores, `build/` dirs.
