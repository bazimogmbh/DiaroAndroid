package com.pixelcrater.Diaro.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.View;
import android.widget.ScrollView;

import androidx.core.app.ShareCompat;
import androidx.core.content.FileProvider;
import androidx.preference.PreferenceManager;

import com.pixelcrater.Diaro.BuildConfig;
import com.pixelcrater.Diaro.MyApp;
import com.pixelcrater.Diaro.R;
import com.pixelcrater.Diaro.config.AppConfig;
import com.pixelcrater.Diaro.config.GlobalConstants;
import com.pixelcrater.Diaro.config.Prefs;
import com.pixelcrater.Diaro.settings.PreferencesHelper;
import com.pixelcrater.Diaro.storage.dropbox.DropboxAccountManager;
import com.pixelcrater.Diaro.utils.storage.AppLifetimeStorageUtils;
import com.pixelcrater.Diaro.utils.storage.PermanentStorageUtils;
import com.pixelcrater.Diaro.utils.storage.StorageUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import static com.pixelcrater.Diaro.utils.Static.REQUEST_AMAZON_APPSTORE;
import static com.pixelcrater.Diaro.utils.Static.REQUEST_GOOGLE_PLAY;

public class GeneralUtils {

    public static void openMarket(Activity activity) {
        openDiaroInGooglePlay(activity);
    }

    public static void openDiaroInAmazonAppstore(Activity activity) {
        Intent intent = new Intent(Intent.ACTION_VIEW);

        try {
            intent.setData(Uri.parse(activity.getString(R.string.amazon_appstore_url)));
            activity.startActivityForResult(intent, REQUEST_AMAZON_APPSTORE);
        } catch (Exception e) {
            intent.setData(Uri.parse(activity.getString(R.string.web_amazon_appstore_url)));
            activity.startActivityForResult(intent, REQUEST_AMAZON_APPSTORE);
        }
    }

    public static void openDiaroInGooglePlay(Activity activity) {
        Intent intent = new Intent(Intent.ACTION_VIEW);

        try {
            intent.setData(Uri.parse(activity.getString(R.string.google_play_url)));
            activity.startActivityForResult(intent, REQUEST_GOOGLE_PLAY);
        } catch (Exception e) {
            intent.setData(Uri.parse(activity.getString(R.string.web_google_play_url)));
            activity.startActivityForResult(intent, REQUEST_GOOGLE_PLAY);
        }
    }

    public static void recommendToFriend(Activity activity) {
        Intent intent = new Intent(Intent.ACTION_SEND);

        String appStoreUrl;
        appStoreUrl = activity.getString(R.string.web_google_play_url);

        intent.putExtra(Intent.EXTRA_SUBJECT, "Found this great Android app - " + "'Diaro - diary, journal, notes'");
        intent.putExtra(Intent.EXTRA_TEXT, "Hi,\nI found a great diary writing / " +
                "note taking app called 'Diaro - diary, journal, notes' for Android.\n\n" +
                "You can find it here:\n"
                + appStoreUrl);
        intent.setType("message/rfc822");
        activity.startActivityForResult(Intent.createChooser(intent, activity.getString(R.string.choose_email_app)), Static.REQUEST_RECOMMEND_TO_FRIEND);

    }

    public static void openFacebookPage(Context context) {
        try {
            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("fb://page/100507909995752")));
        } catch (Exception e) {
            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(context.getString(R.string.facebook_page_url))));
        }
    }

    public static void sendSupportEmail(Activity activity) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        String[] recipients = new String[]{GlobalConstants.SUPPORT_EMAIL};

        intent.putExtra(Intent.EXTRA_EMAIL, recipients);
        String more = "";
        if (Static.isProUser()) {

            String text = "PRO";
            if(Static.isSubscribedCurrently())
                text = "PREMIUM";

            more += " ("+text+")";
        }

        String prefLocale = PreferencesHelper.getCurrentLocaleAsCode(activity.getApplicationContext());

        intent.putExtra(Intent.EXTRA_SUBJECT, "Diaro v" + MyDevice.getInstance().appVersion + more + " problem reported from the app");

        String diaroEmail = "Not signed in";

        if (MyApp.getInstance().userMgr.isSignedIn()) {
            diaroEmail = MyApp.getInstance().userMgr.getSignedInEmail();
        }

        long attachmentsDirSize = StorageUtils.getUsedSizeInBytes(new File(AppLifetimeStorageUtils.getMediaDirPath()));
        long profileDirSize = StorageUtils.getUsedSizeInBytes(new File(AppLifetimeStorageUtils.getProfilePhotoDirPath()));
