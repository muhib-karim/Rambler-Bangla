# Project brain

Working memory for coding agents, required by [`AGENTS.md`](../AGENTS.md). Keep
**Status** and **Next step** current, and append one **Session log** line before
you stop. Rules live in `AGENTS.md`; history lives in git and
[`CHANGELOG.md`](../CHANGELOG.md). Do not copy either here.

## Status

- Branch `tab/production-grade` holds a production-grade pass and is open as
  PR #23. The maintainer reported CI green on its first 11 commits
  (2026-11-10). The APK URL allowlist fix made after review is committed
  locally and **not pushed**.
- What the pass changed:
  - CI pass gate for the static suite (`set -euo pipefail` plus the exact
    `TEST SUITE: N passed, 0 failed` line), with
    `.github/scripts/ci_gate_selftest.sh` now run by the `unit` job.
  - Patcher (`standalone-patcher/dex_patch.py`): strict smali parsing, abort
    when `String.replace` overwrites the languages register, `--extension-dex`
    and `--workdir` checked before any work, output APK written atomically.
  - `scripts/verify_apk.sh single` exits 1 on a failed signature, a `MISSING`
    fingerprint or a `NOT FOUND` method, and accepts only `--profile <file>`.
  - `scripts/bootstrap_tools.sh` deletes partial or mismatched downloads.
  - `ci.yml` actions pinned by commit SHA.
  - `android-smoke.yml`, `clipboard-e2e.yml` and
    `keyboard-onboarding-probe.yml` share one APK URL check: https only, and
    the allowlist is matched against the real host (authority without
    userinfo or port). `.github/scripts/url_allowlist_selftest.sh` runs that
    block from the workflows and fails if the three copies differ; the `unit`
    job runs it.
- Checked locally: 66 Python unit tests and both self-tests pass.
  `verify_apk.sh` and `bootstrap_tools.sh` were exercised with stubs only.
- The static suite (`tests/run_tests.sh`) is never run locally; `AGENTS.md`
  reserves it for CI.
- `README.md` quotes the number of Python unit tests, and
  `tests/python/test_docs_homepage.py` fails if it is wrong. Update the number
  whenever a test is added or removed.

## Next step

1. Push the allowlist fix to PR #23 and confirm CI is green again, including
   the new self-test step. `AGENTS.md` asks for patcher changes in their own
   reviewed PR; PR #23 carries the three `patcher:` commits with the rest, so
   split them out if the reviewer wants that.
2. Follow-ups found during the pass, none started:
   - `CHANGELOG.md` has no entry for this pass.

## Session log

- 2026-11-15: scan_fingerprints fail-closed abort, verify_apk.sh static-suite
  gate (21 -> 22 checks), bootstrap_tools.sh header correction. Local
  verification: 88 Python tests, static suite 22/22. Delivered as unmerged
  PR #27, stacked on #26.
- 2026-11-15: Bumped pinned guava 27.1-android to 33.8.0-android and
  failureaccess 1.0.1 to 1.0.3 (version from guava-parent pom). Upstream .sha1
  matched; re-pinned only those two sha256 lines by hand. Local verification:
  bootstrap verify, static suite 22/22, 88 Python tests OK.

- 2026-11-10: Fixed the CI pass gate and added its self-test (1 commit).
- 2026-11-10: Production-grade pass, phase 2 (10 commits): patcher fail-open
  fixes with unit tests, `verify_apk.sh` exit codes, `score.py` default
  baseline, `bootstrap_tools.sh` download handling, `ci.yml` SHA pins and
  self-test step, onboarding-probe URL allowlist, README known limitations,
  user-guide environment variables, this file. Not pushed.
- 2026-11-10: After review on PR #23, fixed the APK URL host parsing in the
  three emulator workflows and added `url_allowlist_selftest.sh` (1 commit).
  Not pushed.
