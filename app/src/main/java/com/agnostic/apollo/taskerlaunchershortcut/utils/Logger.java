package com.agnostic.apollo.taskerlaunchershortcut.utils;


import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

public class Logger {

    static String tag = "custom_taskerlaunchershortcut_log";
    //static String tag = TAG;
    static public void logDebug(Context context, String message)
    {
        if(context!=null && QueryPreferences.getLoggingState(context))
            Log.d(tag,message);
    }

    static public void logError(Context context, String message)
    {
        if(context!=null && QueryPreferences.getLoggingState(context))
            Log.e(tag,message);
    }

    static public void logStackTrace(Context context, Exception e)
    {
        if(context!=null && QueryPreferences.getLoggingState(context))
        {
            try {
                StringWriter errors = new StringWriter();
                PrintWriter pw = new PrintWriter(errors);
                e.printStackTrace(pw);
                pw.close();
                Log.e(tag,errors.toString());
                errors.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }

    }

    static public void showToast(final Context context, final String toastText){
        new Handler(Looper.getMainLooper()).post(new Runnable() {

            @Override
            public void run() {
                Toast.makeText(context, toastText, Toast.LENGTH_LONG).show();
            }
        });
    }

    static public void logDebugAndShowToast(Context context, String message) {
        logDebug(context,message);
        showToast(context,message);
    }

    static public void logErrorAndShowToast(Context context, String message) {
        logError(context,message);
        showToast(context,message);
    }

}
