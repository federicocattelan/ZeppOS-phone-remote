# Phone Remote — Zepp OS Watch App

A Zepp OS mini program for Amazfit smartwatches that puts two big, thumb-friendly
buttons on your wrist: **open Waze** on your phone (even if its screen is off or
locked) and **toggle GPS** on/off, with the button showing the current GPS state
at a glance.

The watch never talks to Android directly — it sends a request over BLE to a
Side Service running inside the Zepp app on the phone, which forwards it to a
small local HTTP API exposed by the [Android companion app](../android-companion).
See the [root README](../README.md) for the full-system overview and the
companion app setup.

## Features

- **Waze, one tap away** — wakes the phone screen if needed and launches Waze,
  no unlocking required.
- **GPS toggle with live state** — the button reads `GPS: ON` / `GPS: OFF` and
  changes color (green/gray) to reflect the phone's actual location setting,
  fetched on page load and updated after every tap.
- **Curve-aware round UI** — buttons and text are laid out to stay clear of the
  bezel on a circular display instead of using a generic rectangular grid.
- **No cloud, no accounts** — everything happens over `localhost` on the phone;
  the watch and phone talk to each other only through Zepp's own BLE channel.

## How it works

```text
┌─────────────┐   BLE    ┌────────────────────┐   HTTP (127.0.0.1)   ┌──────────────────────┐
│  Watch page │ ───────> │ Zepp App Side       │ ───────────────────> │ Android Companion App │
│ (this app)  │ <─────── │ Service (this app)  │ <─────────────────── │ (../android-companion) │
└─────────────┘          └────────────────────┘                       └──────────────────────┘
```

- **`page/`** — what you see and tap on the watch.
- **`app-side/`** — headless service that runs inside the Zepp phone app; it
  is the only part of this app that ever makes an HTTP call.
- **`setting/`** — the configuration screen shown inside the Zepp app (auth
  token, companion host/port, Waze package name).

## Supported watches

The app targets the `466x466-amazfit-active-2` device profile and declares
these platforms in `app.json`:

| Device | `deviceSource` |
|---|---|
| Amazfit Active 2 Premium (NFC) | `10092801` |
| Amazfit Active 2 Premium | `10092800` |
| Amazfit Active 2 (NFC) | `8913153` |
| Amazfit Active 2 | `8913152` |

Built and tested against **Zepp OS 4.2** (`apiVersion.target: "4.2"`,
`compatible: "4.0"`). Other round 466×466 devices may work but are untested;
adjust `platforms` in `app.json` to add them.

## Prerequisites

