<div align="center">

# 📱 ScriptGlance Android

*Voice-controlled teleprompter application with automatic speech recognition*

[![Kotlin](https://img.shields.io/badge/kotlin-%237F52FF.svg?style=for-the-badge&logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)](https://developer.android.com/)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack_Compose-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Vosk](https://img.shields.io/badge/Vosk-Speech_Recognition-orange?style=for-the-badge)](https://alphacephei.com/vosk/)

---

**ScriptGlance** is an Android application designed for voice-controlled teleprompting and automatic speech recognition. The app provides a mobile teleprompter interface with Ukrainian speech recognition capabilities powered by Vosk.

</div>

---
<div align="center">
<table>
<tr>
<td width="50%" valign="top">

### 🎤 **Voice Recognition Features**
- 🇺🇦 Ukrainian speech recognition via Vosk
- 🔊 Real-time speech processing
- 🎙️ Microphone integration

### 📺 **Teleprompter Interface**
- 🎬 Mobile teleprompter display
- 📏 Text size customization
- 🎯 Reading position tracking

</td>
<td width="50%" valign="top">

### 📱 **Mobile Experience**
- 🔧 Native Android performance
- 📱 Portrait and landscape modes

### 🎬 **Presentation Features**
- 📄 Script loading and display
- 🎯 Focus mode for reading
- 🔄 Auto-scroll functionality

</td>
</tr>
</table>
</div>

---

## 🛠️ Tech Stack

<div align="center">

| Category | Technologies |
|----------|-------------|
| **Language** | ![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?style=flat-square&logo=kotlin&logoColor=white) |
| **Platform** | ![Android](https://img.shields.io/badge/Android-3DDC84?style=flat-square&logo=android&logoColor=white) |
| **Speech Recognition** | ![Vosk](https://img.shields.io/badge/Vosk-orange?style=flat-square) |
| **UI Framework**       | ![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-4285F4?style=flat-square&logo=jetpackcompose&logoColor=white) |
| **Build System** | ![Gradle](https://img.shields.io/badge/Gradle-02303A?style=flat-square&logo=gradle&logoColor=white) |
</div>

---

## 📋 Prerequisites

<div align="center">

| Requirement | Version | Status |
|-------------|---------|--------|
| ![Android Studio](https://img.shields.io/badge/Android%20Studio-3DDC84?style=flat-square&logo=android-studio&logoColor=white) | Latest | ✅ Required |
| **Android SDK** | API 21+ | ✅ Required |
| **Kotlin** | Latest | ✅ Required |
| **Gradle** | 7.0+ | ✅ Required |

</div>

---

## 🚀 Quick Start

### 📥 Installation

```bash
# Clone the repository
git clone https://github.com/ScriptGlance/android.git
cd android

# Open in Android Studio
# File -> Open -> Select the android folder
```

---

## 🎤 Adding the Ukrainian Vosk Model

**Note:**  
Vosk speech recognition models are not stored in this repository due to their large size.  
You need to download and add the model manually before building or running the app.

### How to set up the Ukrainian model:

1. **Download the Ukrainian model** from the official Vosk website:  
   [https://alphacephei.com/vosk/models](https://alphacephei.com/vosk/models)  
   (Recommended: `vosk-model-small-uk-v3`)

2. **Unzip the downloaded archive** and rename the extracted folder to:  
   ```
   vosk-model-uk
   ```

3. **Move the renamed folder into your app's assets directory:**  
   ```
   app/src/main/assets/vosk-model-uk/
   ```

<div align="center">

⚠️ **Important:** The app will not work without the downloaded Ukrainian speech recognition model located in `assets/vosk-model-uk/`.

</div>

---

## 🏗️ Project Structure

```
📁 app/
├── 🔧 build.gradle                    # App-level build configuration & dependencies
├── 🛡️ proguard-rules.pro           
└── 📁 src/
    └── 📁 main/
        ├── 📋 AndroidManifest.xml    
        ├── 📦 assets/
        │   └── 🇺🇦 vosk-model-uk/    # Ukrainian Vosk speech recognition model
        ├── 🎯 kotlin/
        │   └── 📁 com/
        │       └── 📁 scriptglance/
        │           ├── 💾 data/                    # Data sources & repositories
        │           │   ├── 🏠 local/              # Local storage (DataStore)
        │           │   ├── 📊 model/              # Data models & entities
        │           │   ├── 🌐 remote/             # Network APIs
        │           │   └── 🗄️ repository/         # Data access abstraction layer
        │           ├── 💉 di/                     # Dependency Injection configuration
        │           ├── 🧠 domain/                 # Business logic
        │           ├── 🎨 ui/                     # Presentation layer & UI components
        │           │   ├── 🏃 activity/           # Main Activity & lifecycle management
        │           │   ├── 🔧 common/             # Reusable UI components & utilities
        │           │   ├── 📱 screen/             # Feature-based screen components
        │           │   │   ├── 🔐 auth/           # Authentication & login screens
        │           │   │   ├── 💬 chat/           # Real-time chat interface
        │           │   │   ├── 👑 premium/        # Premium features & subscription UI
        │           │   │   ├── 📺 presentation/   # Teleprompter & presentation screens
        │           │   │   ├── 👤 profile/        # User profile management UI
        │           │   │   └── 📊 userDashboard/  # Main dashboard & navigation
        │           │   └── 🎭 theme/              # Material Design themes & styling
        │           ├── 🛠️ utils/                  # Helper functions
        │           └── 🚀 App                     # Application class & initialization
        └── 📁 res/
            ├── 📝 values/                         # App resources & configuration
            └── 🖼️ drawable/                       # Visual assets & graphics
```

---


<div align="center">
  
**Built with ❤️ for Ukrainian speech recognition**

[![Kotlin](https://img.shields.io/badge/Made%20with-Kotlin-purple?style=for-the-badge&logo=kotlin)](https://kotlinlang.org/)
[![Android](https://img.shields.io/badge/Built%20for-Android-green?style=for-the-badge&logo=android)](https://developer.android.com/)
[![Vosk](https://img.shields.io/badge/Powered%20by-Vosk-orange?style=for-the-badge)](https://alphacephei.com/vosk/)

---

*Last updated: June 12, 2025*

</div>
