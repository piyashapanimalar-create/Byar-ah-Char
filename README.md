# Byar ah char Translate (ဗျာအာချာ Translate)
**Target Hardware:** OnePlus Nord CE 2 Lite 5G (OxygenOS 12 / 13 / 14, Snapdragon 695 5G)

Native Android application for instant Burmese (Myanmar) to English and Tamil translations with:
- Floating chat-head bubble overlay across all apps (WhatsApp, Facebook, Chrome)
- Android 6.0+ `PROCESS_TEXT` text-selection menu translation
- Real Google Translate / Gemini backend with zero mock data
- Local Room Database history
- Background clipboard monitoring

---

## How to Build and Install APK using ONLY an Android Phone (No PC Required!)

You do **NOT** need a computer to compile this APK. Here are the 2 fastest ways directly from your phone:

### Method 1: GitHub Actions (Recommended & 100% Free - 3 Minutes)
1. In your phone's browser (Chrome), open GitHub.com.
2. Create a new repository named `byar-ah-char-translate`.
3. Upload the project ZIP or files (including the `.github/workflows/build-apk.yml` included in this project).
4. GitHub Actions will automatically start compiling the APK in the cloud.
5. Tap **Actions** tab on your phone -> Tap the latest build -> Download **ByarAhCharTranslate-Debug-APK**.
6. Open the downloaded `.apk` and tap **Install**!

### Method 2: On-Device Build via Termux (Terminal on Phone)
1. Install **Termux** from F-Droid (do not use Play Store version).
2. Open Termux and run:
   ```bash
   pkg update && pkg install openjdk-17 gradle git -y
   ```
3. Extract this project into your storage:
   ```bash
   cd ~/storage/downloads/ByarAhCharTranslate
   gradle assembleDebug
   ```
4. Your APK is built at:
   `app/build/outputs/apk/debug/app-debug.apk`
5. Tap it in your OnePlus Files app to install!

---

## OnePlus Nord CE 2 Lite 5G (OxygenOS) Optimization Guide

To make sure the floating bubble never disappears or gets killed by OxygenOS battery management:

1. **Overlay Permission**:
   - Go to **Settings** -> **Apps** -> **Special app access** -> **Display over other apps** -> Turn ON for **Byar ah char Translate**.
2. **Battery Optimization**:
   - Go to **Settings** -> **Battery** -> **More settings** -> **App battery management** -> Find **Byar ah char Translate** -> Select **Allow background activity** and set to **Don't optimize**.
3. **Lock in Recent Apps**:
   - Open Byar ah char Translate.
   - Swipe up to the Recent Apps view on your OnePlus.
   - Tap the three dots (⋮) above the app card and tap **Lock** (padlock icon). This prevents OxygenOS from closing the floating bubble service when clearing RAM.
