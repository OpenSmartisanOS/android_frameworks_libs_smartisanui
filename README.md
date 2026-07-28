# Smartisan UI

Shared Smartisan-inspired UI components and design tokens for OpenSmartisanOS.

The library is intended to provide a single visual and behavioral foundation for
OpenSmartisanOS system applications. It will contain reusable themes, controls,
dialogs, preferences, drawables, and motion specifications derived from measured
reference behavior.

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
