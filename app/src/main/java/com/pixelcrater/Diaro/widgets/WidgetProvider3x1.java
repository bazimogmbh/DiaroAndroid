package com.pixelcrater.Diaro.widgets;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import com.pixelcrater.Diaro.utils.AppLog;

public class WidgetProvider3x1 extends WidgetProviderWrapper {

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        String strAction = intent.getAction();
        AppLog.d("strAction: " + strAction);

        if (strAction.equals(AppWidgetManager.ACTION_APPWIDGET_UPDATE)) {
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, WidgetProvider3x1.class));
            updateAllWidgets(context, appWidgetManager, appWidgetIds, 3, -1, null);
        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        updateAllWidgets(context, appWidgetManager, appWidgetIds, 3, -1, null);
    }
}
