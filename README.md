# TaskerLauncherShortcut

This is a basic Android Launcher App that was mainly created to be used as a plugin for the [Tasker App]. Tasker can call the plugin to run static, dynamic and pinned shortcuts using intents. In android versions `>=7.1`, only the default launcher app or currently active voice interaction service can run shortcuts, hence whenever a shortcut needs to be launched, the `TaskerLauncherShortcut` app needs to be set as the default launcher app, afterwards the normal launcher that the user uses needs to be set back to the default launcher. Setting the default launcher using background commands either requires `root` or `ADB` access, the sample [TaskerLauncherShortcut Tasker Project] can be used for automatically changing and reverting default launchers and launching shortcuts intents.

The app homescreen only shows a list of installed apps that can be started on click and it does not support adding shortcuts to the homescreen. It's options menu supports changing the default launcher by showing the android's `Choose Default Home` screen and also supports searching for static, dynamic and pinned shortcuts depending on android version, the selected shortcut's intent `Uri` is only copied to the clipboard for other uses like using it in the plugin inside Tasker. The pinned shortcut are of course only received by the launcher app when an app sends them and can't be searched. Previously pinned shortcuts can technically be shown but are not.

The `TaskerLauncherShortcut` app plugin action inside Tasker also has an internal `Search Shortcuts` button when you open the plugin configuration activity that allows the user to search and select static, dynamic or pinned shortcuts and automatically sets the plugin input field with the intent `Uri` of the shortcut selected.


TaskerLauncherShortcut was initially released on the [\[Plugin\]\[Beta\] TaskerLauncherShortcut Reddit Thread].
##


