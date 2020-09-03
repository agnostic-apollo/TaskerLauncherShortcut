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

package com.agnostic.apollo.taskerlaunchershortcut;

import com.agnostic.apollo.taskerlaunchershortcut.utils.Logger;
import com.twofortyfouram.log.Lumberjack;

import android.app.Application;

/**
 * Implements an application object for the plug-in.
 */
/*
 * This application is non-essential for the plug-in's operation; it simply enables debugging
 * options globally for the app.
 */
public final class PluginApplication extends Application {

    // uncaught exception handler variable
    private Thread.UncaughtExceptionHandler defaultUncaughtExceptionHandler;

    @Override
    public void onCreate() {
        super.onCreate();

        _setDefaultUncaughtExceptionHandler();

        Lumberjack.init(getApplicationContext());
    }

    private void _setDefaultUncaughtExceptionHandler() {
        super.onCreate();

        // handler listener
        Thread.UncaughtExceptionHandler _uncaughtExceptionHandler = new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(final Thread thread, final Throwable e) {
                Logger.logStackTrace(getApplicationContext(), new Exception(e));
                Logger.logError(getApplicationContext(), "Uncaught Exception caught: " + e.getMessage());

                if(thread!=null && defaultUncaughtExceptionHandler!=null) {
                    // re-throw critical exception further to the os (important)
                    defaultUncaughtExceptionHandler.uncaughtException(thread, e);
                }
            }
        };

        // setup handler for uncaught exception
        defaultUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(_uncaughtExceptionHandler);

        Lumberjack.init(getApplicationContext());
    }
}
