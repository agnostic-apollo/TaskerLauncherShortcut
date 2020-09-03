package com.agnostic.apollo.taskerlaunchershortcut;

import android.content.Context;
import android.content.Intent;

import androidx.fragment.app.Fragment;

public class LauncherHomeActivity extends SingleFragmentActivity {

    public static final String ARG_SHORTCUT_INTENT_URI = "arg_shortcut_intent_uri";
    public static final String ARG_FIRE_INTENT_FROM_HOST = "arg_fire_intent_from_host";
    public static final String ACTION_FIRE_SHORTCUT_INTENT_URI = "action_fire_shortcut_intent_uri";

    public static Intent newFireShortcutIntentUriIntent(Context context, String shortcutIntentUri, Intent fireIntentFromHost) {
        //Logger.logDebug(context, "Creating Intent For: \"" + shortcutIntentUri + "\"");
        Intent newIntent = new Intent(context, LauncherHomeActivity.class);
        newIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS | Intent.FLAG_ACTIVITY_NO_HISTORY);
        newIntent.setAction(ACTION_FIRE_SHORTCUT_INTENT_URI);
        newIntent.putExtra(ARG_SHORTCUT_INTENT_URI, shortcutIntentUri);
        newIntent.putExtra(ARG_FIRE_INTENT_FROM_HOST, fireIntentFromHost);
        return newIntent;
    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);

        LauncherHomeFragment fragment = (LauncherHomeFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if(fragment!=null)
            fragment.updateUI();
    }

    @Override
    protected Fragment createFragment() {
        return LauncherHomeFragment.newInstance();
    }
}