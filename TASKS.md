# TASKS — v29.4.1 README and homepage refresh

Branch `docs/readme-refresh` from `main` (1ec05a9). Docs only: no patcher, runtime, fingerprint or signing changes,
no APKs or binaries. The coordinator opens and merges the PR.

## Plan

| # | Step | Commit | State |
| --- | --- | --- | --- |
| 1 | Project memory files (AGENTS.md, CLAUDE.md, docs/PROJECT_BRAIN.md) and this task list | `chore: project memory and v29.4.1 task list` | done |
| 2 | Failing docs test: README sections, badges, local links, Pages page has no external scripts or trackers | `test: homepage docs checks` | todo |
| 3 | README homepage refresh (banner, badges, what/why, architecture mermaid, device-free CI, build, safety, source-only and legal, FAQ, roadmap, links) | `docs: README homepage refresh` | todo |
| 4 | Pages landing page restyle to match (mobile-first, contrast, no trackers) | `docs(site): restyle landing page` | todo |
| 5 | Dependabot action bumps in `ci.yml` | `ci: bump checkout 7, setup-python 7, setup-java 6` | todo |
| 6 | CHANGELOG [29.4.1], SKILLS_USED.md, HANDOFF.md, this file | `docs: v29.4.1 changelog, skills and handoff` | todo |
| 7 | Push via the gated push script, CI green on the head, Pages 200 | — | todo |

## Dependabot PRs

| PR | Bump | PR CI | On this branch |
| --- | --- | --- | --- |
| #15 | `actions/checkout` 4 to 7 (ci.yml) | green | pending |
| #16 | `actions/setup-java` 4 to 6 (ci.yml) | green | pending |
| #17 | `actions/setup-python` 5 to 7 (ci.yml) | green | pending |
