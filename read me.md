# VISION AI 3.0 — Omega Edition

**A zero-dependency, single-file Java holographic AI assistant and cross-device command center for macOS and Android.**

VISION AI 3.0 combines a **Three.js 9,000-particle 3D holographic HUD**, **MediaPipe real-time hand gesture tracking**, **hands-free Web Speech recognition**, **macOS system automation**, and **wireless Android Debug Bridge (ADB) device control** inside a single self-contained Java file (`src/Main.java`).

---

## Key Features

* **Interactive 3D Holographic Core (Three.js):**
  * Renders 9,000 dynamic particles with real-time morphing across 4 visual modes:
    1. **Core Sphere** (Crimson Red with orbital ring)
    2. **Workplace Armor / Mark LXXXV** (Cyan humanoid armor silhouette)
    3. **Biometric Helix** (Emerald DNA double-helix matrix)
    4. **Quantum Vortex** (Gold 3-arm spiral galaxy)
  * Includes a 28-bar live audio equalizer, CRT scanline overlay, and real-time Mac CPU, battery, and volume telemetry.
* **Touchless Hand Gesture Control (MediaPipe Hands):**
  * Tracks hand landmarks via webcam to rotate the 3D hologram in space and pinch/spread with one or two hands to scale the core dynamically.
* **Continuous Voice & Terminal Command Engine:**
  * Uses continuous browser speech recognition (`en-IN`) paired with macOS native `/usr/bin/say` text-to-speech, protected by a single-speaker mutex and cooldown lock to prevent audio echo or duplicate triggers.
  * Includes an embedded HUD command bar and one-click quick chips for silent execution.
* **Universal macOS Application & System Automation:**
  * Recursively indexes `/Applications`, `/System/Applications`, `~/Applications`, and Spotlight (`mdfind`) at startup to launch or quit any installed Mac app by voice (e.g., *"Vision Safari"*, *"Open IntelliJ"*, *"Close Notes"*), with automatic web-app fallback for 20+ platforms (WhatsApp, LinkedIn, Unstop, GitHub, YouTube, etc.).
  * Controls system volume, screen lock, sleep, shutdown, and desktop screenshots.
* **Wireless Android Control via ADB:**
  * Connects over Wi-Fi using Android Wireless Debugging to toggle mobile Bluetooth and Wi-Fi, wake/unlock the screen, place phone calls, and launch installed apps or search the Google Play Store remotely.
* **Live Notification HUD & WhatsApp Quick-Reply:**
  * Displays incoming alerts with one-click or voice-dictated quick replies routed directly to WhatsApp.

---

## Project Structure

```text
VISION-AI-OMEGA/
├── src/
│   └── Main.java        # Complete HTTP server, 3D HUD, Voice Brain, Mac & ADB controllers
└── README.md
```

---

## Getting Started

### Prerequisites
* **macOS** (Apple Silicon or Intel)
* **Java 17+** (JDK 17, 21, or 25+ with `com.sun.net.httpserver`)
* **Google Chrome** (recommended for Web Speech API microphone support)
* **Android Platform-Tools (`adb`)** *(optional, required only for wireless phone control)*

### Run via IntelliJ IDEA
1. Clone or download this repository and open the project folder in **IntelliJ IDEA**.
2. Open `src/Main.java` and click the green **Run (`▶`)** button.
3. The local server automatically starts on `http://localhost:8080` (or the next free port `8080`–`8090`) and launches the HUD in your browser.

### Run via macOS Terminal
```bash
cd src
javac Main.java
java VisionAgent
```

### Wireless Android Setup (Optional)
1. Enable **Developer options** and **Wireless debugging** on your Android device (connected to the same Wi-Fi network as your Mac).
2. Pair and connect in Terminal:
   ```bash
   adb pair <PHONE_IP>:<PAIRING_PORT>
   adb connect <PHONE_IP>:<CONNECT_PORT>
   adb devices
   ```
3. VISION AI automatically detects the connected ADB device and updates the `VIVO PHONE (ADB)` telemetry status to `CONNECTED`.

---

## Voice & Terminal Commands

| Category | Example Commands | Action |
| :--- | :--- | :--- |
| **Mac Applications** | `"Vision Safari"`, `"Open Calculator"`, `"Open VS Code"` | Launches any installed macOS `.app` |
| **Web Platforms** | `"Open Facebook"`, `"Open Unstop"`, `"Open LinkedIn"` | Opens web apps in your default browser |
| **Close Apps** | `"Close Safari"`, `"Quit Notes"` | Gracefully quits the running macOS app |
| **Hologram Modes** | `"Workplace mode"`, `"Helix mode"`, `"Galaxy mode"`, `"Core mode"` | Morphs the 3D particle formation and HUD theme |
| **Mac System** | `"Volume up"`, `"Mute"`, `"Screenshot"`, `"Lock screen"`, `"Check battery"` | Executes macOS hardware and power controls |
| **Android (ADB)** | `"Turn on phone Bluetooth"`, `"Unlock phone"`, `"Open phone YouTube"` | Controls your paired Android phone wirelessly |
| **Calls & Media** | `"Call Rahul"`, `"Play Believer on YouTube"` | Places a call or searches YouTube |

---

## Author

Developed by **Bharath Kumar R** ([@itsmerbk](https://github.com/itsmerbk))