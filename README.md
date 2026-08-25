# Touch Control

**An invisible, full-screen gesture layer for volume, brightness, screen lock, and the power menu — built for phones with a broken, worn-out, or hard-to-reach hardware button.**

Touch Control replaces the physical buttons with swipes and taps on the screen itself. No visible UI sits on top of your apps — the control layer is fully transparent and only intercepts touches while it's active.

## Features

| Gesture | Action |
|---|---|
| Swipe up / down | Volume up / down |
| Swipe left / right | Brightness down / up |
| Double-tap | Lock the screen |
| Long-press | Open the power menu (reboot / power off / emergency) |

- Vertical and horizontal gestures are axis-locked per gesture — a single swipe controls *either* volume *or* brightness, never both, so there's no accidental cross-triggering.
- The overlay auto-dismisses after 2 seconds of inactivity, and immediately when you leave the app.
- 100% offline. No network permission, no analytics, no data collection of any kind.

## How it works

The app draws a transparent, full-screen `WindowManager` overlay and reads raw touch events off it. There's no visible button or handle — the entire screen becomes the control surface for as long as the overlay is up.

## Permissions — and why each one is needed

This app requests a few sensitive-looking permissions. Here's exactly what each one is for and nothing more:

| Permission | Why it's needed |
|---|---|
| `SYSTEM_ALERT_WINDOW` | Draws the invisible, full-screen touch-capture layer. |
| `WRITE_SETTINGS` | Changes system volume and screen brightness directly from a gesture. |
| Device Admin (`force-lock` policy only) | Powers the double-tap-to-lock gesture via `DevicePolicyManager.lockNow()`. No other device admin policy is requested. |
| Accessibility Service | Used *only* to call `performGlobalAction(GLOBAL_ACTION_POWER_DIALOG)` for the long-press gesture. It does not read screen content (`canRetrieveWindowContent="false"`) and does not listen to accessibility events. |

No permission is used for anything beyond what's listed above — you can verify this directly in `MainActivity.kt`, `PowerAccessibilityService.kt`, and `LockAdminReceiver.kt`.

> **Note on Google Play:** using the Accessibility API for anything other than assisting users with disabilities falls outside Google Play's normal accessibility-tool policy. If you plan to publish this on the Play Store, you'll need to frame and declare it (with the required justification/video) as an assistive tool for users who can't use a physical button — this is a store-listing/policy step, not a code change.

## Requirements

- Android 9.0 (API 28) or higher
- Android Studio (Gradle and the Android Gradle Plugin are managed by the wrapper/project config)

## Building

```bash
git clone https://github.com/iamnaimul/touchcontrol.git
cd touchcontrol
```

Open the project in Android Studio and run it on a device or emulator, or build from the command line:

```bash
./gradlew assembleDebug
```

## First-time setup

On first launch, the app walks you through granting the four permissions above, one at a time. Once all four are granted, the gesture layer activates automatically every time you open the app.

## Privacy

Touch Control has no `INTERNET` permission and makes no network calls. Nothing is collected, logged, or transmitted. The only persisted state is a single boolean (whether one-time setup is complete), stored locally in `SharedPreferences`.

## Known limitations / roadmap

- Accessibility Service usage needs a Play Console policy declaration before this can be published on the Play Store (see note above).
- Not yet tested on large screens (tablets/foldables) under Android 16's relaxed orientation-lock behavior.
- No automated tests yet.

## Author

Naimul
