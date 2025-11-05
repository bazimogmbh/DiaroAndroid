package com.pixelcrater.Diaro.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.ContentProviderClient;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelFileDescriptor;
import android.os.ParcelFileDescriptor.AutoCloseInputStream;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.telephony.TelephonyManager;
import android.text.Spannable;
import android.text.style.BackgroundColorSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.loader.content.CursorLoader;

import com.bumptech.glide.signature.ObjectKey;
import com.pixelcrater.Diaro.BuildConfig;
import com.pixelcrater.Diaro.MyApp;
import com.pixelcrater.Diaro.R;
import com.pixelcrater.Diaro.analytics.AnalyticsConstants;
import com.pixelcrater.Diaro.atlas.AtlasActivity;
import com.pixelcrater.Diaro.backuprestore.BackupFile;
import com.pixelcrater.Diaro.config.AppConfig;
import com.pixelcrater.Diaro.config.GlobalConstants;
import com.pixelcrater.Diaro.config.Prefs;
import com.pixelcrater.Diaro.entries.attachments.AttachmentsStatic;
import com.pixelcrater.Diaro.gallery.Constants;
import com.pixelcrater.Diaro.gallery.activities.MediaActivity;
import com.pixelcrater.Diaro.main.ActivityState;
import com.pixelcrater.Diaro.model.LocationInfo;
import com.pixelcrater.Diaro.model.TagInfo;
import com.pixelcrater.Diaro.onthisday.OnThisDayActivity;
import com.pixelcrater.Diaro.premium.PremiumActivity;
import com.pixelcrater.Diaro.profile.ProfileActivity;
import com.pixelcrater.Diaro.profile.SignInActivity;
import com.pixelcrater.Diaro.settings.PreferencesHelper;
import com.pixelcrater.Diaro.settings.SettingsActivity;
import com.pixelcrater.Diaro.stats.StatsActivity;
import com.pixelcrater.Diaro.storage.Tables;
import com.pixelcrater.Diaro.storage.dropbox.DropboxAccountManager;
import com.pixelcrater.Diaro.storage.dropbox.SyncService;
import com.pixelcrater.Diaro.storage.sqlite.helpers.SQLiteMgr;
import com.pixelcrater.Diaro.templates.TemplatesActivity;
import com.pixelcrater.Diaro.utils.storage.FileUtil;
import com.pixelcrater.Diaro.utils.storage.PermanentStorageUtils;
import com.yariksoffice.lingver.Lingver;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.cert.CertificateException;
import java.text.BreakIterator;
import java.text.DateFormatSymbols;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;
import java.util.Random;
import java.util.StringTokenizer;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import es.dmoral.toasty.Toasty;
import okhttp3.OkHttpClient;

import static com.pixelcrater.Diaro.config.GlobalConstants.PHOTO;

public class Static {

    // Broadcast receivers
    public static final String BR_IN_MAIN = "BR_IN_MAIN";
    public static final String BR_IN_BACKUP_RESTORE = "BR_IN_BACKUP_RESTORE";
    public static final String BR_IN_SIGN_IN = "BR_IN_SIGN_IN";
    public static final String BR_IN_PROFILE = "BR_IN_PROFILE";
    public static final String BR_IN_PROFILE_PHOTO = "BR_IN_PROFILE_PHOTO";
    public static final String BR_IN_GET_PRO = "BR_IN_GET_PRO";
    public static final String BR_IN_SETTINGS_DATA = "BR_IN_SETTINGS_DATA";

    public static final String BROADCAST_DO = "BROADCAST_DO";
    public static final String BROADCAST_PARAMS = "BROADCAST_PARAMS";

    // Broadcasts do actions
    public static final String DO_SHOW_HIDE_BANNER = "DO_SHOW_HIDE_BANNER";
    public static final String DO_SHOW_HIDE_PRO_LABEL = "DO_SHOW_HIDE_PRO_LABEL";
    public static final String DO_UPDATE_PROFILE_PHOTO = "DO_UPDATE_PROFILE_PHOTO";
    public static final String DO_RECREATE_CALENDAR = "DO_RECREATE_CALENDAR";
    public static final String DO_REFRESH_BACKUP_FILES_LIST = "DO_REFRESH_BACKUP_FILES_LIST";
    public static final String DO_ACTIONS_ON_DOWNLOAD_COMPLETE = "DO_ACTIONS_ON_DOWNLOAD_COMPLETE";
    public static final String DO_ACTIONS_ON_DOWNLOAD_CANCELED = "DO_ACTIONS_ON_DOWNLOAD_CANCELED";
    public static final String DO_SHOW_ENTRY_ARCHIVE_UNDO_TOAST = "DO_SHOW_ENTRY_ARCHIVE_UNDO_TOAST";
    public static final String DO_UPDATE_UI = "DO_UPDATE_UI";
    public static final String DO_CHECK_STATUS = "DO_CHECK_STATUS";
    public static final String DO_DISMISS_SIGNIN_DIALOG = "DO_DISMISS_SIGNIN_DIALOG";
    public static final String DO_DISMISS_SIGNUP_DIALOG = "DO_DISMISS_SIGNUP_DIALOG";

    // Broadcasts params
    public static final String PARAM_RESTORE = "PARAM_RESTORE";
    public static final String PARAM_DELETE_OLD_DATA = "PARAM_DELETE_OLD_DATA";

    // Intents request codes
    public static final int REQUEST_SECURITY_CODE = 1;
    public static final int REQUEST_VIEW_EDIT_ENTRY = 2;
    public static final int REQUEST_PHOTO_GRID = 3;
    public static final int REQUEST_PHOTO_PAGER = 4;
    public static final int REQUEST_TAKE_PHOTO = 5;
    public static final int REQUEST_SELECT_PHOTO = 6;
    public static final int REQUEST_FOLDER_ADDEDIT = 8;
    public static final int REQUEST_TAG_ADDEDIT = 9;
    public static final int REQUEST_SETTINGS = 10;
    public static final int REQUEST_SETTINGS_GROUP = 11;
    public static final int REQUEST_SETTINGS_SET_TIME_TO_WRITE_NOTIFICATION = 12;
    public static final int REQUEST_LINK_TO_DBX = 13;
    public static final int REQUEST_SIGN_IN_WITH_GOOGLE = 14;
    public static final int REQUEST_GET_PRO = 15;
    public static final int REQUEST_DIAL_NUMBER = 16;
    public static final int REQUEST_OPEN_MAP = 17;
    public static final int REQUEST_PROFILE_PHOTO = 18;
    public static final int REQUEST_SIGN_IN = 19;
    public static final int REQUEST_WEB_URL = 20;
    public static final int REQUEST_SEND_EMAIL = 21;
    public static final int REQUEST_RECOMMEND_TO_FRIEND = 22;
    public static final int REQUEST_ABOUT = 24;
    public static final int REQUEST_PROFILE = 26;
    public static final int REQUEST_SETTINGS_BACKUP_RESTORE = 27;
    public static final int REQUEST_SHARE_PHOTO = 28;
    public static final int REQUEST_LOCATION_ADDEDIT = 29;
    public static final int REQUEST_GOOGLE_PLAY = 30;
    public static final int REQUEST_SHARE_ENTRY = 31;
    public static final int REQUEST_AMAZON_APPSTORE = 32;
    public static final int REQUEST_SEND_SUPPORT_EMAIL = 33;
    public static final int REQUEST_SHOW_ON_MAP = 34;
    public static final int REQUEST_SELECT_SD_CARD = 36;
    public static final int REQUEST_MOODS_ADDEDIT = 61;

    public static final int REQUEST_ON_THIS_DAY = 37;
    public static final int REQUEST_STATS = 38;
    public static final int REQUEST_ALL_PHOTOS = 39;
    public static final int REQUEST_MAPS = 40;
    public static final int REQUEST_TEMPLATES = 41;

