# SmartisanUiLib

Shared Smartisan-inspired UI components and design tokens for OpenSmartisanOS.

The library is intended to provide a single visual and behavioral foundation for
OpenSmartisanOS system applications. It will contain reusable themes, controls,
dialogs, preferences, drawables, and motion specifications derived from measured
reference behavior.

## Source-tree location

Place this repository at:

```text
frameworks/libs/smartisanui
```

Consumers can add the library to an Android Soong module:

```bp
static_libs: ["SmartisanUiLib"],
```

## Conventions

- Android resources use the `smartisan_` prefix.
- Public Java APIs live under `org.opensmartisanos.ui`.
- Compatibility namespaces for original Smartisan APIs must be based on an API
  inventory and added separately.
- Visual constants belong in design tokens rather than individual applications.
- Reference-derived assets must record their source and licensing status before
  they are committed.

## Initial roadmap

1. Inventory shared UI APIs and resources used by the source applications.
2. Establish measured color, typography, spacing, elevation, and motion tokens.
3. Implement a component catalog application for screenshot comparison.
4. Add title bars, list rows, switches, buttons, text fields, dialogs, and
   preference components.
5. Integrate the library into Settings before expanding to other system apps.
