package com.agnostic.apollo.taskerlaunchershortcut;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.agnostic.apollo.taskerlaunchershortcut.utils.Logger;

public class ShortcutChooserActivity extends SingleFragmentActivity {

    public static final String ARG_SHORTCUT_INTENT_URI = "arg_shortcut_intent_uri";
    public static final String ACTION_CHOOSE_SHORTCUT = "action_choose_shortcut";
    public static final int REQUEST_CHOOSE_SHORTCUT = 0;
    public static final int REQUEST_CREATE_SHORTCUT = 1;
    public static final String CHOOSE_SHORTCUT_RETURN_STATE = "choose_shortcut_return_state";
    public static final int CHOOSE_SHORTCUT_RETURN_STATE_SUCCESS = 0;
    public static final int CHOOSE_SHORTCUT_RETURN_STATE_FAILED = 0;

    public static Intent newShortcutTypeChooserIntent(Context context) {
        Intent newIntent = new Intent(context, ShortcutChooserActivity.class);
        newIntent.setAction(ACTION_CHOOSE_SHORTCUT);
        newIntent.setFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        return newIntent;
    }

    public static void returnShortcutIntentUri(FragmentActivity context, String shortcutIntentUri) {
        if(context == null || shortcutIntentUri==null)
            return;

        Logger.logDebug(context, "Returning Shortcut Intent Uri From ShortcutChooserActivity: \"" + shortcutIntentUri + "\"");
        Logger.showToast(context, "Shortcut Intent Uri: \"" + shortcutIntentUri + "\"");

        if(context.getCallingActivity()!=null) {

            Intent resultIntent = new Intent();
            resultIntent.putExtra(CHOOSE_SHORTCUT_RETURN_STATE, CHOOSE_SHORTCUT_RETURN_STATE_SUCCESS);
            resultIntent.putExtra(ARG_SHORTCUT_INTENT_URI, shortcutIntentUri);
            context.setResult(Activity.RESULT_OK, resultIntent);
            context.finish();
        } else {
            ClipboardManager clipboard = (ClipboardManager) context.getSystemService(CLIPBOARD_SERVICE);
            if (clipboard != null) {
                clipboard.setPrimaryClip(ClipData.newPlainText("text", shortcutIntentUri));
                Logger.showToast(context, "Shortcut Uri Copied To Clipboard");
            }
        }
    }

    public static void cancel(FragmentActivity context) {
        Intent resultIntent = new Intent();
        resultIntent.putExtra(CHOOSE_SHORTCUT_RETURN_STATE, CHOOSE_SHORTCUT_RETURN_STATE_FAILED);
        if(context!=null) {
            context.setResult(Activity.RESULT_OK, resultIntent);
            context.finish();
        }
    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);

        ShortcutTypeChooserFragment fragment = (ShortcutTypeChooserFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if(fragment!=null)
            fragment.updateUI();
    }

    @Override
    protected Fragment createFragment() {
        return ShortcutTypeChooserFragment.newInstance();
    }
}