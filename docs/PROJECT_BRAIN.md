# PROJECT_BRAIN — Rambler Bangla

## Goal
v29.4.1: README and GitHub Pages homepage refresh on branch `docs/readme-refresh` (from `main` 1ec05a9). Docs only:
no patcher, runtime, fingerprint or signing changes, no binaries. Also fold in the green Dependabot action bumps
(#15 checkout 7, #16 setup-java 6, #17 setup-python 7) if CI stays green. Done = branch pushed, CI green on its head.
The coordinator opens and merges the PR; the draft release `v29.4-private-1` and all tags are off limits.

## Status
- Plan and memory files written (TASKS.md, this file, AGENTS.md, CLAUDE.md).

## Next step
- Write the failing docs test (`tests/python/test_docs_homepage.py`), then refresh README.md.

## Decisions
- Docs checks live in `tests/python/` so the existing CI `unit` job runs them; stdlib only.
- Dependabot bumps applied as the same major tags Dependabot proposed (ci.yml uses tags; other workflows pin SHAs).

## Gotchas
- `tests/run_tests.sh` needs the fetched toolchain (`tools/`); do not run it locally on a shared box, rely on CI.
- README test counts (unit tests, static checks) are quoted in README, CONTRIBUTING and the site: update all when they change.

## Session log
- 2026-10-22 05:57Z claude-opus-5-5: created memory files and v29.4.1 plan; next: docs test + README.
