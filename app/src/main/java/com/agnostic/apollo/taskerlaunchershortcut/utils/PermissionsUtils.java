package com.agnostic.apollo.taskerlaunchershortcut.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

import androidx.core.content.ContextCompat;

public class PermissionsUtils {

    public static final int ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE = 0;

    public static String[] mPermissions = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE
    };

    public static boolean checkPermissions(Context context, String[] permissions) {
        int result;

        for (String p:permissions) {
            result = ContextCompat.checkSelfPermission(context,p);
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    public static void askPermissions(Activity context, String[] permissions) {

        int result;
        Logger.showToast(context, "Please grant permissions on next screen");
        try {Thread.sleep(1000);} catch (InterruptedException e) {e.printStackTrace();}

        for (String p:permissions) {
            result = ContextCompat.checkSelfPermission(context,p);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && result != PackageManager.PERMISSION_GRANTED) {
                Logger.logDebug(context, "Requesting Permissions");
                context.requestPermissions(new String[]{p}, 0);
            }
        }
    }

    public static boolean checkSystemAlertWindowPermission(Context context) {
        boolean result;

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
            return true;

        result = Settings.canDrawOverlays(context);
        if (!result) {
            Logger.logDebug(context, "App does not have SYSTEM_ALERT_WINDOW permission");
            return false;
        } else {
            Logger.logDebug(context, "App has SYSTEM_ALERT_WINDOW permission");
            return true;
        }
    }

    public static void askSystemAlertWindowPermission(Activity context) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + context.getPackageName()));
            context.startActivityForResult(intent, ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE);
        }
    }

}
