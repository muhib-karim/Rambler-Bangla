# Handoff — Rambler Bangla

**Date:** 2026-11-27  
**Repo:** `/root/workspace/rambler-work/Rambler-Bangla` (`origin/main` @ `f4f0123`, tag `v31.2.0`)

## Current Objective
1. **Completed:** Shipped **`Rambler Bangla v31.2.0`** (59-patch unified Morphe suite for Gboard `18.5.4.993608717-release-arm64-v8a`) with **100% personalized, native Gboard suggestion-strip `@` mentions** (zero floating overlay/white background; automatically matches AMOLED Dark, Material You, Frosted Glass & Custom Themes; native drag-up-to-remove; learns from typing & voice dictation) and signed the public Release APK with the repository's **persistent maintainer certificate (`679cfdec...`)** for seamless in-place upgrades over `v31.1.0`+.
2. **Awaiting user approval before applying:**
   - Proposed `AGENTS.md` cleanup and on-demand `.agents/skills/` split.
   - Recommended script/tool consolidation (`scripts/`, `.github/scripts/`) and concise log-file output modes.
   - Optional Firebase Test Lab Robo testing once `gcloud` + a Spark-plan Firebase project are configured.

## Completed Work
- **`v31.2.0`** ([PR #33](https://github.com/muhib-karim/Rambler-Bangla/pull/33), `f4f0123`, [Release v31.2.0](https://github.com/muhib-karim/Rambler-Bangla/releases/tag/v31.2.0)):
  - Rewrote `RamblerAtMentionStripRuntime` + `RamblerSharedPatches.kt` to inject `@` candidates directly into Gboard's native candidate strip (`pbh.K(Z)V`, `pbo.aa(IZ)V`, `phr.aw(Lohe;)Z`, `owu` candidates) with native theme styling and native `-10031` drag-up-to-remove.
  - Signed with persistent maintainer cert (`679cfdec...`), verified on Android 15 KVM emulator (`#45902128012`), and published with HTTP 200 checks.
- **`v31.1.0`** ([PR #32](https://github.com/muhib-karim/Rambler-Bangla/pull/32), `efe1b6b`, [Release v31.1.0](https://github.com/muhib-karim/Rambler-Bangla/releases/tag/v31.1.0)):
  - Removed hardcoded defaults from standalone `@` mentions; added learning on space/punctuation, Send/Enter, field switch, keyboard hide, and voice dictation; added manual `@mention` add dialog in Settings; switched APK signing to persistent maintainer key (`private-test-build.yml`).
- **`v31.0.0`** ([PR #31](https://github.com/muhib-karim/Rambler-Bangla/pull/31), `411d8ba`, [Release v31.0.0](https://github.com/muhib-karim/Rambler-Bangla/releases/tag/v31.0.0)):
  - Fixed Rambler+ activation (`enable_agentic_dictation` unconditionally true in `GboardRamblerStockPolicy` + `RamblerFlagRuntime`).
  - Wired active Gboard locale (`fhv.w()`) into `GboardRamblerScriptFix` and defaulted EN/BN pairing ON in `GboardRamblerLayoutPair`.
  - Ported `nl-morphe-patches` Gboard 18.5.4 features (`AMOLED Dark Keyboard`, `Rambler Toolbar & Revert Extras`, `Hidden Flags Override`).
  - Set in-app author to `muhib-karim` / `Rambler Bangla` while preserving full upstream license notices across repo docs; visually inspected and fixed `docs/index.html` on desktop & mobile via Playwright Chromium.
- **Colab cleanup:** Zero active Colab sessions (`colab --auth=oauth2 sessions` verified empty).

## Verified Decisions
- **Native candidate strip over floating overlays:** Injecting `owu` candidates into `pbh`/`pbo`/`phr` eliminates theme/background mismatch and gains native drag-up-to-remove automatically.
- **Persistent maintainer signing (`679cfdec...`):** Always sign release APKs via `.github/workflows/private-test-build.yml` (`RAMBLER_KEYSTORE_BASE64`) so users can upgrade in-place without uninstalling or losing learned `@mentions`/settings.
- **Fail-closed target gating:** 18.8-beta-only `nl-morphe-patches` flags absent from Gboard 18.5.4 DEX remain excluded; password/PIN/incognito fields remain strictly fail-closed.
- **No binaries in git:** Zero `.apk`, `.apks`, `.dex`, `.mpp`, `.rve`, or `.keystore` files tracked in git.

## Relevant File Paths
- **Morphe patches:** `patches/src/main/kotlin/dev/abotmaker/ramblerenhancements/patches/{NlMorpheGboardPatches,RamblerBanglaPatches,RamblerSharedPatches,PatchCatalog}.kt`
- **Extensions / runtime:**
  - `extensions/rambler-enhancements/src/main/java/dev/abotmaker/ramblerenhancements/{RamblerAtMentionStore,RamblerAtMentionStripRuntime,RamblerFlagRuntime,RamblerHiddenFlagOverrideRuntime,RamblerEnhancementsActivity}.java`
  - `extensions/rambler-enhancements/src/main/java/com/akshaykadam/pixelboard/extension/rambler/{GboardRamblerScriptFix,GboardRamblerLayoutPair}.java`
  - `extensions/extension/src/main/java/dev/jason/gboardpatches/extension/rambler/GboardRamblerStockPolicy.java`
- **Docs & metadata:** `README.md`, `CHANGELOG.md`, `docs/index.html`, `docs/PROJECT_BRAIN.md`, `patches-bundle.json`, `patches-list.json`, `ATTRIBUTION.md`, `THIRD-PARTY-NOTICES.md`, `NOTICE`
- **Release artifacts (`v31.2.0`, maintainer-signed cert `679cfdec...`):**
  - `/sdcard/Download/RamblerBangla-v31.2.0-gboard-18.5.4-arm64-v8a.apk` (`SHA-256: 4a542fd3dbe05a416d72be655c7c6d496e87deac3a21e6b753b0f2c211b734fd`)
  - `patches-31.2.0.mpp` on GitHub Release (`SHA-256: f26e58a20c244ac14e53c1ea7b4173f3aac567aec00ac4019b4cad9755d49198`)

## Test Results (`v31.2.0`)
- **Python unit tests & selftests:** **88/88** passed (1 skipped), `ci_gate_selftest.sh` **PASS**, `url_allowlist_selftest.sh` **PASS**.
- **CI (`#45901580767`) & Static test suite (`#45901580798`):** 22/22 static checks, extension unit tests & lint, `:patches:verifyGboardCapabilityWiring`, **312/312** `:patches:test` — **PASS**.
- **Colab Morphe CLI & JADX:** **59/59** patches applied, `apksigner` / `zipalign` OK, DEX markers & JADX checks on `pbh`, `pbo`, `phr`, `fja`, `rmt` — **PASS**.
- **Maintainer signing (`#45901632030`) & Android 15 KVM emulator smoke test (`#45902128012`):** **PASS**.
- **Public URLs:** `patches-bundle.json`, `patches-31.2.0.mpp`, and Release APK all verified **HTTP 200**.

## Unresolved Issues
- Real-device microphone ASR check (spoken Bangla dictation) requires physical phone testing by the maintainer.
- `gcloud` / Firebase Test Lab is not configured in this container.

## Exact Next Steps
1. On approval, apply the proposed `AGENTS.md` rewrite, `.agents/skills/` split, and script cleanups in a dedicated PR.
2. Install `/sdcard/Download/RamblerBangla-v31.2.0-gboard-18.5.4-arm64-v8a.apk` directly over `v31.1.0` to verify native `@` suggestions, theme matching, drag-up-to-remove, and spoken Bangla dictation on device.
