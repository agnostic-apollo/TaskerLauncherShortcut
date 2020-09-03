package com.agnostic.apollo.taskerlaunchershortcut.utils;

import android.os.BaseBundle;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;

import androidx.annotation.RequiresApi;

public class BundleUtils {
    /**
     * Creates a new {@link Bundle} based on the specified {@link PersistableBundle}.
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP_MR1)
    public static Bundle toBundle(PersistableBundle persistableBundle) {
        if (persistableBundle == null) {
            return null;
        }
        Bundle bundle = new Bundle();
        bundle.putAll(persistableBundle);
        return bundle;
    }

    /**
     * Creates a new {@link PersistableBundle} from the specified {@link Bundle}.
     * Will ignore all values that are not persistable, according
     * to {@link #isPersistableBundleType(Object)}.
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP_MR1)
    public static PersistableBundle toPersistableBundle(Bundle bundle) {
        if (bundle == null) {
            return null;
        }
        PersistableBundle persistableBundle = new PersistableBundle();
        for (String key : bundle.keySet()) {
            Object value = bundle.get(key);
            if (isPersistableBundleType(value)) {
                putIntoBundle(persistableBundle, key, value);
            }
        }
        return persistableBundle;
    }

    /**
     * Checks if the specified object can be put into a {@link PersistableBundle}.
     *
     * @see <a href="https://android.googlesource.com/platform/frameworks/base/+/master/core/java/android/os/PersistableBundle.java#49">PersistableBundle Implementation</a>
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP_MR1)
    public static boolean isPersistableBundleType(Object value) {
        return ((value instanceof PersistableBundle) ||
                (value instanceof Integer) || (value instanceof int[]) ||
                (value instanceof Long) || (value instanceof long[]) ||
                (value instanceof Double) || (value instanceof double[]) ||
                (value instanceof String) || (value instanceof String[]) ||
                (value instanceof Boolean) || (value instanceof boolean[])
        );
    }

    /**
     * Attempts to insert the specified key value pair into the specified bundle.
     *
     * @throws IllegalArgumentException if the value type can not be put into the bundle.
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP_MR1)
    public static void putIntoBundle(BaseBundle baseBundle, String key, Object value) throws IllegalArgumentException {
        if (value == null) {
            throw new IllegalArgumentException("Unable to determine type of null values");
        } else if (value instanceof Integer) {
            baseBundle.putInt(key, (int) value);
        } else if (value instanceof int[]) {
            baseBundle.putIntArray(key, (int[]) value);
        } else if (value instanceof Long) {
            baseBundle.putLong(key, (long) value);
        } else if (value instanceof long[]) {
            baseBundle.putLongArray(key, (long[]) value);
        } else if (value instanceof Double) {
            baseBundle.putDouble(key, (double) value);
        } else if (value instanceof double[]) {
            baseBundle.putDoubleArray(key, (double[]) value);
        } else if (value instanceof String) {
            baseBundle.putString(key, (String) value);
        } else if (value instanceof String[]) {
            baseBundle.putStringArray(key, (String[]) value);
        } else if (value instanceof Boolean) {
            baseBundle.putBoolean(key, (boolean) value);
        } else if (value instanceof boolean[]) {
            baseBundle.putBooleanArray(key, (boolean[]) value);
        } else if (value instanceof PersistableBundle) {
            if (baseBundle instanceof PersistableBundle)
                ((PersistableBundle) baseBundle).putPersistableBundle(key, (PersistableBundle)value);
            else if (baseBundle instanceof Bundle)
                ((Bundle) baseBundle).putBundle(key, toBundle((PersistableBundle) value));
        } else {
            throw new IllegalArgumentException("Objects of type " + value.getClass().getSimpleName()
                    + " can not be put into a " + BaseBundle.class.getSimpleName());
        }
    }
}
