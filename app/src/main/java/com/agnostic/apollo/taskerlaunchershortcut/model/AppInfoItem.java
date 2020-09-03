package com.agnostic.apollo.taskerlaunchershortcut.model;

import android.graphics.drawable.Drawable;


public class AppInfoItem implements Comparable<AppInfoItem> {

    private CharSequence label;
    private CharSequence packageName;
    private Drawable icon;

    public CharSequence getLabel() {
        return label;
    }

    public void setLabel(CharSequence label) {
        this.label = label;
    }

    public CharSequence getPackageName() {
        return packageName;
    }

    public void setPackageName(CharSequence packageName) {
        this.packageName = packageName;
    }

    public Drawable getIcon() {
        return icon;
    }

    public void setIcon(Drawable icon) {
        this.icon = icon;
    }

    @Override
    public int compareTo(AppInfoItem app) {
        return this.getLabel().toString().compareTo(app.getLabel().toString());
    }
}
