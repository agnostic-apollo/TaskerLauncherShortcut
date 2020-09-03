package com.agnostic.apollo.taskerlaunchershortcut.shortcuts;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;

//Activity to process pin shortcuts requests from other applications on Android >= 8
@TargetApi(Build.VERSION_CODES.O)
public class PinShortcutReceiver extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ShortcutReceiver.processReceivedShortcutIntent(this, getIntent());
        finish();
    }
}
