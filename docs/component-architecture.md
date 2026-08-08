# SmartisanUI component architecture

SmartisanUI is bounded by the Android public SDK. Core and Catalog do not depend on AndroidX,
Google Material, hidden framework APIs, or Settings application color tokens.

Components use the narrowest implementation that matches their behavior:

- Platform adapters subclass the corresponding `android.widget` control and provide Smartisan
  defaults through `Widget.SmartisanUi.*` styles. XML and caller setters always win.
- Compound components subclass a platform `ViewGroup` and compose public SDK child views.
- Custom-rendered controls keep their own drawing, animation, and interaction contracts instead of
  pretending to be Material controls.
- Dialogs and popups expose only the platform semantics they implement. The fixed bottom dialog is
  not a draggable Material bottom sheet.

Existing class names, superclasses, and public methods are compatibility API. Additive overloads are
allowed, while superclass or return-type changes require an explicit migration. `core/api/current.txt`
is the complete exported API baseline. ROM artwork is recorded under `core/provenance` and project
adaptations are labeled separately from source ports.