    public static final int REQUEST_CODE_TXT_SAVE = 42;
    public static final int REQUEST_CODE_CSV_SAVE = 43;
    public static final int REQUEST_CODE_DRAW = 44;
    public final static int REQUEST_SPEECH_TO_TXT = 45;
    public final static int REQUEST_TXT_TO_AUDIO = 46;
    public static final int REQUEST_TEXT_RECOGNITION = 47;

    public static final int REQUEST_CODE_EVERNOTE_IMPORT = 50;
    public static final int REQUEST_CODE_DAYONE_IMPORT = 51;
    public static final int REQUEST_CODE_JOURNEY_IMPORT = 52;
    public static final int REQUEST_CODE_DIARIUM_IMPORT = 53;
    public static final int REQUEST_CODE_MEMORIZE_IMPORT = 54;
    public static final int REQUEST_CODE_REDNOTEBOOK_IMPORT = 55;
    public static final int REQUEST_CODE_UNIVERSUM_IMPORT = 56;
    public static final int REQUEST_CODE_CSV_IMPORT = 57;
    public static final int REQUEST_CODE_SIMPLE_JOURNAL_IMPORT = 58;

    // Permissions request codes
    public static final int PERMISSION_REQUEST_STORAGE = 100;
    public static final int PERMISSION_REQUEST_LOCATION = 101;
    public static final int PERMISSION_REQUEST_CONTACTS = 103;

    // Dialogs tags
    public static final String DIALOG_SELECT_WEATHER = "DIALOG_SELECT_WEATHER";
    public static final String DIALOG_CONFIRM_ENTRY_DUPLICATE = "DIALOG_CONFIRM_ENTRY_DUPLICATE";
    public static final String DIALOG_CONFIRM_ENTRY_DELETE = "DIALOG_CONFIRM_ENTRY_DELETE";
    public static final String DIALOG_CONFIRM_SELECTED_ENTRIES_DELETE = "DIALOG_CONFIRM_SELECTED_ENTRIES_DELETE";
    public static final String DIALOG_CONFIRM_FOLDER_DELETE = "DIALOG_CONFIRM_FOLDER_DELETE";
    public static final String DIALOG_CONFIRM_TAG_DELETE = "DIALOG_CONFIRM_TAG_DELETE";
    public static final String DIALOG_CONFIRM_LOCATION_DELETE = "DIALOG_CONFIRM_LOCATION_DELETE";
    public static final String DIALOG_CONFIRM_MOOD_DELETE = "DIALOG_CONFIRM_MOOD_DELETE";
    public static final String DIALOG_CONFIRM_DROPBOX_UNLINK = "DIALOG_CONFIRM_DROPBOX_UNLINK";
    public static final String DIALOG_CONFIRM_BACKUP_FILE_DELETE = "DIALOG_CONFIRM_BACKUP_FILE_DELETE";
    public static final String DIALOG_CONFIRM_CURRENT_DATA_DELETE = "DIALOG_CONFIRM_CURRENT_DATA_DELETE";
    public static final String DIALOG_CONFIRM_MOBILE_INTERNET = "DIALOG_CONFIRM_MOBILE_INTERNET";
    public static final String DIALOG_CONFIRM_MOBILE_INTERNET_UPLOAD = "DIALOG_CONFIRM_MOBILE_INTERNET_UPLOAD";
    public static final String DIALOG_CONFIRM_MOBILE_INTERNET_DOWNLOAD = "DIALOG_CONFIRM_MOBILE_INTERNET_DOWNLOAD";
    public static final String DIALOG_CONFIRM_PHOTO_DELETE = "DIALOG_CONFIRM_PHOTO_DELETE";
    public static final String DIALOG_CONFIRM_RESTORE = "DIALOG_CONFIRM_RESTORE";
    public static final String DIALOG_CONFIRM_SAVE_AS_ENTRY_DATE = "DIALOG_CONFIRM_SAVE_AS_ENTRY_DATE";
    public static final String DIALOG_FOLDER_PATTERN_SELECT = "DIALOG_FOLDER_PATTERN_SELECT";
    public static final String DIALOG_SELECTED_ENTRIES_SET_FOLDER = "DIALOG_SELECTED_ENTRIES_SET_FOLDER";
    public static final String DIALOG_SELECTED_ENTRIES_SET_TAGS = "DIALOG_SELECTED_ENTRIES_SET_TAGS";
    public static final String DIALOG_SELECTED_ENTRIES_SET_LOCATION = "DIALOG_SELECTED_ENTRIES_SET_LOCATION";
    public static final String DIALOG_FOLDER_SELECT = "DIALOG_FOLDER_SELECT";
    public static final String DIALOG_TAGS_SELECT = "DIALOG_TAGS_SELECT";
    public static final String DIALOG_MOOD_SELECT = "DIALOG_MOOD_SELECT";
    public static final String DIALOG_TEMPLATE_SELECT = "DIALOG_TEMPLATE_SELECT";
    public static final String DIALOG_LOCATION_SELECT = "DIALOG_LOCATION_SELECT";
    public static final String DIALOG_ADD_PHOTO = "DIALOG_ADD_PHOTO";
    public static final String DIALOG_PICKER_DATE = "DIALOG_PICKER_DATE";
    public static final String DIALOG_PICKER_TIME = "DIALOG_PICKER_TIME";
    public static final String DIALOG_PICKER_DATE_FROM = "DIALOG_PICKER_DATE_FROM";
    public static final String DIALOG_PICKER_DATE_TO = "DIALOG_PICKER_DATE_TO";
    public static final String DIALOG_PICKER_COLOR = "DIALOG_PICKER_COLOR";
    public static final String DIALOG_SELECT_SETTINGS_SC_REQUEST_PERIOD = "DIALOG_SELECT_SETTINGS_SC_REQUEST_PERIOD";
    public static final String DIALOG_SELECT_SETTINGS_LOCALE = "DIALOG_SELECT_SETTINGS_LOCALE";
    public static final String DIALOG_SELECT_SETTINGS_UI_THEME = "DIALOG_SELECT_SETTINGS_UI_THEME";
    public static final String DIALOG_SELECT_SETTINGS_UI_COLOR = "DIALOG_SELECT_SETTINGS_UI_COLOR";
    public static final String DIALOG_SELECT_SETTINGS_UI_ACCENT_COLOR = "DIALOG_SELECT_SETTINGS_UI_ACCENT_COLOR";
    public static final String DIALOG_SELECT_SETTINGS_DISPLAY_DENSITY = "DIALOG_SELECT_SETTINGS_DISPLAY_DENSITY";
    public static final String DIALOG_SELECT_SETTINGS_FONT = "DIALOG_SELECT_SETTINGS_FONT";
    public static final String DIALOG_SELECT_SETTINGS_UNITS = "DIALOG_SELECT_SETTINGS_UNITS";
    public static final String DIALOG_SELECT_SETTINGS_FIRST_DAY_OF_WEEK = "DIALOG_SELECT_SETTINGS_FIRST_DAY_OF_WEEK";
    public static final String DIALOG_SELECT_SETTINGS_ENTRY_DATE_STYLE = "DIALOG_SELECT_SETTINGS_ENTRY_DATE_STYLE";
    public static final String DIALOG_SELECT_SETTINGS_TEXT_SIZE = "DIALOG_SELECT_SETTINGS_TEXT_SIZE";
    public static final String DIALOG_SELECT_SETTINGS_MAP_TYPE = "DIALOG_SELECT_SETTINGS_MAP_TYPE";
    public static final String DIALOG_RESTORE = "DIALOG_RESTORE";
    public static final String DIALOG_BACKUP = "DIALOG_BACKUP";
    public static final String DIALOG_SIGN_IN = "DIALOG_SIGN_IN";
    public static final String DIALOG_SIGN_UP = "DIALOG_SIGN_UP";
    public static final String DIALOG_SIGN_OUT = "DIALOG_SIGN_OUT";
    public static final String DIALOG_FORGOT_PASSWORD = "DIALOG_FORGOT_PASSWORD";
    public static final String DIALOG_FORGOT_SECURITY_CODE = "DIALOG_FORGOT_SECURITY_CODE";
    public static final String DIALOG_FOLLOW_US_ON_FACEBOOK = "DIALOG_FOLLOW_US_ON_FACEBOOK";
    public static final String DIALOG_SORT = "DIALOG_SORT";
    public static final String DIALOG_CHOOSE_BACKUP_STORAGE = "DIALOG_CHOOSE_BACKUP_STORAGE";
    public static final String DIALOG_CHOOSE_ATTACHMENTS_STORAGE = "DIALOG_CHOOSE_ATTACHMENTS_STORAGE";
    public static final String DIALOG_EXPORT_PDF = "DIALOG_EXPORT_PDF";
    public static final String DIALOG_IMPORT_EVERNOTE = "DIALOG_IMPORT_EVERNOTE";
    public static final String DIALOG_PHOTO_DETAILS = "DIALOG_PHOTO_DETAILS";
    public static final String DIALOG_LOCATION_OPTIONS = "DIALOG_LOCATION_OPTIONS";
    public static final String DIALOG_LICENSES = "DIALOG_LICENSES";
    public static final String DIALOG_CONFIRM_RATIONALE_LOCATION = "DIALOG_CONFIRM_RATIONALE_LOCATION";
    public static final String DIALOG_CONFIRM_RATIONALE_STORAGE = "DIALOG_CONFIRM_RATIONALE_STORAGE";
    public static final String DIALOG_CONFIRM_RATIONALE_CONTACTS = "DIALOG_CONFIRM_RATIONALE_CONTACTS";
    public static final String DIALOG_SELECT_SD_HINT = "DIALOG_SELECT_SD_HINT";
    public static final String DIALOG_PHOTO_METADATA_SUGGESTION = "DIALOG_PHOTO_METADATA_SUGGESTION";

