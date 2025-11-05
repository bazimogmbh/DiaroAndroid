package com.pixelcrater.Diaro.widgets;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import com.pixelcrater.Diaro.R;

public class WidgetProvider1x1New extends WidgetProviderWrapper {

    public static final int ICON_RES_ID = R.drawable.ic_add_white_24dp;
    public static final String ACTION = "ACTION_NEW";

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        String strAction = intent.getAction();

        if (strAction.equals(AppWidgetManager.ACTION_APPWIDGET_UPDATE)) {
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, WidgetProvider1x1New.class));

            updateAllWidgets(context, appWidgetManager, appWidgetIds, 1, ICON_RES_ID, ACTION);
        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        updateAllWidgets(context, appWidgetManager, appWidgetIds, 1, ICON_RES_ID, ACTION);
    }
}
