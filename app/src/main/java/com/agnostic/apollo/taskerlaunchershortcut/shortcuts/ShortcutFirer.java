package com.agnostic.apollo.taskerlaunchershortcut.shortcuts;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.LauncherApps;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.os.UserHandle;

import com.agnostic.apollo.taskerlaunchershortcut.TaskerPlugin;
import com.agnostic.apollo.taskerlaunchershortcut.utils.Logger;
import com.agnostic.apollo.taskerlaunchershortcut.utils.PermissionsUtils;

import java.net.URISyntaxException;

import static android.content.Context.LAUNCHER_APPS_SERVICE;
import static com.agnostic.apollo.taskerlaunchershortcut.LauncherHomeActivity.ARG_FIRE_INTENT_FROM_HOST;
import static com.agnostic.apollo.taskerlaunchershortcut.LauncherHomeActivity.ARG_SHORTCUT_INTENT_URI;
import static com.agnostic.apollo.taskerlaunchershortcut.TaskerPlugin.Setting.RESULT_CODE_FAILED_PLUGIN_FIRST;
import static com.agnostic.apollo.taskerlaunchershortcut.TaskerPlugin.Setting.VARNAME_ERROR_MESSAGE;
import static com.agnostic.apollo.taskerlaunchershortcut.TaskerPlugin.VARIABLE_PREFIX;

public class ShortcutFirer {

