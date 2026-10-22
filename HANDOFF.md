# HANDOFF — v29.4.1 README and homepage refresh

Branch `docs/readme-refresh` from `main` 1ec05a9. Docs only. The coordinator opens and merges the PR; nothing here
touches `main`, any tag or the draft release `v29.4-private-1`.

## What changed

| Commit | Change |
| --- | --- |
| `chore: project memory and v29.4.1 task list` | `AGENTS.md`, `CLAUDE.md`, `docs/PROJECT_BRAIN.md`, `TASKS.md` |
| `test: homepage docs checks` | `tests/python/test_docs_homepage.py`, 14 stdlib tests (written first, 8 red) |
| `docs: README homepage refresh` | `README.md` rewritten; `docs/README.md` lists the V29.4 notes |
| `docs(site): restyle landing page` | `docs/index.html` |
| `ci: bump checkout 7, setup-python 7, setup-java 6` | `.github/workflows/ci.yml` only |
| `docs: v29.4.1 changelog entry and skills record` | `CHANGELOG.md`, `SKILLS_USED.md` |
| `docs: address review of the homepage refresh` | Two overstated claims corrected, dropped content restored, tests tightened |
| `docs(user-guide): exit 1 during repack ...` | `docs/USER_GUIDE.md` exit-code row corrected |
| `docs: handoff ...` | `HANDOFF.md`, `TASKS.md`, `docs/PROJECT_BRAIN.md` |

Untouched: `standalone-patcher/`, `extension-src/`, `fingerprints/`, `rename/`, `tools-src/`, signing workflows and
every binary asset. `git diff --stat origin/main..HEAD` lists only docs, the new test, `ci.yml` and the memory files.

## Verification

- Local: `python3 -m unittest discover -s tests/python` ran 40 tests, OK. The static suite was not run locally
  (shared machine rule); CI runs it.
- Site: headless renders at 390px and 1280px checked; no horizontal scroll, AA contrast on every token pair
  (lowest 7.9:1). This machine has no Bengali fonts, so Bangla glyphs rendered as boxes locally; the page names
  Noto Sans Bengali, Kohinoor Bangla, Nirmala UI and Vrinda, which ship on Android, Apple and Windows devices.
- Review: one fresh reviewer read the full diff; findings and what was fixed are listed in `TASKS.md`.
- CI on the pushed head: see "CI runs" below.
- Pages: serves from `main:/docs`, so the restyle goes live only after merge. The current site returned HTTP 200
  before push.

## For the coordinator

- After merging, re-check `https://muhib-karim.github.io/Rambler-Bangla/` returns 200 and close Dependabot PRs
  #15, #16 and #17 as superseded (their bumps are on this branch).
- The README now quotes "40 Python unit tests"; `test_quoted_unit_test_count_matches_suite` fails if a test is
  added without updating that number, by design.

## CI runs

`ci.yml` and `tests.yml` trigger on pull requests and pushes to `main` only, so they were dispatched on this branch.
