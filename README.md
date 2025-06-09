# ScriptGlance

**ScriptGlance** is an Android application designed for voice-controlled teleprompting and automatic speech recognition.  
To enable Ukrainian speech recognition features, you must manually add the Ukrainian Vosk model to your project as described below.

---

## Adding the Ukrainian Vosk Model

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
  
---

**The app will not work without the downloaded Ukrainian speech recognition model located in `assets/vosk-model-uk/`.**
