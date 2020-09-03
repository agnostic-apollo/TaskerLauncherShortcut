package com.agnostic.apollo.taskerlaunchershortcut.shortcuts;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.agnostic.apollo.taskerlaunchershortcut.utils.Logger;

//Intent receiver used to install legacy shortcuts from other applications on Android < 8
public class InstallShortcutReceiver extends BroadcastReceiver {

    public void onReceive(Context context, Intent intent) {
        if (intent != null) {
            Logger.logDebug(context, "Processing Intent: \"" + intent.toUri(0));

            if (intent.getAction() != null) {
                ShortcutReceiver.processReceivedShortcutIntent(context, intent);
            }
        }
    }
}
