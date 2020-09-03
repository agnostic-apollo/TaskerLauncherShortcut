package com.agnostic.apollo.taskerlaunchershortcut.model;

import android.content.ComponentName;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.os.UserHandle;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.agnostic.apollo.taskerlaunchershortcut.utils.BundleUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

//check ShortcutInfo or ShortcutInfoCompat for details
public class ShortcutInfoItem implements Comparable<ShortcutInfoItem> {

    private String mId;

    private CharSequence mLabel;
    private CharSequence mLongLabel;

    private String mAppLabel;
    private ComponentName mActivity;
    private Drawable mIcon;
    private UserHandle mUserHandle;
    private int mUserId;

    private Intent[] mIntents;
    private Set<String> mCategories;
    private PersistableBundle[] mIntentPersistableExtrases;
    private PersistableBundle mExtras;



    @NonNull
    public String getId() {
        return mId;
    }

    @NonNull
    public CharSequence getShortLabel() {
        return mLabel;
    }

    @Nullable
    public CharSequence getLongLabel() {
        return mLongLabel;
    }

    @Nullable
    public String getAppLabel() {
        return mAppLabel;
    }

    @Nullable
    public ComponentName getActivity() {
        return mActivity;
    }

    public Drawable getIcon() {
        return mIcon;
    }

    public UserHandle getUserHandle() {
        return mUserHandle;
    }

    public int getUserId() {
        return mUserId;
    }

    @NonNull
    public Intent getIntent() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            if (mIntents == null || mIntents.length == 0) {
                return null;
            }
            final int last = mIntents.length - 1;
            final Intent intent = new Intent(mIntents[last]);
            return setIntentExtras(intent, mIntentPersistableExtrases[last]);
        } else {
            return mIntents[mIntents.length - 1];
        }
    }

    @NonNull
    public Intent[] getIntents() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            final Intent[] ret = new Intent[mIntents.length];

            for (int i = 0; i < ret.length; i++) {
                ret[i] = new Intent(mIntents[i]);
                setIntentExtras(ret[i], mIntentPersistableExtrases[i]);
            }

            return ret;
        } else {
            return Arrays.copyOf(mIntents, mIntents.length);
        }
    }

    public Set<String> getCategories() {
        return mCategories;
    }

    public PersistableBundle getExtras() {
        return mExtras;
    }

    public void setId(String id) {
        this.mId = id;
    }

    public void setShortLabel(@NonNull CharSequence shortLabel) {
        this.mLabel = shortLabel;
    }

    public void setLongLabel(@NonNull CharSequence longLabel) {
        this.mLongLabel = longLabel;
    }

    public void setAppLabel(String appLabel) {
        mAppLabel = appLabel;
    }

    public void setActivity(@NonNull ComponentName activity) {
        this.mActivity = activity;
    }

    public void setIcon(Drawable icon) {
        this.mIcon = icon;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void setUserHandle(int userId) {
        this.mUserHandle = UserHandle.getUserHandleForUid(userId);
    }

    public void setUserHandle(UserHandle userHandle) {
        this.mUserHandle = userHandle;
    }

    public void setUserId(int userId) {
        this.mUserId = userId;
    }

    public void setIntent(@NonNull Intent intent) {
        setIntents(new Intent[]{intent});
    }

    public void setIntents(@NonNull Intent[] intents) {
        this.mIntents = cloneIntents(intents);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1)
            fixUpIntentExtras();
    }

    public void setCategories(@NonNull Set<String> categories) {
        this.mCategories = cloneCategories(categories);
    }

    public void setExtras(@NonNull PersistableBundle extras) {
        this.mExtras = extras;
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static Intent setIntentExtras(Intent intent, PersistableBundle extras) {
        if (extras == null) {
            intent.replaceExtras((Bundle) null);
        } else {
            intent.replaceExtras(new Bundle(extras));
        }
        return intent;
    }

    private static Set<String> cloneCategories(Set<String> source) {
        if (source == null) {
            return null;
        }
        final Set<String> ret = new HashSet<>(source.size());
        for (CharSequence s : source) {
            if (!TextUtils.isEmpty(s)) {
                ret.add(s.toString().intern());
            }
        }
        return ret;
    }

    private static Intent[] cloneIntents(Intent[] intents) {
        if (intents == null) {
            return null;
        }
        final Intent[] ret = new Intent[intents.length];
        for (int i = 0; i < ret.length; i++) {
            if (intents[i] != null) {
                ret[i] = new Intent(intents[i]);
            }
        }
        return ret;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP_MR1)
    private void fixUpIntentExtras() {
        if (mIntents == null) {
            mIntentPersistableExtrases = null;
            return;
        }
        mIntentPersistableExtrases = new PersistableBundle[mIntents.length];
        for (int i = 0; i < mIntents.length; i++) {
            final Intent intent = mIntents[i];
            final Bundle extras = intent.getExtras();
            if (extras == null) {
                mIntentPersistableExtrases[i] = null;
            } else {
                mIntentPersistableExtrases[i] = BundleUtils.toPersistableBundle(extras);
                intent.replaceExtras((Bundle) null);
            }
        }
    }

    @Override
    public int compareTo(ShortcutInfoItem shortcut) {

        if(this.getActivity()!=null && shortcut.getActivity()!=null) {
            int i = this.getActivity().toString().compareTo(shortcut.getActivity().toString());
            if (i != 0) return i;
        }

        return this.getShortLabel().toString().compareTo(shortcut.getShortLabel().toString());
    }

    public String toString() {
        final StringBuilder sb = new StringBuilder();

        sb.append("ShortcutInfoItem {");

        sb.append("id=");
        if (mId == null)
            sb.append("null");
        else
            sb.append(mId);
        sb.append(", ");


        sb.append("activity=");
        if (mActivity == null)
            sb.append("null");
        else
            sb.append(mActivity);
        sb.append(", ");

        sb.append("shortLabel=");
        if (mLabel == null)
            sb.append("null");
        else
            sb.append(mLabel);
        sb.append(", ");

        sb.append("longLabel=");
        if (mLongLabel == null)
            sb.append("null");
        else
            sb.append(mLongLabel);
        sb.append(", ");

        sb.append("categories=");
        if (mCategories == null)
            sb.append("null");
        else
            sb.append(mCategories);
        sb.append(", ");

        sb.append("icon=");
        if (mIcon == null)
            sb.append("null");
        else
            sb.append(mIcon);
        sb.append(", ");

        sb.append("intents=");
        if (mIntents == null) {
            sb.append("null");
        } else {
            final int size = mIntents.length;
            sb.append("[");
            String sep = "";
            for (int i = 0; i < size; i++) {
                sb.append(sep);
                sep = ", ";
                sb.append(mIntents[i]);
                if (mIntentPersistableExtrases != null) {
                    sb.append("/");
                    sb.append(mIntentPersistableExtrases[i]);
                }
            }
            sb.append("]");
        }

        sb.append(", ");

        sb.append("extras=");
        if (mExtras == null)
            sb.append("null");
        else
            sb.append(mExtras);

        sb.append("}");
        return sb.toString();
    }
}
