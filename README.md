# Amazfit Phone Remote

Control your Android phone from an Amazfit watch (Active 2 and other Zepp OS devices).

- Toggle GPS on/off from the wrist
- Launch configured apps without touching the phone
- Secure local bridge between the Zepp OS app and an Android companion service

## Architecture

```text
Watch (Zepp OS)  --BLE-->  Zepp App Side Service  --HTTP localhost-->  Android Companion
```

The watch app never talks to Android APIs directly. The Side Service inside the Zepp app forwards commands to the companion app on `127.0.0.1:8765`.

## Components

| Folder | Description |
|--------|-------------|
| `android-companion/` | Android foreground service with a local HTTP API |
| `zepp-watch-app/` | Zepp OS mini program for the watch |

## 1. Android companion setup

1. Open `android-companion/` in Android Studio.
2. Build and install the APK on your phone.
3. Open **Phone Remote** and copy the auth token.
4. Keep the service running (persistent notification).

### GPS control (one-time)

Android blocks GPS toggling unless the companion app has `WRITE_SECURE_SETTINGS`.

Connect the phone via USB debugging and run:

```bash
adb shell pm grant com.phoneremote.companion android.permission.WRITE_SECURE_SETTINGS
```

Without this grant, app launching still works, but GPS commands return an error.

## 2. Watch app setup

```bash
cd zepp-watch-app
npm install
zeus preview
```

Or use `zeus dev` with the Zepp simulator.

In the Zepp phone app:

1. Enable developer mode (tap version 7 times in About).
2. Install/preview the watch app.
3. Open app settings in Zepp.
4. Paste the auth token from the Android app.
5. Edit shortcuts JSON if needed.

### Default shortcuts

```json
[
  { "id": "gps_on", "label": "GPS ON", "action": "gps_on" },
  { "id": "gps_off", "label": "GPS OFF", "action": "gps_off" },
  { "id": "maps", "label": "Maps", "action": "launch", "package": "com.google.android.apps.maps" },
  { "id": "camera", "label": "Camera", "action": "launch", "package": "com.android.camera" }
]
```

Shortcut fields:

| Field | Required | Values |
|-------|----------|--------|
| `label` | yes | Text shown on the watch button |
| `action` | yes | `gps_on`, `gps_off`, or `launch` |
| `package` | for `launch` | Android package name |

Find package names with:

```bash
adb shell pm list packages | grep maps
```

## HTTP API (localhost)

| Method | Path | Auth | Body |
|--------|------|------|------|
| GET | `/health` | no | — |
| GET | `/api/gps` | no | — |
| POST | `/api/gps` | yes | `{ "enabled": true }` |
| POST | `/api/launch` | yes | `{ "package": "com.example.app" }` |

Auth header: `X-Auth-Token: <token from Android app>`

## Requirements

- Android phone with Zepp app installed
- Amazfit watch with Zepp OS 3.0+ (Active 2 Premium tested target)
- USB debugging for the one-time GPS permission grant
- Node.js + `@zeppos/zeus-cli` for watch development

## Limitations

- iOS is not supported (no Android companion bridge).
- The companion service must stay running on the phone.
- GPS toggle needs the ADB permission grant on most Android versions.
- Package names differ by phone manufacturer (e.g. Samsung Camera vs `com.android.camera`).

## License

MIT
