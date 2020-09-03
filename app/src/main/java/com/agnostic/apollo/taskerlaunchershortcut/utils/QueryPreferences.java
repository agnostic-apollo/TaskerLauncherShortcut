package com.agnostic.apollo.taskerlaunchershortcut.utils;


import android.content.Context;
import android.preference.PreferenceManager;

public class QueryPreferences {

    private static final String LOGGING_STATE = "logging_state";
    private static final String PINNED_SHORTCUT_INTENT_URI = "pinned_shortcut_intent_uri";

    public static final Boolean DEFAULT_LOGGING_STATE = true;
    public static final String DEFAULT_PINNED_SHORTCUT_INTENT_URI = "";

    public static boolean getLoggingState(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(LOGGING_STATE, DEFAULT_LOGGING_STATE);
    }

    public static void setLoggingState(Context context, Boolean loggingState) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(LOGGING_STATE, loggingState)
                .apply();
    }

    public static String getPinnedShortcutIntentUri(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(PINNED_SHORTCUT_INTENT_URI, DEFAULT_PINNED_SHORTCUT_INTENT_URI);
    }

    public static void setPinnedShortcutIntentUri(Context context, String pinnedShortcutIntentUri) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString(PINNED_SHORTCUT_INTENT_URI, pinnedShortcutIntentUri)
                .apply();
    }
}