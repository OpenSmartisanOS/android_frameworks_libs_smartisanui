# Smartisan common component scope

This library ports only UI families that Smartisan modified and reused across applications. App
counts come from static call-site analysis of 59 applications in the R2 8.5.3 ROM.

## Core families

| Family | ROM evidence | Apps | Public component |
| --- | --- | ---: | --- |
| Title bars | `TitleBar`, `MenuDialogTitleBar` | 42 | `SmartisanTitleBar`, `SmartisanDialogTitleBar` |
| Progress dialog | `SmartisanProgressDialog` | 33 | `SmartisanProgressDialog` |
| Menu dialog | `MenuDialog` | 29 | `SmartisanMenuDialog` |
| Switch | `SwitchEx` | 23 | `SmartisanSwitch` |
| Buttons | `SmartisanButton`, `ShadowButton` | 20 | `SmartisanButton` |
| Grouped rows | `ListContentItem*` | 20 | `SmartisanListItem` |
| Search | `SearchBar` | 16 | `SmartisanSearchBar` |
| Popup menu | `SmartisanListPopupMenu` | 15 | `SmartisanPopupMenu` |
| Tips and empty state | `TipsView`, `SmartisanBlankView` | 15 / 9 | `SmartisanTipsBar`, `SmartisanEmptyView` |
| Segmented choice | `ButtonTabGroup` | 9 | `SmartisanSegmentedControl` |

Second-stage common controls are quick-delete and password editors, label editor, bottom bar,
editable tab switcher, slider, circle/download progress, snackbar, and button group.

Date/time/number pickers, calendars, BHM, sector menu, search business flows, candidates, app lock,
tab switcher, TNT, and unmodified platform widgets are intentionally excluded.

## First-layer status

The first layer is complete. Core contains the title bars, buttons, switch, grouped rows, setting
rows, phone search bar, menu and progress dialogs, anchored list popup, tips, empty state, and
segmented control. It also includes the ROM tab switcher's shared bottom/overflow editing flow.
The Catalog exposes interactive variants of every family, and the release AAR
is verified from a separate `minSdk 23` Android application.

## Porting rule

Java is a faithful source port from `smartisanos.jar`: retain rendering, state, measurement, and
interaction behavior while replacing unavailable hidden APIs with public-SDK equivalents. Bitmap
and Nine-patch files remain byte-identical. XML resources only receive deterministic resource-name
remapping so the AAR cannot collide with application resources.
