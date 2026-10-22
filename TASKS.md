# TASKS — v29.4.1 README and homepage refresh

Branch `docs/readme-refresh` from `main` (1ec05a9). Docs only: no patcher, runtime, fingerprint or signing changes,
no APKs or binaries. The coordinator opens and merges the PR.

## Plan

| # | Step | Commit | State |
| --- | --- | --- | --- |
| 1 | Project memory files (AGENTS.md, CLAUDE.md, docs/PROJECT_BRAIN.md) and this task list | `chore: project memory and v29.4.1 task list` | done |
| 2 | Failing docs test: README sections, badges, local links, Pages page has no external scripts or trackers | `test: homepage docs checks` | done |
| 3 | README homepage refresh (banner, badges, what/why, architecture mermaid, device-free CI, build, safety, source-only and legal, FAQ, roadmap, links) | `docs: README homepage refresh` | done |
| 4 | Pages landing page restyle to match (mobile-first, contrast, no trackers) | `docs(site): restyle landing page` | done |
| 5 | Dependabot action bumps in `ci.yml` | `ci: bump checkout 7, setup-python 7, setup-java 6` | done |
| 6 | CHANGELOG [29.4.1], SKILLS_USED.md, HANDOFF.md, this file | `docs: v29.4.1 changelog, skills and handoff` | done |
| 7 | Push via the gated push script, CI green on the head, Pages 200 | — | done (CI green on `acb57fb`: runs 39732633550, 39732636153) |

## Dependabot PRs

| PR | Bump | PR CI | On this branch |
| --- | --- | --- | --- |
| #15 | `actions/checkout` 4 to 7 (ci.yml) | green | applied |
| #16 | `actions/setup-java` 4 to 6 (ci.yml) | green | applied |
| #17 | `actions/setup-python` 5 to 7 (ci.yml) | green | applied |

`ci.yml` and `tests.yml` run on pull requests and pushes to `main`, not on branch pushes, so their runs on this
branch are started with `workflow_dispatch`. The bumps stay only if those runs are green: they were (run 39732633550 used checkout v7, setup-python v7 and
setup-java v6).

## Review

A fresh reviewer read the whole diff (verdict: ready with fixes, no critical issues). Fixed in
`docs: address review of the homepage refresh` and `docs(user-guide): ...`: the exit-1 "no output" claim (a repack
error can leave a partial `--out` file), "no verbatim strings" (the profile keeps hashes plus short fragments),
the test-run image caption, content the rewrite dropped, V29.4.1 marked untagged, site Limitations, reduced-motion
smooth scroll, and stronger docs tests (anchors, remote links, `@import`/`url()`, first CSS token).

Left as is, by choice: the CI job name "Python unit tests (patcher helpers)" (renaming changes the required-check
name the coordinator may rely on), table captions (tables are labelled by their scroll regions), and the
hard-coded `v29.4` check (it should be edited at each release, which is the point).

## Follow-up for a code PR (not this branch)

- `dex_patch.py` writes `--out` in place; write to a temporary file and rename it so a repack error never leaves a
  partial APK.
