package com.agnostic.apollo.taskerlaunchershortcut;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.sqlite.SQLiteDatabase;
import com.agnostic.apollo.taskerlaunchershortcut.model.AppInfoItem;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class AppsLab {

    private static AppsLab sAppsLab;
    private Context mContext;
    private SQLiteDatabase mDatabase;

    public static AppsLab get(Context context) {
        if (sAppsLab == null) {
            sAppsLab = new AppsLab(context);
        }
        return sAppsLab;
    }
    private AppsLab(Context context)
    {
        mContext = context.getApplicationContext();
    }

    public List<AppInfoItem> getApps()
    {

        PackageManager pm = mContext.getPackageManager();
        List<AppInfoItem> apps = new ArrayList<>();

        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);

        List<ResolveInfo> allApps = pm.queryIntentActivities(intent, 0);
        for(ResolveInfo resolveInfo:allApps) {
            AppInfoItem app = new AppInfoItem();
            app.setLabel(resolveInfo.loadLabel(pm));
            app.setPackageName(resolveInfo.activityInfo.packageName);
            app.setIcon(resolveInfo.activityInfo.loadIcon(pm));
            apps.add(app);
        }

        Collections.sort(apps);

        return apps;
    }

    public List<AppInfoItem> searchAppsByName(String search)
    {
        List<AppInfoItem> apps = getApps();
        List<AppInfoItem> matchedApps = new ArrayList<>();

        for(AppInfoItem app:apps) {
            if(app.getLabel().toString().toLowerCase().contains(search.toLowerCase()))
                matchedApps.add(app);
        }
        return matchedApps;
    }

}