//            AppLog.d("#2 Needed space for /profile: " + profileDirSize + "B");

        String sizeWithUnitsAttachments = StorageUtils.getSizeWithUnits(attachmentsDirSize + profileDirSize, null);

        long backupDirSize = StorageUtils.getUsedSizeInBytes(new File(PermanentStorageUtils.getDiaroBackupDirPath()));
        AppLog.d("Needed space for /backup: " + backupDirSize + "B");
        String sizeWithUnits = StorageUtils.getSizeWithUnits(backupDirSize, null);

        String dropboxToken = PreferenceManager.getDefaultSharedPreferences(activity.getApplicationContext()).getString(DropboxAccountManager.PREF_DROPBOX_TOKEN, "");
        String dropboxEmail = PreferenceManager.getDefaultSharedPreferences(activity.getApplicationContext()).getString(DropboxAccountManager.PREF_DROPBOX_EMAIL, "");

        String deviceInfo = "- - - - - - - - - - - - - - - - - - - -"
                + "\nInformation (Please do not modify)"
                + "\nOS: " + MyDevice.getInstance().deviceOS + " " + prefLocale
                + "\nEmail: " + diaroEmail
                + "\nDropbox: " + dropboxToken
                + "\nDropbox Email: " + dropboxEmail
                + "\nDevice: " + MyDevice.getInstance().deviceName
                + "\nAttachmentsDir: " + AppLifetimeStorageUtils.getAppLifetimeStoragePref()  + " : " + sizeWithUnitsAttachments
                + "\nBackupsDir: " + PermanentStorageUtils.getPermanentStoragePref() + " : " + sizeWithUnits
                + "\nWifiActive: " + MyApp.getInstance().networkStateMgr.isConnectedToInternetUsingWiFi()
                + "\nWifiOnly: " + MyApp.getInstance().prefs.getBoolean(Prefs.PREF_SYNC_ON_WIFI_ONLY, true)
           //     + "\nApp version: " + MyDevice.getInstance().appVersion + more
                + "\n- - - - - - - - - - - - - - - - - - - -"
                + "\n\n";

        intent.putExtra(Intent.EXTRA_TEXT, deviceInfo);
        intent.setType("message/rfc822");
        activity.startActivityForResult(Intent.createChooser(intent, activity.getApplicationContext().getString(R.string.report_problem)), Static.REQUEST_SEND_SUPPORT_EMAIL);
    }

    public static Bitmap getBitmapFromView(Activity activity, int viewId) {
        View view = activity.findViewById(viewId);

        int width = view.getWidth();
        int height = view.getHeight();

        if(view instanceof ScrollView) {
            height =  ((ScrollView) view).getChildAt(0).getHeight();
        }

        view.destroyDrawingCache();
        view.setDrawingCacheEnabled(true);
        view.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        view.buildDrawingCache();

        Bitmap returnedBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(returnedBitmap);
        Drawable bgDrawable = view.getBackground();
        if (bgDrawable != null)
            bgDrawable.draw(canvas);
        else
            canvas.drawColor(Color.WHITE);
        view.draw(canvas);
        return returnedBitmap;
    }

    public static void shareBitmap(Activity activity, int viewId, String title) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("image/*");

        // save bitmap to cache directory
        try {
            File cachePath = new File(activity.getCacheDir(), "image");
            cachePath.mkdirs(); // don't forget to make the directory
            FileOutputStream stream = new FileOutputStream(cachePath + "/statistics.png"); // overwrites this image every time

            Bitmap bitmap = getBitmapFromView(activity, viewId);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            stream.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        File cacheFile = new File(activity.getCacheDir(), "image/statistics.png");
        Uri contentUri = FileProvider.getUriForFile(activity, BuildConfig.APPLICATION_ID + ".provider", cacheFile);

        Intent intent = ShareCompat.IntentBuilder.from(activity).setType("image/png").setSubject(title).setStream(contentUri).setChooserTitle(R.string.share).createChooserIntent().addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        activity.startActivity(intent);
    }
}
