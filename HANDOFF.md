# HANDOFF — V29.5 clipboard Recent | History

Branch `feature/clipboard-history` from `main` 625ca1e. This is the feature PR. The coordinator signs the APK through
`private-test-build` and decides on merge and release. Nothing here touches `main`, any tag or release, the signing
workflows or the voice-typing patcher.

**Status: static and host-tested only.** No phone or emulator has run this build. Everything below about behaviour on
a device is design intent until the device checklist is done.

## What it does

- The clipboard panel gets a small **Recent | History** tab row between the panel header and the clip list.
- **Recent** is the stock list, unchanged. ClipOrder, ClipDrag, ClipSmooth and ClipAccess still act on it.
- **History** is a scrolling list of the last 50 unique text clips, newest first, above up to 10 pinned clips that do
  not count towards the 50. Copying the same text again moves it to the top.
- **Tap** pastes into the current field. **Long-press** offers Pin or Unpin and Delete.
- History lives in one app-private file, `no_backup/rambler_clip_history`, written atomically. It survives keyboard
  restarts and is never read before the phone's first unlock. No network code.

## How it is wired

| Hook (rc4, hash-pinned) | Calls | Why |
| --- | --- | --- |
| `aeij.onStartInput` entry | `start(service, editorInfo)` | Keeps the IME reference. Closes History if the new field is private |
| `aeij.onFinishInput`, `aeij.onDestroy` entry | `finish()` | Drops the reference and restores the stock panel |
| `kvk.call`, after stock `kvw.b` | `capture(kvw, ksw, clipData, clipDescription)` | Gboard's own clipboard read. It runs on every clipboard change, panel open or not |
| `ClipboardKeyboard.e` before return | `attach(keyboard)` | The panel is bound and field `i` is the clip RecyclerView |
| `ClipboardKeyboard.f`, `ClipboardKeyboard.k` entry | `detach(keyboard)` | Puts the stock view tree back before Gboard tears it down |

The hooks sit in Gboard code because ClipOrder has no panel hook (it only reorders lists) and `classes.dex` is DEX 040,
which the pinned smali can't reassemble. ClipOrder and ClipDrag are already called from Gboard's `kut`/`kta` the same
way. The runtime ships as `classes6.dex`.

`attach` moves the stock RecyclerView into a holder inside a wrapper. The wrapper takes the RecyclerView's
ConstraintLayout slot and params. In both rc4 clipboard layouts nothing else constrains to the RecyclerView's id.
Gboard changes the list's own visibility and alpha, so History hides the holder rather than the list. Any exception
puts the original view back where it was.

## Privacy rules (`ClipHistoryPolicy`)

A clip is recorded only if all of these hold:
- Gboard clipboard opt-in is on (stock `kvw.v()`).
- Gboard is not incognito (stock `aelb.f()`, the same check stock `onPrimaryClipChanged` uses; unknown state counts
  as private).
- Neither ClipDescription has `android.content.extra.IS_SENSITIVE`, and Gboard did not mask the clip (`ksw.m()`).
- If an editor session is active, it is not a password field and not `IME_FLAG_NO_PERSONALIZED_LEARNING`.
- No private session (one of those editors, or Gboard incognito) was running at the clip's timestamp, give or take
  1 s. This still holds if focus moved on before the read was handled. Private sessions less than 20 s apart count
  as one.
- It is a live copy: the clip timestamp is within 10 s of now. A re-read when the process restarts can't import a clip
  copied earlier, for example while incognito.
- It is plain text from `ClipData.Item.getText()`, between 1 and 32,768 characters. URIs, HTML and images are never
  coerced; longer clips are skipped, not truncated.

History is shown only in a non-private editor, with opt-in on and incognito off. Turning opt-in off hides History but
does not delete the file. Deleting an entry removes its text from the file; no hash of it is kept.

## Build

```bash
source tools/tool-env.sh          # pinned smali/baksmali 2.5.2 and r8 8.3.37 (scripts/bootstrap_tools.sh)
export ANDROID_HOME=/path/to/android-sdk   # platform android-32+ and build-tools zipalign
scripts/build_clip_history.sh rambler-bangla-v29.4-rc4-newkey.apk rambler-v29.5-unsigned-aligned.apk
```

The build exits 2 and writes no output if any of these fail:
- the base SHA-256 doesn't match `fingerprints/clipboard-history-rc4.json`;
- a hooked method hash or anchor doesn't match;
- a hook calls a runtime method that is missing or takes a different number of arguments;
- a class, member or the opt-in resource id that the runtime reaches by reflection is missing from the base
  (`contract` in the profile);
- an untouched class, method or APK entry changes;
- the runtime DEX holds anything outside the Rambler package.

The output is unsigned and zipaligned (`-P 16`). The manifest, resources and versionCode are unchanged. Memory: one
Java step at a time, `-Xmx2g`; the script waits for 3 GiB free.

## Verification done (2026-10-24 AEDT, local, no device)

- `python3 -m unittest discover -s tests/python`: 49 tests, OK, including the JVM store and policy harness.
- A real build from rc4 (SHA-256 `d834ee67…cd2c`) exited 0. The output APK's SHA-256 is
  `e94fdc9abf8bac093d0636e6ed5a5b66996f8d6e19d31213a7847ea34a7175fd`. Then, checked independently:
  - baksmali of the output's classes2/3 against the base: same class sets, and only `aeij`, `kvk` and
    `ClipboardKeyboard` differ, by the seven hook lines plus label renumbering;
  - classes6 holds only the 25 runtime classes;
  - method refs: classes2 64,595, classes3 62,801, classes6 286;
  - `zipalign -c -P 16 4` passes and `aapt2 dump badging` is identical to the base;
  - a throwaway debug-signed copy passes `apksigner verify` (key and copy deleted).
- The static suite runs in CI (shared-machine rule).

## Device checklist (pending)

1. Open the clipboard in a chat app. The tab row shows under the header in light and dark themes, and Recent works
   as before: order, drag, pin, delete.
2. Copy text in Chrome with no field focused, then open a chat field: the clip is in History. Copy again: it moves to
   the top, with no duplicate.
3. Tap a History row with and without a half-typed word: the clip is inserted and the typed word is kept.
4. Long-press: Pin moves the clip to the top and keeps it past 50 new copies. Unpin and Delete work.
5. Force-stop the keyboard, or reboot and unlock: History is still there.
6. Password field, incognito Chrome tab, a password manager's copy, clipboard opt-in off: nothing recorded and no
   History tab.
   Also copy a password with an app's own copy button and leave the field at once: nothing recorded.
7. Switch fields and rotate with History open. Close and reopen the panel: no crash, and the stock panel comes back.

## Known risks

- The paste goes through `InputConnection` (batch edit, `finishComposingText`, `commitText`), not Gboard's internal
  paste event. Gboard resyncs from the selection update. Check apps that restart input on every edit.
- The long-press `PopupMenu` is a framework popup inside the IME window. If it doesn't show on One UI, Delete and Pin
  are unreachable but nothing else breaks.
- Colours come from the panel's first visible TextView, falling back to `textColorPrimary`.
- Recording needs a ClipDescription timestamp, which AOSP sets on every copy. If One UI left it at 0, History would
  record nothing; checklist item 2 shows that at once.
- If the clipboard change is handled more than 10 s late, for example on a heavily loaded device, the clip is not
  recorded.
