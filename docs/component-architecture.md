# SmartisanUI component architecture

SmartisanUI is bounded by the Android public SDK. Core and Catalog do not depend on AndroidX,
Google Material, hidden framework APIs, or Settings application color tokens.

Components use the narrowest implementation that matches their behavior:

- Platform adapters subclass the corresponding `android.widget` control and provide Smartisan
  defaults through `Widget.SmartisanUi.*` styles. XML and caller setters always win.
- Compound components subclass a platform `ViewGroup` and compose public SDK child views.
- Custom-rendered controls keep their own drawing, animation, and interaction contracts instead of
  pretending to be Material controls.
- Dialogs and popups expose only the platform semantics they implement. Bottom actions use
  `SmartisanBottomMenuPopupWindow`; there is no Material-style draggable bottom sheet API.

Existing class names, superclasses, and public methods are compatibility API. Additive overloads are
allowed, while superclass or return-type changes require an explicit migration. API version 3 has
one named break from v1: `SmartisanBottomSheetDialog` is replaced by
`SmartisanBottomMenuPopupWindow`, whose platform semantics match the original bottom menu.
`core/api/v1.txt` is immutable; `core/api/current.txt` is the complete API 3 export, and
`core/api/r2-mapping.json` records approved changes plus the original DEX access mapping; the
checker validates the complete member set of every mapped SDK class rather than class names alone.
The raw class, field and method access flags are stored in `core/api/r2-dex-access.txt`; they can be
reproduced from the hash-pinned `smartisanos.jar` with `tools/dump_dex_access.py --mapping`.

The title drop-down selector follows the original compound structure:
`SmartisanSpinnerView` owns the normal, drop and range modes, while
`SmartisanWheelTextView` performs the original text drawing and vertical wheel interaction.
The older `SmartisanSpinner extends android.widget.Spinner` remains available only as a
compatibility platform adapter; Catalog demonstrates the compound selector.

Required original layouts, selectors, NinePatch artwork, localized values and animations are stored
directly under `core/src/main/res`. Neither Gradle nor Soong imports resources from a ROM while
building. The manifests under `core/provenance` separately pin the source artifact, canonical
decoded source, named transformation recipe and vendored destination hashes. Missing or unknown
recipes, conflicting hashes and resources outside the ledger fail the boundary check. Coverage is
not based on `git diff HEAD`: all current resources must either be manifest-tracked or byte-identical
to the immutable pre-port commit pinned by `tools/check_sdk_boundaries.sh`, so it remains effective
after the changes are committed.

The ordinary boundary check verifies destination hashes and does not require licensed ROM files. The
optional `tools/rom/audit_r2_resources.py` command additionally requires every declared original
artifact and both canonical decoded resource trees, checks their hashes, and replays transforms
that have a machine-readable `transform`. One-to-one XML namespace remaps also receive a normalized
tree/attribute structure comparison. Public-SDK substitutions recorded only as named review recipes
cannot be mechanically reconstructed; the strict audit reports those separately after verifying
both their decoded source and vendored destination. The same audit reproduces one canonical Java
source tree from the pinned `smartisanos.jar` and a separate framework Alert source root with the
hash-pinned JADX 1.5.6 engine; missing, duplicate or undeclared ledger entries fail verification.
The decompiled trees remain temporary audit outputs and are not stored in the repository. Neither
audit path is a build input.
Public-SDK replacements are limited to unavailable private infrastructure such as the pager,
indicator, vibration and external-display state.

`SmartisanGroupMenuAdapter` uses `Menu.performIdentifierAction()` as its no-configuration public
SDK fallback. Callers using that fallback must assign a unique non-zero item ID to every actionable
item. Programmatic menus that reuse `Menu.NONE`, or otherwise reuse an ID, must instead install
`setOnMenuItemActionListener`; that callback receives the exact selected `MenuItem` and takes
precedence over identifier dispatch. This is the public-SDK replacement for R2's hidden
`MenuBuilder.performItemAction(MenuItemImpl, ...)` call.