- [Node.js](https://nodejs.org/) 16+
- The Zeus CLI, installed globally:

  ```bash
  npm install -g @zeppos/zeus-cli
  ```

- Zepp app on your phone with **Developer Mode** enabled (open the Zepp app →
  Profile → About → tap the version number 7 times).
- The [Android companion app](../android-companion) installed and running, to
  actually execute commands (the watch UI works standalone, but every button
  press will report "Companion non raggiungibile" without it).

## Project structure

```text
zepp-watch-app/
├── app.js                    # App entry point / lifecycle logging
├── app.json                  # Manifest: appId, permissions, API version, device targets
├── app-side/
│   └── index.js               # Side Service — bridges the watch page to the companion HTTP API
├── page/
│   ├── index.js                # Main (and only) screen: state, click handlers, rendering
│   └── index.r.layout.js       # Hand-tuned widget coordinates for the round 466x466 display
├── setting/
│   └── index.js                # Settings page shown inside the Zepp app
├── utils/
│   └── shortcuts.js             # Default connection/package constants
├── scripts/
│   └── gen_buttons.py           # Generates the button background images (see below)
├── assets/
│   └── 466x466-amazfit-active-2/
│       ├── waze_normal.png / waze_press.png
│       └── gps_on_normal.png / gps_on_press.png / gps_off_normal.png / gps_off_press.png
└── package.json
```

> The other folders under `assets/` (e.g. `454x454-amazfit-t-rex-2`,
> `480x480-amazfit-balance`, …) are boilerplate scaffolded by `zeus create` for
> device families this app doesn't target. They're harmless and can be deleted
> if you only build for the Active 2 family.

## Getting started

```bash
cd zepp-watch-app
npm install
```

### Run in the simulator

```bash
npm run dev
```

Opens the app in the Zepp OS Simulator (desktop). Fastest way to iterate on
UI/layout, but the simulator has no BLE link to a real phone, so companion
calls will fail there — use it for visual/layout checks only.

### Install on a real watch

```bash
npm run preview
```

This builds the app and shows a QR code; scan it from the Zepp app to sideload
the app onto your paired watch. If the transfer stalls or the app never shows
up on the watch (common on phones with aggressive battery managers, e.g.
Xiaomi HyperOS), use the bridge instead:

```bash
npx zeus bridge
```

Then, inside the interactive prompt:

```text
bridge$ connect
bridge$ install
```

`zeus bridge` keeps a persistent connection to the Zepp app and gives a clear
`Install lite app result: success` (or a real error) instead of silently
failing, which makes it the more reliable option for day-to-day development.

### Build only (no install)

```bash
npm run build
```

All three scripts target `Amazfit Active 2 NFC (Round)` by default (see
`package.json`); change the `-t` flag if you add other device targets.

## Pairing with the Android companion

1. Install and open the [companion app](../android-companion) on your phone,
   note the **auth token** it shows.
2. Install this watch app (see above).
3. In the Zepp phone app, open **Phone Remote → settings (gear icon)**.
4. Fill in:

   | Field | Default | Notes |
   |---|---|---|
   | Auth token | *(empty)* | Paste it from the companion app — required, every command fails without it |
   | Server host | `127.0.0.1` | Only change this if you modify the companion to listen elsewhere |
   | Server port | `8765` | Must match the companion's configured port |
   | Package Waze | `com.waze` | Change if you sideloaded a different Waze build/variant |

5. Save. The watch app re-reads these values from `settingsStorage` on every
   request, so no reinstall is needed after changing them.

## On-watch UI

The page renders three widgets, positioned to stay inside the visible circle
of a 466×466 round display (see the comments in `index.r.layout.js` for the
curve-trimming rationale):

| Widget | Behavior |
|---|---|
| Status text | Shows transient state: `Phone Remote` → `Pronto` once the companion answers, or a progress/result message after each action, or `Companion non raggiungibile` if the phone/companion can't be reached |
| **Waze** button | Fixed light-blue pill with the Waze icon. Tap → asks the companion to wake the phone and launch the configured Waze package |
| **GPS** button | Starts as `GPS: ...`, then flips between green **`GPS: ON`** and gray **`GPS: OFF`** based on the companion's reported state; tap toggles it |

On page load, `checkHealth()` calls the companion's `/health` endpoint through
the side service to read the current GPS state before you've tapped anything.

## Communication protocol

The page talks to the Side Service via `BasePage#request` (two RPC methods);
the Side Service talks to the companion over plain HTTP with a shared token.

### Page ⇄ Side Service (BLE, internal)

| `method` | `params` | Returns |
|---|---|---|
| `HEALTH_CHECK` | — | Companion's `/health` response, e.g. `{ ok, version, gpsEnabled }` |
| `EXECUTE_SHORTCUT` | `{ action: "gps_on" \| "gps_off" \| "launch_waze" \| "launch", package? }` | `{ success, message, enabled? }` |

### Side Service ⇄ Companion (HTTP, `http://<serverHost>:<serverPort>`)

| Action | HTTP call | Header |
|---|---|---|
| `gps_on` / `gps_off` | `POST /api/gps` `{ "enabled": true\|false }` | `X-Auth-Token: <authToken>` |
| `launch_waze` | `POST /api/launch` `{ "package": "<wazePackage>" }` | `X-Auth-Token: <authToken>` |
| `launch` (generic) | `POST /api/launch` `{ "package": "<params.package>" }` | `X-Auth-Token: <authToken>` |
| health check | `GET /health` | none |

If `authToken` is empty, the Side Service short-circuits with
`{ success: false, message: "Auth token missing. Configure it in the Zepp app settings." }`
without making a network call. See the [root README](../README.md#http-api-localhost)
and `android-companion/app/src/main/java/com/phoneremote/companion/RemoteHttpServer.kt`
for the server-side implementation of these endpoints.

## Customizing the button graphics

The pill-shaped button backgrounds (with the icon baked in) are generated by
`scripts/gen_buttons.py` using Pillow, at 4× supersampling for clean
anti-aliased edges, then saved into
`assets/466x466-amazfit-active-2/`.

```bash
pip install pillow
python scripts/gen_buttons.py
```

The script expects pre-rendered icon glyphs next to it
(`waze_icon_normal_raw.png`, `waze_icon_press_raw.png`,
`gps_icon_on_normal_raw.png`, `gps_icon_on_press_raw.png`,
`gps_icon_off_normal_raw.png`, `gps_icon_off_press_raw.png`) drawn on the
exact same background color as the target pill, so edges blend without
needing alpha compositing. These raw glyphs aren't checked in (they were
produced once by rendering an SVG icon in a browser and cropping it); the
already-generated button PNGs under `assets/` are committed, so you only need
to run this script if you want to change colors, sizes, or icons — in which
case, supply your own `*_raw.png` glyphs or adapt `paste_icon()` to draw
vector shapes directly with Pillow.

Key tunables in the script:

| Constant | Meaning |
|---|---|
| `WAZE_SIZE` / `GPS_SIZE` | Button pixel dimensions |
| `RADIUS` | Pill corner radius |
| `ICON_BOX` / `WAZE_ICON_BOX` | Icon size inside the button |
| `ICON_PAD` / `WAZE_ICON_PAD` | Icon offset from the top-left corner |
| `*_COLOR` / `*_PRESS` | Normal/pressed background colors (RGBA) |

After regenerating images, bump `version.code`/`version.name` in `app.json`
(and `version` in `package.json`) before reinstalling, so the Zepp app treats
it as an update.

## Troubleshooting

- **App doesn't appear on the watch after `preview`/scanning the QR code** —
  the BLE transfer was likely interrupted by the phone's battery manager
  (common on Xiaomi/HyperOS). Whitelist the Zepp app from battery
  restrictions, enable autostart, and lock it in recents; or use
  `npx zeus bridge` (see above), which is more resilient.
- **App installs but a previous version's ghost entry stays / conflicts** —
  try a distinct `appId` in `app.json`; a clash with another registered app
  can cause silent install failures.
- **Buttons don't reflect the phone's current GPS state** — the Zepp OS
  `BUTTON` widget only supports live updates for `x/y/w/h/text` via
  `setProperty(hmUI.prop.MORE, …)` (all four geometry fields are required on
  every call), while `normal_src`/`press_src` must be reassigned directly on
  the widget instance — see `renderGpsButton()` in `page/index.js`.
- **"Companion non raggiungibile"** — make sure the companion app's
  foreground service is running on the phone, the auth token in the watch
  settings matches, and `serverHost`/`serverPort` point at the companion
  (defaults `127.0.0.1:8765` assume the Zepp app and companion are on the
  same phone).
- **Waze opens only while the companion app is in the foreground** — that's
  an Android-side background-activity-start restriction, fixed on the
  companion by granting it the "display over other apps" permission
  (`Settings.ACTION_MANAGE_OVERLAY_PERMISSION`) — see
  `android-companion/app/src/main/java/com/phoneremote/companion/MainActivity.kt`.

## License

MIT — see the [root README](../README.md).
