# PROJECT_BRAIN — Rambler Bangla

## Goal
V29.5: clipboard Recent | History in the clipboard panel (50 unique text clips, up to 10 pins, tap to paste,
long-press Pin/Unpin/Delete, persistent, sensitive clips skipped) on the dedicated feature PR from
`feature/clipboard-history`. Deliver the unsigned aligned APK locally; the coordinator signs, merges and releases.

## Status
- Done: runtime (`extension-src/.../rambler/ClipHistory*`, `GboardRamblerClipHistory`), guarded build
  (`scripts/build_clip_history.sh`, `fingerprints/clipboard-history-rc4.json`), 49 unit tests, docs. A real build from
  rc4 was verified independently (see HANDOFF.md). Codex's draft was reviewed and fixed: capture gating dropped normal
  copies, the stock list could bleed under History, a clock step stopped recording, direct boot could wipe history,
  and the build did not check hook targets.
- On-device behaviour: not tested.

## Next step
- Coordinator: sign via `private-test-build`, run the HANDOFF device checklist on the S23 Ultra, then merge/release.

## Decisions
- Hooks live in Gboard methods (classes2/3, DEX 039), each pinned by method hash. ClipOrder has no panel hook and
  classes.dex is DEX 040.
- Record from Gboard's own clipboard read (`kvk.call`), mirroring stock gating (opt-in, `aelb.f()` incognito), plus
  sensitive/masked/private-editor checks and a 10 s live-copy window.
- Framework views only, no resources or manifest edits. Storage is `no_backup/rambler_clip_history` (RCH2,
  AtomicFile), only after unlock. No network.

## Gotchas
- `tests/run_tests.sh` needs the fetched toolchain (`tools/`); do not run it locally on a shared box, rely on CI.
- The README quotes the Python unit test count (`test_quoted_unit_test_count_matches_suite`); update it with tests.
- Compile the runtime against an SDK `platforms/android-32+/android.jar`; the pinned `tools/android.jar` is too old.
- classes2 has 64,595 method refs after the hooks: little headroom for more hooks in that dex.

## Session log
- 2026-10-22 05:57Z claude-opus-5-5: created memory files and v29.4.1 plan; next: docs test + README.
- 2026-10-22 06:13Z claude-opus-5-5: README, site, docs tests, ci bumps, changelog, review fixes committed; next: push, dispatch CI, record runs.
- 2026-10-22 06:14Z claude-opus-5-5: pushed, CI green on acb57fb with bumped actions; handoff recorded. Next: coordinator PR + merge.
- 2026-10-23 18:12Z Codex: store, bridge/UI and guarded build drafted (local only); stopped before review.
- 2026-10-23 19:05Z claude-opus-5-5: reviewed the draft against the rc4 disassembly, fixed capture/UI/store/build defects, added pins, built and verified the APK, rewrote docs. Next: PR + CI.
- 2026-10-23 19:20Z claude-opus-5-5: PR #20 open, CI green; fixed Codex P2 (private window now monotonic), rebuilt and re-verified APK f416f7d4. Next: coordinator signs + device checklist.
