package com.pixelcrater.Diaro.utils;

import android.util.Log;

import com.pixelcrater.Diaro.config.AppConfig;
import com.pixelcrater.Diaro.BuildConfig;
import com.pixelcrater.Diaro.MyApp;

public class AppLog {

    public static String TAG = "Diaro";

    private AppLog() {
        throw new AssertionError("Instance creation not allowed!");
    }

    public static String getTraceLine(StackTraceElement traceElement) {
        return "\"" + traceElement.toString().replaceFirst(MyApp.getInstance().getPackageName(), "") + " > ";
    }

    public static boolean isLogEnabled() {
        if(BuildConfig.DEBUG)
            return AppConfig.DEV_ENABLE_LOGGING ;
        else
            return false;
    }

    public static void d(String msg) {
        if (isLogEnabled()) {
            StackTraceElement traceElement = Thread.currentThread().getStackTrace()[3];
            Log.d(TAG, getTraceLine(traceElement) + msg);
        }
    }

    public static void w(String msg) {
        if (isLogEnabled()) {
            StackTraceElement traceElement = Thread.currentThread().getStackTrace()[3];
            Log.w(TAG, getTraceLine(traceElement) + msg);
        }
    }

    public static void e(String msg) {
        if (isLogEnabled()) {
            StackTraceElement traceElement = Thread.currentThread().getStackTrace()[3];
            Log.e(TAG, getTraceLine(traceElement) + msg);
        }
    }

    public static void e(String msg, Throwable e) {
        if (isLogEnabled()) {
            StackTraceElement traceElement = Thread.currentThread().getStackTrace()[3];
            Log.e(TAG, getTraceLine(traceElement) + msg, e);
        }
    }

    public static void wtf(String msg, Throwable e) {
        if (isLogEnabled()) {
            StackTraceElement traceElement = Thread.currentThread().getStackTrace()[3];
            Log.wtf(TAG, getTraceLine(traceElement) + msg, e);
        }
    }

    public static void i(String msg) {
        if (isLogEnabled()) {
            StackTraceElement traceElement = Thread.currentThread().getStackTrace()[3];
            Log.i(TAG, getTraceLine(traceElement) + msg);
        }
    }
}
