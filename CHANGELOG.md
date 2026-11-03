# Changelog

Each release needs a `## <version> - <date>` section here before it is tagged;
the release notes lead with it.

## 1.7.0 - unreleased

- New local web GUI (`java -jar EyesUtilities.jar --gui`) and per-OS installers that open it.
- Download batch images filtered by test status.
- File paths are sanitized for Windows.
- New `diffDescriptionsCount` field; unknown API properties are ignored instead of failing.
