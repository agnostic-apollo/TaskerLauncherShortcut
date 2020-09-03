package com.agnostic.apollo.taskerlaunchershortcut;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.LauncherApps;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.ShortcutInfo;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Parcel;
import android.os.Process;
import android.os.UserHandle;
import android.util.DisplayMetrics;

import androidx.annotation.RequiresApi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.agnostic.apollo.taskerlaunchershortcut.model.ShortcutInfoItem;
import com.agnostic.apollo.taskerlaunchershortcut.utils.Logger;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static android.content.Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED;
import static android.content.pm.LauncherApps.ShortcutQuery.FLAG_MATCH_DYNAMIC;
import static android.content.pm.LauncherApps.ShortcutQuery.FLAG_MATCH_MANIFEST;
import static android.content.pm.LauncherApps.ShortcutQuery.FLAG_MATCH_PINNED;


public class ShortcutsLab {

    private static ShortcutsLab sShortcutsLab;
    private Context mContext;
    private SQLiteDatabase mDatabase;

    public static final String TYPE_STATIC_SHORTCUTS = "static_shortcuts";
    public static final String TYPE_DYNAMIC_SHORTCUTS = "dynamic_shortcuts";
    public static final String TYPE_PINNED_SHORTCUTS = "pinned_shortcuts";
    public static final String TYPE_ALL_SHORTCUTS = "all_shortcuts";

    public static ShortcutsLab get(Context context) {
        if (sShortcutsLab == null) {
            sShortcutsLab = new ShortcutsLab(context);
        }
        return sShortcutsLab;
    }
    private ShortcutsLab(Context context)
    {
        mContext = context.getApplicationContext();
    }