    public static void fireShortcutIntentUriIntent(Context context, Intent intent) {
        try {
            if(context==null)
                return;

            //if an intent is received to send an intent uri
            String shortcutIntentUri = intent.getStringExtra(ARG_SHORTCUT_INTENT_URI);
            Intent fireIntentFromHost = intent.getParcelableExtra(ARG_FIRE_INTENT_FROM_HOST);

            //Logger.logDebug(context, "Fire Shortcut Intent Received: \"" + shortcutIntentUri + "\"");
            if (shortcutIntentUri != null && !shortcutIntentUri.isEmpty()) {

                Intent shortcutIntentUriIntent;
                int result_code = 1;
                String error_message = "";


                //This is required because of android 10 background activity start restrictions
                boolean permissionsGranted = true;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                    permissionsGranted = PermissionsUtils.checkSystemAlertWindowPermission(context);

                if(!permissionsGranted) {
                    error_message = "Permission Exception: Apps Cannot Start Activities From Background Without SYSTEM_ALERT_WINDOW Permission";
                    Logger.logErrorAndShowToast(context, error_message);
                    sendResultBundleToTasker(context, fireIntentFromHost, result_code, error_message);
                    return;
                }

                try {
                    shortcutIntentUriIntent = Intent.parseUri(shortcutIntentUri, 0);
                } catch (URISyntaxException e) {
                    error_message = "URISyntaxException: Invalid Shortcut Intent Uri";
                    Logger.logErrorAndShowToast(context, error_message);
                    Logger.logStackTrace(context, e);
                    sendResultBundleToTasker(context, fireIntentFromHost, result_code, error_message);
                    return;
                }

                if (shortcutIntentUriIntent.hasCategory("com.android.launcher3.DEEP_SHORTCUT")) {
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N_MR1) {
                        error_message = "Dynamic or Pinned DEEP_SHORTCUT Intents Can Only Be Sent On API>=" + Build.VERSION_CODES.N_MR1;
                        Logger.logErrorAndShowToast(context, error_message);
                        sendResultBundleToTasker(context, fireIntentFromHost, result_code, error_message);
                        return;
                    }

                    LauncherApps launcherApps = (LauncherApps) context.getSystemService(LAUNCHER_APPS_SERVICE);
                    if (launcherApps == null) {
                        error_message = "LAUNCHER_APPS_SERVICE is Unexpectedly Null";
                        Logger.logErrorAndShowToast(context, error_message);
                        sendResultBundleToTasker(context, fireIntentFromHost, result_code, error_message);
                        return;
                    }

                    if (!launcherApps.hasShortcutHostPermission()) {
                        error_message = "TaskerLauncherShortcut Must Be Set As Default Launcher To Send Dynamic or Pinned DEEP_SHORTCUT Intents";
                        Logger.logErrorAndShowToast(context, error_message);
                        sendResultBundleToTasker(context, fireIntentFromHost, result_code, error_message);
                        return;
                    }

                    String packageName = shortcutIntentUriIntent.getPackage();
                    String shortcutId = shortcutIntentUriIntent.getStringExtra("shortcut_id");
                    int profile = shortcutIntentUriIntent.getIntExtra("profile", -1);

                    Logger.logDebug(context, "packageName: \"" + packageName + "\", shortcutId: \"" + shortcutId + "\", profile: \"" + profile + "\"");

                    if (packageName == null || shortcutId==null) {
                        error_message = "Package Name or Shortcut ID is null: Invalid Shortcut Intent Uri";
                        Logger.logErrorAndShowToast(context, error_message);
                        sendResultBundleToTasker(context, fireIntentFromHost, result_code, error_message);
                        return;
                    }

                    try {
                        //send dynamic shortcut intent
                        Logger.logDebug(context, "Sending Shortcut Intent");
                        launcherApps.startShortcut(packageName, shortcutId, (Rect) null, (Bundle) null, UserHandle.getUserHandleForUid(profile));
                        Logger.logDebug(context, "Shortcut Intent Sent");
                        result_code = 0;
                    } catch (ActivityNotFoundException e) {
                        error_message = "ActivityNotFoundException: Invalid Shortcut Intent Uri";
                        Logger.logErrorAndShowToast(context, error_message);
                        Logger.logStackTrace(context, e);
                    }

                } else {
                    try {
                        //send shortcut intent
                        Logger.logDebug(context, "Sending Shortcut Intent");
                        shortcutIntentUriIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        context.startActivity(shortcutIntentUriIntent);
                        Logger.logDebug(context, "Shortcut Intent Sent");
                        result_code = 0;
                    } catch (ActivityNotFoundException e) {
                        error_message = "ActivityNotFoundException: Invalid Shortcut Intent Uri";
                        Logger.logErrorAndShowToast(context, error_message);
                        Logger.logStackTrace(context, e);
                    }
                }

                sendResultBundleToTasker(context, fireIntentFromHost, result_code, error_message);
            }



        } catch (Exception e) {
            Logger.logStackTrace(context, e);
        }
    }

    private static void sendResultBundleToTasker(Context context, Intent fireIntentFromHost, int result_code, String error_message) {

        Bundle resultBundle = new Bundle();

        Logger.logDebug(context, "result_code: \"" + result_code + "\", error_message: \"" + error_message + "\"");

        // If result_code is not negative
        if(result_code>=0) {
            // Set %result_code
            String result_code_variable_name = VARIABLE_PREFIX + "result_code";
            if (TaskerPlugin.variableNameValid(result_code_variable_name))
                resultBundle.putString(result_code_variable_name, String.valueOf(result_code));

            // If plugin action failed
            if (result_code > 0) {
                // Set %err. does not work currently for unknown reason
                String err_variable_name = VARIABLE_PREFIX + "err";
                if (TaskerPlugin.variableNameValid(err_variable_name))
                    resultBundle.putString(err_variable_name, String.valueOf(RESULT_CODE_FAILED_PLUGIN_FIRST + result_code));

                // Set %errmsg
                String errmsg_variable_name = VARNAME_ERROR_MESSAGE;
                if (TaskerPlugin.variableNameValid(errmsg_variable_name))
                    resultBundle.putString(errmsg_variable_name, error_message);
            }
        }

        // Send result back to Tasker
        if(fireIntentFromHost!=null)
            TaskerPlugin.Setting.signalFinish( context, fireIntentFromHost, TaskerPlugin.Setting.RESULT_CODE_OK, resultBundle );
    }

}
