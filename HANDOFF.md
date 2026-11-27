# Handoff — Rambler Bangla

**Date:** 2026-11-27  
**Repo:** `/root/workspace/rambler-work/Rambler-Bangla` (`origin/main`, tag `v31.3.0`)

## Current Objective
1. **Completed:** Shipped **`Rambler Bangla v31.3.0`** (60-patch unified Morphe suite for Gboard `18.5.4.993608717-release-arm64-v8a`) adding:
   - **Live in-app `AMOLED Dark Keyboard` toggle** (default **OFF** so stock Material You dark gray contrast stays normal unless the user turns AMOLED `#000000` ON in **Rambler Enhancements → Keyboard appearance & clipboard**).
   - **Persistent 4-direction `Clipboard Drag-to-Reorder`** (new 60th Morphe patch, default **ON**): tap & hold + drag clipboard cards within **Pinned** or **Recent** with persistent `ClipboardContentProvider` timestamp ordering (plus image clip file renames), while keeping stationary long-press Pin/Paste/Delete popup, swipe-to-delete with Undo, and Edit Mode intact.
   - Maintainer-signed Release APK (`679cfdec...`) for seamless in-place upgrades over `v31.2.0`+.
2. **Awaiting user approval before applying:**
   - Proposed `AGENTS.md` cleanup and on-demand `.agents/skills/` split.
   - Recommended script/tool consolidation (`scripts/`, `.github/scripts/`) and concise log-file output modes.
   - Optional Firebase Test Lab Robo testing once `gcloud` + a Spark-plan Firebase project are configured.

## Completed Work
- **`v31.3.0`** (60 Morphe patches):
  - Gated `Lrmt;-><init>(Context, Z)V` in `NlMorpheGboardPatches.kt` on `RamblerEnhancementsSettings.readAmoledDarkKeyboardEnabled(Context)` (default `false`).
  - Added `gboardClipboardReorderPatch` (`Clipboard Drag-to-Reorder`) patching Gboard's clipboard `ItemTouchHelper.Callback` (`fqi`) via `RamblerClipboardReorderRuntime` (`computeMovementFlags`, `isLongPressDragEnabled`, `onMove`, `onDragDraw`, `onSelectedChanged`, `onClearView`).
  - Added **Keyboard appearance & clipboard** live toggles directly on the main **Rambler Enhancements** hub screen (`RamblerEnhancementsActivity.java`).
  - Synced `README.md`, `CHANGELOG.md`, `docs/index.html`, `docs/banner.svg`, `docs/feature-showcase.svg`, `docs/PROJECT_BRAIN.md`, `patches-bundle.json`, and `patches-list.json`.
  - Signed with persistent maintainer cert (`679cfdec...`), verified on Android 15 KVM emulator, and published with HTTP 200 checks.
- **`v31.2.0`** ([PR #33](https://github.com/muhib-karim/Rambler-Bangla/pull/33), [PR #34](https://github.com/muhib-karim/Rambler-Bangla/pull/34), [PR #35](https://github.com/muhib-karim/Rambler-Bangla/pull/35)):
  - Native Gboard suggestion-strip `@Mention` integration (`pbh`/`pbo`/`owu`/`phr`), full GPL-3.0/MIT/MPL-2.0/BSD/§7c licensing sync, and production-grade GitHub homepage.
- **Colab cleanup:** Zero active Colab sessions (`colab sessions` verified empty).

## Verified Decisions
- **AMOLED Dark Keyboard default OFF + live toggle:** Prevents pitch-black surface flattening on stock Material You dark themes while keeping `#000000` OLED mode one tap away in Settings.
- **Clipboard drag-to-reorder on `fqi` (`ItemTouchHelper.Callback`):** Preserves stock swipe-to-delete (`48`), keeps card opacity (`0.94f`) during drag instead of horizontal swipe-fade, replays `performLongClick()` on stationary tap-and-hold release so Pin/Delete popup still works, locks headers & Edit Mode, and persists order by reassigning the section's existing sorted descending timestamps so recent TTLs stay intact.
- **Persistent maintainer signing (`679cfdec...`):** Always sign release APKs via `.github/workflows/private-test-build.yml` (`RAMBLER_KEYSTORE_BASE64`) so users upgrade in-place without uninstalling or losing learned `@mentions`/settings.
- **No binaries in git:** Zero `.apk`, `.apks`, `.dex`, `.mpp`, `.rve`, or `.keystore` files tracked in git.

## Relevant File Paths
- **Morphe patches:** `patches/src/main/kotlin/dev/abotmaker/ramblerenhancements/patches/{NlMorpheGboardPatches,RamblerBanglaPatches,RamblerSharedPatches,PatchCatalog}.kt`
- **Extensions / runtime:**
  - `extensions/rambler-enhancements/src/main/java/dev/abotmaker/ramblerenhancements/{RamblerClipboardReorderRuntime,RamblerEnhancementsSettings,RamblerEnhancementsActivity,RamblerAtMentionStore,RamblerAtMentionStripRuntime,RamblerFlagRuntime,RamblerHiddenFlagOverrideRuntime}.java`
  - `extensions/rambler-enhancements/src/main/java/com/akshaykadam/pixelboard/extension/rambler/{GboardRamblerScriptFix,GboardRamblerLayoutPair}.java`
  - `extensions/extension/src/main/java/dev/jason/gboardpatches/extension/rambler/GboardRamblerStockPolicy.java`
- **Docs & metadata:** `README.md`, `CHANGELOG.md`, `docs/index.html`, `docs/banner.svg`, `docs/feature-showcase.svg`, `docs/PROJECT_BRAIN.md`, `patches-bundle.json`, `patches-list.json`, `ATTRIBUTION.md`, `THIRD-PARTY-NOTICES.md`, `NOTICE`
- **Release artifacts (`v31.3.0`, maintainer-signed cert `679cfdec...`):**
  - `/sdcard/Download/RamblerBangla-v31.3.0-gboard-18.5.4-arm64-v8a.apk`
  - `patches-31.3.0.mpp` on GitHub Release (`SHA-256: 8ed207a351a7d9683e9d2ddcfab278e2cdecd217f244618f99b5da376a971dcd`)

## Test Results (`v31.3.0`)
- **Python unit tests & selftests:** **88/88** passed (1 skipped), `ci_gate_selftest.sh` **PASS**, `url_allowlist_selftest.sh` **PASS**.
- **Colab Gradle, Morphe CLI & JADX:** Full extension unit tests + Android Lint, `:patches:verifyGboardCapabilityWiring`, **313/313** `:patches:test`, **60/60** Morphe CLI patches on Gboard `18.5.4`, DEX markers & JADX checks on `rmt` & `fqi` — **PASS**.
- **GitHub CI, Maintainer Signing & Android 15 KVM Smoke Test:** Verified on PR #36 and `v31.3.0` release.

## Unresolved Issues
- Real-device microphone ASR check (spoken Bangla dictation) requires physical phone testing by the maintainer.
- `gcloud` / Firebase Test Lab is not configured in this container.

## Exact Next Steps
1. Install `/sdcard/Download/RamblerBangla-v31.3.0-gboard-18.5.4-arm64-v8a.apk` directly over `v31.2.0` to confirm balanced stock dark theme (AMOLED toggle OFF by default, toggleable ON in Rambler Enhancements) and tap-and-hold clipboard drag-to-reorder.
2. On approval, apply the proposed `AGENTS.md` rewrite, `.agents/skills/` split, and script cleanups in a dedicated PR.
