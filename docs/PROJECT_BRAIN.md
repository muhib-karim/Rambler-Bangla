# Project brain

Working memory for coding agents, required by [`AGENTS.md`](../AGENTS.md). Keep
**Status** and **Next step** current, and append one **Session log** line before
you stop. Rules live in `AGENTS.md`; history lives in git and
[`CHANGELOG.md`](../CHANGELOG.md). Do not copy either here.

## Status

- Branch `tab/production-grade` holds a production-grade pass: 11 commits on
  top of `main`, **not pushed**. Nothing on it has run in GitHub Actions yet.
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
  - `ci.yml` actions pinned by commit SHA; the onboarding probe validates the
    APK URL like the other emulator workflows.
- Checked locally: 66 Python unit tests and the gate self-test pass.
  `verify_apk.sh` and `bootstrap_tools.sh` were exercised with stubs only.
- **Not** checked locally: the static suite (`tests/run_tests.sh`), which
  `AGENTS.md` reserves for CI. The patcher changes first meet a real
  smali/baksmali round trip when this branch runs in CI.
- `README.md` quotes the number of Python unit tests, and
  `tests/python/test_docs_homepage.py` fails if it is wrong. Update the number
  whenever a test is added or removed.

## Next step

1. Push `tab/production-grade` and open the pull request(s). The three
   `patcher:` commits belong in their own reviewed PR (`AGENTS.md`). Confirm the
   static suite is green there before merging anything.
2. Follow-ups found during the pass, none started:
   - **guava bump** in the pinned toolchain. Skipped because
     `tools-src/toolchain.sha256` must be re-pinned against real downloads
     (`UPDATE_SHA=1 bash scripts/bootstrap_tools.sh`) and the static suite
     re-run.
   - **APK URL allowlist gap** in `android-smoke.yml`, `clipboard-e2e.yml` and
     `keyboard-onboarding-probe.yml`: the host is cut at the first `/` only, so
     `https://storeX.example?x=.gofile.io` matches `store*.gofile.io`. Cut at
     `/`, `?` and `#` in all three together. The SHA-256 check still applies.
   - **No automated test runs `verify_apk.sh`.** Add a fixture run to the
     static suite; that changes the "17 checks" count quoted in `README.md`,
     `CONTRIBUTING.md` and `docs/RELEASING.md`.
   - `scan_fingerprints` raises `UnicodeEncodeError` (traceback, exit 1) on a
     const-string that contains a surrogate-pair escape. It aborts, but not as
     a `FAIL-CLOSED ABORT`.
   - `scripts/bootstrap_tools.sh` says in its header that it uses `$JAVA_HOME`;
     it calls `javac` from `PATH`.
   - `CHANGELOG.md` has no entry for this pass.

## Session log

- 2026-11-10: Fixed the CI pass gate and added its self-test (1 commit).
- 2026-11-10: Production-grade pass, phase 2 (10 commits): patcher fail-open
  fixes with unit tests, `verify_apk.sh` exit codes, `score.py` default
  baseline, `bootstrap_tools.sh` download handling, `ci.yml` SHA pins and
  self-test step, onboarding-probe URL allowlist, README known limitations,
  user-guide environment variables, this file. Not pushed.