    // AlarmManager request codes
    public static final int REQUEST_SHOW_TIME_TO_WRITE_NOTIFICATION = 1;
    public static final int REQUEST_AUTO_BACKUP = 2;

    public static final double PHOTO_PROPORTION = 0.8;

    // Skip security code extra
    public static String EXTRA_SKIP_SC = "skip_sc";

    public static ArrayList<Pattern> patternsArrayList;

    /**
     * Returns String digit with front zero (for months and days)
     */
    public static String getDigitWithFrontZero(int digit) {
        if (digit < 10) return "0" + digit;
        return String.valueOf(digit);
    }

    public static String getReadableFileSize(long sizeBytes) {
        if (sizeBytes <= 0) return "0";
        final String[] units = new String[]
                {"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(sizeBytes) / Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(sizeBytes / Math.pow(1024, digitGroups)) + units[digitGroups];
    }

    /**
     * Copy file/directory - if targetLocation does not exist, it will be created.
     */
    public static void copyFileOrDirectory(File sourceFile, File targetFile) throws Exception {
        AppLog.d("sourceLocation: " + sourceFile.getPath() + ", targetLocation: " + targetFile.getPath() + " -");

        if (sourceFile.isDirectory()) {
            FileUtils.copyDirectory(sourceFile, targetFile);
        } else {
            try {
                FileUtils.copyFile(sourceFile, targetFile);
            } catch (IOException e) {
                Log.e("copy error", e.getLocalizedMessage());
            }
        }
    }

    /**
     * Moves file/folder from one path to another
     */
    public static void moveFileOrDirectory(File sourceFile, File targetFile) throws Exception {
        AppLog.d("sourceFile: " + sourceFile.getPath() + ", targetFile: " + targetFile.getPath());

        if (sourceFile.isDirectory()) {
            if (!targetFile.exists()) {
                FileUtils.moveDirectory(sourceFile, targetFile);
            } else {
                FileUtils.copyDirectory(sourceFile, targetFile);
            }
        } else {
            if (!targetFile.exists()) {
                FileUtils.moveFile(sourceFile, targetFile);
            } else {
                FileUtils.copyFile(sourceFile, targetFile);
            }
        }
    }

    /**
     * Executes AsyncTask in a separate thread (parallel)
     */
    @SuppressLint("NewApi")
    public static void startMyTask(AsyncTask<Object, String, Boolean> asyncTask) {
        if (!MyApp.getInstance().asyncsMgr.isAsyncRunning(asyncTask)) {

            //asyncTask.execute();
            asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    public static String getFileExtension(String filenameWithExt) {
        return FileUtil.getExtension(filenameWithExt);
    }

    public static String getFilenameWithoutExtension(String filenameWithExt) {
        return FileUtil.removeExtension(filenameWithExt);
    }

    public static String readableFileSize(long size) {
        if (size <= 0) return "0";
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + units[digitGroups];
    }

    /**
     * Returns md5 hash of given string
     */
    public static String md5(String string) {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("MD5");
            digest.reset();
            digest.update(string.getBytes());
            byte[] a = digest.digest();
            int len = a.length;
            StringBuilder sb = new StringBuilder(len << 1);
            for (int i = 0; i < len; i++) {
                sb.append(Character.forDigit((a[i] & 0xf0) >> 4, 16));
                sb.append(Character.forDigit(a[i] & 0x0f, 16));
            }
            return sb.toString();
        } catch (Exception e) {
            AppLog.e("Exception: " + e);
        }
        return null;
    }

    public static void showToastLong(String msg) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(() -> Toasty.normal(MyApp.getInstance(), msg).show(), 300);
    }

    public static void showToastSuccess(String msg) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(() -> Toasty.success(MyApp.getInstance(), msg, Toast.LENGTH_SHORT, true).show(), 500);
    }

