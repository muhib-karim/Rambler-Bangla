# Changelog

All notable changes to Rambler Bangla. Dates are AEST/AEDT. Test builds are maintainer builds, not public downloads; see the README's Distribution section.

## V29.4 - 2026-10-18

Source-only release. No runtime or conversion changes against V29.3. Details: [`docs/V29.4-RELEASE-NOTES.md`](docs/V29.4-RELEASE-NOTES.md).

- New maintainer signing key (certificate SHA-256 `679cfdec...2e78`); the V29.3 key is lost. Builds signed with the new key cannot install over V29.3 builds: uninstall once first. Record: [`ledger/v29.4-provenance.md`](ledger/v29.4-provenance.md).
- The maintainer key now lives only in GitHub Actions secrets (plus an offline backup kept by the maintainer). New manual workflow `maintainer-key-check.yml` signs the synthetic fixture with it and checks the signer; it publishes no APK.
- Docs: portfolio README, architecture, user guide and releasing docs, docs index; community files (code of conduct, contributing, PR and issue templates, CODEOWNERS, Dependabot).
- CI: `ci.yml` runs 26 Python unit tests for the patcher helpers and the static suite on every push and pull request.
- Patcher fix: array parameters count as one register slot in `dex_patch.py`, whatever the element type.
- Project website (GitHub Pages), banner, brand sheet, icon showcase and favicon (2026-10-05 to 2026-10-08).
- Fixture hygiene: personal-looking emails in the v29.3 corpus, target and golden fixtures replaced with `example.com` addresses.
- Removed the temporary clipboard-mask and instrumented-settings diagnostic probes and their workflows.
- Dependency bumps: `actions/checkout` 4.2.2 to 7.0.1, `actions/upload-artifact` 4.6.2 to 7.0.2.

### Maintainer test builds 245 to 253 - October 2026

Private builds, not public downloads, and not part of this source release. Evidence levels in the README's "Maintainer builds, October 2026" table.

- 253: compatibility update, two static reviews passed; the maintainer reports the Gboard AI writing-tools icon is back on their phone. Cause not proven.
- 251: removes the pin exemption for the stray "123" label (static review; phone check pending).
- 250: unpinned path clean on the phone; pinned still showed "123".
- 247 to 249: spacebar label and list/popup candidates; not shipped.
- Planned: clause segmentation, phonetic typing with EN/BN toggle, Bangla (Latin).

## V29.3 - 2026-09-24

- One language per sentence in multilingual voice typing.
- Real Bangla spellings for English words (CMU Pronouncing Dictionary plus reviewed overrides).
- Protected text: #hashtags, @handles, links, emails and times stay in Latin letters.
- Clipboard drag-to-reorder (in development).
- Host corpus of 292 cases passes all gates. Details: [`docs/V29.3-RELEASE-NOTES.md`](docs/V29.3-RELEASE-NOTES.md).

## V29 - 2026-09-23

- Bangla spelling normalization: garble-resistant English near-match demotion and a 2,265-pair roman-to-script table.
- Design: [`docs/V29-DESIGN.md`](docs/V29-DESIGN.md).

## V28 / v28d

- Segment-locked multilingual dictation. Last stable baseline. Evidence: [`docs/EVIDENCE.md`](docs/EVIDENCE.md).