### Contents
- [Compatibility](#Compatibility)
- [Downloads](#Downloads)
- [Shortcut Types](#Shortcut-Types)
- [Shortcut Permissions And Access](#Shortcut-Permissions-And-Access)
- [Build Instructions](#Build-Instructions)
- [Current Features](#Current-Features)
- [Planned Features](#Planned-Features)
- [Issues](#Issues)
- [Worthy Of Note](#Worthy-Of-Note)
- [FAQs And FUQs](#FAQs-And-FUQs)
- [Changelog](#Changelog)
- [Contributions](#Contributions)
##


### Compatibility

- Android >= 4.1 (API level 16).
- For android `>=10` [restrictions on starting activities from background were introduced](https://developer.android.com/guide/components/activities/background-starts), so to run shortcuts from background tasks when the app is being used as a tasker plugin, the `Draw Over Other Apps` permission is required.
##


### Downloads

- [GitHub releases](https://github.com/agnostic-apollo/TaskerLauncherShortcut/releases).
##


### Shortcut Types

There are mainly 3 types of [Shortcuts](https://developer.android.com/guide/topics/ui/shortcuts), static, dynamic and pinned.

In Android `7.1(API 25)` new [ShortcutManager](https://developer.android.com/reference/android/content/pm/ShortcutManager) APIs were added for apps to create shortcuts and [LauncherApps](https://developer.android.com/reference/android/content/pm/LauncherApps) APIs for launcher apps to access and start shortcuts.



- Static shortcuts are for accessing content that is to remain consistent throughout the app's lifeycle. They can be added to the launcher homescreen like in nova launcher from the widgets list.
  - Static shortcuts that require special configuration can be created by declaring activities with the `ACTION_CREATE_SHORTCUT` [intent-filter](https://developer.android.com/guide/topics/manifest/intent-filter-element) in the `AndroidManifest.xml` of the app. These activities can then be queried by the launcher apps from the [PackageManager](https://developer.android.com/reference/android/content/pm/PackageManager) and started so that the user can configure and confirm the shortcut creation, the app can then send the final shortcut intent back to the launcher app as the activity result.
  - Static shortcuts that do not require special configuration can be entirely declared in the `res/xml/shortcuts.xml` resource file of the app in android `>=7.1`. The declaration includes the icon, label, action, component and any categories. These shortcuts can be queried by the launcher apps using the [LauncherApps.getShortcuts()](https://developer.android.com/reference/android/content/pm/LauncherApps#getShortcuts(android.content.pm.LauncherApps.ShortcutQuery,%20android.os.UserHandle)) function and passing the [FLAG_MATCH_MANIFEST](https://developer.android.com/reference/android/content/pm/LauncherApps.ShortcutQuery#FLAG_MATCH_MANIFEST) query flag. 


- Dynamic shortcuts are context sensitive and are displayed by launchers like nova launcher by long pressing the app icon and only exist in android `>=7.1`. Android apps can register dynamic shortcuts with the `ShortcutManager` at runtime by sending a list of [ShortcutInfo](https://developer.android.com/reference/android/content/pm/ShortcutInfo) objects to [ShortcutManager.addDynamicShortcuts()](https://developer.android.com/reference/android/content/pm/ShortcutManager#addDynamicShortcuts(java.util.List%3Candroid.content.pm.ShortcutInfo%3E)) function. Each `ShortcutInfo` contains information like the icon, label, the real intent and any extras that will be sent when that shortcut will be requested to be launched by the launcher. These shortcuts can be queried by the launcher apps using the [LauncherApps.getShortcuts()](https://developer.android.com/reference/android/content/pm/LauncherApps#getShortcuts(android.content.pm.LauncherApps.ShortcutQuery,%20android.os.UserHandle)) function and passing the [FLAG_MATCH_DYNAMIC](https://developer.android.com/reference/android/content/pm/LauncherApps.ShortcutQuery#FLAG_MATCH_DYNAMIC) query flag.

- Pinned shortcuts are sent by apps to the launcher when you press buttons like `Add to Home screen`, like pinning a website or chat shortcut on the launcher home.
  - In android `<8`, apps can send shortcuts to be pinned to the launchers with a broadcast intent with the `com.android.launcher.action.INSTALL_SHORTCUT` action, `EXTRA_SHORTCUT_INTENT` intent extra and `EXTRA_SHORTCUT_NAME` string extra using [Context.sendBroadcast()](https://developer.android.com/reference/android/content/Context#sendBroadcast(android.content.Intent)) function. The launcher apps can register a `BroadcastReceiver` with the `com.android.launcher.permission.INSTALL_SHORTCUT` permission and `com.android.launcher.action.INSTALL_SHORTCUT` action to receive these shortcuts. This is [no longer supported](https://android.googlesource.com/platform/frameworks/base/+/refs/heads/oreo-release/services/core/java/com/android/server/am/ActivityManagerService.java#19328) in android `>=8`.
  - In android `>=8`, apps can send shortcuts to be pinned to the launchers by creating a `ShortcutInfo` object and sending it to the [ShortcutManager.requestPinShortcut()](https://developer.android.com/reference/android/content/pm/ShortcutManager#requestPinShortcut(android.content.pm.ShortcutInfo,%20android.content.IntentSender)) function. The launcher apps can declare an `Activity` with the `android.content.pm.action.CONFIRM_PIN_SHORTCUT` [intent-filter](https://developer.android.com/guide/topics/manifest/intent-filter-element) to receive and confirm these shortcuts. The shortcuts owned by the launcher apps can be queried using the [LauncherApps.getShortcuts()](https://developer.android.com/reference/android/content/pm/LauncherApps#getShortcuts(android.content.pm.LauncherApps.ShortcutQuery,%20android.os.UserHandle)) function and passing the [FLAG_MATCH_PINNED](https://developer.android.com/reference/android/content/pm/LauncherApps.ShortcutQuery#FLAG_MATCH_PINNED) query flag.

Android docs are really really shitty for creating launchers, had to go through android default AOSP [launcher](https://android.googlesource.com/platform/packages/apps/Launcher3/+/refs/heads/master) [port](https://github.com/amirzaidi/Launcher3) and [Nova Launcher](https://play.google.com/store/apps/details?id=com.teslacoilsw.launcher&hl=en) source codes to figure lot of stuff out.
##


### Shortcut Permissions And Access
The configurable static shortcuts created using `ACTION_CREATE_SHORTCUT` action and pinned shortcuts created using `com.android.launcher.action.INSTALL_SHORTCUT` action that mainly exist in android `<7.1` could be started by any app that somehow got the intent (like extracting from launcher database) by sending them through java or using `am` commands.

The non configurable static shortcuts and dynamic shortcuts in android `>=7.1` and pinned shortcuts in android `>=8` can only be queried from the `ShortcutManager` and started by the default launcher app or currently active voice interaction service. The permission can be checked by an app using [hasShortcutHostPermission()](https://developer.android.com/reference/android/content/pm/LauncherApps#hasShortcutHostPermission()) function. If the app doesn't have the permission, the shortcut intent's desired action will not be successful, even though the target app may open. These shortcuts contain a special category called `com.android.launcher3.DEEP_SHORTCUT` and also have a string extra with the key `shortcut_id` which defines the id with which the shortcut is registered with the `ShortcutManager`.

The launcher apps do not have access to the real intents and their extras of the shortcuts stored by the `ShortcutManager`, likely for privacy reasons, they can only query intents using [LauncherApps.getShortcuts()](https://developer.android.com/reference/android/content/pm/LauncherApps#getShortcuts(android.content.pm.LauncherApps.ShortcutQuery,%20android.os.UserHandle)) function and get back `ShortcutInfo` objects with limited information like the icon, label and `shortcut_id`. The desired shortcut can then be started using the [LauncherApps.startShortcut()](https://developer.android.com/reference/android/content/pm/LauncherApps#startShortcut(java.lang.String,%20java.lang.String,%20android.graphics.Rect,%20android.os.Bundle,%20android.os.UserHandle)) function. 

Note that shortcuts generated on android `>=7.1` can't be used in android `<7.1` since `ShortcutManager` doesn't exist and so the real intent and it's extras have nowhere to be stored and apps can't publish them anyways.

Another thing is that there is a way for non configurable static shortcuts in android `>=7.1` to be shown in older versions as well, since nova launcher does it, but this has to yet to be investigated and the `TaskerLauncherShortcut` app doesn't support it currently. This is likely done by extracting them from `res/xml/shortcuts.xml` resource files of the app apks.

Due to the above reasons, the Tasker `Shortcut` action, `AutoShortcut`, `Java` intents or `am` commands are not going to work in android `>=7.1` or `>=8` for shortcuts with the `com.android.launcher3.DEEP_SHORTCUT` category. What can be done is either ask your default launcher dev to add support for the launcher to be used as a tasker plugin or ask for support for special intents that may be used to start shortcut intents stored in the launcher database or use the `TaskerLauncherShortcut` app tasker plugin action which takes a shortcut intent `Uri` as input and starts the shortcut. The sample [TaskerLauncherShortcut Tasker Project] can be used with the `TaskerLauncherShortcut` app for changing and reverting launchers and launching shortcuts intents.

Only configurable static shortcuts and pinned shortcuts for android `<8` are likely going to be `Non DEEP_SHORTCUT shortcuts`.
##


### Build Instructions

You will need the [android-locale-plugin-libraries] as local modules that have been patched so that the app can be built for `targetSdkVersion 29`. Currently, the `android-locale-plugin-libraries` is **experimental** and not merged with the upstream repo and so is not available from maven and so needs to be included as local modules instead.

- If you are not using a linux distro, then download zips of both repositories and extract them. Then copy all the module directories in `android-locale-plugin-libraries` directory to the root directory of the `TaskerLauncherShortcut` project. If you are using a linux distro, git clone the required repositories and create symlinks to locale libraries in root directory of the `TaskerLauncherShortcut` project (not app directory).

```
git clone https://github.com/agnostic-apollo/TaskerLauncherShortcut
git clone https://github.com/agnostic-apollo/android-locale-plugin-libraries

cd TaskerLauncherShortcut
for dir in annotationLib pluginApiLib pluginHostSdkLib testLib assertionLib pluginClientSdkLib spackleLib
do
   ln -s ../android-locale-plugin-libraries/$dir $dir
done
```

The final directory structure should be like the following.

```
- TaskerLauncherShortcut
  - annotationLib
  - app
    - src
  - assertionLib
  - pluginApiLib
  - pluginClientSdkLib
  - pluginHostSdkLib
  - spackleLib
  - testLib
```

- Import project in Android Studio using `File` -> `New` -> `Import Project`.
- Check the `app/build.gradle` file and optionally removed signing key information.
- Build in Android Studio using `Build` -> `Build Bundle(s)/APK(s)` -> `Build APK(s)`.
##


### Current Features

- Shows a list of installed packages
- Use as a [Tasker App] plugin to run static, dynamic and pinned shortcuts using intents from the background tasker tasks. 
##


### Planned Features

- Support static shortcuts for android < 7.1
##


### Issues

`-`
##


### Worthy Of Note

`-`
##


### FAQs And FUQs

Check [FAQs_And_FUQs.md](FAQs_And_FUQs.md) file for the **Frequently Asked Questions(FAQs)** and **Frequently Unasked Questions(FUQs)**.
##


### Changelog

Check [CHANGELOG.md](CHANGELOG.md) file for the **Changelog**.
##


### Contributions

`-`
##


[Tasker App]: https://play.google.com/store/apps/details?id=net.dinglisch.android.taskerm
[TaskerLauncherShortcut Tasker Project]: https://github.com/agnostic-apollo/TaskerLauncherShortcut-Tasker-Project
[android-locale-plugin-libraries]: https://github.com/agnostic-apollo/android-locale-plugin-libraries
[\[Plugin\]\[Beta\] TaskerLauncherShortcut Reddit Thread]: https://www.reddit.com/r/tasker/comments/gkounp/pluginbeta_taskerlaunchershortcut/
