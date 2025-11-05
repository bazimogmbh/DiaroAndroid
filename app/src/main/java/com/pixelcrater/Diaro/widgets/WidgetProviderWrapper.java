package com.pixelcrater.Diaro.widgets;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.widget.RemoteViews;

import com.pixelcrater.Diaro.MyApp;
import com.pixelcrater.Diaro.R;
import com.pixelcrater.Diaro.main.SplashActivity;
import com.pixelcrater.Diaro.utils.MyThemesUtils;
import com.pixelcrater.Diaro.utils.Static;

public class WidgetProviderWrapper extends AppWidgetProvider {

    public void updateAllWidgets(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds, int size, int iconResId, String action) {
        for (int appWidgetId : appWidgetIds) {
//            Bundle options = appWidgetManager.getAppWidgetOptions(appWidgetId);
            updateWidget(context, appWidgetManager, appWidgetId, size, iconResId, action);
        }
    }

    public void updateWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId, int size, int iconResId, String action) {
        // Do whatever you need to do to the widget. Include any customization based  on the size, included in the options. Be sure to have a graceful fallback if
        // no valid size data is included in the bundle.

        RemoteViews remoteViews;
        if (size == 1) {
            remoteViews = setup1x1Widget(context, iconResId, action);
        } else {
            remoteViews = setup3x1Widget(context);
        }

        // Widget dimensions
//        String dimensions = String.format(Locale.getDefault(), "[%d-%d] x [%d-%d]", minWidth, maxWidth, minHeight, maxHeight);
//        remoteViews.setTextViewText(R.id.size, dimensions);

        appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
    }

    private RemoteViews setup1x1Widget(Context context, int iconResId, String action) {
//        AppLog.d("iconResId:" + iconResId);

        // Get the layout for the App Widget and attach an on-click listener to the button
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_1x1_layout);

        // Set widget background color
        BitmapDrawable drawable = getUiColorDrawable(56, 56);
        remoteViews.setImageViewBitmap(R.id.background, drawable.getBitmap());

        // Icon
        remoteViews.setImageViewResource(R.id.add_new, iconResId);

        // New entry icon starts new entry activity
        Intent intent = new Intent(context, SplashActivity.class);
        intent.setAction(action);
        intent.putExtra("widget", true);

        final int flag =  Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE : PendingIntent.FLAG_UPDATE_CURRENT;
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, flag);

        remoteViews.setOnClickPendingIntent(R.id.add_new, pendingIntent);

        return remoteViews;
    }

    private RemoteViews setup3x1Widget(Context context) {
        // Get the layout for the App Widget and attach an on-click listener to the button
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_3x1_layout);

        // Set widget background color
        BitmapDrawable drawable = getUiColorDrawable(260, 52);
        remoteViews.setImageViewBitmap(R.id.background, drawable.getBitmap());

        final int flag =  Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE : PendingIntent.FLAG_UPDATE_CURRENT;

        // App icon
        Intent intent1 = new Intent(context, SplashActivity.class);
        intent1.setAction("ACTION_MAIN");
        PendingIntent pendingIntent1 = PendingIntent.getActivity(context, 0, intent1, flag);
        remoteViews.setOnClickPendingIntent(R.id.app_icon, pendingIntent1);

        // New entry icon
        Intent intent2 = new Intent(context, SplashActivity.class);
        intent2.setAction("ACTION_NEW");
        intent2.putExtra("widget", true);
        PendingIntent pendingIntent2 = PendingIntent.getActivity(context, 0, intent2, flag);
        remoteViews.setOnClickPendingIntent(R.id.new_entry_icon, pendingIntent2);

        // Photo icon
        Intent intent3 = new Intent(context, SplashActivity.class);
        intent3.setAction("ACTION_PHOTO");
        intent3.putExtra("widget", true);
        PendingIntent pendingIntent3 = PendingIntent.getActivity(context, 0, intent3, flag);
        remoteViews.setOnClickPendingIntent(R.id.photo_icon, pendingIntent3);

        // Camera icon
        Intent intent4 = new Intent(context, SplashActivity.class);
        intent4.setAction("ACTION_CAMERA");
        intent4.putExtra("widget", true);
        intent4.putExtra("capturePhoto", true);
        PendingIntent pendingIntent4 = PendingIntent.getActivity(context, 0, intent4, flag);
        remoteViews.setOnClickPendingIntent(R.id.camera_icon, pendingIntent4);

        return remoteViews;
    }

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
    }

    public BitmapDrawable getUiColorDrawable(int width, int height) {
        int round = Static.getPixelsFromDip((width == height) ? width / 2 : 5);
        int widthPx = Static.getPixelsFromDip(width);
        int heightPx = Static.getPixelsFromDip(height);

        Bitmap bm = Bitmap.createBitmap(widthPx, heightPx, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bm);

        String uiColorCode = MyThemesUtils.getPrimaryColorCode();
        int uiColor = Color.parseColor(uiColorCode);
        int color = Color.argb(240, Color.red(uiColor), Color.green(uiColor), Color.blue(uiColor));

        // Draw rounder rectangle
        Paint p = new Paint();
        p.setColor(color);

        Rect rect = new Rect(0, 0, widthPx, heightPx);
        RectF rectF = new RectF(rect);
        canvas.drawRoundRect(rectF, round, round, p);

        return new BitmapDrawable(MyApp.getInstance().getResources(), bm);
    }

}
