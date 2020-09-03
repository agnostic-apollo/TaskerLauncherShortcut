/*
 * android-toast-setting-plugin-for-locale <https://github.com/twofortyfouram/android-toast-setting-plugin-for-locale>
 * Copyright 2014 two forty four a.m. LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.agnostic.apollo.taskerlaunchershortcut.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import net.jcip.annotations.NotThreadSafe;

import com.agnostic.apollo.taskerlaunchershortcut.R;
import com.agnostic.apollo.taskerlaunchershortcut.ShortcutChooserActivity;
import com.agnostic.apollo.taskerlaunchershortcut.TaskerPlugin;
import com.agnostic.apollo.taskerlaunchershortcut.bundle.PluginBundleValues;
import com.agnostic.apollo.taskerlaunchershortcut.utils.Logger;
import com.twofortyfouram.locale.sdk.client.ui.activity.AbstractAppCompatPluginActivity;
import com.twofortyfouram.log.Lumberjack;

import static com.agnostic.apollo.taskerlaunchershortcut.bundle.PluginBundleValues.BUNDLE_EXTRA_STRING_SHORTCUT_INTENT_URI;


@NotThreadSafe
public final class EditActivity extends AbstractAppCompatPluginActivity {

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_edit);

        /*
         * To help the user keep context, the title shows the host's name and the subtitle
         * shows the plug-in's name.
         */
        CharSequence callingApplicationLabel = null;
        try {
            callingApplicationLabel =
                    getPackageManager().getApplicationLabel(
                            getPackageManager().getApplicationInfo(getCallingPackage(),
                                    0));
        } catch (final PackageManager.NameNotFoundException e) {
            Lumberjack.e("Calling package couldn't be found%s", e); //$NON-NLS-1$
        }
        if (null != callingApplicationLabel) {
            setTitle(callingApplicationLabel);
        }

        getSupportActionBar().setSubtitle(R.string.plugin_name);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ImageButton mSearchShortcutButton = findViewById(R.id.search_shortcut_button);
        mSearchShortcutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(ShortcutChooserActivity.newShortcutTypeChooserIntent(EditActivity.this), ShortcutChooserActivity.REQUEST_CHOOSE_SHORTCUT);
            }
        });

        mSearchShortcutButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Logger.showToast(EditActivity.this, view.getContentDescription().toString());
                return true;
            }
        });
    }

    @Override
    public void onPostCreateWithPreviousResult(@NonNull final Bundle previousBundle,
                                               @NonNull final String previousBlurb) {
        final String message = PluginBundleValues.getMessage(previousBundle);
        ((EditText) findViewById(R.id.shortcut_uri_text)).setText(message);
    }

    @Override
    public boolean isBundleValid(@NonNull final Bundle bundle) {
        //Logger.logDebug(this, "Bundle: \"" + bundle.toString() + "\"");
        return PluginBundleValues.isBundleValid(bundle);
    }

    @NonNull
    @Override
    public Intent getResultIntent() {
        Intent resultIntent = new Intent();

        // Configuration information for Tasker variables returned from the executed task
        if (TaskerPlugin.hostSupportsRelevantVariables(getIntent().getExtras())) {
            TaskerPlugin.addRelevantVariableList(resultIntent, new String[]{
                    "%result_code\nResult Code\nThe result code for sending shortcut intent.\n" +
                            "0 for success, otherwise a failure.",
                    "%errmsg\nError Message\nThe err message of the action."
            });
        }

        // Notify Tasker to wait for max 10 seconds for plugin to send result
        if (TaskerPlugin.Setting.hostSupportsSynchronousExecution(getIntent().getExtras())) {
            TaskerPlugin.Setting.requestTimeoutMS(resultIntent, 10000);
        }

        return resultIntent;
    }

    @Nullable
    @Override
    public Bundle getResultBundle() {
        Bundle resultBundle = null;

        final String message = ((EditText) findViewById(R.id.shortcut_uri_text)).getText().toString();
        if (!TextUtils.isEmpty(message)) {
            resultBundle = PluginBundleValues.generateBundle(getApplicationContext(), message);

            if (TaskerPlugin.Setting.hostSupportsOnFireVariableReplacement(this)) {
                TaskerPlugin.Setting.setVariableReplaceKeys(resultBundle, new String[]{
                        BUNDLE_EXTRA_STRING_SHORTCUT_INTENT_URI
                });
            }
        }

        return resultBundle;
    }

    @NonNull
    @Override
    public String getResultBlurb(@NonNull final Bundle bundle) {
        final String message = PluginBundleValues.getMessage(bundle);

        final int maxBlurbLength = getResources().getInteger(
                R.integer.com_twofortyfouram_locale_sdk_client_maximum_blurb_length);

        if (message.length() > maxBlurbLength) {
            return message.substring(0, maxBlurbLength);
        }

        return message;
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.activity_edit, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        if (android.R.id.home == item.getItemId()) {
            finish();
        } else if (R.id.menu_discard_changes == item.getItemId()) {
            // Signal to AbstractAppCompatPluginActivity that the user canceled.
            mIsCancelled = true;
            finish();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != Activity.RESULT_OK) {
            return;
        }

        if (requestCode == ShortcutChooserActivity.REQUEST_CHOOSE_SHORTCUT) {
            int returnValue = data.getIntExtra(ShortcutChooserActivity.CHOOSE_SHORTCUT_RETURN_STATE, ShortcutChooserActivity.CHOOSE_SHORTCUT_RETURN_STATE_FAILED);
            if (returnValue == ShortcutChooserActivity.CHOOSE_SHORTCUT_RETURN_STATE_SUCCESS) {
                String shortcutIntentUri = data.getStringExtra(ShortcutChooserActivity.ARG_SHORTCUT_INTENT_URI);
                if (shortcutIntentUri != null) {
                    Logger.logDebug(this, "Chosen Shortcut: \"" + shortcutIntentUri + "\"");
                    ((EditText) findViewById(R.id.shortcut_uri_text)).setText(shortcutIntentUri);
                    return;
                }
            }
        }

        // Signal to AbstractAppCompatPluginActivity that the user canceled.
        mIsCancelled = true;
        Logger.logDebugAndShowToast(this, "Choose Shortcut Cancelled");
        finish();
    }

}