    public static void showToastError(String msg) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(() -> Toasty.error(MyApp.getInstance(), msg, Toast.LENGTH_SHORT, true).show(), 500);
    }

    public static void showToast(String msg, int duration) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(() -> Toasty.normal(MyApp.getInstance(), msg).show(), 100);
    }

    public static String generateRandomUid() {
        return md5(String.valueOf(new DateTime().getMillis()) + new BigInteger(130, new Random()).toString(32));
    }

    /**
     * Requires android.permission.READ_PHONE_STATE permission
     */
    public static String getDeprecatedDeviceUid() {
        // IMEI
        TelephonyManager tm = (TelephonyManager) MyApp.getInstance().getSystemService(Context.TELEPHONY_SERVICE);
        String imei = tm.getDeviceId();
        AppLog.d("imei: " + imei);

        // md5 device id
        return Static.md5(imei + MyDevice.getInstance().deviceName);
    }

    public static void turnOnPro() {
        if (!isPro()) {
            // Encrypt and set preference
            try {
                String encryptedString = AES256Cipher.encodeString("true", MyDevice.getInstance().deviceUid);
                MyApp.getInstance().prefs.edit().putString(Prefs.PREF_ENC_PRO, encryptedString).apply();
            } catch (Exception e) {
                AppLog.e("Exception: " + e);
            }

            if (MyApp.getInstance().storageMgr.isStorageDropbox()) {
                SyncService.startService();
            }
            sendBroadcastsAfterPurchaseStateChange();
            MyApp.getInstance().checkAppState();
        }
    }

    public static void turnOffPro() {
        if (isPro()) {
            MyApp.getInstance().prefs.edit().putString(Prefs.PREF_ENC_PRO, null).apply();
            sendBroadcastsAfterPurchaseStateChange();
            MyApp.getInstance().checkAppState();
        }
    }

    public static boolean isPro() {
        // Decrypt preference
        String decryptedString;
        try {
            String encryptedPrefValue = MyApp.getInstance().prefs.getString(Prefs.PREF_ENC_PRO, null);
            if (encryptedPrefValue != null) {
                decryptedString = AES256Cipher.decodeString(encryptedPrefValue, MyDevice.getInstance().deviceUid);
                if (decryptedString.equals("true")) {
                    return true;
                }
            }
        } catch (Exception e) {
            AppLog.e("Exception: " + e);
            MyApp.getInstance().prefs.edit().putString(Prefs.PREF_ENC_PRO, null).apply();
            sendBroadcastsAfterPurchaseStateChange();
            MyApp.getInstance().checkAppState();
        }
        return false;
    }

    public static boolean isProUser() {
        boolean isPro = isPro();
        boolean isSubscribedYearly = isSubscribedCurrently();
        boolean isNboSubscribed = isPlayNboSubscription();

        return isPro || isSubscribedYearly || isNboSubscribed;
    }

    //  Subscribed currently
    public static void turnOnSubscribedCurrently() {
        if (!isSubscribedCurrently()) {
            try {
                String encryptedString = AES256Cipher.encodeString("true", MyDevice.getInstance().deviceUid);
                MyApp.getInstance().prefs.edit().putString(Prefs.PREF_ENC_SUBSCRIBED_CURRENTLY, encryptedString).apply();
            } catch (Exception e) {
                AppLog.e("Exception: " + e);
            }

            if (MyApp.getInstance().storageMgr.isStorageDropbox()) {
                SyncService.startService();
            }
            sendBroadcastsAfterPurchaseStateChange();
            MyApp.getInstance().checkAppState();
        }
    }

    public static void turnOffSubscribedCurrently() {
        if (isSubscribedCurrently()) {
            MyApp.getInstance().prefs.edit().putString(Prefs.PREF_ENC_SUBSCRIBED_CURRENTLY, null).apply();
            sendBroadcastsAfterPurchaseStateChange();
            MyApp.getInstance().checkAppState();
        }
    }

    public static boolean isSubscribedCurrently() {
        String decryptedString;
        try {
            String encryptedPrefValue = MyApp.getInstance().prefs.getString(Prefs.PREF_ENC_SUBSCRIBED_CURRENTLY, null);
            if (encryptedPrefValue != null) {
                decryptedString = AES256Cipher.decodeString(encryptedPrefValue, MyDevice.getInstance().deviceUid);
                if (decryptedString.equals("true")) {
                    return true;
                }
            }
        } catch (Exception e) {
            AppLog.e("Exception: " + e);
            MyApp.getInstance().prefs.edit().putString(Prefs.PREF_ENC_SUBSCRIBED_CURRENTLY, null).apply();
            sendBroadcastsAfterPurchaseStateChange();
            MyApp.getInstance().checkAppState();
        }

        return false;
    }

    // Google play NBO Subscription
    public static void turnOnPlayNboSubscription() {
        if (!isPlayNboSubscription()) {
            try {
                String encryptedString = AES256Cipher.encodeString("true", MyDevice.getInstance().deviceUid);
                MyApp.getInstance().prefs.edit().putString(Prefs.PREF_ENC_SUBSCRIBED_PLAY_NBO, encryptedString).apply();
            } catch (Exception e) {
                AppLog.e("Exception: " + e);
            }

            if (MyApp.getInstance().storageMgr.isStorageDropbox()) {
                SyncService.startService();
            }
            sendBroadcastsAfterPurchaseStateChange();
            MyApp.getInstance().checkAppState();
        }
    }

    public static void turnOffPlayNboSubscription() {
        if (isPlayNboSubscription()) {
            MyApp.getInstance().prefs.edit().putString(Prefs.PREF_ENC_SUBSCRIBED_PLAY_NBO, null).apply();
            sendBroadcastsAfterPurchaseStateChange();
            MyApp.getInstance().checkAppState();
        }
    }

    public static boolean isPlayNboSubscription() {
        String decryptedString;
        try {
            String encryptedPrefValue = MyApp.getInstance().prefs.getString(Prefs.PREF_ENC_SUBSCRIBED_PLAY_NBO, null);
            if (encryptedPrefValue != null) {
                decryptedString = AES256Cipher.decodeString(encryptedPrefValue, MyDevice.getInstance().deviceUid);
                if (decryptedString.equals("true")) {
                    return true;
                }
            }
        } catch (Exception e) {
            AppLog.e("Exception: " + e);
            MyApp.getInstance().prefs.edit().putString(Prefs.PREF_ENC_SUBSCRIBED_PLAY_NBO, null).apply();
            sendBroadcastsAfterPurchaseStateChange();
            MyApp.getInstance().checkAppState();
        }

        return false;
    }

    public static boolean isValidEmail(CharSequence target) {
        return target != null && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }

    public static void startProActivity(Activity activity, ActivityState activityState) {
        if (activityState.isActivityPaused) {
            return;
        }

        Intent intent = new Intent(activity, PremiumActivity.class);
        intent.putExtra(EXTRA_SKIP_SC, true);
        activity.startActivityForResult(intent, REQUEST_GET_PRO);
    }

    public static void startSignInActivity(Activity activity, ActivityState activityState) {
        if (activityState.isActivityPaused) {
            return;
        }

        Intent intent = new Intent(activity, SignInActivity.class);
        intent.putExtra(EXTRA_SKIP_SC, true);
        activity.startActivityForResult(intent, REQUEST_SIGN_IN);
    }

    public static void startProfileActivity(Activity activity, ActivityState activityState) {
        if (activityState.isActivityPaused) {
            return;
        }

        Intent intent = new Intent(activity, ProfileActivity.class);
        intent.putExtra(EXTRA_SKIP_SC, true);
        activity.startActivityForResult(intent, REQUEST_PROFILE);
    }


    public static void startOnThisDayActivity(Activity activity, ActivityState activityState) {
        if (activityState.isActivityPaused) {
            return;
        }

        activityState.logAnalyticsEvent(AnalyticsConstants.EVENT_LOG_ONTHISDAY_VIEW);

        Intent intent = new Intent(activity, OnThisDayActivity.class);
        intent.putExtra(EXTRA_SKIP_SC, true);
        activity.startActivityForResult(intent, REQUEST_ON_THIS_DAY);
    }

    public static void startStatsActivity(Activity activity, ActivityState activityState) {
        if (activityState.isActivityPaused) {
            return;
        }

        activityState.logAnalyticsEvent(AnalyticsConstants.EVENT_LOG_STATS_VIEW);

        Intent intent = new Intent(activity, StatsActivity.class);
        intent.putExtra(EXTRA_SKIP_SC, true);
        activity.startActivityForResult(intent, REQUEST_STATS);
    }

    public static void startMapsActivity(Activity activity, ActivityState activityState) {
        if (activityState.isActivityPaused) {
            return;
        }
        activityState.logAnalyticsEvent(AnalyticsConstants.EVENT_LOG_ATLAS_VIEW);

        Intent intent = new Intent(activity, AtlasActivity.class);
        intent.putExtra(EXTRA_SKIP_SC, true);
        activity.startActivityForResult(intent, REQUEST_MAPS);
    }

    public static void startTemplatesActivity(Activity activity, ActivityState activityState) {
        if (activityState.isActivityPaused) {
            return;
        }
        activityState.logAnalyticsEvent(AnalyticsConstants.EVENT_LOG_TEMPLATES_VIEW);

        Intent intent = new Intent(activity, TemplatesActivity.class);
        intent.putExtra(EXTRA_SKIP_SC, true);
        activity.startActivityForResult(intent, REQUEST_TEMPLATES);
    }


    public static void startSettingsActivity(Activity activity, ActivityState activityState) {
        if (activityState.isActivityPaused) {
            return;
        }
        activityState.logAnalyticsEvent(AnalyticsConstants.EVENT_LOG_SETTINGS_VIEW);

        Intent intent = new Intent(activity, SettingsActivity.class);
        intent.putExtra(Static.EXTRA_SKIP_SC, true);
        activity.startActivityForResult(intent, Static.REQUEST_SETTINGS);
    }

    public static void startAllPhotosActivity(Activity activity, ActivityState activityState) {
        if (activityState.isActivityPaused) {
            return;
        }
        activityState.logAnalyticsEvent(AnalyticsConstants.EVENT_LOG_ALL_PHOTOS_VIEW);

        Intent intent = new Intent(activity, MediaActivity.class);
        intent.putExtra(Constants.IntentPassingParams.COUNT, 3);
        intent.putExtra(Constants.IntentPassingParams.TITLE, "Gallery");
      //  intent.putExtra(Constants.IntentPassingParams.TOOLBAR_COLOR_ID, R.color.colorPrimary);
      //  intent.putExtra(Constants.IntentPassingParams.IMG_PLACEHOLDER, R.color.colorPrimary);
        intent.putExtra(Static.EXTRA_SKIP_SC, true);
        activity.startActivityForResult(intent, REQUEST_ALL_PHOTOS);
    }

    public static void sendBroadcastsAfterPurchaseStateChange() {
        sendBroadcast(BR_IN_MAIN, DO_SHOW_HIDE_PRO_LABEL, null);
        sendBroadcast(BR_IN_MAIN, DO_SHOW_HIDE_BANNER, null);
        sendBroadcast(BR_IN_PROFILE, DO_UPDATE_UI, null);
        sendBroadcast(BR_IN_GET_PRO, DO_UPDATE_UI, null);
    }

    /**
     * Calculates size in px from given dip
     */
    public static int getPixelsFromDip(int dipValue) {
        DisplayMetrics dm = MyApp.getInstance().getResources().getDisplayMetrics();
        return (int) (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dipValue, dm) + 0.5f);
    }

    public static int countUnicodeChars(String str) {
        BreakIterator iter = BreakIterator.getCharacterInstance();
        iter.setText(str);

        int count = 0;
        while (iter.next() != BreakIterator.DONE)
            count++;

        return count;
    }

    public static int countWords(String s) {
        try {
            StringTokenizer st = new StringTokenizer(s);
            return st.countTokens();
            // separate string around spaces
            //            return s.trim().isEmpty() ? 0 : s.trim().split("\\s+").length;
        } catch (Exception e) {
            AppLog.e("Exception: " + e);
        }
        return 0;
    }

    public static int getHeapSizeInMB() {
        DecimalFormat df = new DecimalFormat();
        df.setMaximumFractionDigits(0);
        df.setMinimumFractionDigits(0);

        return Integer.parseInt(df.format((double) (Runtime.getRuntime().maxMemory() / 1048576)));
    }

    public static String getAppVersionName() {
        String versionName = "";
        try {
            PackageInfo pInfo = MyApp.getInstance().getPackageManager().getPackageInfo(MyApp.getInstance().getPackageName(), 0);
            versionName = pInfo.versionName;
        } catch (Exception e) {
            AppLog.e("Exception: " + e);
        }

        return versionName;
    }

    public static int getAppVersionCode() {
        int versionCode;
        try {
            PackageInfo pInfo = MyApp.getInstance().getPackageManager().getPackageInfo(MyApp.getInstance().getPackageName(), 0);
            versionCode = pInfo.versionCode;
        } catch (Exception e) {
            AppLog.e("Exception: " + e);
            throw new RuntimeException(e);
        }
        return versionCode;
    }

    public static void makePause(long ms) {
        //        AppLog.d("ms: " + ms);
        android.os.SystemClock.sleep(ms);
    }

    public static boolean IsNetworkRoaming() {
        try {
            TelephonyManager telephony = (TelephonyManager) MyApp.getInstance().getSystemService(Context.TELEPHONY_SERVICE);
            return telephony.isNetworkRoaming();
        } catch (Exception e) {
            return false;
        }
    }


    public static ArrayList<String> getEnglishWeekDaysArrayList() {
        ArrayList<String> englishWeekDaysArrayList = new ArrayList<>();

        englishWeekDaysArrayList.add("Sunday");
        englishWeekDaysArrayList.add("Monday");
        englishWeekDaysArrayList.add("Tuesday");
        englishWeekDaysArrayList.add("Wednesday");
        englishWeekDaysArrayList.add("Thursday");
        englishWeekDaysArrayList.add("Friday");
        englishWeekDaysArrayList.add("Saturday");

        return englishWeekDaysArrayList;
    }


    public static ArrayList<Pattern> getPatternsArrayList() {
        if (patternsArrayList == null) {
            patternsArrayList = new ArrayList<>();

            patternsArrayList.add(new Pattern("", MyThemesUtils.getEntryListItemColorResId(), MyThemesUtils.getEntryListItemColorResId()));
            patternsArrayList.add(new Pattern("color", android.R.color.transparent, R.color.md_grey_300));
            patternsArrayList.add(new Pattern("pattern01", R.drawable.pat01, R.drawable.pat01_thumb));
            patternsArrayList.add(new Pattern("pattern02", R.drawable.pat02, R.drawable.pat02_thumb));
            patternsArrayList.add(new Pattern("pattern03", R.drawable.pat03, R.drawable.pat03_thumb));
            patternsArrayList.add(new Pattern("pattern04", R.drawable.pat04, R.drawable.pat04_thumb));
            patternsArrayList.add(new Pattern("pattern05", R.drawable.pat05, R.drawable.pat05_thumb));
            patternsArrayList.add(new Pattern("pattern06", R.drawable.pat06, R.drawable.pat06_thumb));
            patternsArrayList.add(new Pattern("pattern07", R.drawable.pat07, R.drawable.pat07_thumb));
            patternsArrayList.add(new Pattern("pattern08", R.drawable.pat08, R.drawable.pat08_thumb));
            patternsArrayList.add(new Pattern("pattern09", R.drawable.pat09, R.drawable.pat09_thumb));
            patternsArrayList.add(new Pattern("pattern10", R.drawable.pat10, R.drawable.pat10_thumb));
            patternsArrayList.add(new Pattern("pattern11", R.drawable.pat11, R.drawable.pat11_thumb));
            patternsArrayList.add(new Pattern("pattern12", R.drawable.pat12, R.drawable.pat12_thumb));
            patternsArrayList.add(new Pattern("pattern13", R.drawable.pat13, R.drawable.pat13_thumb));
        }

        return patternsArrayList;
    }

    public static int convertDayOfWeekFromCalendarToJodaTime(int dayOfWeekInCalendar) {
        int dayOfWeekInJodaTime = dayOfWeekInCalendar - 1;
        if (dayOfWeekInCalendar == Calendar.SUNDAY) {
            dayOfWeekInJodaTime = DateTimeConstants.SUNDAY;
        }

        return dayOfWeekInJodaTime;
    }

    public static int convertDayOfWeekFromJodaTimeToCalendar(int dayOfWeekInJodaTime) {
        int dayOfWeekInCalendar = dayOfWeekInJodaTime + 1;
        if (dayOfWeekInJodaTime == DateTimeConstants.SUNDAY) dayOfWeekInCalendar = Calendar.SUNDAY;

        return dayOfWeekInCalendar;
    }


    public static String getDayOfWeekTitle(int dayOfWeek) {
        DateFormatSymbols mDateFormatSymbols = new DateFormatSymbols(MyApp.getInstance().getResources().getConfiguration().locale);
        return mDateFormatSymbols.getWeekdays()[dayOfWeek];
    }

    public static String getDayOfWeekShortTitle(int dayOfWeek) {
        DateFormatSymbols mDateFormatSymbols = new DateFormatSymbols(MyApp.getInstance().getResources().getConfiguration().locale);
        return mDateFormatSymbols.getShortWeekdays()[dayOfWeek];
    }

    public static String getMonthTitleStandAlone(int month) {
        SimpleDateFormat monthParse = new SimpleDateFormat("MM", MyApp.getInstance().getResources().getConfiguration().locale);
        SimpleDateFormat monthDisplay = new SimpleDateFormat("LLLL", MyApp.getInstance().getResources().getConfiguration().locale);

        String monthDsp = "";
        try {
            monthDsp = monthDisplay.format(monthParse.parse(month + ""));
        } catch (Exception e) {
            AppLog.e("getMonthTitleStandAlone - " + e.getMessage());
            DateFormatSymbols mDateFormatSymbols = new DateFormatSymbols(MyApp.getInstance().getResources().getConfiguration().locale);
            monthDsp = mDateFormatSymbols.getMonths()[month - 1];
        }
        return
                monthDsp;
    }

    public static String getMonthTitle(int month) {
        DateFormatSymbols mDateFormatSymbols = new DateFormatSymbols(MyApp.getInstance().getResources().getConfiguration().locale);
        return mDateFormatSymbols.getMonths()[month - 1];
    }

    public static String getMonthShortTitle(int month) {
        DateFormatSymbols mDateFormatSymbols = new DateFormatSymbols(MyApp.getInstance().getResources().getConfiguration().locale);
        return mDateFormatSymbols.getShortMonths()[month - 1];
    }

    /**
     * Returns 0 if pattern not found
     */
    public static int getPatternPosition(String pattern) {
        ArrayList<Pattern> patternsArrayList = getPatternsArrayList();

        int count = patternsArrayList.size();
        for (int i = 1; i < count; i++) {
            if (patternsArrayList.get(i).patternTitle.equals(pattern)) return i;
        }

        return 0;
    }

    public static void sendBroadcast(String toActivity, String doWhat, ArrayList<String> params) {
        //		AppLog.d("toActivity: " + toActivity + ", doWhat: " + doWhat + ", params: " + params);

        // Send broadcast
        Intent i = new Intent(toActivity);
        i.putExtra(BROADCAST_DO, doWhat);
        i.putExtra(BROADCAST_PARAMS, params);
        MyApp.getInstance().sendBroadcast(i);
    }

    public static void sendBroadcastToRedrawCalendar() {
        sendBroadcast(BR_IN_MAIN, DO_RECREATE_CALENDAR, null);
    }

    public static void sendBroadcastsToUpdateProfilePhoto() {
        AppLog.d("");

        sendBroadcast(BR_IN_PROFILE, DO_UPDATE_PROFILE_PHOTO, null);
        sendBroadcast(BR_IN_PROFILE_PHOTO, DO_UPDATE_PROFILE_PHOTO, null);
        sendBroadcast(BR_IN_MAIN, DO_UPDATE_PROFILE_PHOTO, null);
    }

    /**
     * Moves photos from given directory to '/media/photo' directory
     */
    public static void moveAllPhotos(String currentPhotosDirectory) throws Exception {
        File[] entriesDirectories = new File(currentPhotosDirectory).listFiles();
        if (entriesDirectories != null) {
            for (File entryDirectory : entriesDirectories) {
                String entryUid = entryDirectory.getName();
                AppLog.d("entryUid: " + entryUid);

                // Read entry photos
                File[] entryPhotos = entryDirectory.listFiles();
                if (entryPhotos != null) {
                    for (File entryPhoto : entryPhotos) {
                        AppLog.d("entryPhoto: " + entryPhoto);

                        if (entryPhoto != null && !entryPhoto.isDirectory()) {
                            String filename = entryPhoto.getName();
                            String filePath = entryPhoto.getAbsolutePath();

                            // Check if photo has extension
                            String fileExtension = getFileExtension(filename);

                            if (StringUtils.isEmpty(fileExtension)) {
                                // Add '.jpg' extension
                                filename += ".jpg";
                                filePath += ".jpg";
                                entryPhoto.renameTo(new File(filePath));
                            }

                            ContentValues whereCv = new ContentValues();
                            whereCv.put(Tables.KEY_ATTACHMENT_ENTRY_UID, entryUid);
                            whereCv.put(Tables.KEY_ATTACHMENT_FILENAME, filename);

                            String[] whereArgs = new String[2];
                            whereArgs[0] = entryUid;
                            whereArgs[1] = filename;

                            Cursor cursor = MyApp.getInstance().storageMgr.getSQLiteAdapter().getSingleRowCursor(Tables.TABLE_ATTACHMENTS, "WHERE " + Tables.KEY_ATTACHMENT_ENTRY_UID + "=?" + " AND " + Tables.KEY_ATTACHMENT_FILENAME + "=?", whereArgs);
                            AppLog.d("cursor.getCount(): " + cursor.getCount());

                            // If photo file does not exist
                            if (cursor.getCount() == 0) {
                                // Save attachment
                                AttachmentsStatic.saveAttachment(entryUid, filePath, PHOTO, true);
                            }
                            cursor.close();

                            AppLog.d(entryPhoto.getName() + " moved.");
                        }
                    }
                }
            }
        }
    }

    /**
     * Sets entry pattern color by folder
     */
    public static void setBgColorWithAlpha(int folderColor, View overlayView) {
        int red = Color.red(folderColor);
        int green = Color.green(folderColor);
        int blue = Color.blue(folderColor);
        int alphaColor = Color.argb(50, red, green, blue);
        overlayView.setBackgroundColor(alphaColor);
    }

    public static Bitmap setPattern(Context context, int patternPosition, ViewGroup patternView) {
        Pattern o = getPatternsArrayList().get(patternPosition);
        Bitmap bitmap = null;

        // Pattern
        if (patternView != null) {
            if (patternPosition == 0 || patternPosition == 1) {
                patternView.setBackgroundColor(context.getResources().getColor(o.patternRepeat));
            } else {
                bitmap = BitmapFactory.decodeResource(context.getResources(), o.patternRepeat);
                BitmapDrawable bitmapDrawable = new BitmapDrawable(context.getResources(), bitmap);
                bitmapDrawable.setTileModeXY(TileMode.REPEAT, TileMode.REPEAT);
                patternView.setBackground(bitmapDrawable);
            }
        }

        return bitmap;
    }

    public static void throwIfNotConnectedToInternet() throws Exception {
        // Test if connected to Internet
        if (!MyApp.getInstance().networkStateMgr.isConnectedToInternet()) {
            throw new Exception(MyApp.getInstance().getString(R.string.error_internet_connection));
        }
    }

    public static void throwIfDropboxNotConnected() throws Exception {
        if (!DropboxAccountManager.isLoggedIn(MyApp.getInstance())) {
            throw new Exception(MyApp.getInstance().getString(
                    R.string.please_reconnect_with_dropbox));
        }
    }

    public static void throwIfNotPro() throws Exception {
        if (!Static.isProUser()) {
            throw new Exception("Not PRO");
        }
    }

    public static void throwExceptionIfRestoringOrCreatingBackup() throws Exception {
        if (MyApp.getInstance().asyncsMgr.isAsyncRunning(MyApp.getInstance().asyncsMgr.createBackupAsync)) {
            throw new Exception(MyApp.getInstance().getString(
                    R.string.sync_canceled_because_creating_backup));
        }
        if (MyApp.getInstance().asyncsMgr.isAsyncRunning(MyApp.getInstance().asyncsMgr.restoreFromBackupFileAsync)) {
            throw new Exception(MyApp.getInstance().getString(
                    R.string.sync_canceled_because_restoring_from_backup));
        }
    }

    public static void refreshLocale() {
        //        AppLog.d("getLocalClassName(): " + context.getLocalClassName());

        // Check for NullPointerException, because crashes on Android 4.1.2 using widget
        if (MyApp.getInstance() == null || MyApp.getInstance().getResources() == null) {
            return;
        }

        String prefLocale = PreferencesHelper.getCurrentLocaleAsCode(MyApp.getInstance());
        setLocale(prefLocale);
    }

    public static void setLocale(String prefLocale) {
        Configuration config = MyApp.getInstance().getResources().getConfiguration();

        // Create new locale
        Locale locale = new Locale(prefLocale.substring(0, 2), prefLocale.length() == 5 ? prefLocale.substring(3, 5) : "");
        if (!config.locale.equals(locale)) {
            //			AppLog.d("setDefault: " + locale);

            Locale.setDefault(locale);
            config.locale = locale;
            DisplayMetrics dm = MyApp.getInstance().getResources().getDisplayMetrics();
            MyApp.getInstance().getResources().updateConfiguration(config, dm);

            Lingver.getInstance().setLocale(MyApp.getInstance(), locale);
        }
    }

    public static void copyPrefsAndDatabaseFiles() throws Exception {
        File dbFile = SQLiteMgr.plainDbFile;
        if (!dbFile.exists()) {
            dbFile = SQLiteMgr.encryptedDbFileV1;
        }
        if (!dbFile.exists()) {
            dbFile = SQLiteMgr.encryptedDbFileV2;
        }
        if (!dbFile.exists()) {
            throw new Exception("Database file not found.");
        }

        // Create backup directory
        File backupDir = new File(PermanentStorageUtils.getDiaroBackupDirPath());
        backupDir.mkdirs();
        AppLog.d("backupDir.getAbsolutePath(): " + backupDir.getAbsolutePath());

        String encKey = Prefs.getFullDbEncryptionKey();
        File copyToFile = new File(backupDir.getAbsolutePath() + "/" + encKey + dbFile.getName());

        if (copyToFile.exists()) {
            copyToFile.delete();
        }

        Static.copyFileOrDirectory(dbFile, copyToFile);

        // Copy prefs file
        File prefsFile = new File("/data/data/" + MyApp.getInstance().getPackageName() + "/shared_prefs/" + Prefs.NAME + ".xml");

        if (!prefsFile.exists()) {
            return;
        }

        File copyToPrefsFile = new File(backupDir.getAbsolutePath() + "/" + prefsFile.getName());
        Static.copyFileOrDirectory(prefsFile, copyToPrefsFile);
    }

    public static void uploadDatabaseToDropbox() {

        File dbFile = SQLiteMgr.plainDbFile;

        if (dbFile.exists()) {
            try {
                DropboxAccountManager.uploadFile(dbFile, "/dev/" + SQLiteMgr.plainDbFile);
            } catch (Exception e) {
                AppLog.e(e.getMessage());
            }
        }

        if (!dbFile.exists()) {
            dbFile = SQLiteMgr.encryptedDbFileV1;
        }

        if (dbFile.exists()) {
            try {
                DropboxAccountManager.uploadFile(dbFile, "/dev/" + SQLiteMgr.encryptedDbFileV1);
            } catch (Exception e) {
                AppLog.e(e.getMessage());
            }
        }

        if (!dbFile.exists()) {
            dbFile = SQLiteMgr.encryptedDbFileV2;
        }
        if (!dbFile.exists()) {
            return;
        }

        SimpleDateFormat s = new SimpleDateFormat("ddMMyyyyhhmmss");
        String format = s.format(new Date());

        String encKey = Prefs.getFullDbEncryptionKey();
        String dbxFileName = encKey + "_" + format + "_" + dbFile.getName();

        try {
            DropboxAccountManager.uploadFile(dbFile, "/dev/" + dbxFileName);
        } catch (Exception e) {
            AppLog.e(e.getMessage());
        }
    }

    public static String getPhotoFilePathFromUri(Uri uri) {
        AppLog.i("getPhotoFilePathFromUri uri: " + uri);

        String filePath = null;

        if (uri != null) {
            // - Try to get a photo (Picasa photos) -
            try {
                File file = getFileFromUri(uri);
                filePath = file.getAbsolutePath();
//					AppLog.d("getFileFromUri() fileUri: " + fileUri);
            } catch (Exception e) {
                AppLog.d("getFileFromUri() Exception: " + e);
            }

            // - Try method to get a photo (from Dropbox app) -
            if (filePath == null) {
                try {
                    filePath = uri.getPath();
//					AppLog.d("uri.getPath() fileUri: " + fileUri);
                } catch (Exception e) {
                    AppLog.d("uri.getPath() Exception: " + e);
                }
            }

            // - Try to get a photo (regular photos from SD card) -
            if (filePath == null) {
                try {
                    filePath = getRealPathFromURI(uri);
                    AppLog.e("getRealPathFromURI() fileUri: " + filePath);
                } catch (Exception e) {
                    AppLog.d("getRealPathFromURI() Exception: " + e);
                }
            }
        }

        AppLog.i("getPhotoFilePathFromUri filePath: " + filePath);

        return filePath;
    }


    public static String getPhotoFilePathFromUriTest(Uri uri) {
        String filePath = null;

        if (uri != null) {
            // - Try to get a photo (regular photos from SD card) -
            try {
                if (Build.VERSION.SDK_INT < 19)
                    filePath = getRealPathFromURI(uri);
                else
                    filePath = getRealPathFromURI_API19(uri);

                AppLog.d("getRealPathFromURI() fileUri: " + filePath);
            } catch (Exception e) {
                AppLog.d("getRealPathFromURI() Exception: " + e);
            }

            if (filePath == null) {
                // - Try to get a photo (Picasa photos) -
                try {
                    File file = getFileFromUri(uri);
                    filePath = file.getAbsolutePath();
                    AppLog.d("Picasa getFileFromUri() fileUri: " + filePath);
                } catch (Exception e) {
                    AppLog.d("Picasa getFileFromUri() Exception: " + e);
                }
            }

            // - Try method to get a photo (from Dropbox app) -
            if (filePath == null) {
                try {
                    filePath = uri.getPath();
                    AppLog.d(" Dropbox uri.getPath() fileUri: " + filePath);
                } catch (Exception e) {
                    AppLog.d("Dropbox uri.getPath() Exception: " + e);
                }
            }

        }
        AppLog.i("uri -> " + uri);
        AppLog.i("getPhotoFilePathFromUri filePath: " + filePath);

        return filePath;
    }

    public static String getRealPathFromURI_API19(Uri uri) {
        String filePath = "";
        String wholeID = DocumentsContract.getDocumentId(uri);

        // Split at colon, use second item in the array
        String id = wholeID.split(":")[1];

        String[] column = {MediaStore.Images.Media.DATA};

        // where id is equal to
        String sel = MediaStore.Images.Media._ID + "=?";

        Cursor cursor = MyApp.getInstance().getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, column, sel, new String[]{id}, null);

        int columnIndex = cursor.getColumnIndex(column[0]);

        if (cursor.moveToFirst()) {
            filePath = cursor.getString(columnIndex);
        }
        cursor.close();
        return filePath;
    }

    /**
     * Converts URI into an absolute path
     */
    public static String getRealPathFromURI(Uri contentUri) {
        String[] proj = {MediaStore.Images.Media.DATA};
        CursorLoader loader = new CursorLoader(MyApp.getInstance(), contentUri, proj, null, null, null);
        Cursor cursor = loader.loadInBackground();
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();

        String path = cursor.getString(column_index);
        cursor.close();

        return path;
    }

    public static File getFileFromUri(Uri uri) {
        AppLog.d("uri: " + uri);

        File file = null;

        if (uri != null) {
            // This is the key line. Content provider client gives us access to
            // file no matter if it is a local or a remote one
            ContentProviderClient client = MyApp.getInstance().getContentResolver().acquireContentProviderClient(uri);

            try {
                // Here we save copy of the file to temporary
                ParcelFileDescriptor descriptor = client.openFile(uri, "r");
                // AppLog.d("descriptor: " + descriptor);

                AutoCloseInputStream is = new AutoCloseInputStream(descriptor);
                file = File.createTempFile("image", ".jpg");
                AppLog.d("file.getAbsolutePath(): " + file.getAbsolutePath());

                OutputStream outS = new FileOutputStream(file);
                byte[] buf = new byte[1024];
                int len;
                while ((len = is.read(buf)) > 0) {
                    outS.write(buf, 0, len);
                }
                is.close();
                outS.close();
            } catch (Exception e) {
                AppLog.e("Exception: " + e);
            } finally {
                if (client != null) {
                    client.release();
                }
            }

        }

        return file;
    }

    public static int getThumbnailWidthForGrid() {
        DisplayMetrics dm = MyApp.getInstance().getResources().getDisplayMetrics();

        int gridWidth = dm.widthPixels - Static.getPixelsFromDip(4);
        int horizontalSpacing = Static.getPixelsFromDip(2);

        int thumbnailWidth = (gridWidth - horizontalSpacing) / 2;
        if (Static.isLandscape()) {
            thumbnailWidth = (gridWidth - horizontalSpacing * 2) / 3;
        }

        return thumbnailWidth;
    }

    public static String getApiUrl() {
        if (AppConfig.DEV_LABS_MODE && BuildConfig.DEBUG) {
            return GlobalConstants.API_LABS_BASE_URL;
        } else
            return GlobalConstants.API_BASE_URL;
    }

    public static OkHttpClient getUnsafeOkHttpClient() {
        try {
            // Create a trust manager that does not validate certificate chains
            final TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return new java.security.cert.X509Certificate[]{};
                        }
                    }
            };

            // Install the all-trusting trust manager
            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            // Create an ssl socket factory with our all-trusting manager
            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            builder.sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0]);
            builder.hostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });

            OkHttpClient okHttpClient = builder.build();
            return okHttpClient;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean isLandscape() {
        DisplayMetrics dm = MyApp.getInstance().getResources().getDisplayMetrics();
        return dm.widthPixels > dm.heightPixels;
    }

    public static void showSoftKeyboard(final EditText editText) {
        MyApp.getInstance().handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                editText.requestFocus();
                InputMethodManager imm = (InputMethodManager) MyApp.getInstance().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(editText, 0);
            }
        }, 100);
    }

    public static void hideSoftKeyboard(EditText editText) {
        InputMethodManager imm = (InputMethodManager) MyApp.getInstance().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
    }

    public static long byteToKB(long byteTransform) {
        return byteTransform / 1024L;
    }

    public static int getPercents(long transferred, long total) {
        int percents = (int) (100.0 * (double) transferred / total);
        if (percents < 0) {
            percents = 0;
        }

        return percents;
    }


    public static String getFormattedDate(long millis) {
        if (millis == 0) {
            return "...";
        }

        DateTime dt = new DateTime(millis);
        return dt.getDayOfMonth() + " " + Static.getMonthShortTitle(dt.getMonthOfYear()) + " " + dt.getYear();
    }

    public static ObjectKey getGlideSignature(File photoFile) {
        return new ObjectKey(photoFile.length() + "" + photoFile.lastModified());
    }

    public static void highlightSearchText(TextView textView, String fullText, String searchText) {
        textView.setText(fullText, TextView.BufferType.SPANNABLE);

        boolean doubleQuotesSearch = false;
        if (searchText.startsWith("\"") || searchText.endsWith("\""))
            doubleQuotesSearch = true;

        searchText = searchText.replaceAll("\"", "");

        if (StringUtils.isNotBlank(searchText)) {
            try {
                // Double quotes search
                if (doubleQuotesSearch) {
                    highlightString(textView, fullText, searchText);
                } else {
                    highlightString(textView, fullText, searchText);
                    String[] splitted = searchText.split(" ");
                    for (String s : splitted) {
                        if (!s.equals(" ") && !s.equals("")) {
                            highlightString(textView, fullText, s);
                        }
                    }
                }
            } catch (Exception e) {
                AppLog.e("Exception: " + e);
            }
        }
    }

    private static void highlightString(TextView textView, String fullText, String s) {
        int index = StringUtils.indexOfIgnoreCase(fullText, s);
        if (index >= 0) {
            final Spannable str = (Spannable) textView.getText();
            while (index >= 0) {
                // A different span must be used for each occurrence
                final BackgroundColorSpan bcSpan = new BackgroundColorSpan(MyApp.getInstance().getResources().getColor(R.color.search_highlight));
                str.setSpan(bcSpan, index, index + s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                index = StringUtils.indexOfIgnoreCase(fullText, s, index + s.length());
            }
        }
    }

    public static boolean isTablet() {
        return MyApp.getInstance().getResources().getBoolean(R.bool.isTablet);
    }

    public static void resizeDialog(Dialog dialog) {
        // Get existing layout params for the window
        ViewGroup.LayoutParams params = dialog.getWindow().getAttributes();
        // Assign window properties to fill the parent
        params.width = Static.getPixelsFromDip(320);
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        dialog.getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);

    }

    public static class ComparatorTags implements Comparator<TagInfo> {
        @Override
        public int compare(TagInfo o1, TagInfo o2) {
            if (o1 == null || o2 == null) {
                return 0;
            }
            return o1.title.compareToIgnoreCase(o2.title);
        }
    }

    public static class ComparatorLocations implements Comparator<LocationInfo> {
        @Override
        public int compare(LocationInfo o1, LocationInfo o2) {
            if (o1 == null || o2 == null) {
                return 0;
            }
            return o1.getLocationTitle().compareToIgnoreCase(o2.getLocationTitle());
        }
    }

    public static class ComparatorByBackupFilename implements Comparator<BackupFile> {
        public int compare(BackupFile o1, BackupFile o2) {
            if (o1 == null || o2 == null) {
                return 0;
            }
            return o2.filename.compareToIgnoreCase(o1.filename);
        }
    }

    public static class ComparatorByBackupLastModified implements Comparator<BackupFile> {
        public int compare(BackupFile o1, BackupFile o2) {
            if (o1 == null || o2 == null) {
                return 0;
            }

            if (o2.lastModified > o1.lastModified)
                return 1;
            else if (o2.lastModified < o1.lastModified)
                return -1;
            else
                return 0;

        }
    }

    public static int getResId(String resourceName, Class<?> c) {
        try {
            Field idField = c.getDeclaredField(resourceName);
            return idField.getInt(idField);
        } catch (Exception e) {
            throw new RuntimeException("No resource ID found for: " + resourceName + " / " + c, e);

        }
    }

    public static void printCursor(Cursor cursor) {

        if (cursor == null) {
            AppLog.e("Cursor is null");
            return;
        }


        StringBuilder sb = new StringBuilder();
        int columnsQty = cursor.getColumnCount();
        for (int idx = 0; idx < columnsQty; ++idx) {
            sb.append(cursor.getString(idx));
            if (idx < columnsQty - 1)
                sb.append("; ");
        }
        AppLog.e(String.format("Cursor Row: %d, Values: %s", cursor.getPosition(), sb.toString()));
    }

}
