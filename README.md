# Smartisan UI

Shared Smartisan UI components ported from the Smartisan OS 8.5.3 R2 ROM for OpenSmartisanOS.

The library provides a single visual and behavioral foundation for OpenSmartisanOS
system and third-party applications. Core controls retain the ROM rendering,
measurement, state and interaction behavior. Required original resources are vendored directly in
`core/src/main/res`, so published AARs and source-tree builds never depend on a ROM extraction step;
their artifact, source and destination hashes are pinned under `core/provenance` for offline audit.

The ordinary SDK boundary check verifies every vendored destination and closes the complete Core
resource tree against the provenance manifests plus an immutable pre-port commit. It does not need
the ROM. Maintainers with licensed R2 artifacts can additionally verify the artifact files and
canonical decoded sources with:

```shell
python3 tools/rom/audit_r2_resources.py \
  --framework-res-apk /path/to/framework-res.apk \
  --framework-smartisanos-res-apk /path/to/framework-smartisanos-res.apk \
  --smartisanos-jar /path/to/smartisanos.jar \
  --framework-jar /path/to/framework.jar \
  --jadx /path/to/jadx-1.5.6/bin/jadx \
  --settings-apk /path/to/SettingsSmartisan.apk \
  --decoded-framework-res /path/to/decoded/framework-res \
  --decoded-smartisanos-res /path/to/decoded/framework-smartisanos-res \
  --decoded-settings /path/to/decoded/SettingsSmartisan
```

## Modules

```text
core/       Public-API-only UI library for system and third-party applications
system/     Platform-only integration layer for hidden APIs and privileged code
catalog/    Public-API-only component catalog and compatibility test application
```

`core` is the reusable design system. It must remain buildable with the public
Android SDK. Code that needs `@hide` APIs, platform resources, or privileged
services belongs in `system`; it must never leak into the public AAR.

## AOSP source-tree use

Place this repository at:

```text
frameworks/libs/smartisanui
```

Consumers can add the library to an Android Soong module:

```bp
static_libs: ["SmartisanUiCore"],
```

Platform modules may additionally use `SmartisanUiSystem`.

## Third-party use

The `core` module is also a standard Android Gradle library. Build its AAR with:

```shell
./gradlew :core:assembleRelease
```

The output is written below `core/build/outputs/aar/`. The `catalog` Gradle app
depends only on `core`, so it acts as a guard against accidental platform API
dependencies.

## Conventions

- Android resources use the `smartisan_` prefix.
- Public Java APIs live under `org.opensmartisanos.ui`.
- The Core module uses `sdk_version: "current"`; never enable platform APIs there.
- Hidden APIs and privileged integrations live under
  `org.opensmartisanos.ui.system` in the System module.
- Compatibility namespaces for original Smartisan APIs must be based on an API
  inventory and added separately.
- Visual constants belong in design tokens rather than individual applications.
- Reference-derived assets must record their source and licensing status before
  they are committed.

## Initial roadmap

1. Inventory shared UI APIs and resources used by the source applications.
2. Establish measured color, typography, spacing, elevation, and motion tokens.
3. Expand the component catalog for screenshot comparison.
4. Add title bars, list rows, switches, buttons, text fields, dialogs, and
   preference components.
5. Integrate the library into Settings before expanding to other system apps.
