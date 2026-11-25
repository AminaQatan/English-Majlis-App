# English Majlis

Chat in everyday English with an Omani friend who corrects you kindly.

**Play:** https://aminaqatan.github.io/English-Majlis-App/

## Install it as an app

English Majlis is an installable web app. It works on phones, tablets and computers, opens full screen with its own icon, and keeps working without internet once it has been opened.

- **Android (Chrome):** open the link and tap **Install the app** on the first screen, or use the menu ⋮ → **Install app**.
- **iPhone / iPad (Safari):** open the link, tap **Share**, then **Add to Home Screen**.
- **Windows / Mac / Chromebook (Chrome or Edge):** open the link and click the install icon in the address bar.

## Android app (phones, tablets and smart TVs)

Every time the game files change, GitHub builds a real Android app automatically (see `.github/workflows/android.yml`).
Download **EnglishMajlis.apk** from this repository's **Releases** page:
https://github.com/AminaQatan/English-Majlis-App/releases/latest

- **Android phone or tablet:** open the APK file and allow "Install unknown apps" when asked. English Majlis then appears in the app list with its own icon.
- **Android TV / Google TV (for example Sony):** copy the APK to the TV (with a USB stick and a file manager, or an app such as *Send files to TV*), allow installing unknown apps for that file manager, and install it. It appears on the TV home screen with its own banner. Use the remote's arrows and OK button to play.
- The app has the game built in, so it works offline, uses the device's own voice, and listens through the device microphone.

## How to play

1. Choose your friend: **Muzna** (black abaya and shayla) or **Hamed** (white dishdasha with a kumma cap or a massar).
2. Type your name and choose your level: Beginner, Intermediate or Advanced.
3. Pick a situation and answer your friend by typing or by tapping the mic.

## Situations

Meet & greet · Restaurant · Hotel · Bus tickets · Airport · Clothes shop · Pharmacy · Directions

## Features

- Your friend speaks first and replies automatically after you finish talking.
- Your friend really listens: it repeats back your choices, answers your questions (prices, times, Wi-Fi, directions…), repeats slowly when you ask, gives an example when you don't know what to say, and asks again kindly when it doesn't understand.
- Mistakes are corrected straight away in a friendly, motivating way: spelling, grammar and more natural wording, with a short reason, **Practise it** and **Listen** buttons.
- Choose a **British** or **American** accent. The game picks the most natural English voice on each device.
- Stars, streaks and a "Well done" screen with the phrases to remember.
- Example answers ("Ideas") for players who get stuck.
- Respectful content only: no religion, romance, alcohol, pork or anything against Islamic values.

## Good to know

- On this website the game runs in **Practice mode**: built-in conversations and correction rules. The live AI conversation works when the game is opened inside Claude.
- The voice uses the device's own text-to-speech. For the most natural voice on iPhone or iPad, download an Enhanced or Premium English voice in Settings → Accessibility → Spoken Content → Voices. On Windows, Microsoft Edge has very natural voices.
- Hands-free talking needs Chrome or Safari and microphone permission.
- After uploading a new `index.html`, change the version in `sw.js` (for example from `english-majlis-v3` to `english-majlis-v4`) so installed apps pick up the update.

## Files

| File | What it is |
|---|---|
| `index.html` | The game |
| `manifest.webmanifest` | App name, colours and icons |
| `words.txt` | Word list for spelling corrections |
| `sw.js` | Offline support |
| `android/` | The Android app project (phones, tablets, TV) |
| `.github/workflows/android.yml` | Builds the Android app on GitHub |
| `icon-192.png`, `icon-512.png`, `icon-maskable-512.png`, `apple-touch-icon.png` | App icons |

---

Developed by **Ms. Amina Qatan**
