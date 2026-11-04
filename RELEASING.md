# Releasing EyesUtilities

One tag push builds and publishes everything: the CLI jar and four GUI
installers, attached to a GitHub release marked Latest by
`.github/workflows/release.yml`.

## What a release contains

| Asset | Built by | Runner |
|---|---|---|
| `EyesUtilities_<version>.jar` | `build-jar` job: `mvn -DskipTests package` | ubuntu-latest |
| `EyesUtilities-<version>-Windows.msi` | `build` matrix: `mvn -Pgui-installers -DskipTests clean verify` | windows-latest |
| `EyesUtilities-<version>-macOS-AppleSilicon.dmg` | 〃 | macos-latest |
| `EyesUtilities-<version>-macOS-Intel.dmg` | 〃 | macos-15-intel |
| `EyesUtilities-<version>-Linux.deb` | 〃 | ubuntu-latest |

Release notes start with a **What's changed** section, taken from the tagged
version's section in `CHANGELOG.md`, followed by `.github/release-template.md`
with `{{VERSION}}` filled in. `.github/scripts/release-notes.sh` puts them
together and fails the release if the CHANGELOG has no section for the version.

## Steps

1. **Bump the version** in both places, which must match:
   - `pom.xml` (`<version>`)
   - `cur_ver` in `src/main/java/com/applitools/eyesutilities/EyesUtilities.java`

   **Add a matching `## <version> - <date>` section to `CHANGELOG.md`.**
   The workflow refuses tags whose base version differs from the pom, and fails
   if `cur_ver` or the CHANGELOG don't match it.
2. Merge to `main` with CI green.
3. **Tag and push**:
   ```
   git tag v1.7.0
   git push origin v1.7.0
   ```
   Suffixed tags (`v1.7.0-rc1`) are allowed; the base must equal the pom version.
4. The Release workflow publishes a release marked **Latest** with all five
   assets plus `SHA256SUMS.txt`. The README's Download badge points at the
   latest release, so it goes live immediately. Download and smoke-test an
   installer and the jar right away.

## Dry run

Run the Release workflow manually (`workflow_dispatch`). It builds every asset
as workflow artifacts but never creates a release.

## Building locally

- CLI jar: `mvn -DskipTests package` (JDK 11+) → `jars/EyesUtilities_<version>.jar`.
- GUI installer for your OS: `mvn -Pgui-installers -DskipTests clean verify`
  (needs JDK 17 for jpackage) → `target/installers/`.

## Signing

Installers currently ship unsigned (the release notes explain the OS warnings).
The workflow fails loudly if `WINDOWS_SIGNING_CERT` / `APPLE_SIGNING_CERT`
secrets are set before the signing steps are implemented, so a "signed" release
can never silently ship unsigned.
