package com.agnostic.apollo.taskerlaunchershortcut.shortcuts;

import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.LauncherApps;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.ShortcutInfo;
import android.os.Build;
import android.os.Parcelable;
import android.os.Process;
import android.text.TextUtils;
import androidx.annotation.RequiresApi;

import com.agnostic.apollo.taskerlaunchershortcut.ShortcutChooserActivity;
import com.agnostic.apollo.taskerlaunchershortcut.ShortcutsLab;
import com.agnostic.apollo.taskerlaunchershortcut.utils.Logger;
import com.agnostic.apollo.taskerlaunchershortcut.utils.QueryPreferences;

public class ShortcutReceiver {

    private static final String ACTION_INSTALL_SHORTCUT =
            "com.android.launcher.action.INSTALL_SHORTCUT";

    public static void processReceivedShortcutIntent(Context context, Intent intent) {
        if (intent!=null)
            Logger.logDebug(context, "Processing Intent To ShortcutReceiver: \"" + intent.toUri(0));
        else
            return;

        if (intent.getComponent() == null) {
            Logger.logError(context, "Ignoring intent that does not have a component set");
            return;
        }

        String shortcutIntentUri = convertShortcutIntentToUri(context, intent);
        if (shortcutIntentUri!=null) {
            Logger.logDebug(context, "shortcutIntentUri: \"" + shortcutIntentUri + "\"");
            Logger.showToast(context, "Pinned Shortcut Received\nPlease Return To Shortcut Chooser Screen");
            QueryPreferences.setPinnedShortcutIntentUri(context, shortcutIntentUri);
        } else {
            Logger.logError(context, "Failed to convert shortcut intent to Uri");
        }

    }

    public static String convertShortcutIntentToUri(Context context, Intent intent) {

        if (intent==null)
            return null;

        if (intent.getComponent() == null) {
            return null;
        }

        ShortcutInfo shortcutInfo = null;
        String shortcutName = null;
        Intent shortcutIntent = null;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            shortcutInfo = createShortcutInfoFromPinItemRequest(context, getPinItemRequest(intent));
            if (shortcutInfo != null) {
                Logger.logDebug(context, "shortcutInfo: \"" + shortcutInfo.toString() + "\"");
                if (shortcutInfo.getShortLabel() != null) {
                    shortcutName = shortcutInfo.getShortLabel().toString();
                } else {
                    shortcutName = "";
                }
                ShortcutsLab shortcutsLab = ShortcutsLab.get(context);
                shortcutIntent = shortcutsLab.createDeepShortcutFromShortcutInfo(shortcutInfo);
            } else {
                Logger.logError(context, "Pin Request ShortcutInfo is null");
                return null;
            }

        } else {

            if (intent.getAction() != null) {
                if (!intent.getAction().equals(ACTION_INSTALL_SHORTCUT)) {
                    Logger.logError(context, "Ignoring intent that does not have an action that equals \"" + ACTION_INSTALL_SHORTCUT + "\"");
                    return null;
                }
            }

            shortcutName = intent.getStringExtra(Intent.EXTRA_SHORTCUT_NAME);
            shortcutIntent = intent.getParcelableExtra(Intent.EXTRA_SHORTCUT_INTENT);

            // Legacy shortcuts are only supported for primary profile.
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN && Process.myUserHandle().equals(intent.getParcelableExtra(Intent.EXTRA_USER))) {
                Logger.logError(context, "Legacy shortcuts are only supported for primary profile");
                return null;
            }
        }

        if (shortcutName == null || shortcutName.isEmpty()) {
            Logger.logError(context, "Shortcut name is null or empty");
            return null;
        } else
            Logger.logDebug(context, "shortcutName: \"" + shortcutName + "\"");

        if (shortcutIntent == null) {
            Logger.logError(context, "Shortcut intent is null");
            return null;
        } else
            Logger.logDebug(context, "shortcutIntent: \"" + shortcutIntent.toUri(0) + "\"");

        if (!hasPermissionForActivity(context, shortcutIntent, intent.getComponent().getPackageName())) {
            // The app is trying to add a shortcut without sufficient permissions
            Logger.logError(context, "Ignoring malicious intent " + intent.toUri(0));
            return null;
        }

        return shortcutIntent.toUri(0);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private static ShortcutInfo createShortcutInfoFromPinItemRequest(Context context, LauncherApps.PinItemRequest request) {
        if (request != null &&
                request.getRequestType() == LauncherApps.PinItemRequest.REQUEST_TYPE_SHORTCUT &&
                request.isValid()) {
            ShortcutInfo shortcutInfo = request.getShortcutInfo();
            if (shortcutInfo!=null && shortcutInfo.getIntent() != null)
                Logger.logDebug(context, "shortcutIntent: \"" + shortcutInfo.getIntent().toUri(0) + "\"");
            request.accept();
            return shortcutInfo;
        } else {
            return null;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private static LauncherApps.PinItemRequest getPinItemRequest(Intent intent) {
        Parcelable extra = intent.getParcelableExtra(LauncherApps.EXTRA_PIN_ITEM_REQUEST);
        return extra instanceof LauncherApps.PinItemRequest ? (LauncherApps.PinItemRequest) extra : null;
    }

    /**
     * Returns true if {@param srcPackage} has the permission required to start the activity from
     * {@param intent}. If {@param srcPackage} is null, then the activity should not need
     * any permissions
     */
    public static boolean hasPermissionForActivity(Context context, Intent intent, String srcPackage) {

        PackageManager mPm = context.getPackageManager();

        ResolveInfo target = mPm.resolveActivity(intent, 0);
        if (target == null) {
            // Not a valid target
            return false;
        }
        if (TextUtils.isEmpty(target.activityInfo.permission)) {
            // No permission is needed
            return true;
        }
        if (TextUtils.isEmpty(srcPackage)) {
            // The activity requires some permission but there is no source.
            return false;
        }

        // Source does not have sufficient permissions.
        if(mPm.checkPermission(target.activityInfo.permission, srcPackage) !=
                PackageManager.PERMISSION_GRANTED) {
            return false;
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            // These checks are sufficient for below M devices.
            return true;
        }

        // On M and above also check AppOpsManager for compatibility mode permissions.
        if (TextUtils.isEmpty(AppOpsManager.permissionToOp(target.activityInfo.permission))) {
            // There is no app-op for this permission, which could have been disabled.
            return true;
        }

        // There is no direct way to check if the app-op is allowed for a particular app. Since
        // app-op is only enabled for apps running in compatibility mode, simply block such apps.

        try {
            return mPm.getApplicationInfo(srcPackage, 0).targetSdkVersion >= Build.VERSION_CODES.M;
        } catch (PackageManager.NameNotFoundException e) { Logger.logError(context, e.getMessage()); }

        return false;
    }
}
