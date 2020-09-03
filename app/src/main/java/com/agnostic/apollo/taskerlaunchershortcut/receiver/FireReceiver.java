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

package com.agnostic.apollo.taskerlaunchershortcut.receiver;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;

import com.agnostic.apollo.taskerlaunchershortcut.LauncherHomeActivity;
import com.agnostic.apollo.taskerlaunchershortcut.TaskerPlugin;
import com.agnostic.apollo.taskerlaunchershortcut.bundle.PluginBundleValues;
import com.agnostic.apollo.taskerlaunchershortcut.shortcuts.ShortcutFirer;
import com.agnostic.apollo.taskerlaunchershortcut.utils.Logger;
import com.twofortyfouram.locale.sdk.client.receiver.AbstractPluginSettingReceiver;

import static com.agnostic.apollo.taskerlaunchershortcut.LauncherHomeActivity.ARG_FIRE_INTENT_FROM_HOST;
import static com.agnostic.apollo.taskerlaunchershortcut.LauncherHomeActivity.ARG_SHORTCUT_INTENT_URI;

public final class FireReceiver extends AbstractPluginSettingReceiver {

    @Override
    protected boolean isBundleValid(@NonNull final Bundle bundle) {
        return PluginBundleValues.isBundleValid(bundle);
    }

    @Override
    protected boolean isAsync() {
        return false;
    }

    @Override
    protected void firePluginSetting(@NonNull final Context context, @NonNull final Intent fireIntentFromHost, @NonNull final Bundle fireBundleFromHost) {
        final String shortcutIntentUri = PluginBundleValues.getMessage(fireBundleFromHost);

        if(!shortcutIntentUri.isEmpty())
        {
            //This is required because of android 10 background activity start restrictions
            //If an activity wasn't started that sent the shortcut and the receiver was use to send the shortcut,
            // the shortcuts wouldn't send and there were exceptions in the logcat
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                context.startActivity(LauncherHomeActivity.newFireShortcutIntentUriIntent(context, shortcutIntentUri, fireIntentFromHost));
            } else {
                Logger.logDebug(context, "Shortcut Intent Received By FireReceiver: \"" + shortcutIntentUri + "\"");
                Intent newIntent = new Intent();
                newIntent.putExtra(ARG_SHORTCUT_INTENT_URI, shortcutIntentUri);
                newIntent.putExtra(ARG_FIRE_INTENT_FROM_HOST, fireIntentFromHost);
                ShortcutFirer.fireShortcutIntentUriIntent(context, newIntent);
            }
            /*
            // Send result back to Tasker now
            if ( isOrderedBroadcast() )  {

                setResultCode( TaskerPlugin.Setting.RESULT_CODE_OK );

                if ( TaskerPlugin.Setting.hostSupportsVariableReturn( fireIntentFromHost.getExtras() ) ) {
                    Bundle resultBundle = new Bundle();
                    resultBundle.putString( "%result_code", "0" );

                    TaskerPlugin.addVariableBundle( getResultExtras( true ), resultBundle );
                }
            }
*           */

            // Notify Tasker that result will be sent later
            if ( isOrderedBroadcast() ) {
                setResultCode(TaskerPlugin.Setting.RESULT_CODE_PENDING);
            }

            //Sending result back to Tasker with either of the ways above is not necessary, it would depend on the plugin
        }
    }
}
