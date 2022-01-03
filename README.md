# ElectronicMonopoly
A distributed application in Android. It helps you to manage your financial information in the game "Monopoly".
 This application is a support and it has not all information about the game.

# Compilation
1. Java 8
2. Download Android SDK (sdkmanager)
- Directory:

```
./android-sdk
|- cmdline-tools (Command line tools only, https://developer.android.com/studio)
|-- latest (if not exists, create it)
```

- Env variables:
```bash
export ANDROID_SDK_ROOT="/Users/miguel/Documents/programs/android-sdk"
export ANDROID_HOME=$ANDROID_SDK_ROOT
export PATH="$ANDROID_SDK_ROOT/cmdline-tools/latest/bin:$PATH"
```

3. Install using sdkmanager
- build-tools;23.0.2
- platforms;android-23

4. Run: `./gradlew assembleDebug`, it will generate the apk

# Setup
First of all, the server side (Bank) has to be running in the same network. The bank is a stand-alone application and it can be 
found at: https://github.com/MinMiguelM/MonopolyBank.git. <br>
The bank is in Spanish, therefore it is necessary that you know some about it.

# Running
When the APP is running, the first screen takes two inputs:
* Host: the IP address where the server side is hosted. By default is: 10.42.0.1
* Name: a nickname that allows to identify a player.

# Future work
* Identifying available hosts.