    public List<ShortcutInfoItem> getStaticShortcuts(String packageName)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            List<ShortcutInfoItem> shortcuts = new ArrayList<>();
            shortcuts.addAll(getStaticShortcutsFromPackageManager(packageName));
            shortcuts.addAll(getShortcutsFromShortcutManager(TYPE_STATIC_SHORTCUTS, packageName));
            Collections.sort(shortcuts);
            return shortcuts;
        }
        else
            return getStaticShortcutsFromPackageManager(packageName);
    }

    public List<ShortcutInfoItem> getDynamicShortcuts(String packageName)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1)
            return getShortcutsFromShortcutManager(TYPE_DYNAMIC_SHORTCUTS, packageName);
        else
            return new ArrayList<>();
    }

    public List<ShortcutInfoItem> getPinnedShortcuts(String packageName)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1)
            return getShortcutsFromShortcutManager(TYPE_PINNED_SHORTCUTS, packageName);
        else
            return new ArrayList<>();
    }

    @RequiresApi(api = Build.VERSION_CODES.N_MR1)
    private List<ShortcutInfoItem> getShortcutsFromShortcutManager(String type, String packageName) {
        PackageManager pm = mContext.getPackageManager();
        LauncherApps launcherApps = (LauncherApps) mContext.getSystemService(Context.LAUNCHER_APPS_SERVICE);

        List<ShortcutInfoItem> shortcuts = new ArrayList<>();

        if(launcherApps==null) {
            Logger.logError(mContext, "LAUNCHER_APPS_SERVICE is Unexpectedly Null");
            return shortcuts;
        }
        // Only the default launcher is allowed to start shortcuts
        if (!launcherApps.hasShortcutHostPermission()) {
            Logger.logError(mContext, "Calling ShortcutsLab.getShortcutsFromShortcutManager(), but does not have launcherApps.hasShortcutHostPermission()");
            return shortcuts;
        }

        LauncherApps.ShortcutQuery query = new LauncherApps.ShortcutQuery();

        if(packageName!=null && !packageName.isEmpty())
            query.setPackage(packageName);

        if(type.equals(TYPE_STATIC_SHORTCUTS))
            query.setQueryFlags(FLAG_MATCH_MANIFEST);
        else if(type.equals(TYPE_DYNAMIC_SHORTCUTS))
            query.setQueryFlags(FLAG_MATCH_DYNAMIC);
        else if(type.equals(TYPE_PINNED_SHORTCUTS))
            query.setQueryFlags(FLAG_MATCH_PINNED);
        else if(type.equals(TYPE_ALL_SHORTCUTS))
            query.setQueryFlags(FLAG_MATCH_MANIFEST | FLAG_MATCH_DYNAMIC | FLAG_MATCH_PINNED);
        else
            return shortcuts;

        List<UserHandle> userHandles = new ArrayList<>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            userHandles = launcherApps.getProfiles();
        else {

            userHandles.add(Process.myUserHandle());
        }

        List<ShortcutInfo> shortcutInfos = new ArrayList<>();

        for (UserHandle userHandle : userHandles) {
            List<ShortcutInfo> currentShortcutInfos = launcherApps.getShortcuts(query, userHandle);
            if (currentShortcutInfos != null && currentShortcutInfos.size() > 0) {
                shortcutInfos.addAll(currentShortcutInfos);
            }
        }

        for (ShortcutInfo shortcutInfo : shortcutInfos) {
            ShortcutInfoItem shortcut = new ShortcutInfoItem();

            if(!shortcutInfo.isEnabled())
                continue;

            shortcut.setId(shortcutInfo.getId());
            shortcut.setShortLabel(shortcutInfo.getShortLabel());
            shortcut.setLongLabel(shortcutInfo.getLongLabel());
            shortcut.setAppLabel(getAppLabelFromComponentName(mContext, shortcutInfo.getActivity()));
            shortcut.setActivity(shortcutInfo.getActivity());
            shortcut.setUserHandle(shortcutInfo.getUserHandle());
            int userId = getUserIdFromShortcutInfo(shortcutInfo);
            shortcut.setUserId(userId);
            shortcut.setIcon(launcherApps.getShortcutIconDrawable(shortcutInfo, DisplayMetrics.DENSITY_DEFAULT));
            if(shortcutInfo.getIntent()!=null) {
                Intent[] intents = shortcutInfo.getIntents();
                shortcut.setIntents(intents);
            }
            shortcut.setExtras(shortcutInfo.getExtras());
            shortcuts.add(shortcut);
        }

        Collections.sort(shortcuts);

        return shortcuts;
    }

    private List<ShortcutInfoItem> getStaticShortcutsFromPackageManager(String packageName) {
        PackageManager pm = mContext.getPackageManager();
        List<ShortcutInfoItem> shortcuts = new ArrayList<>();

        Intent intent = new Intent(Intent.ACTION_CREATE_SHORTCUT);

        List<ResolveInfo> allApps = pm.queryIntentActivities(intent, 0);
        for(ResolveInfo resolveInfo:allApps) {

            ShortcutInfoItem shortcut = new ShortcutInfoItem();
            ComponentName componentName = new ComponentName(resolveInfo.activityInfo.applicationInfo.packageName, resolveInfo.activityInfo.name);

            if(packageName!=null && !packageName.isEmpty() && !packageName.equals(componentName.getPackageName()))
                continue;

            shortcut.setId(resolveInfo.loadLabel(pm).toString().replace(" ", "-"));
            shortcut.setShortLabel(resolveInfo.loadLabel(pm));
            shortcut.setAppLabel(getAppLabelFromComponentName(mContext, componentName));
            shortcut.setActivity(componentName);
            shortcut.setIcon(resolveInfo.activityInfo.loadIcon(pm));
            shortcut.setIntent(new Intent(Intent.ACTION_CREATE_SHORTCUT)
                    .setComponent(componentName));
            shortcuts.add(shortcut);
        }

        Collections.sort(shortcuts);

        return shortcuts;
    }

    public List<ShortcutInfoItem> searchShortcuts(List<ShortcutInfoItem> shortcuts, String search)
    {
        List<ShortcutInfoItem> matchedShortcuts = new ArrayList<>();

        for(ShortcutInfoItem shortcut:shortcuts) {
            if(shortcut.getShortLabel().toString().toLowerCase().contains(search.toLowerCase()) ||
                    shortcut.getIntent().toString().toLowerCase().contains(search.toLowerCase()))
                matchedShortcuts.add(shortcut);
        }
        return matchedShortcuts;
    }

    public static String getAppLabelFromComponentName(Context context, ComponentName componentName) {
        if(componentName==null)
            return "";

        try {
            PackageManager pm = context.getPackageManager();
            ApplicationInfo info = pm.getApplicationInfo(componentName.getPackageName(), PackageManager.GET_META_DATA);
            return (String) pm.getApplicationLabel(info);
        } catch (PackageManager.NameNotFoundException e) {
            Logger.logStackTrace(context, e);
            return "";
        }
    }

    private Drawable getActivityIcon(ComponentName componentName) {
        if(componentName==null)
            return null;

        PackageManager pm = mContext.getPackageManager();

        Intent intent = new Intent();
        intent.setComponent(componentName);
        ResolveInfo resolveInfo = pm.resolveActivity(intent, 0);

        if (resolveInfo != null)
            return resolveInfo.loadIcon(pm);
        else
            return null;
    }

    public Intent createDeepShortcutFromShortcutInfoItem(ShortcutInfoItem shortcutInfo) {
        Intent intent = new Intent();
        intent.setAction("android.intent.action.MAIN");
        intent.addCategory("com.android.launcher3.DEEP_SHORTCUT");
        ComponentName componentName = shortcutInfo.getActivity();
        if(componentName!=null) {
            intent.setComponent(componentName);
            intent.setPackage(componentName.getPackageName());
        } else
            return null;

        intent.setFlags(FLAG_ACTIVITY_NEW_TASK | FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        intent.putExtra("shortcut_id", shortcutInfo.getId());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            intent.putExtra("profile", shortcutInfo.getUserId());
        } else
            intent.putExtra("profile", "-1");

        return intent;
    }

    @RequiresApi(api = Build.VERSION_CODES.N_MR1)
    public Intent createDeepShortcutFromShortcutInfo(ShortcutInfo shortcutInfo) {
        Intent intent = new Intent();
        intent.setAction("android.intent.action.MAIN");
        intent.addCategory("com.android.launcher3.DEEP_SHORTCUT");
        intent.setComponent(shortcutInfo.getActivity());
        intent.setPackage(shortcutInfo.getPackage());
        intent.setFlags(FLAG_ACTIVITY_NEW_TASK | FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        intent.putExtra("shortcut_id", shortcutInfo.getId());
        int userId = getUserIdFromShortcutInfo(shortcutInfo);
        intent.putExtra("profile", userId);
        return intent;
    }

    public Intent createStaticShortcutFromShortcutInfoItemAndExtras(ShortcutInfoItem shortcutInfo, Intent extras_intent) {
        Intent intent = new Intent();
        intent.setAction("android.intent.action.MAIN");
        intent.addCategory("com.android.launcher3.DEEP_SHORTCUT");
        ComponentName componentName = shortcutInfo.getActivity();
        if(componentName!=null) {
            intent.setComponent(componentName);
            intent.setPackage(componentName.getPackageName());
        } else
            return null;

        intent.setFlags(FLAG_ACTIVITY_NEW_TASK | FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        intent.putExtra("shortcut_id", shortcutInfo.getId());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            intent.putExtra("profile", shortcutInfo.getUserId());
        } else
            intent.putExtra("profile", "-1");

        return intent;
    }

    @RequiresApi(api = Build.VERSION_CODES.N_MR1)
    public int getUserIdFromShortcutInfo(ShortcutInfo shortcutInfo) {

        Parcel parcel = Parcel.obtain();
        shortcutInfo.writeToParcel(parcel, 0);
        return parcel.readInt();

    }

}