# Touch Control

### Control your Android phone with simple screen gestures.

**Touch Control** is a lightweight Android utility that replaces difficult-to-reach or worn-out physical buttons with an **invisible, full-screen gesture layer**.

Adjust volume, change screen brightness, lock the screen, or open the system power menu — without placing visible controls over your apps.

<p align="center">
  <a href="https://github.com/iamnaimul/TouchControl">
    <img src="https://img.shields.io/badge/GitHub-Repository-black?style=for-the-badge&logo=github" alt="GitHub Repository">
  </a>
  <img src="https://img.shields.io/badge/Android-9%2B-green?style=for-the-badge&logo=android" alt="Android 9+">
  <img src="https://img.shields.io/badge/Offline-100%25-blue?style=for-the-badge" alt="100% Offline">
  <img src="https://img.shields.io/badge/License-MIT-yellow?style=for-the-badge" alt="MIT License">
</p>

---

## 📱 Overview

Physical buttons can become difficult to use because of wear, damage, poor placement, or accessibility needs.

**Touch Control** turns the screen itself into a temporary control surface.

The application uses a transparent `WindowManager` overlay to capture touch gestures while remaining visually invisible. There are no floating buttons, handles, or permanent controls covering your screen.

### The result

**Touch → Gesture → System action**

Simple, fast, and distraction-free.

---

## ✨ Features

| Gesture         | Action                     |
| --------------- | -------------------------- |
| 👆 Swipe Up     | Increase volume            |
| 👇 Swipe Down   | Decrease volume            |
| 👉 Swipe Right  | Increase brightness        |
| 👈 Swipe Left   | Decrease brightness        |
| 👆👆 Double Tap | Lock the screen            |
| ✋ Long Press    | Open the system power menu |

### 🎯 Smart Gesture Detection

Volume and brightness gestures are **axis-locked**.

A vertical swipe controls **volume**.

A horizontal swipe controls **brightness**.

This prevents a single gesture from accidentally triggering both controls and keeps the interaction predictable.

### ⚡ Automatic Dismissal

The gesture layer automatically disappears after a short period of inactivity and is dismissed immediately when you leave the app.

### 🔒 Privacy First

Touch Control is designed to work completely offline.

* No internet permission
* No analytics
* No advertising
* No account
* No cloud service
* No data collection
* No network communication

Your data stays on your device.

---

## 🧩 How It Works

Touch Control creates a transparent, full-screen Android `WindowManager` overlay.

While the control layer is active:

1. Touch events are captured from the screen.
2. The gesture direction is detected.
3. The gesture is classified as vertical or horizontal.
4. The corresponding system action is performed.
5. The overlay automatically dismisses after inactivity.

There is no visible control panel sitting on top of your applications.

---

## 🔐 Permissions

Touch Control requires several Android permissions because it interacts directly with system-level functions.

| Permission            | Purpose                                       |
| --------------------- | --------------------------------------------- |
| `SYSTEM_ALERT_WINDOW` | Creates the invisible full-screen touch layer |
| `WRITE_SETTINGS`      | Allows volume and brightness control          |
| Device Admin          | Enables the double-tap screen-lock gesture    |
| Accessibility Service | Opens the Android system power menu           |

### Accessibility Privacy

The Accessibility Service is used **only** to invoke the Android global power dialog.

It does **not**:

* Read screen content
* Retrieve window content
* Monitor accessibility events
* Collect user information

The service explicitly disables window-content retrieval.

You can inspect the implementation in:

* `MainActivity.kt`
* `PowerAccessibilityService.kt`
* `LockAdminReceiver.kt`

---

## 🚀 Requirements

* **Android 9.0 (API 28) or higher**
* Android Studio
* Android SDK
* Gradle Wrapper included with the project

---

## 🛠️ Build From Source

### 1. Clone the repository

```bash
git clone https://github.com/iamnaimul/TouchControl.git
```

### 2. Enter the project directory

```bash
cd TouchControl
```

### 3. Open in Android Studio

Open the project directory in Android Studio and allow Gradle to synchronize.

### 4. Build the debug APK

Linux/macOS:

```bash
./gradlew assembleDebug
```

Windows:

```bat
gradlew.bat assembleDebug
```

The generated APK will be available under:

```text
app/build/outputs/apk/debug/
```

---

## ⚙️ First-Time Setup

On the first launch, Touch Control guides you through the required permissions.

The setup process requests each permission individually.

Once the required permissions are granted, the gesture layer becomes available automatically whenever the application is opened.

---

## 🔋 Lightweight by Design

Touch Control is designed to remain simple and lightweight.

The application does not require:

* A backend
* Internet access
* Cloud synchronization
* User accounts
* Remote services
* Continuous data processing

Only minimal local state is stored to remember whether the initial setup has been completed.

---

## 🛡️ Privacy

Privacy is a core design principle of Touch Control.

The application does not request the Android `INTERNET` permission and does not make network requests.

No personal information is collected, transmitted, or stored remotely.

The only persistent application state is the local setup-completion status stored using Android `SharedPreferences`.

---

## 📌 Current Limitations

The project is still under development.

Current limitations include:

* Accessibility Service usage requires appropriate policy declaration if distributed through Google Play.
* Large-screen devices such as tablets and foldables have not yet been fully tested.
* Automated tests have not yet been implemented.

---

## 🗺️ Roadmap

Future improvements may include:

* [ ] More gesture customization
* [ ] Adjustable gesture sensitivity
* [ ] Improved tablet and foldable support
* [ ] More device compatibility testing
* [ ] Automated testing
* [ ] Additional accessibility improvements
* [ ] Further performance optimization

---

## 📂 Project Structure

```text
TouchControl/
├── app/
│   └── src/
├── gradle/
│   └── wrapper/
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
├── gradlew
├── gradlew.bat
├── LICENSE
└── README.md
```

---

## 📸 Screenshots

> Screenshots can be added here to demonstrate the setup process and gesture controls.

```text
Coming soon
```

---

## 📄 License

Touch Control is released under the **MIT License**.

See the [`LICENSE`](LICENSE) file for the complete license text.

---

## 👨‍💻 Author

**Naimul Hassan**

Teacher • Developer • Technology Enthusiast

GitHub: [@iamnaimul](https://github.com/iamnaimul)

YouTube: [DORPON](https://www.youtube.com/c/DORPON)

---

## ⭐ Support the Project

If you find **Touch Control** useful, consider giving the repository a ⭐ on GitHub.

Your support helps encourage further development and improvements.

<p align="center">

**Built with ❤️ by Naimul**

</p>
